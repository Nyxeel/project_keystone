package keystone.npc.commands.marker;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
import keystone.npc.domain.NpcRecord;
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
                + "'. Use: bed|chest|food|work|chill"));
            return;
        }

        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) {
            context.sendMessage(Message.raw("[knpc] Could not read player position (TransformComponent missing)."));
            return;
        }

        Vector3d p = transform.getPosition();
        var worldId = new WorldId(world.getName());
        var pos = new Vec3(p.x(), p.y(), p.z());

        String markerTypeName = type.name().toLowerCase(Locale.ROOT);
        NpcRecord targetNpc = null;
        if (context.provided(targetNpcArg)) {
            String target = targetNpcArg.get(context);
            targetNpc = resolveTargetNpc(context, target, markerTypeName);
            if (targetNpc == null) {
                return;
            }

            if (!isMarkerAllowedForNpc(targetNpc, type)) {
                List<String> allowed = requiredMarkerResolver.resolveRequiredMarkerNames(targetNpc.roleId());
                context.sendMessage(Message.raw("[knpc] Marker '" + markerTypeName + "' ist fuer NPC '"
                    + targetNpc.npcName() + "' nicht erlaubt. requiredMarkers="
                    + (allowed.isEmpty() ? "none" : String.join(", ", allowed))));
                return;
            }
        }

        markerRegistry.setActive(type, worldId, pos);
        String markerId = markerRegistry.getActive(type).map(marker -> marker.markerId()).orElse(null);
        if (markerId == null || markerId.isBlank()) {
            context.sendMessage(Message.raw("[knpc] Marker konnte nicht gespeichert werden."));
            return;
        }

        if (targetNpc != null) {
            scheduler.assignMarkerToNpc(targetNpc, type, markerId);
            plugin.saveState();

            int npcIndex = scheduler.indexOfNpc(targetNpc.npcId());
            context.sendMessage(Message.raw("[KNPC][Marker] " + markerTypeName
                + " gesetzt bei " + NpcDebugSupport.formatPositionForChat(pos)
                + " und NPC #" + npcIndex + " ('" + targetNpc.npcName() + "') zugewiesen."));
        } else {
            context.sendMessage(Message.raw("[KNPC][Marker] " + markerTypeName
                + " gesetzt bei " + NpcDebugSupport.formatPositionForChat(pos)));
        }
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

    private static MarkerType parseMarkerType(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "bed" -> MarkerType.BED;
            case "chest" -> MarkerType.CHEST;
            case "food" -> MarkerType.FOOD;
            case "work" -> MarkerType.WORK;
            case "chill" -> MarkerType.CHILL;
            default -> null;
        };
    }
}
