package keystone.npc.world;

import java.util.Objects;

public record MarkerRecord(
        String markerId,
        WorldId worldId,
        Vec3 position,
        MarkerType type
) {
    public MarkerRecord {
        Objects.requireNonNull(markerId);
        Objects.requireNonNull(worldId);
        Objects.requireNonNull(position);
        Objects.requireNonNull(type);
    }
}
