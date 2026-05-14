
















package keystone.npc.lifecycle;

import keystone.npc.definition.NpcDefinition;
import keystone.npc.state.NpcStateStore;

public final class NpcRelink {

    private final NpcStateStore stateStore;
    private final NpcDefinition definition;

    public NpcRelink(
            NpcStateStore stateStore,
            NpcDefinition definition
    ) {
        this.stateStore = stateStore;
        this.definition = definition;
    }

    public void prepareRelinkAfterStartup() {
        // TODO: Relink nach Start vorbereiten.
    }

    public Object tryRelink(String npcId) {
        // TODO: UUID zuerst, Anchor nur letzter Fallback.
        return null;
    }

    public Object tryUuidRelink(String npcId) {
        // TODO: Entity über entityUuid suchen.
        return null;
    }

    public Object tryAnchorRelink(String npcId) {
        // TODO: Nur letzter Fallback.
        return null;
    }
}