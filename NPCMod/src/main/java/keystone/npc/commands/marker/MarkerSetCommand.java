package keystone.npc.commands.marker;

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
import java.util.Locale;
import java.util.Objects;
import javax.annotation.Nonnull;
import keystone.npc.markers.MarkerRegistry;
import keystone.npc.markers.MarkerType;
import keystone.npc.markers.Vec3;
import keystone.npc.markers.WorldId;
import org.joml.Vector3d;

public final class MarkerSetCommand extends AbstractPlayerCommand {

    @Nonnull
    private final RequiredArg<String> markerTypeArg = this.withRequiredArg("markerType", "keystone.commands.knpc.marker.set.type", ArgTypes.STRING);

    private final MarkerRegistry markerRegistry;

    public MarkerSetCommand(MarkerRegistry markerRegistry) {
        super("set", "keystone.commands.knpc.marker.set");
        this.markerRegistry = Objects.requireNonNull(markerRegistry);
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
            context.sendMessage(Message.raw("[knpc] Unknown marker type: '" + rawType + "'. Use: bed|door|work"));
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

        markerRegistry.setActive(type, worldId, pos);
        context.sendMessage(Message.raw("[knpc] Marker '" + type.name().toLowerCase(Locale.ROOT) + "' set to " + pos + " (world=" + worldId + ")"));
    }

    private static MarkerType parseMarkerType(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "bed" -> MarkerType.BED;
            case "door" -> MarkerType.DOOR;
            case "work" -> MarkerType.WORK;
            default -> null;
        };
    }
}
