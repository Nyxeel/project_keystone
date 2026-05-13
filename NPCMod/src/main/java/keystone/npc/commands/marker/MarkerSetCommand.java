package keystone.npc.commands.marker;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.joml.Vector3d;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import keystone.npc.KeystoneNpcPlugin;
import keystone.npc.debug.NpcDebugSupport;
import keystone.npc.domain.MarkerAssignment;
import keystone.npc.domain.NpcRecord;
import keystone.npc.markers.MarkerRecord;
import keystone.npc.markers.MarkerRegistry;
import keystone.npc.markers.MarkerType;
import keystone.npc.markers.RequiredMarkerResolver;
import keystone.npc.markers.Vec3;
import keystone.npc.markers.WorldId;
import keystone.npc.roles.RoleDefinitionRegistry;
import keystone.npc.routine.NpcRoutineRunner;

public final class MarkerSetCommand extends AbstractPlayerCommand {

    @Nonnull
    private final RequiredArg<String> markerTypeArg = this.withRequiredArg("markerType", "keystone.commands.knpc.marker.set.type", ArgTypes.STRING);

    @Nonnull
    private final OptionalArg<String> targetNpcArg = this.withOptionalArg("targetNpc", "keystone.commands.knpc.marker.set.npc", ArgTypes.STRING);

    private final KeystoneNpcPlugin plugin;
    private final MarkerRegistry markerRegistry;
    private final NpcRoutineRunner scheduler;
    private final RequiredMarkerResolver requiredMarkerResolver;

    public MarkerSetCommand(
        KeystoneNpcPlugin plugin,
        MarkerRegistry markerRegistry,
        NpcRoutineRunner scheduler,
        keystone.npc.definition.NpcTemplateResolver templateResolver,
        RoleDefinitionRegistry roleDefinitions
    ) {
        super("set", "keystone.commands.knpc.marker.set");
        this.plugin = Objects.requireNonNull(plugin);
        this.markerRegistry = Objects.requireNonNull(markerRegistry);
        this.scheduler = Objects.requireNonNull(scheduler);
        this.requiredMarkerResolver = new RequiredMarkerResolver(Objects.requireNonNull(templateResolver), Objects.requireNonNull(roleDefinitions));
    }

    @Override
    protected void execute(
        @Nonnull CommandContext context,
        @Nonnull Store<EntityStore> store,
        @Nonnull Ref<EntityStore> ref,
        @Nonnull PlayerRef playerRef,
        @Nonnull World world
    ) {
        String rawType = markerTypeArg.get(context);
        MarkerType type = parseMarkerType(rawType);
        if (type == null) {
            context.sendMessage(Message.raw("[knpc] Unknown marker type: '" + rawType
                + "'. Use: bed|door|chest|food|work|chill"));
            return;
        }

        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) {
            context.sendMessage(Message.raw("[knpc] Could not read player position (TransformComponent missing)."));
            return;
        }

        Vector3d p = transform.getPosition();
        if (!isFinitePosition(p)) {
            context.sendMessage(Message.raw("[knpc] Could not set marker: player position contains non-finite values."));
            return;
        }

        var worldId = new WorldId(world.getName());
        var pos = new Vec3(p.x(), p.y(), p.z());
        List<MarkerRecord> markersBefore = markerRegistry.snapshot();
        Map<MarkerType, String> activeMarkerIdsBefore = markerRegistry.snapshotActiveMarkerIds();

        String markerTypeName = type.name().toLowerCase(Locale.ROOT);
        NpcRecord targetNpc = null;
        String markerBeforeOnNpc = null;
        MarkerAssignment markerAssignmentBeforeOnNpc = null;
        if (context.provided(targetNpcArg)) {
            String target = targetNpcArg.get(context);
            targetNpc = resolveTargetNpc(context, target, markerTypeName);
            if (targetNpc == null) {
                return;
            }

            if (!isMarkerAllowedForNpc(targetNpc, type)) {
                List<String> allowed = resolveAllowedMarkerNames(targetNpc);
                context.sendMessage(Message.raw("[knpc] Marker " + type.name() + " is not valid for role "
                    + targetNpc.roleId() + "."));
                context.sendMessage(Message.raw("[knpc] Allowed markers: "
                    + (allowed.isEmpty() ? "none" : String.join(", ", allowed))));
                return;
            }

            markerBeforeOnNpc = markerIdForType(targetNpc, type);
            markerAssignmentBeforeOnNpc = markerAssignmentForType(targetNpc, type);
        }

