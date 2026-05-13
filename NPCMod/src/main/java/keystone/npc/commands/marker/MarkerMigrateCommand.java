package keystone.npc.commands.marker;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import keystone.npc.KeystoneNpcPlugin;
import keystone.npc.domain.MarkerAssignment;
import keystone.npc.domain.NpcRecord;
import keystone.npc.markers.MarkerType;
import keystone.npc.routine.NpcRoutineRunner;

public final class MarkerMigrateCommand extends CommandBase {
    private static final int MAX_DRY_RUN_LINES = 20;
    private static final long DRY_RUN_APPLY_WINDOW_MS = 5 * 60 * 1000L;

    @Nonnull
    private final RequiredArg<String> modeArg = this.withRequiredArg("mode", "keystone.commands.knpc.marker.migrate.mode", ArgTypes.STRING);

    private final KeystoneNpcPlugin plugin;
    private final NpcRoutineRunner scheduler;

    private String lastDryRunFingerprint;
    private long lastDryRunAtMs;

    public MarkerMigrateCommand(KeystoneNpcPlugin plugin, NpcRoutineRunner scheduler) {
        super("migrate", "keystone.commands.knpc.marker.migrate");
        this.plugin = Objects.requireNonNull(plugin);
        this.scheduler = Objects.requireNonNull(scheduler);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        String mode = normalizeMode(modeArg.get(context));
        boolean dryRun = "dry-run".equals(mode);
        boolean apply = "apply".equals(mode);
        if (!dryRun && !apply) {
            context.sendMessage(Message.raw("[knpc] Usage: /knpc marker migrate --dry-run|--apply"));
            return;
        }

        if (plugin.isStateLoadFailed()) {
            context.sendMessage(Message.raw("[knpc] Marker migration blocked: state load failed earlier."));
            return;
        }

        if (plugin.isStateLoadPartial()) {
            context.sendMessage(Message.raw("[knpc] Marker migration blocked: state was loaded partially."));
            return;
        }

        MigrationPlan plan = buildMigrationPlan();
        if (dryRun) {
            lastDryRunFingerprint = plan.fingerprint();
            lastDryRunAtMs = System.currentTimeMillis();
            emitDryRunReport(context, plan);
            return;
        }

        if (!isApplyAllowedAfterDryRun(plan.fingerprint())) {
            context.sendMessage(Message.raw("[knpc] Migration apply blocked: run /knpc marker migrate --dry-run immediately before --apply."));
            return;
        }

        if (plan.assignmentChanges() == 0) {
            context.sendMessage(Message.raw("[knpc] Marker migration apply: no changes required."));
            return;
        }

        Path backupPath = plugin.createStateBackupForMigration();
        if (backupPath == null) {
            context.sendMessage(Message.raw("[knpc] Marker migration apply aborted: state backup failed."));
            return;
        }

        ApplyResult applyResult = applyMigrationPlan(plan);
        if (!plugin.saveStateSafely()) {
            boolean rolledBack = rollbackAssignments(applyResult.beforeAssignmentsByNpcId());
            if (rolledBack) {
                context.sendMessage(Message.raw("[knpc] Marker migration apply aborted: state save failed, runtime assignments were rolled back."));
            } else {
                context.sendMessage(Message.raw("[knpc] Marker migration apply aborted: state save failed and rollback was incomplete."));
                context.sendMessage(Message.raw("[knpc] Runtime/state drift risk: markerAssignments rollback failed."));
            }
            return;
        }

        context.sendMessage(Message.raw("[knpc] Marker migration applied: npcs=" + applyResult.updatedNpcs()
            + " assignments=" + applyResult.writtenAssignments()
            + " backup=" + backupPath.toAbsolutePath().normalize()));
    }

    private void emitDryRunReport(CommandContext context, MigrationPlan plan) {
        if (plan.assignmentChanges() == 0) {
            context.sendMessage(Message.raw("[knpc] Marker migration dry-run: no changes required."));
            context.sendMessage(Message.raw("[knpc] Legacy fields and markerAssignments are already aligned."));
            return;
        }

        context.sendMessage(Message.raw("[knpc] Marker migration dry-run:"));
        context.sendMessage(Message.raw("[knpc] - scanned npcs: " + plan.scannedNpcs()));
        context.sendMessage(Message.raw("[knpc] - npcs to update: " + plan.npcChanges()));
        context.sendMessage(Message.raw("[knpc] - assignment updates: " + plan.assignmentChanges()));

        int limit = Math.min(MAX_DRY_RUN_LINES, plan.entries().size());
        for (int i = 0; i < limit; i++) {
            NpcMigrationEntry entry = plan.entries().get(i);
            int index = scheduler.indexOfNpc(entry.npc().npcId());
            String keys = entry.assignmentUpdates().keySet().stream().sorted().collect(Collectors.joining(","));
            context.sendMessage(Message.raw("[knpc] - npc #" + index + " ('" + entry.npc().npcName() + "') keys=" + keys));
        }

        if (plan.entries().size() > limit) {
            context.sendMessage(Message.raw("[knpc] - ... and " + (plan.entries().size() - limit) + " more NPC(s)."));
        }

        context.sendMessage(Message.raw("[knpc] Dry-run fingerprint: " + plan.fingerprint()));
        context.sendMessage(Message.raw("[knpc] To apply: /knpc marker migrate --apply"));
    }

