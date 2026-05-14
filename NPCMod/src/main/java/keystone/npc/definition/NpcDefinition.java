package keystone.npc.definition;

import keystone.npc.KeystoneNPCPlugin;

public final class NpcDefinition {

    private final KeystoneNPCPlugin plugin;

    public NpcDefinition(KeystoneNPCPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadDefinitions() {
        // TODO: NPC-Definitionen laden.
        // Prüft hytaleRole, roleId, requiredMarkers, markerRoles.
    }

    public void reloadDefinitions() {
        // TODO: Definitionen neu laden.
    }

    public boolean isSpawnable(String roleId) {
        // TODO: Prüfen, ob Rolle gültig spawnbar ist.
        return false;
    }

    public KeystoneNPCPlugin plugin() {
        return plugin;
    }
}