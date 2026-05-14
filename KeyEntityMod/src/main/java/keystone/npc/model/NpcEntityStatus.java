package keystone.npc.model;

/*
 * NpcEntityStatus beschreibt den technischen Live-Entity-Zustand eines NPCs.
 *
 * Diese Werte sagen NICHT, was der NPC gerade fachlich tut.
 * Sie sagen nur, ob die Hytale-Entity gerade sicher verwendbar ist.
 *
 * Beispiel:
 * ACTIVE = EntityRef ist gültig.
 * NEEDS_RELINK = Entity muss nach Restart wiedergefunden werden.
 * MISSING_ENTITY = Entity fehlt wahrscheinlich.
 */
public enum NpcEntityStatus {

    /*
     * Der NPC ist live und darf normale Logik ausführen.
     */
    ACTIVE,

    /*
     * Der NPC-Record existiert, aber die Live-Entity muss erst wieder verbunden werden.
     */
    NEEDS_RELINK,

    /*
     * Die Entity konnte nicht gefunden werden oder gilt als fehlend.
     */
    MISSING_ENTITY,

    /*
     * Der NPC ist bewusst deaktiviert und darf keine normale Logik ausführen.
     */
    DISABLED
}