        MarkerRecord activeMarkerBefore = markerRegistry.getActive(type).orElse(null);
        String markerIdBefore = activeMarkerBefore == null ? null : activeMarkerBefore.markerId();
        boolean markerSelectionChanged = false;

        String markerId = markerIdBefore;
        if (activeMarkerBefore == null || !isSameLocation(activeMarkerBefore, worldId, pos)) {
            markerRegistry.setActive(type, worldId, pos);
            markerSelectionChanged = true;
            markerId = markerRegistry.getActive(type).map(MarkerRecord::markerId).orElse(null);
        }

        if (markerId == null || markerId.isBlank()) {
            context.sendMessage(Message.raw("[knpc] Marker konnte nicht gespeichert werden."));
            return;
        }

        boolean npcAssignmentChanged = false;
        boolean rerouteStarted = false;
        if (targetNpc != null) {
            boolean legacyNeedsUpdate = !Objects.equals(markerBeforeOnNpc, markerId);
            MarkerAssignment expectedAssignment = new MarkerAssignment(markerId, type);
            boolean assignmentNeedsUpdate = !expectedAssignment.equals(markerAssignmentBeforeOnNpc);

            if (legacyNeedsUpdate) {
                rerouteStarted = scheduler.assignMarkerToNpc(targetNpc, type, markerId);
                boolean legacyUpdated = Objects.equals(markerIdForType(targetNpc, type), markerId);
                if (!legacyUpdated) {
                    RuntimeRollbackResult rollback = rollbackRuntimeState(
                        type,
                        targetNpc,
                        markerBeforeOnNpc,
                        markerAssignmentBeforeOnNpc,
                        markersBefore,
                        activeMarkerIdsBefore,
                        rerouteStarted
                    );
                    if (rollback.rolledBack() && !rollback.driftRisk()) {
                        context.sendMessage(Message.raw("[knpc] Marker assignment failed. Runtime changes were rolled back."));
                    } else if (rollback.rolledBack()) {
                        context.sendMessage(Message.raw("[knpc] Marker assignment failed. Marker-Daten wurden zurueckgesetzt, aber Reroute-/Navigation-Runtime konnte nicht sicher vollstaendig zurueckgesetzt werden."));
                    } else {
                        context.sendMessage(Message.raw("[knpc] Marker assignment failed and rollback was incomplete."));
                    }
                    if (rollback.driftRisk()) {
                        context.sendMessage(Message.raw("[knpc] Runtime/state drift risk: " + rollback.reason()));
                    }
                    return;
                }

                npcAssignmentChanged = true;
            }

            if (assignmentNeedsUpdate) {
                MarkerAssignmentGateResult gateResult = validateMarkerAssignmentGate(targetNpc, type, markerId);
                if (!gateResult.allowed()) {
                    RuntimeRollbackResult rollback = rollbackRuntimeState(
                        type,
                        targetNpc,
                        markerBeforeOnNpc,
                        markerAssignmentBeforeOnNpc,
                        markersBefore,
                        activeMarkerIdsBefore,
                        rerouteStarted
                    );
                    context.sendMessage(Message.raw("[knpc] Marker assignment failed: " + gateResult.reason()));
                    if (rollback.rolledBack() && !rollback.driftRisk()) {
                        context.sendMessage(Message.raw("[knpc] Runtime changes were rolled back."));
                    } else if (rollback.rolledBack()) {
                        context.sendMessage(Message.raw("[knpc] Marker-Daten wurden zurueckgesetzt, aber Reroute-/Navigation-Runtime konnte nicht sicher vollstaendig zurueckgesetzt werden."));
                    } else {
                        context.sendMessage(Message.raw("[knpc] Marker assignment failed and rollback was incomplete."));
                    }
                    if (rollback.driftRisk()) {
                        context.sendMessage(Message.raw("[knpc] Runtime/state drift risk: " + rollback.reason()));
                    }
                    return;
                }

                setMarkerAssignmentForType(targetNpc, type, markerId);
                if (!expectedAssignment.equals(markerAssignmentForType(targetNpc, type))) {
                    RuntimeRollbackResult rollback = rollbackRuntimeState(
                        type,
                        targetNpc,
                        markerBeforeOnNpc,
                        markerAssignmentBeforeOnNpc,
                        markersBefore,
                        activeMarkerIdsBefore,
                        rerouteStarted
                    );
                    if (rollback.rolledBack() && !rollback.driftRisk()) {
                        context.sendMessage(Message.raw("[knpc] Marker assignment failed. Runtime changes were rolled back."));
                    } else if (rollback.rolledBack()) {
                        context.sendMessage(Message.raw("[knpc] Marker assignment failed. Marker-Daten wurden zurueckgesetzt, aber Reroute-/Navigation-Runtime konnte nicht sicher vollstaendig zurueckgesetzt werden."));
                    } else {
                        context.sendMessage(Message.raw("[knpc] Marker assignment failed and rollback was incomplete."));
                    }
                    if (rollback.driftRisk()) {
                        context.sendMessage(Message.raw("[knpc] Runtime/state drift risk: " + rollback.reason()));
                    }
                    return;
                }

                npcAssignmentChanged = true;
            }
        }

