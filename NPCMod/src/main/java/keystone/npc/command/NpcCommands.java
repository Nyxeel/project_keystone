package keystone.npc.command;

import java.util.Objects;

import keystone.npc.lifecycle.NpcRemoval;
import keystone.npc.lifecycle.NpcRespawn;
import keystone.npc.lifecycle.NpcSpawn;
import keystone.npc.marker.MarkerAssignment;
import keystone.npc.state.NpcStateStore;

/*
 * NpcCommands ist die einfache Command-Schicht der Mod.
 *
 * Diese Klasse soll keine tiefe NPC-Logik selbst ausführen.
 * Sie prüft nur einfache Eingaben und ruft dann die passenden Services auf.
 *
 * Wichtig:
 * - Keine EntityRef speichern.
 * - Keine Runtime-Entity speichern.
 * - Keine state.json direkt verändern.
 * - Keine Marker direkt mutieren.
 */
public final class NpcCommands {

    private final NpcSpawn spawn;
    private final NpcRemoval removal;
    private final MarkerAssignment markerAssignment;
    private final NpcRespawn respawn;
    private final NpcStateStore stateStore;

    /*
     * Erstellt die Command-Schicht mit allen benötigten Services.
     */
    public NpcCommands(
            NpcSpawn spawn,
            NpcRemoval removal,
            MarkerAssignment markerAssignment,
            NpcRespawn respawn,
            NpcStateStore stateStore
    ) {
        this.spawn = Objects.requireNonNull(spawn, "spawn must not be null");
        this.removal = Objects.requireNonNull(removal, "removal must not be null");
        this.markerAssignment = Objects.requireNonNull(markerAssignment, "markerAssignment must not be null");
        this.respawn = Objects.requireNonNull(respawn, "respawn must not be null");
        this.stateStore = Objects.requireNonNull(stateStore, "stateStore must not be null");
    }

    /*
     * Registriert später echte Hytale-Commands.
     * Im Skeleton wird hier noch nichts registriert.
     */
    public void registerCommands() {
        // TODO: Commands registrieren, sobald die echte Hytale-Command-API angebunden wird.
    }

    /*
     * Leitet einen Spawn-Befehl an den Spawn-Service weiter.
     */
    public Object handleSpawnCommand(String roleId, String npcName) {
        requireText(roleId, "roleId");
        requireText(npcName, "npcName");

        return spawn.spawnNpc(roleId, npcName);
    }

    /*
     * Leitet einen Remove-Befehl an den Removal-Service weiter.
     */
    public Object handleRemoveCommand(String npcId) {
        requireText(npcId, "npcId");

        return removal.removeNpcDetailed(npcId);
    }

    /*
     * Leitet eine Marker-Zuweisung an den MarkerAssignment-Service weiter.
     */
    public boolean handleMarkerSetCommand(String npcId, String markerName, String markerId) {
        requireText(npcId, "npcId");
        requireText(markerName, "markerName");
        requireText(markerId, "markerId");

        return markerAssignment.assignMarkerToNpc(npcId, markerName, markerId);
    }

    /*
     * Leitet den Respawn-Missing-Befehl an den Respawn-Service weiter.
     */
    public void handleRespawnMissingCommand(boolean dryRun, boolean force) {
        respawn.respawnMissingNpcs(dryRun, force);
    }

    /*
     * Prüft, ob ein Command-Textwert vorhanden ist.
     */
    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank.");
        }
    }
}