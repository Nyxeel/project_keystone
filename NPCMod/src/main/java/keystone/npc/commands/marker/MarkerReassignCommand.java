package keystone.npc.commands.marker;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.joml.Vector3d;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import keystone.npc.KeystoneNpcPlugin;
import keystone.npc.debug.NpcDebugSupport;
import keystone.npc.definition.NpcTemplateResolver;
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

public final class MarkerReassignCommand extends AbstractPlayerCommand {

    private final RequiredArg<String> npcNameArg = this.withRequiredArg("npcName", "keystone.commands.knpc.marker.reassign.npcName", ArgTypes.STRING);
    private final RequiredArg<String> markerNameArg = this.withRequiredArg("markerName", "keystone.commands.knpc.marker.reassign.markerName", ArgTypes.STRING);

    private final KeystoneNpcPlugin plugin;
    private final MarkerRegistry markerRegistry;
    private final NpcRoutineRunner scheduler;
    private final RoleDefinitionRegistry roleDefinitions;
    private final RequiredMarkerResolver requiredMarkerResolver;

    public MarkerReassignCommand(
        KeystoneNpcPlugin plugin,
        MarkerRegistry markerRegistry,
        NpcRoutineRunner scheduler,
        NpcTemplateResolver templateResolver,
        RoleDefinitionRegistry roleDefinitions
    ) {
        super("reassign", "keystone.commands.knpc.marker.reassign");
        this.plugin = Objects.requireNonNull(plugin);
        this.markerRegistry = Objects.requireNonNull(markerRegistry);
        this.scheduler = Objects.requireNonNull(scheduler);
        this.roleDefinitions = Objects.requireNonNull(roleDefinitions);
        this.requiredMarkerResolver = new RequiredMarkerResolver(Objects.requireNonNull(templateResolver), this.roleDefinitions);
    }

