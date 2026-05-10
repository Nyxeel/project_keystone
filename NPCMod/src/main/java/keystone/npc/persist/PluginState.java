package keystone.npc.persist;

import keystone.npc.model.NpcRecord;
import keystone.npc.world.MarkerRecord;
import keystone.npc.world.MarkerType;

import java.util.List;
import java.util.Map;

/** Persisted root object for MVP A. */
public record PluginState(
        List<MarkerRecord> markers,
        List<NpcRecord> npcs,
        Map<MarkerType, String> activeMarkerIds
) {
    public static PluginState empty() {
        return new PluginState(List.of(), List.of(), Map.of());
    }
}
