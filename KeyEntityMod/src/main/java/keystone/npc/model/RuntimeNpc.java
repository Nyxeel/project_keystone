package keystone.npc.model;

/*
 * RuntimeNpc enthält Live-Daten eines NPCs während der Server läuft.
 *
 * Diese Klasse darf NICHT in state.json gespeichert werden.
 *
 * Hier landen später:
 * - entityRef
 * - aktive Navigation
 * - aktive Action
 * - Door-Runtime
 *
 * Im Skeleton nutzen wir Object für entityRef,
 * damit wir noch keine Hytale-API erraten müssen.
 *
 * Wichtig:
 * RuntimeNpc ist nur die laufende Kopie.
 * Persistente Wahrheit bleibt immer NpcRecord.
 */
public final class RuntimeNpc {

    private final String npcId;

    private Object entityRef;
    private boolean liveEntityValid;

    /*
     * Erstellt Runtime-Daten für einen NPC.
     */
    public RuntimeNpc(String npcId) {
        this.npcId = requireText(npcId, "npcId");
    }

    /*
     * Gibt die NPC-ID zurück, zu der diese Runtime-Daten gehören.
     */
    public String npcId() {
        return npcId;
    }

    /*
     * Gibt die aktuelle Runtime-EntityRef zurück.
     *
     * Wichtig:
     * Dieser Wert darf nie persistiert werden.
     */
    public Object entityRef() {
        return entityRef;
    }

    /*
     * Setzt die Runtime-EntityRef nur, wenn sie wirklich als gültig markiert ist.
     *
     * Wenn keine gültige Live-Entity vorhanden ist, werden Runtime-Daten sicher geleert.
     * Dadurch bleibt keine alte oder ungültige EntityRef im Speicher hängen.
     */
    public void setEntityRef(Object entityRef, boolean liveEntityValid) {
        if (entityRef == null || !liveEntityValid) {
            clearRuntime();
            return;
        }

        this.entityRef = entityRef;
        this.liveEntityValid = true;
    }

    /*
     * Prüft, ob aktuell eine sichere Live-Entity vorhanden ist.
     */
    public boolean hasLiveEntity() {
        return entityRef != null && liveEntityValid;
    }

    /*
     * Löscht alle Runtime-Daten.
     *
     * Diese Methode wird wichtig bei:
     * - Restart
     * - EntityRef-Verlust
     * - Relink-Failure
     * - Remove
     */
    public void clearRuntime() {
        this.entityRef = null;
        this.liveEntityValid = false;

        // TODO: Später aktive Navigation clearen.
        // TODO: Später aktive Action clearen.
        // TODO: Später Door-Runtime clearen.
    }

    /*
     * Prüft, ob ein Pflicht-Text gültig ist.
     */
    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank.");
        }

        return value;
    }
}