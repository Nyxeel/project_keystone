package keystone.npc.marker;

import java.util.Objects;

import keystone.npc.definition.NpcDefinition;
import keystone.npc.state.NpcStateStore;

/*
 * MarkerAssignment ist die Skeleton-Schicht für NPC-Marker-Zuweisungen.
 *
 * Später entscheidet diese Klasse:
 * - welcher NPC welchen Marker benutzen darf
 * - ob markerId existiert
 * - ob MarkerType passt
 * - ob Marker und NPC in derselben Welt sind
 *
 * Wichtig:
 * Read-only-Methoden dürfen niemals state.json verändern.
 * Marker dürfen nicht blind oder automatisch ersetzt werden.
 */
public final class MarkerAssignment {

    private final MarkerRegistry markerRegistry;
    private final NpcStateStore stateStore;
    private final NpcDefinition definition;

    /*
     * Erstellt den MarkerAssignment-Service mit Zugriff auf Marker, State und Definitionen.
     */
    public MarkerAssignment(
            MarkerRegistry markerRegistry,
            NpcStateStore stateStore,
            NpcDefinition definition
    ) {
        this.markerRegistry = Objects.requireNonNull(markerRegistry, "markerRegistry must not be null");
        this.stateStore = Objects.requireNonNull(stateStore, "stateStore must not be null");
        this.definition = Objects.requireNonNull(definition, "definition must not be null");
    }

    /*
     * Weist später einem NPC einen Marker zu.
     * Im Skeleton wird noch nichts verändert.
     */
    public boolean assignMarkerToNpc(String npcId, String markerName, String markerId) {
        requireText(npcId, "npcId");
        requireText(markerName, "markerName");
        requireText(markerId, "markerId");

        // TODO: Marker sicher zuweisen.
        // TODO: Prüfen: markerId existiert, MarkerType passt, worldId passt, NPC existiert.
        // TODO: Save-Failure später ehrlich behandeln.
        return false;
    }

    /*
     * Entfernt später eine Marker-Zuweisung von einem NPC.
     * Im Skeleton ist diese mutierende Aktion noch nicht implementiert.
     */
    public void clearMarkerAssignment(String npcId, String markerName) {
        requireText(npcId, "npcId");
        requireText(markerName, "markerName");

        // TODO: Marker-Zuweisung später sicher entfernen.
        throw new UnsupportedOperationException("NPC marker assignment clearing is not implemented yet.");
    }

    /*
     * Löst später einen Marker nur lesend auf.
     * Diese Methode darf niemals MarkerAssignments reparieren oder state.json verändern.
     */
    public Object resolveMarkerReadOnly(String npcId, String markerName) {
        requireText(npcId, "npcId");
        requireText(markerName, "markerName");

        // TODO: Marker später nur lesend auflösen, ohne State zu mutieren.
        throw new UnsupportedOperationException("NPC marker read-only resolving is not implemented yet.");
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