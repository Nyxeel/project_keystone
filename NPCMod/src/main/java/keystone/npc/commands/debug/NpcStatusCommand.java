package keystone.npc.commands.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import keystone.npc.debug.NpcDebugSupport;
import keystone.npc.definition.NpcTemplateResolver;
import keystone.npc.domain.NpcRecord;
import keystone.npc.markers.MarkerRegistry;
import keystone.npc.markers.MarkerType;
import keystone.npc.markers.RequiredMarkerResolver;
import keystone.npc.roles.RoleDefinitionRegistry;
import keystone.npc.routine.NpcRoutineRunner;
import keystone.npc.skills.NpcSkill;

public final class NpcStatusCommand extends CommandBase {

    @Nonnull
    private final OptionalArg<String> npcArg = this.withOptionalArg("npc", "keystone.commands.knpc.status.npc", ArgTypes.STRING);

    private final MarkerRegistry markerRegistry;
    private final NpcRoutineRunner scheduler;
    private final NpcTemplateResolver templateResolver;
    private final RoleDefinitionRegistry roleDefinitions;
    private final RequiredMarkerResolver requiredMarkerResolver;

    public NpcStatusCommand(
        MarkerRegistry markerRegistry,
        NpcRoutineRunner scheduler,
        NpcTemplateResolver templateResolver,
        RoleDefinitionRegistry roleDefinitions
    ) {
        super("status", "keystone.commands.knpc.status");
        this.markerRegistry = Objects.requireNonNull(markerRegistry);
        this.scheduler = Objects.requireNonNull(scheduler);
        this.templateResolver = Objects.requireNonNull(templateResolver);
        this.roleDefinitions = Objects.requireNonNull(roleDefinitions);
        this.requiredMarkerResolver = new RequiredMarkerResolver(
            templateResolver,
            this.roleDefinitions
        );
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        List<NpcRecord> snapshot = new ArrayList<>(scheduler.snapshot());
        if (snapshot.isEmpty()) {
            context.sendMessage(Message.raw("[KNPC][Status] Keine NPCs aktiv."));
            return;
        }

        if (context.provided(npcArg)) {
            String requested = npcArg.get(context);
            NpcRecord npc = scheduler.findNpcByNameOrId(requested).orElse(null);
            if (npc == null) {
                context.sendMessage(Message.raw("[KNPC][Status] NPC nicht gefunden: " + requested));
                context.sendMessage(Message.raw("[KNPC][Status] Verfuegbar: " + availableNpcNames(snapshot)));
                return;
            }

            sendNpcSnapshot(context, npc);
            return;
        }

        for (int i = 0; i < snapshot.size(); i++) {
            if (i > 0) {
                context.sendMessage(Message.raw(""));
            }
            sendNpcSnapshot(context, snapshot.get(i));
        }
    }