        if (!markerSelectionChanged && !npcAssignmentChanged) {
            context.sendMessage(Message.raw(targetNpc == null
                ? "[knpc] Keine Änderung: Marker ist bereits aktiv."
                : "[knpc] Keine Änderung: Marker ist bereits aktiv und zugewiesen."));
            return;
        }

        if (!plugin.saveStateSafely()) {
            RuntimeRollbackResult rollback = rollbackRuntimeState(
                type,
                targetNpc,
                markerBeforeOnNpc,
                markerAssignmentBeforeOnNpc,
                markersBefore,
                activeMarkerIdsBefore,
                rerouteStarted
            );
            if (rollback.rolledBack() && !rollback.driftRisk()) {
                context.sendMessage(Message.raw("[knpc] Save failed. Runtime changes were rolled back."));
            } else if (rollback.rolledBack()) {
                context.sendMessage(Message.raw("[knpc] Save failed. Marker-Daten wurden zurueckgesetzt, aber Reroute-/Navigation-Runtime konnte nicht sicher vollstaendig zurueckgesetzt werden."));
            } else {
                context.sendMessage(Message.raw("[knpc] Save failed and rollback was incomplete."));
            }
            if (rollback.driftRisk()) {
                context.sendMessage(Message.raw("[knpc] Runtime/state drift risk: " + rollback.reason()));
            }
            return;
        }

        if (targetNpc != null) {
            int npcIndex = scheduler.indexOfNpc(targetNpc.npcId());
            if (!markerSelectionChanged && npcAssignmentChanged) {
                context.sendMessage(Message.raw("[KNPC][Marker] existing " + markerTypeName
                    + " marker reused and assigned to NPC #" + npcIndex + " ('" + targetNpc.npcName() + "')."));
                if (rerouteStarted) {
                    context.sendMessage(Message.raw("[KNPC][Marker] Reroute zum neu zugewiesenen Marker wurde gestartet."));
                }
                return;
            }

            context.sendMessage(Message.raw("[KNPC][Marker] " + markerTypeName
                + " gesetzt bei " + NpcDebugSupport.formatPositionForChat(pos)
                + " und NPC #" + npcIndex + " ('" + targetNpc.npcName() + "') zugewiesen."));
            if (rerouteStarted) {
                context.sendMessage(Message.raw("[KNPC][Marker] Reroute zum neu zugewiesenen Marker wurde gestartet."));
            }
            return;
        }

