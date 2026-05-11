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

import keystone.npc.capabilities.NpcCapability;
import keystone.npc.debug.NpcDebugSupport;
import keystone.npc.definition.NpcTemplateResolver;
import keystone.npc.domain.NpcRecord;
import keystone.npc.markers.MarkerRegistry;
import keystone.npc.markers.RequiredMarkerResolver;
import keystone.npc.roles.RoleDefinitionRegistry;
import keystone.npc.routine.NpcRoutineRunner;

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
        context.sendMessage(Message.raw("targetMarker: " + nullToNone(npc.activeRoutineMarker())));
        context.sendMessage(Message.raw("currentAction: " + nullToNone(npc.activeActionId())));
        context.sendMessage(Message.raw("routine: " + routineLabel(npc)));

        String capabilitySummary = "OPEN_DOORS=" + NpcDebugSupport.capabilityValueForStatus(
            templateResolver,
            npc,
            NpcCapability.OPEN_DOORS,
            true
        )
            + ", USE_CHEST=" + NpcDebugSupport.capabilityValueForStatus(templateResolver, npc, NpcCapability.USE_CHEST, false)
            + ", USE_BED=" + NpcDebugSupport.capabilityValueForStatus(templateResolver, npc, NpcCapability.USE_BED, false)
            + ", USE_TOOLS=" + NpcDebugSupport.capabilityValueForStatus(templateResolver, npc, NpcCapability.USE_TOOLS, false)
            + ", ATTACK_MELEE=" + NpcDebugSupport.capabilityValueForStatus(templateResolver, npc, NpcCapability.ATTACK_MELEE, false)
            + ", ATTACK_RANGED=" + NpcDebugSupport.capabilityValueForStatus(templateResolver, npc, NpcCapability.ATTACK_RANGED, false);
        context.sendMessage(Message.raw("capabilities: " + capabilitySummary));

        List<String> markerLines = NpcDebugSupport.buildMarkerSnapshotLines(
            npc,
            markerRegistry,
            templateResolver,
            roleDefinitions
        );
        List<String> missingMarkers = NpcDebugSupport.missingRequiredMarkerNames(
            npc,
            markerRegistry,
            templateResolver,
            roleDefinitions
        );
        List<String> requiredMarkers = requiredMarkerResolver.resolveRequiredMarkerNames(npc.roleId());
        List<String> unsupportedMarkers = requiredMarkerNames(
            NpcDebugSupport.resolveRequiredMarkerStatuses(
                npc,
                markerRegistry,
                templateResolver,
                roleDefinitions
            ),
            false
        );

        context.sendMessage(Message.raw("requiredMarkers: "
            + (requiredMarkers.isEmpty() ? "none" : String.join(", ", requiredMarkers))));
        context.sendMessage(Message.raw("unsupportedRequiredMarkers: "
            + (unsupportedMarkers.isEmpty() ? "none" : String.join(", ", unsupportedMarkers))));
        context.sendMessage(Message.raw("missingMarkers: "
            + (missingMarkers.isEmpty() ? "none" : String.join(", ", missingMarkers))));
        context.sendMessage(Message.raw(""));

        for (String line : markerLines) {
            context.sendMessage(Message.raw(line));
        }

        for (String missing : missingMarkers) {
            context.sendMessage(Message.raw("[KNPC][Warning] " + npc.npcName()
                + ": required marker " + missing + " fehlt"));
        }
    }

    private List<String> requiredMarkerNames(List<NpcDebugSupport.RequiredMarkerStatus> statuses, boolean supportedOnly) {
        List<String> names = new ArrayList<>();
        for (NpcDebugSupport.RequiredMarkerStatus status : statuses) {
            if (status == null || status.name() == null || status.name().isBlank()) {
                continue;
            }

            if (supportedOnly && !status.supported()) {
                continue;
            }

            if (!supportedOnly && status.supported()) {
                continue;
            }

            names.add(status.name().toLowerCase(Locale.ROOT));
        }
        return names;
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
}
