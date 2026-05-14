package keystone.npc.lifecycle;

import java.util.Objects;

import keystone.npc.definition.NpcDefinition;
import keystone.npc.marker.MarkerAssignment;
import keystone.npc.state.NpcStateStore;

/*
 * NpcSpawn ist die Skeleton-Schicht für kontrolliertes NPC-Spawning.
 *
 * Später muss diese Klasse prüfen:
 * - ist die Rolle gültig?
 * - sind alle nötigen Marker vorhanden?
 * - kann sicher gespeichert werden?
 * - kann bei Fehler sauber zurückgerollt werden?
 *
 * Wichtig:
 * Diese Klasse darf keine halbfertigen NPCs erzeugen und keine EntityRef persistieren.
 */
public final class NpcSpawn {

    private final NpcStateStore stateStore;
    private final NpcDefinition definition;
    private final MarkerAssignment markerAssignment;
    private final NpcRemoval removal;

    /*
     * Erstellt den Spawn-Service mit Zugriff auf State, Definitionen, Marker und Removal.
     */
    public NpcSpawn(
            NpcStateStore stateStore,
            NpcDefinition definition,
            MarkerAssignment markerAssignment,
            NpcRemoval removal
    ) {
        this.stateStore = Objects.requireNonNull(stateStore, "stateStore must not be null");
        this.definition = Objects.requireNonNull(definition, "definition must not be null");
        this.markerAssignment = Objects.requireNonNull(markerAssignment, "markerAssignment must not be null");
        this.removal = Objects.requireNonNull(removal, "removal must not be null");
    }

    /*
     * Erzeugt später kontrolliert einen NPC.
     * Im Skeleton ist echtes Spawning noch nicht implementiert.
     */
    public Object spawnNpc(String roleId, String npcName) {
        requireText(roleId, "roleId");
        requireText(npcName, "npcName");

        // TODO: NPC kontrolliert erzeugen.
        // TODO: Vor echtem Spawn Marker, Definition, Save und Rollback prüfen.
        throw new UnsupportedOperationException("NPC spawn is not implemented yet.");
    }

    /*
     * Prüft später, ob eine Spawn-Anfrage erlaubt ist.
     * Im Skeleton ist Spawn nur erlaubt, wenn die Definition es ausdrücklich erlaubt.
     */
    public boolean validateSpawnRequest(String roleId, String npcName) {
        if (roleId == null || roleId.isBlank()) {
            return false;
        }

        if (npcName == null || npcName.isBlank()) {
            return false;
        }

        // TODO: Spawn-Anfrage später zusätzlich gegen Marker und Spawn-Policy prüfen.
        return definition.isSpawnable(roleId);
    }

    /*
     * Prüft, ob ein Textwert vorhanden ist.
     */
    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank.");
        }
    }
}