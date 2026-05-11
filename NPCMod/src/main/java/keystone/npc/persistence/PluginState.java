package keystone.npc.persistence;

import keystone.npc.domain.NpcRecord;
import keystone.npc.markers.MarkerRecord;
import keystone.npc.markers.MarkerType;

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
