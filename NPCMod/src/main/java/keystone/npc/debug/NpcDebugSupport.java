package keystone.npc.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.Universe;

import keystone.npc.definition.EffectiveNpcDefinition;
import keystone.npc.definition.NpcDebugDefinition;
import keystone.npc.definition.NpcTemplateResolver;
import keystone.npc.domain.MarkerAssignment;
import keystone.npc.domain.NpcRecord;
import keystone.npc.markers.MarkerRecord;
import keystone.npc.markers.MarkerRegistry;
import keystone.npc.markers.MarkerType;
import keystone.npc.markers.RequiredMarkerResolver;
import keystone.npc.markers.Vec3;
import keystone.npc.roles.RoleDefinitionRegistry;
import keystone.npc.skills.NpcSkill;

public final class NpcDebugSupport {

    public record RequiredMarkerStatus(String name, MarkerType markerType, MarkerRecord resolvedMarker, boolean supported) {
    }

    private NpcDebugSupport() {
    }

    public static boolean showMarkersEnabled(NpcTemplateResolver resolver, String definitionId) {
        return resolveFlag(resolver, definitionId, DebugFlag.SHOW_MARKERS);
    }

    public static boolean logRoutineChangesEnabled(NpcTemplateResolver resolver, String definitionId) {
        return resolveFlag(resolver, definitionId, DebugFlag.LOG_ROUTINE_CHANGES);
    }

    public static boolean logSkillChecksEnabled(NpcTemplateResolver resolver, String definitionId) {
        return resolveFlag(resolver, definitionId, DebugFlag.LOG_SKILL_CHECKS);
    }

    public static boolean logMotionChangesEnabled(NpcTemplateResolver resolver, String definitionId) {
        return resolveFlag(resolver, definitionId, DebugFlag.LOG_MOTION_CHANGES);
    }

    public static void sendGlobalChat(String message) {
        if (message == null || message.isBlank()) {
            return;
        }

        try {
            Universe universe = Universe.get();
            if (universe == null) {
                return;
            }
            universe.sendMessage(Message.raw(message));
        } catch (RuntimeException | LinkageError ex) {
            System.err.println("[KNPC][Warning] Failed to send debug chat message: "
                + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    public static String formatPositionForChat(Vec3 pos) {
        if (pos == null) {
            return "(-,-,-)";
        }
        return String.format(Locale.ROOT, "(%.2f, %.2f, %.2f)", pos.x(), pos.y(), pos.z());
    }

    public static List<String> buildMarkerSnapshotLines(
        NpcRecord npc,
        MarkerRegistry markerRegistry,
        NpcTemplateResolver templateResolver,
        RoleDefinitionRegistry roleDefinitions
    ) {
        List<String> lines = new ArrayList<>();
        lines.add("[KNPC][Markers] " + npc.npcName());

        for (RequiredMarkerStatus status : resolveRequiredMarkerStatuses(npc, markerRegistry, templateResolver, roleDefinitions)) {
            if (!status.supported()) {
                lines.add(status.name() + " unsupported");
            } else if (status.resolvedMarker() != null) {
                lines.add(status.name() + " OK " + formatPositionForChat(status.resolvedMarker().position()));
            } else {
                lines.add(status.name() + " fehlt");
            }
        }

        return lines;
    }

    public static List<String> missingRequiredMarkerNames(
        NpcRecord npc,
        MarkerRegistry markerRegistry,
        NpcTemplateResolver templateResolver,
        RoleDefinitionRegistry roleDefinitions
    ) {
        List<String> missing = new ArrayList<>();
        for (RequiredMarkerStatus status : resolveRequiredMarkerStatuses(npc, markerRegistry, templateResolver, roleDefinitions)) {
            if (status.supported() && status.resolvedMarker() == null) {
                missing.add(status.name());
            }
        }
        return missing;
    }

    public static List<RequiredMarkerStatus> resolveRequiredMarkerStatuses(
        NpcRecord npc,
        MarkerRegistry markerRegistry,
        NpcTemplateResolver templateResolver,
        RoleDefinitionRegistry roleDefinitions
    ) {
        RequiredMarkerResolver requiredMarkerResolver = new RequiredMarkerResolver(templateResolver, roleDefinitions);

        List<RequiredMarkerStatus> statuses = new ArrayList<>();
        for (RequiredMarkerResolver.Requirement requirement : requiredMarkerResolver.resolveRequirements(npc.roleId())) {
            if (requirement.markerType() == null) {
                statuses.add(new RequiredMarkerStatus(requirement.name(), null, null, false));
                continue;
            }

            MarkerRecord marker = resolveMarker(npc, markerRegistry, requirement.markerType()).orElse(null);
            statuses.add(new RequiredMarkerStatus(requirement.name(), requirement.markerType(), marker, true));
        }

        return statuses;
    }

    public static String markerNameForType(MarkerType markerType) {
        return markerType.name().toLowerCase(Locale.ROOT);
    }

    public static String skillValueForStatus(
        NpcTemplateResolver templateResolver,
        NpcRecord npc,
        NpcSkill skill,
        boolean defaultValue
    ) {
        boolean enabled = templateResolver.resolveByRoleId(npc.roleId())
            .map(def -> def.skills().has(skill))
            .orElse(defaultValue);
        return enabled ? "true" : "false";
    }

    private static Optional<MarkerRecord> resolveMarker(NpcRecord npc, MarkerRegistry markerRegistry, MarkerType markerType) {
        String markerId = markerIdForType(npc, markerType);
        if (markerId == null || markerId.isBlank()) {
            return Optional.empty();
        }

        return markerRegistry.getById(markerId)
            .filter(marker -> marker.type() == markerType)
            .filter(marker -> marker.worldId().equals(npc.worldId()));
    }

    private static String markerIdForType(NpcRecord npc, MarkerType markerType) {
        if (npc == null || markerType == null) {
            return null;
        }

        String logicalKey = markerNameForType(markerType);
        MarkerAssignment assignment = npc.markerAssignments().get(logicalKey);
        if (assignment == null || assignment.markerType() != markerType) {
            return null;
        }

        String markerId = assignment.markerId();
        if (markerId == null || markerId.isBlank()) {
            return null;
        }

        return markerId.trim();
    }

    private static boolean resolveFlag(NpcTemplateResolver resolver, String definitionId, DebugFlag flag) {
        Optional<NpcDebugDefinition> debug = resolver.resolveByRoleId(definitionId)
            .map(EffectiveNpcDefinition::definition)
            .map(def -> def.debug());

        if (debug.isEmpty() || debug.get() == null) {
            return false;
        }

        return switch (flag) {
            case SHOW_MARKERS -> Boolean.TRUE.equals(debug.get().showMarkers());
            case LOG_ROUTINE_CHANGES -> Boolean.TRUE.equals(debug.get().logRoutineChanges());
            case LOG_SKILL_CHECKS -> Boolean.TRUE.equals(debug.get().logSkillChecks());
            case LOG_MOTION_CHANGES -> Boolean.TRUE.equals(debug.get().logMotionChanges());
        };
    }

    private enum DebugFlag {
        SHOW_MARKERS,
        LOG_ROUTINE_CHANGES,
        LOG_SKILL_CHECKS,
        LOG_MOTION_CHANGES
    }
}