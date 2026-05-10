package keystone.npc.persist;

import keystone.npc.model.NpcRecord;
import keystone.npc.world.MarkerRecord;
import keystone.npc.world.MarkerType;

import java.util.List;
import java.util.Map;

public interface StateStore {
    PluginState load();

    void save(List<MarkerRecord> markers, List<NpcRecord> npcs, Map<MarkerType, String> activeMarkerIds);
}
