package keystone.npc.spawn;

import java.util.List;

public record SpawnProfile(
    String id,
    Integer version,
    List<String> allowedBiomes,
    List<String> blockedStructureTags,
    String spawnMode,
    Integer maxPerChunkArea,
    Boolean despawnWhenFarFromPlayers
)
{
    public SpawnProfile {
        allowedBiomes = allowedBiomes == null ? List.of() : List.copyOf(allowedBiomes);
        blockedStructureTags = blockedStructureTags == null ? List.of() : List.copyOf(blockedStructureTags);
    }
}
