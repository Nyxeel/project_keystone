package keystone.npc.commands.marker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import keystone.npc.KeystoneNpcPlugin;
import keystone.npc.domain.NpcRecord;
import keystone.npc.markers.MarkerRegistry;
import keystone.npc.markers.MarkerType;
import keystone.npc.routine.NpcRoutineRunner;

public final class MarkerClearCommand extends CommandBase {
    private static final int MAX_BLOCK_REPORT_LINES = 12;

    private final KeystoneNpcPlugin plugin;
    private final MarkerRegistry markerRegistry;
    private final NpcRoutineRunner scheduler;

    public MarkerClearCommand(KeystoneNpcPlugin plugin, MarkerRegistry markerRegistry, NpcRoutineRunner scheduler) {
        super("clear", "keystone.commands.knpc.marker.clear");
        this.plugin = Objects.requireNonNull(plugin);
        this.markerRegistry = Objects.requireNonNull(markerRegistry);
        this.scheduler = Objects.requireNonNull(scheduler);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        List<keystone.npc.markers.MarkerRecord> markersBefore = markerRegistry.snapshot();
        Map<MarkerType, String> activeBefore = markerRegistry.snapshotActiveMarkerIds();

        boolean hasPersistedMarkers = !markersBefore.isEmpty();
        boolean hasActiveSelections = !activeBefore.isEmpty();
        if (!hasPersistedMarkers && !hasActiveSelections) {
            context.sendMessage(Message.raw("[knpc] No markers to clear."));
            return;
        }

        if (hasPersistedMarkers) {
            Set<String> existingMarkerIds = new HashSet<>();
            for (var marker : markersBefore) {
                existingMarkerIds.add(marker.markerId());
            }

            MarkerUsageReport usageReport = collectBlockingUsage(existingMarkerIds);
            if (usageReport.hasBlockingUsage()) {
                context.sendMessage(Message.raw("[knpc] Marker clear blocked: " + usageReport.blockingNpcCount()
                    + " NPC(s) still use markers scheduled for deletion."));
                context.sendMessage(Message.raw("[knpc] Remove marker assignments/targets first and retry /knpc marker clear."));
                int limit = Math.min(MAX_BLOCK_REPORT_LINES, usageReport.details().size());
                for (int i = 0; i < limit; i++) {
                    context.sendMessage(Message.raw("- " + usageReport.details().get(i)));
                }
                if (usageReport.details().size() > limit) {
                    context.sendMessage(Message.raw("[knpc] ... and " + (usageReport.details().size() - limit)
                        + " more marker usage references."));
                }
                return;
            }
        }

        markerRegistry.clear();
        if (!plugin.saveStateSafely()) {
            boolean rollbackRestored;
            try {
                markerRegistry.restore(markersBefore, activeBefore);
                rollbackRestored = true;
            } catch (RuntimeException ex) {
                rollbackRestored = false;
            }

            if (rollbackRestored) {
                context.sendMessage(Message.raw("[knpc] Marker clear aborted: state save failed, runtime markers were restored."));
            } else {
                context.sendMessage(Message.raw("[knpc] Marker clear aborted: state save failed and runtime rollback was incomplete."));
                context.sendMessage(Message.raw("[knpc] Runtime/state drift risk: marker restore failed after save failure."));
            }
            return;
        }

        if (hasPersistedMarkers) {
            context.sendMessage(Message.raw("[knpc] Cleared markers (bed/door/chest/food/work/chill) and persisted state."));
            return;
        }

        context.sendMessage(Message.raw("[knpc] Cleared stale active marker selections and persisted state."));
    }

    private MarkerUsageReport collectBlockingUsage(Set<String> existingMarkerIds) {
        Set<String> blockingNpcIds = new LinkedHashSet<>();
        List<String> details = new ArrayList<>();

        for (NpcRecord npc : scheduler.snapshot()) {
            collectAssignedMarkerUsage(existingMarkerIds, npc, blockingNpcIds, details);
            collectNavigationTargetUsage(existingMarkerIds, npc, blockingNpcIds, details);
            collectRoutineCurrentMarkerUsage(existingMarkerIds, npc, blockingNpcIds, details);
        }

        return new MarkerUsageReport(blockingNpcIds.size(), details);
    }

    private void collectAssignedMarkerUsage(
        Set<String> existingMarkerIds,
        NpcRecord npc,
        Set<String> blockingNpcIds,
        List<String> details
    ) {
        for (MarkerType markerType : MarkerType.values()) {
            String markerId = markerIdForType(npc, markerType);
            if (!hasMarkerReference(existingMarkerIds, markerId)) {
                continue;
            }

            blockingNpcIds.add(npc.npcId());
            details.add("npcId=" + npc.npcId()
                + " npcName='" + npc.npcName() + "'"
                + " assigned " + markerType.name() + " markerId=" + markerId);
        }
    }

    private void collectNavigationTargetUsage(
        Set<String> existingMarkerIds,
        NpcRecord npc,
        Set<String> blockingNpcIds,
        List<String> details
    ) {
        String targetMarkerId = npc.navigationState().getTargetMarkerId();
        if (!hasMarkerReference(existingMarkerIds, targetMarkerId)) {
            return;
        }

        blockingNpcIds.add(npc.npcId());
        details.add("npcId=" + npc.npcId()
            + " npcName='" + npc.npcName() + "'"
            + " navigation target/pending markerId=" + targetMarkerId
            + " markerType=" + nullToDash(npc.navigationState().getTargetMarkerType() == null
                ? null
                : npc.navigationState().getTargetMarkerType().name()));
    }

    private void collectRoutineCurrentMarkerUsage(
        Set<String> existingMarkerIds,
        NpcRecord npc,
        Set<String> blockingNpcIds,
        List<String> details
    ) {
        MarkerType routineMarkerType = parseRoutineMarkerType(npc.activeRoutineMarker());
        if (routineMarkerType == null) {
            return;
        }

        String routineMarkerId = markerIdForType(npc, routineMarkerType);
        if (!hasMarkerReference(existingMarkerIds, routineMarkerId)) {
            return;
        }

        blockingNpcIds.add(npc.npcId());
        details.add("npcId=" + npc.npcId()
            + " npcName='" + npc.npcName() + "'"
            + " current routine marker=" + routineMarkerType.name()
            + " markerId=" + routineMarkerId);
    }

    private MarkerType parseRoutineMarkerType(String markerName) {
        if (markerName == null || markerName.isBlank()) {
            return null;
        }

        return switch (markerName.trim().toLowerCase()) {
            case "bed" -> MarkerType.BED;
            case "door" -> MarkerType.DOOR;
            case "chest" -> MarkerType.CHEST;
            case "food" -> MarkerType.FOOD;
            case "work" -> MarkerType.WORK;
            case "chill" -> MarkerType.CHILL;
            default -> null;
        };
    }

    private String markerIdForType(NpcRecord npc, MarkerType markerType) {
        return switch (markerType) {
            case BED -> npc.bedMarkerId();
            case DOOR -> npc.doorMarkerId();
            case CHEST -> npc.chestMarkerId();
            case FOOD -> npc.foodMarkerId();
            case WORK -> npc.workMarkerId();
            case CHILL -> npc.chillMarkerId();
        };
    }

    private boolean hasMarkerReference(Set<String> existingMarkerIds, String markerId) {
        return markerId != null && !markerId.isBlank() && existingMarkerIds.contains(markerId);
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private record MarkerUsageReport(int blockingNpcCount, List<String> details) {
        private boolean hasBlockingUsage() {
            return !details.isEmpty();
        }
    }
}
