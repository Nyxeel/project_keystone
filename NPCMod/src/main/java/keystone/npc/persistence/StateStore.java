package keystone.npc.persistence;

import keystone.npc.domain.NpcRecord;
import keystone.npc.markers.MarkerRecord;
import keystone.npc.markers.MarkerType;

import java.util.List;
import java.util.Map;

public interface StateStore {
    PluginState load();

    void save(List<MarkerRecord> markers, List<NpcRecord> npcs, Map<MarkerType, String> activeMarkerIds);
}