        context.sendMessage(Message.raw("[KNPC][Marker] " + markerTypeName
            + " gesetzt bei " + NpcDebugSupport.formatPositionForChat(pos)));
    }

    private boolean isMarkerAllowedForNpc(NpcRecord npc, MarkerType markerType) {
        for (RequiredMarkerResolver.Requirement requirement : requiredMarkerResolver.resolveRequirements(npc.roleId())) {
            if (requirement.markerType() == markerType) {
                return true;
            }
        }
        return false;
    }

    private NpcRecord resolveTargetNpc(CommandContext context, String target, String markerTypeName) {
        if (target == null || target.isBlank()) {
            context.sendMessage(Message.raw("[knpc] NPC-Ziel fehlt. Nutze Name oder Index."));
            return null;
        }

        String normalizedTarget = target.trim();
        Integer selectedIndex = tryParseInt(normalizedTarget);
        if (selectedIndex != null) {
            NpcRecord npcByIndex = scheduler.getNpcByIndex(selectedIndex);
            if (npcByIndex == null) {
                context.sendMessage(Message.raw("[knpc] Ungueltiger NPC-Index: " + selectedIndex + ". Siehe /knpc list"));
                return null;
            }
            return npcByIndex;
        }

        List<NpcRecord> matches = scheduler.findNpcMatchesByNameOrId(normalizedTarget);
        if (matches.isEmpty()) {
            context.sendMessage(Message.raw("[knpc] NPC nicht gefunden: '" + normalizedTarget + "'."));
            return null;
        }

        if (matches.size() > 1) {
            context.sendMessage(Message.raw("[knpc] Mehrdeutig: " + matches.size()
                + " NPCs passen auf '" + normalizedTarget + "'. Nutze einen Index:"));
            for (NpcRecord match : matches) {
                int index = scheduler.indexOfNpc(match.npcId());
                context.sendMessage(Message.raw(index + " - " + match.npcName()
                    + " | " + match.roleId() + " | " + match.npcId()));
            }
            context.sendMessage(Message.raw("[knpc] Beispiel: /knpc marker set " + markerTypeName + " <index>"));
            return null;
        }

        return matches.get(0);
    }

    private Integer tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private List<String> resolveAllowedMarkerNames(NpcRecord npc) {
        return requiredMarkerResolver.resolveRequirements(npc.roleId()).stream()
            .map(RequiredMarkerResolver.Requirement::markerType)
            .filter(Objects::nonNull)
            .map(MarkerType::name)
            .collect(Collectors.toList());
    }

    private static MarkerType parseMarkerType(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "bed" -> MarkerType.BED;
            case "door" -> MarkerType.DOOR;
            case "chest" -> MarkerType.CHEST;
            case "food" -> MarkerType.FOOD;
            case "work" -> MarkerType.WORK;
            case "chill" -> MarkerType.CHILL;
            default -> null;
        };
    }

    private static boolean isFinitePosition(Vector3d position) {
        return position != null
            && Double.isFinite(position.x())
            && Double.isFinite(position.y())
            && Double.isFinite(position.z());
    }

    private static boolean isSameLocation(MarkerRecord marker, WorldId worldId, Vec3 position) {
        return marker.worldId().equals(worldId) && marker.position().equals(position);
    }

    private static String markerIdForType(NpcRecord npc, MarkerType markerType) {
        return switch (markerType) {
            case BED -> npc.bedMarkerId();
            case DOOR -> npc.doorMarkerId();
            case CHEST -> npc.chestMarkerId();
            case FOOD -> npc.foodMarkerId();
            case WORK -> npc.workMarkerId();
            case CHILL -> npc.chillMarkerId();
        };
    }

    private MarkerAssignmentGateResult validateMarkerAssignmentGate(NpcRecord npc, MarkerType markerType, String markerId) {
        if (npc == null || markerType == null || markerId == null || markerId.isBlank()) {
            return new MarkerAssignmentGateResult(false, "invalid marker assignment request");
        }

        if (!isMarkerAllowedForNpc(npc, markerType)) {
            return new MarkerAssignmentGateResult(false,
                "marker " + markerType.name() + " is not valid for role " + npc.roleId());
        }

        MarkerRecord marker = markerRegistry.getById(markerId).orElse(null);
        if (marker == null) {
            return new MarkerAssignmentGateResult(false, "markerId not found in registry: " + markerId);
        }

        if (marker.type() != markerType) {
            return new MarkerAssignmentGateResult(false,
                "marker type mismatch: requested=" + markerType.name() + " actual=" + marker.type().name());
        }

        if (!marker.worldId().equals(npc.worldId())) {
            return new MarkerAssignmentGateResult(false,
                "marker world mismatch: markerWorld=" + marker.worldId().value() + " npcWorld=" + npc.worldId().value());
        }

        return new MarkerAssignmentGateResult(true, "-");
    }

    private RuntimeRollbackResult rollbackRuntimeState(
        MarkerType markerType,
        NpcRecord targetNpc,
        String markerBeforeOnNpc,
        MarkerAssignment markerAssignmentBeforeOnNpc,
        List<MarkerRecord> markersBefore,
        Map<MarkerType, String> activeMarkerIdsBefore,
        boolean rerouteStarted
    ) {
        boolean markerRegistryRestored = true;
        try {
            markerRegistry.restore(markersBefore, activeMarkerIdsBefore);
        } catch (RuntimeException ex) {
            markerRegistryRestored = false;
        }

        boolean npcAssignmentRestored = true;
        if (targetNpc != null) {
            try {
                setMarkerIdForType(targetNpc, markerType, markerBeforeOnNpc);
                restoreMarkerAssignmentForType(targetNpc, markerType, markerAssignmentBeforeOnNpc);
            } catch (RuntimeException ex) {
                npcAssignmentRestored = false;
            }
        }

        boolean rolledBack = markerRegistryRestored && npcAssignmentRestored;
        if (!rolledBack) {
            return new RuntimeRollbackResult(false, true, "rollback could not restore previous marker selection/assignment");
        }

        if (rerouteStarted) {
            return new RuntimeRollbackResult(true, true,
                "Marker-Daten wurden zurueckgesetzt, aber Reroute-/Navigation-Runtime konnte nicht sicher vollstaendig zurueckgesetzt werden.");
        }

        return new RuntimeRollbackResult(true, false, "-");
    }

    private static void setMarkerIdForType(NpcRecord npc, MarkerType markerType, String markerId) {
        switch (markerType) {
            case BED -> npc.bedMarkerId(markerId);
            case DOOR -> npc.doorMarkerId(markerId);
            case CHEST -> npc.chestMarkerId(markerId);
            case FOOD -> npc.foodMarkerId(markerId);
            case WORK -> npc.workMarkerId(markerId);
            case CHILL -> npc.chillMarkerId(markerId);
        }
    }

    private static MarkerAssignment markerAssignmentForType(NpcRecord npc, MarkerType markerType) {
        if (npc == null || markerType == null) {
            return null;
        }
        return npc.markerAssignments().get(logicalKeyForType(markerType));
    }

    private static void setMarkerAssignmentForType(NpcRecord npc, MarkerType markerType, String markerId) {
        if (npc == null || markerType == null) {
            return;
        }

        Map<String, MarkerAssignment> assignments = new LinkedHashMap<>(npc.markerAssignments());
        String logicalKey = logicalKeyForType(markerType);

        if (markerId == null || markerId.isBlank()) {
            assignments.remove(logicalKey);
        } else {
            assignments.put(logicalKey, new MarkerAssignment(markerId, markerType));
        }

        npc.markerAssignments(assignments);
    }

    private static void restoreMarkerAssignmentForType(NpcRecord npc, MarkerType markerType, MarkerAssignment previousAssignment) {
        if (npc == null || markerType == null) {
            return;
        }

        Map<String, MarkerAssignment> assignments = new LinkedHashMap<>(npc.markerAssignments());
        String logicalKey = logicalKeyForType(markerType);

        if (previousAssignment == null) {
            assignments.remove(logicalKey);
        } else {
            assignments.put(logicalKey, previousAssignment);
        }

        npc.markerAssignments(assignments);
    }

    private static String logicalKeyForType(MarkerType markerType) {
        return switch (markerType) {
            case BED -> "bed";
            case DOOR -> "door";
            case CHEST -> "chest";
            case FOOD -> "food";
            case WORK -> "work";
            case CHILL -> "chill";
        };
    }

    private record RuntimeRollbackResult(boolean rolledBack, boolean driftRisk, String reason) {
    }

    private record MarkerAssignmentGateResult(boolean allowed, String reason) {
    }
}