    private MigrationPlan buildMigrationPlan() {
        List<NpcMigrationEntry> entries = new ArrayList<>();
        int scanned = 0;
        int assignmentChanges = 0;
        StringBuilder fingerprintBuilder = new StringBuilder();

        for (NpcRecord npc : scheduler.snapshotIndexed()) {
            scanned++;
            Map<String, MarkerAssignment> updates = collectLegacyToAssignmentUpdates(npc);
            if (updates.isEmpty()) {
                continue;
            }

            entries.add(new NpcMigrationEntry(npc, updates));
            assignmentChanges += updates.size();
            fingerprintBuilder.append(npc.npcId()).append('|');
            updates.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> fingerprintBuilder
                    .append(entry.getKey())
                    .append('=')
                    .append(entry.getValue().markerId())
                    .append(':')
                    .append(entry.getValue().markerType().name())
                    .append(';'));
            fingerprintBuilder.append('\n');
        }

        String fingerprintSource = fingerprintBuilder.length() == 0 ? "none" : fingerprintBuilder.toString();
        String fingerprint = Integer.toHexString(fingerprintSource.hashCode());
        return new MigrationPlan(scanned, entries.size(), assignmentChanges, entries, fingerprint);
    }

    private Map<String, MarkerAssignment> collectLegacyToAssignmentUpdates(NpcRecord npc) {
        Map<String, MarkerAssignment> updates = new LinkedHashMap<>();
        if (npc == null) {
            return updates;
        }

        Map<String, MarkerAssignment> existing = npc.markerAssignments();
        for (MarkerType markerType : MarkerType.values()) {
            String legacyMarkerId = legacyMarkerIdForType(npc, markerType);
            if (legacyMarkerId == null || legacyMarkerId.isBlank()) {
                continue;
            }

            String logicalKey = logicalKeyForType(markerType);
            MarkerAssignment expected = new MarkerAssignment(legacyMarkerId.trim(), markerType);
            MarkerAssignment current = existing.get(logicalKey);
            if (!expected.equals(current)) {
                updates.put(logicalKey, expected);
            }
        }

        return updates;
    }

    private ApplyResult applyMigrationPlan(MigrationPlan plan) {
        Map<String, Map<String, MarkerAssignment>> beforeAssignmentsByNpcId = new LinkedHashMap<>();
        int updatedNpcs = 0;
        int writtenAssignments = 0;

        for (NpcMigrationEntry entry : plan.entries()) {
            NpcRecord npc = entry.npc();
            if (npc == null || npc.npcId() == null || npc.npcId().isBlank()) {
                continue;
            }

            Map<String, MarkerAssignment> before = npc.markerAssignments();
            beforeAssignmentsByNpcId.put(npc.npcId(), before);

            Map<String, MarkerAssignment> merged = new LinkedHashMap<>(before);
            merged.putAll(entry.assignmentUpdates());
            npc.markerAssignments(merged);

            if (!merged.equals(before)) {
                updatedNpcs++;
            }
            writtenAssignments += entry.assignmentUpdates().size();
        }

        return new ApplyResult(updatedNpcs, writtenAssignments, beforeAssignmentsByNpcId);
    }

    private boolean rollbackAssignments(Map<String, Map<String, MarkerAssignment>> beforeAssignmentsByNpcId) {
        if (beforeAssignmentsByNpcId == null || beforeAssignmentsByNpcId.isEmpty()) {
            return true;
        }

        boolean rolledBack = true;
        for (Map.Entry<String, Map<String, MarkerAssignment>> entry : beforeAssignmentsByNpcId.entrySet()) {
            String npcId = entry.getKey();
            NpcRecord npc = scheduler.getNpc(npcId);
            if (npc == null) {
                rolledBack = false;
                continue;
            }

            try {
                npc.markerAssignments(entry.getValue());
            } catch (RuntimeException ex) {
                rolledBack = false;
            }
        }
        return rolledBack;
    }

    private boolean isApplyAllowedAfterDryRun(String expectedFingerprint) {
        if (lastDryRunFingerprint == null || expectedFingerprint == null) {
            return false;
        }

        if (!expectedFingerprint.equals(lastDryRunFingerprint)) {
            return false;
        }

        long ageMs = System.currentTimeMillis() - lastDryRunAtMs;
        return ageMs >= 0 && ageMs <= DRY_RUN_APPLY_WINDOW_MS;
    }

    private String normalizeMode(String rawMode) {
        if (rawMode == null) {
            return "";
        }

        String normalized = rawMode.trim().toLowerCase(Locale.ROOT);
        if ("--dry-run".equals(normalized) || "dry-run".equals(normalized)) {
            return "dry-run";
        }
        if ("--apply".equals(normalized) || "apply".equals(normalized)) {
            return "apply";
        }
        return normalized;
    }

    private String legacyMarkerIdForType(NpcRecord npc, MarkerType markerType) {
        return switch (markerType) {
            case BED -> npc.bedMarkerId();
            case DOOR -> npc.doorMarkerId();
            case CHEST -> npc.chestMarkerId();
            case FOOD -> npc.foodMarkerId();
            case WORK -> npc.workMarkerId();
            case CHILL -> npc.chillMarkerId();
        };
    }

    private String logicalKeyForType(MarkerType markerType) {
        return switch (markerType) {
            case BED -> "bed";
            case DOOR -> "door";
            case CHEST -> "chest";
            case FOOD -> "food";
            case WORK -> "work";
            case CHILL -> "chill";
        };
    }

    private record MigrationPlan(
        int scannedNpcs,
        int npcChanges,
        int assignmentChanges,
        List<NpcMigrationEntry> entries,
        String fingerprint
    ) {
    }

    private record NpcMigrationEntry(NpcRecord npc, Map<String, MarkerAssignment> assignmentUpdates) {
    }

    private record ApplyResult(
        int updatedNpcs,
        int writtenAssignments,
        Map<String, Map<String, MarkerAssignment>> beforeAssignmentsByNpcId
    ) {
    }
}
