package keystone.npc.persist;

import keystone.npc.model.NpcRecord;
import keystone.npc.world.MarkerRecord;

import java.util.List;

/** Persisted root object for MVP A. */
public record PluginState(
        List<MarkerRecord> markers,
        List<NpcRecord> npcs
) {
    public static PluginState empty() {
        return new PluginState(List.of(), List.of());
    }
}
