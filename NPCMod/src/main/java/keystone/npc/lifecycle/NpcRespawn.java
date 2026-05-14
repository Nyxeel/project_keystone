











package keystone.npc.lifecycle;

import keystone.npc.definition.NpcDefinition;
import keystone.npc.state.NpcStateStore;

public final class NpcRespawn {

    private final NpcStateStore stateStore;
    private final NpcDefinition definition;
    private final NpcRelink relink;
    private final NpcSpawn spawn;

    public NpcRespawn(
            NpcStateStore stateStore,
            NpcDefinition definition,
            NpcRelink relink,
            NpcSpawn spawn
    ) {
        this.stateStore = stateStore;
        this.definition = definition;
        this.relink = relink;
        this.spawn = spawn;
    }

    public void queueInitialRespawnCheck() {
        // TODO: Initialen Respawn-Check planen.
    }

    public void respawnMissingNpcs(boolean dryRun, boolean force) {
        // TODO: Fehlende NPCs kontrolliert ersetzen.
    }

    public boolean canRespawn(String npcId) {
        // TODO: Respawn-Policy prüfen.
        return false;
    }
}