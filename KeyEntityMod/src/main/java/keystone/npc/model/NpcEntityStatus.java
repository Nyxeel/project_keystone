package keystone.npc.model;

/*
 * NpcEntityStatus beschreibt den persistenten technischen Entity-Lifecycle eines NPCs.
 *
 * Diese Werte sagen NICHT, was der NPC fachlich tut.
 * Sie beweisen auch NICHT, dass aktuell eine gültige Live-Entity existiert.
 *
 * Die echte Runtime-Prüfung passiert später über RuntimeNpc.hasLiveEntity()
 * beziehungsweise über ein LiveEntityGate.
 *
 * Wichtig:
 * Ein persistierter Status allein darf niemals erlauben, dass Tick-, Routine-,
 * Navigation- oder Action-Logik ausgeführt wird.
 */
public enum NpcEntityStatus {

    /*
     * Der NPC-Record ist registriert.
     *
     * Es gibt noch keine bestätigte Live-Entity.
     * Nach einem Spawn oder Load muss Runtime separat geprüft werden.
     */
    REGISTERED,

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