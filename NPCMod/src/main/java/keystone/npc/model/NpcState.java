package keystone.npc.model;

/*
 * NpcState beschreibt den fachlichen Zustand eines NPCs.
 *
 * Das ist etwas anderes als NpcEntityStatus.
 *
 * Beispiel:
 * EntityStatus ACTIVE sagt: Die Live-Entity ist gültig.
 * NpcState WORKING sagt: Der NPC arbeitet gerade.
 *
 * HIER MUSS NOCH ERWEITERT WERDEN ABER NUR GROB
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