package keystone.npc.lifecycle;

import java.util.Objects;

import keystone.npc.marker.MarkerAssignment;
import keystone.npc.state.NpcStateStore;

/*
 * NpcRemoval ist die Skeleton-Schicht für das sichere Entfernen von NPCs.
 *
 * Später muss diese Klasse prüfen:
 * - darf der NPC entfernt werden?
 * - ist die Runtime-Entity sicher entfernbar?
 * - dürfen MarkerAssignments aufgeräumt werden?
 * - darf der persistente Record gelöscht werden?
 *
 * Wichtig:
 * Diese Klasse darf keine EntityRef persistieren und keine state.json direkt unsicher überschreiben.
 */
public final class NpcRemoval {

    private final NpcStateStore stateStore;
    private final MarkerAssignment markerAssignment;

    /*
     * Erstellt den Removal-Service mit Zugriff auf State und Marker-Zuweisungen.
     */
    public NpcRemoval(
            NpcStateStore stateStore,
            MarkerAssignment markerAssignment
    ) {
        this.stateStore = Objects.requireNonNull(stateStore, "stateStore must not be null");
        this.markerAssignment = Objects.requireNonNull(markerAssignment, "markerAssignment must not be null");
    }

    /*
     * Entfernt später einen NPC über den einfachen Remove-Pfad.
     * Im Skeleton wird noch nichts entfernt.
     */
    public boolean removeNpc(String npcId) {
        requireNpcId(npcId);

        // TODO: Einfacher Remove-Pfad später sicher implementieren.
        return false;
    }

    /*
     * Entfernt später einen NPC mit genauem Ergebnis.
     * Im Skeleton ist der detaillierte Remove-Pfad noch nicht implementiert.
     */
    public Object removeNpcDetailed(String npcId) {
        requireNpcId(npcId);

        // TODO: Später detailliertes RemoveResult zurückgeben statt Object.
        throw new UnsupportedOperationException("NPC detailed removal is not implemented yet.");
    }

    /*
     * Prüft später, ob ein NPC sicher entfernt werden darf.
     * Im Skeleton ist Entfernen sicherheitshalber nicht erlaubt.
     */
    public boolean validateRemoval(String npcId) {
        requireNpcId(npcId);

        // TODO: Prüfen, ob Löschen sicher ist.
        return false;
    }

    /*
     * Prüft, ob eine npcId vorhanden ist.
     * Ohne gültige npcId darf kein Remove gestartet werden.
     */
    private static void requireNpcId(String npcId) {
        if (npcId == null || npcId.isBlank()) {
            throw new IllegalArgumentException("npcId must not be null or blank.");
        }
    }
}