    @Override
    protected void execute(
        @Nonnull CommandContext context,
        @Nonnull Store<EntityStore> store,
        @Nonnull Ref<EntityStore> ref,
        @Nonnull PlayerRef playerRef,
        @Nonnull World world
    ) {
        String rawNpcName = npcNameArg.get(context);
        String normalizedNpcName = normalizeName(rawNpcName);
        if (normalizedNpcName == null) {
            context.sendMessage(Message.raw("[knpc] Usage: /knpc marker reassign <npcName> <markerName>"));
            return;
        }

        NpcLookupResult npcLookup = findNpcByName(normalizedNpcName);
        if (npcLookup.matches().isEmpty()) {
            if (roleDefinitions.findByRoleId(normalizedNpcName).isPresent()) {
                context.sendMessage(Message.raw("[knpc] AMBIGUOUS: '" + rawNpcName.trim() + "' is a roleId."));
                context.sendMessage(Message.raw("[knpc] Use /knpc marker set <role> <markerName> for spawn staging."));
                context.sendMessage(Message.raw("[knpc] Use /knpc marker reassign <npcName> <markerName> for NPC marker assignment."));
            } else {
                context.sendMessage(Message.raw("[knpc] NPC not found by name: '" + rawNpcName.trim() + "'."));
            }
            return;
        }

        if (npcLookup.matches().size() > 1) {
            context.sendMessage(Message.raw("[knpc] AMBIGUOUS npcName '" + rawNpcName.trim()
                + "': " + npcLookup.matches().size() + " NPCs match."));
            for (NpcRecord candidate : npcLookup.matches()) {
                context.sendMessage(Message.raw("- npcName='" + candidate.npcName() + "' npcId=" + candidate.npcId()
                    + " role=" + candidate.roleId()));
            }
            return;
        }

        NpcRecord npc = npcLookup.matches().get(0);
        String roleId = npc.roleId();

        String rawMarkerName = markerNameArg.get(context);
        String markerName = normalizeName(rawMarkerName);
        if (markerName == null) {
            context.sendMessage(Message.raw("[knpc] Usage: /knpc marker reassign <npcName> <markerName>"));
            return;
        }

        RequiredMarkerResolver.Requirement requirement = findRequirement(roleId, markerName);
        if (requirement == null) {
            context.sendMessage(Message.raw("[knpc] markerName '" + markerName + "' is not required for role '" + roleId + "'."));
            context.sendMessage(Message.raw("[knpc] Allowed marker names: " + String.join(", ", requiredMarkerResolver.resolveRequiredMarkerNames(roleId))));
            return;
        }

        MarkerType markerType = requirement.markerType();
        if (markerType == null) {
            context.sendMessage(Message.raw("[knpc] markerRoles mapping is invalid for markerName '" + markerName + "' in role '" + roleId + "'."));
            return;
        }

        var transformType = TransformComponent.getComponentType();
        if (transformType == null) {
            context.sendMessage(Message.raw("[knpc] Could not read player position."));
            return;
        }

        TransformComponent transform = store.getComponent(ref, transformType);
        if (transform == null) {
            context.sendMessage(Message.raw("[knpc] Could not read player position."));
            return;
        }

        Vector3d positionRaw = transform.getPosition();
        if (!isFinitePosition(positionRaw)) {
            context.sendMessage(Message.raw("[knpc] Could not reassign marker: player position contains non-finite values."));
            return;
        }

        WorldId worldId = new WorldId(world.getName());
        Vec3 position = new Vec3(positionRaw.x(), positionRaw.y(), positionRaw.z());
        String newMarkerId = UUID.randomUUID().toString();
        MarkerRecord newMarker = new MarkerRecord(newMarkerId, worldId, position, markerType);

        Map<String, MarkerAssignment> assignmentsBefore = new LinkedHashMap<>(npc.markerAssignments());
        String markerKey = markerName;
        String logicalKey = logicalKeyForType(markerType);

        Set<String> replacedOldMarkerIds = new LinkedHashSet<>();
        addAssignedMarkerIdIfPresent(replacedOldMarkerIds, assignmentsBefore.get(markerKey));
        addAssignedMarkerIdIfPresent(replacedOldMarkerIds, assignmentsBefore.get(logicalKey));

        Map<String, MarkerAssignment> assignmentsAfter = new LinkedHashMap<>(assignmentsBefore);
        MarkerAssignment newAssignment = new MarkerAssignment(newMarkerId, markerType);
        assignmentsAfter.put(markerKey, newAssignment);
        assignmentsAfter.put(logicalKey, newAssignment);

        markerRegistry.upsert(newMarker);
        npc.markerAssignments(assignmentsAfter);

        if (!plugin.saveStateSafely()) {
            npc.markerAssignments(assignmentsBefore);
            boolean markerRollbackOk = markerRegistry.removeById(newMarkerId);

            if (markerRollbackOk) {
                context.sendMessage(Message.raw("[knpc] Reassign aborted: state persistence failed. Runtime rollback completed."));
            } else {
                context.sendMessage(Message.raw("[knpc] Reassign aborted: state persistence failed and rollback was incomplete."));
                context.sendMessage(Message.raw("[knpc] Runtime/state drift risk: new marker could not be removed after save failure."));
            }
            return;
        }

        int retainedOldMarkers = 0;
        List<String> retainedOldMarkerIds = new ArrayList<>();
        for (String oldMarkerId : replacedOldMarkerIds) {
            if (oldMarkerId == null || oldMarkerId.isBlank()) {
                continue;
            }
            if (oldMarkerId.equalsIgnoreCase(newMarkerId)) {
                continue;
            }

            retainedOldMarkers++;
            retainedOldMarkerIds.add(oldMarkerId);
        }

        context.sendMessage(Message.raw("[KNPC][Marker][Reassign] npc='" + npc.npcName()
            + "' markerName='" + markerName
            + "' type=" + markerType.name()
            + " markerId=" + newMarkerId
            + " pos=" + NpcDebugSupport.formatPositionForChat(position)
            + " retainedOldMarkers=" + retainedOldMarkers));

        if (!retainedOldMarkerIds.isEmpty()) {
            context.sendMessage(Message.raw("[KNPC][Marker][Reassign] old marker(s) retained for later safe cleanup: "
                + String.join(", ", retainedOldMarkerIds)));
        }
    }

    private NpcLookupResult findNpcByName(String normalizedNpcName) {
        List<NpcRecord> matches = new ArrayList<>();
        for (NpcRecord npc : scheduler.snapshotIndexed()) {
            String candidate = normalizeName(npc.npcName());
            if (candidate != null && candidate.equals(normalizedNpcName)) {
                matches.add(npc);
            }
        }

        return new NpcLookupResult(List.copyOf(matches));
    }

    private RequiredMarkerResolver.Requirement findRequirement(String roleId, String markerName) {
        for (RequiredMarkerResolver.Requirement requirement : requiredMarkerResolver.resolveRequirements(roleId)) {
            if (markerName.equals(requirement.name())) {
                return requirement;
            }
        }

        return null;
    }

    private static void addAssignedMarkerIdIfPresent(Set<String> markerIds, MarkerAssignment assignment) {
        if (markerIds == null || assignment == null) {
            return;
        }

        String markerId = assignment.markerId();
        if (markerId == null || markerId.isBlank()) {
            return;
        }

        markerIds.add(markerId.trim());
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

    private static String normalizeName(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isFinitePosition(Vector3d position) {
        return position != null
            && Double.isFinite(position.x())
            && Double.isFinite(position.y())
            && Double.isFinite(position.z());
    }

    private record NpcLookupResult(
        List<NpcRecord> matches
    ) {
    }
}