    private void sendNpcSnapshot(CommandContext context, NpcRecord npc) {
        context.sendMessage(Message.raw("[KNPC][Status] " + npc.npcName()));
        context.sendMessage(Message.raw("state: " + npc.state().name()));
        context.sendMessage(Message.raw("entityStatus: " + npc.entityStatus().name()));
        context.sendMessage(Message.raw("targetMarker: " + nullToNone(npc.activeRoutineMarker())));
        context.sendMessage(Message.raw("currentAction: " + nullToNone(npc.activeActionId())));
        context.sendMessage(Message.raw("routine: " + routineLabel(npc)));

        String skillSummary = "OPEN_DOORS=" + NpcDebugSupport.skillValueForStatus(
            templateResolver,
            npc,
            NpcSkill.OPEN_DOORS,
            true
        )
            + ", USE_CHEST=" + NpcDebugSupport.skillValueForStatus(templateResolver, npc, NpcSkill.USE_CHEST, false)
            + ", USE_BED=" + NpcDebugSupport.skillValueForStatus(templateResolver, npc, NpcSkill.USE_BED, false)
            + ", USE_TOOLS=" + NpcDebugSupport.skillValueForStatus(templateResolver, npc, NpcSkill.USE_TOOLS, false)
            + ", ATTACK_MELEE=" + NpcDebugSupport.skillValueForStatus(templateResolver, npc, NpcSkill.ATTACK_MELEE, false)
            + ", ATTACK_RANGED=" + NpcDebugSupport.skillValueForStatus(templateResolver, npc, NpcSkill.ATTACK_RANGED, false);
        context.sendMessage(Message.raw("skills: " + skillSummary));

        List<String> invalidRoleReasons = roleDefinitions.invalidRoleReasons(npc.roleId());
        if (!invalidRoleReasons.isEmpty()) {
            context.sendMessage(Message.raw("Role " + npc.roleId() + " is invalid:"));
            for (String reason : invalidRoleReasons) {
                context.sendMessage(Message.raw("- " + reason));
            }
        }

        List<MarkerType> allowedMarkers = resolveAllowedMarkers(npc);
        List<NpcDebugSupport.RequiredMarkerStatus> requiredStatuses = NpcDebugSupport.resolveRequiredMarkerStatuses(
            npc,
            markerRegistry,
            templateResolver,
            roleDefinitions
        );

        context.sendMessage(Message.raw("Required markers for " + npc.roleId() + ":"));
        if (allowedMarkers.isEmpty()) {
            context.sendMessage(Message.raw("- none"));
        } else {
            for (MarkerType markerType : allowedMarkers) {
                boolean present = false;
                for (NpcDebugSupport.RequiredMarkerStatus status : requiredStatuses) {
                    if (status.markerType() == markerType) {
                        present = status.resolvedMarker() != null;
                        break;
                    }
                }
                context.sendMessage(Message.raw("- " + markerType.name() + ": " + (present ? "OK" : "MISSING")));
            }
        }

        List<String> invalidStoredMarkers = resolveInvalidStoredMarkers(npc, allowedMarkers);
        if (!invalidStoredMarkers.isEmpty()) {
            context.sendMessage(Message.raw("Invalid stored markers:"));
            for (String invalidStoredMarker : invalidStoredMarkers) {
                context.sendMessage(Message.raw("- " + invalidStoredMarker + ": not valid for role " + npc.roleId()));
            }
        }
    }

    private String availableNpcNames(List<NpcRecord> npcs) {
        List<String> names = new ArrayList<>();
        for (NpcRecord npc : npcs) {
            names.add(npc.npcName() + "(" + npc.npcId() + ")");
        }
        return String.join(", ", names);
    }

    private String routineLabel(NpcRecord npc) {
        String marker = npc.activeRoutineMarker();
        if (marker == null || marker.isBlank() || npc.activeRoutineState() == null) {
            return "waiting";
        }

        String source = npc.activeRoutineSource();
        String routine = marker.toLowerCase(Locale.ROOT) + "/" + npc.activeRoutineState().name();
        if (source == null || source.isBlank()) {
            return routine;
        }
        return routine + " source=" + source;
    }

    private String nullToNone(String value) {
        return value == null || value.isBlank() ? "none" : value;
    }

    private List<MarkerType> resolveAllowedMarkers(NpcRecord npc) {
        List<MarkerType> allowed = new ArrayList<>();
        for (RequiredMarkerResolver.Requirement requirement : requiredMarkerResolver.resolveRequirements(npc.roleId())) {
            MarkerType markerType = requirement.markerType();
            if (markerType != null && !allowed.contains(markerType)) {
                allowed.add(markerType);
            }
        }
        return allowed;
    }

    private List<String> resolveInvalidStoredMarkers(NpcRecord npc, List<MarkerType> allowedMarkers) {
        List<String> invalid = new ArrayList<>();
        for (MarkerType markerType : MarkerType.values()) {
            if (allowedMarkers.contains(markerType)) {
                continue;
            }

            String markerId = switch (markerType) {
                case BED -> npc.bedMarkerId();
                case DOOR -> npc.doorMarkerId();
                case CHEST -> npc.chestMarkerId();
                case FOOD -> npc.foodMarkerId();
                case WORK -> npc.workMarkerId();
                case CHILL -> npc.chillMarkerId();
            };

            if (markerId != null && !markerId.isBlank()) {
                invalid.add(markerType.name());
            }
        }

        return invalid;
    }
}
