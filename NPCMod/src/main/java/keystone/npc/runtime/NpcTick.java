package keystone.npc.runtime;

import keystone.npc.definition.NpcDefinition;
import keystone.npc.lifecycle.NpcRelink;
import keystone.npc.navigation.NpcNavigation;
import keystone.npc.state.NpcStateStore;

public final class NpcTick {

    private final NpcStateStore stateStore;
    private final NpcDefinition definition;
    private final NpcRelink relink;
    private final NpcNavigation navigation;

    private boolean running;

    public NpcTick(
            NpcStateStore stateStore,
            NpcDefinition definition,
            NpcRelink relink,
            NpcNavigation navigation
    ) {
        this.stateStore = stateStore;
        this.definition = definition;
        this.relink = relink;
        this.navigation = navigation;
    }

    public void start() {
        running = true;
        // TODO: Tick starten.
    }

    public void stop() {
        running = false;
        // TODO: Tick stoppen.
    }

    public void tickAll() {
        // TODO: Alle NPCs ticken.
    }

    public void tickNpc(String npcId) {
        // TODO: Live-Entity-Gate prüfen.
    }

    public boolean isRunning() {
        return running;
    }
}