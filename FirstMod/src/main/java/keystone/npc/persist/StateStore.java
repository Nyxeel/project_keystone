package keystone.npc.persist;

import keystone.npc.model.NpcRecord;
import keystone.npc.world.MarkerRecord;

import java.util.List;

public interface StateStore {
    PluginState load();

    void save(List<MarkerRecord> markers, List<NpcRecord> npcs);
}
