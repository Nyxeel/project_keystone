package keystone.npc.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/*
 * PersistedWorldState beschreibt den kompletten gespeicherten NPC-State einer Server-Spielwelt.
 *
 * Diese Klasse ist später der Inhalt von:
 * keystone-npc/worlds/<worldKey>/state.json
 *
 * Sie enthält persistente NpcRecord-Objekte.
 * Sie enthält keine RuntimeNpc-Objekte.
 *
 * Wichtig:
 * - keine EntityRef speichern
 * - keine Runtime-Entity speichern
 * - keine aktive Navigation speichern
 * - NPCs aus einer falschen Welt werden abgelehnt
 * - doppelte npcIds werden nicht still überschrieben
 */
public final class PersistedWorldState {

    private final String worldKey;
    private final Map<String, NpcRecord> npcRecordsById = new LinkedHashMap<>();

    /*
     * Erstellt einen neuen Welt-State für eine konkrete Server-Spielwelt.
     */
    public PersistedWorldState(String worldKey) {
        this.worldKey = requireText(worldKey, "worldKey");
    }

    /*
     * Gibt den Welt-Key zurück.
     */
    public String worldKey() {
        return worldKey;
    }

    /*
     * Fügt einen NPC-Record hinzu.
     * Doppelte npcIds werden blockiert, damit nichts still überschrieben wird.
     */
    public void putNpcRecord(NpcRecord record) {
        String npcId = requireValidRecord(record);

        if (npcRecordsById.containsKey(npcId)) {
            throw new IllegalStateException("NPC record already exists in world state: " + npcId);
        }

        npcRecordsById.put(npcId, record);
    }

    /*
     * Sucht einen NPC-Record per npcId.
     */
    public Optional<NpcRecord> findNpcRecord(String npcId) {
        if (npcId == null || npcId.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(npcRecordsById.get(npcId));
    }

    /*
     * Entfernt einen NPC-Record per npcId.
     */
    public boolean removeNpcRecord(String npcId) {
        if (npcId == null || npcId.isBlank()) {
            return false;
        }

        return npcRecordsById.remove(npcId) != null;
    }

    /*
     * Gibt alle NPC-Records dieser Welt als sichere Kopie zurück.
     * Außenstehender Code kann dadurch die interne Map nicht direkt verändern.
     */
    public Collection<NpcRecord> npcRecords() {
        return Collections.unmodifiableCollection(new ArrayList<>(npcRecordsById.values()));
    }

    /*
     * Prüft, ob diese Welt keine NPC-Records enthält.
     */
    public boolean isEmpty() {
        return npcRecordsById.isEmpty();
    }

    /*
     * Prüft, ob ein NpcRecord gültig ist und zu dieser Welt gehört.
     */
    private String requireValidRecord(NpcRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("record must not be null.");
        }

        String npcId = requireText(record.npcId(), "record.npcId");
        String recordWorldKey = requireText(record.worldKey(), "record.worldKey");

        if (!worldKey.equals(recordWorldKey)) {
            throw new IllegalArgumentException(
                    "NPC record belongs to wrong world. expected=" + worldKey + ", actual=" + recordWorldKey
            );
        }

        return npcId;
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