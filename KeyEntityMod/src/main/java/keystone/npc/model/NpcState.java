package keystone.npc.model;

/*
 * NpcState beschreibt den groben fachlichen Zustand eines NPCs.
 *
 * Diese Werte sagen, was der NPC inhaltlich gerade macht,
 * zum Beispiel warten, arbeiten oder schlafen.
 *
 * NpcState beweist NICHT, dass eine gültige Live-Entity existiert.
 * Die technische Entity-Verfügbarkeit wird getrennt über NpcEntityStatus,
 * RuntimeNpc.hasLiveEntity() und später über ein LiveEntityGate geprüft.
 *
 * Wichtig:
 * Ein NpcState allein darf niemals Tick-, Routine-, Navigation-
 * oder Action-Logik erlauben.
 */
public enum NpcState {

    /*
     * Der NPC tut gerade nichts Besonderes.
     */
    IDLE,

    /*
     * Der NPC arbeitet an einem Arbeitsmarker.
     */
    WORKING,

    /*
     * Der NPC schläft an einem Schlafmarker.
     */
    SLEEPING,

    /*
     * Der NPC isst oder nutzt einen Essensmarker.
     */
    EATING,

    /*
     * Der NPC bewegt sich zu einem Ziel.
     */
    MOVING,

    /*
     * Der NPC ist pausiert, weil ein Marker fehlt.
     */
    PAUSED_MISSING_MARKER,

    /*
     * Der NPC ist pausiert, weil seine Struktur beschädigt oder blockiert ist.
     */
    PAUSED_STRUCTURE_BLOCKED
}