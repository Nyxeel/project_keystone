package keystone.npc.core;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import keystone.npc.model.NpcRecord;
import keystone.npc.model.RuntimeNpc;

/*
 * NpcManager ist die zentrale RAM-Verwaltung für alle NPCs.
 *
 * Diese Klasse speichert NICHT direkt in state.json.
 * Sie hält nur während der Server läuft:
 * - persistente NpcRecords
 * - passende RuntimeNpc-Objekte
 *
 * Wichtig:
 * NpcRecord darf später gespeichert werden.
 * RuntimeNpc darf niemals gespeichert werden.
 *
 * Andere Systeme wie Spawn, Relink, Tick und Remove arbeiten später über diesen Manager.
 */
public final class NpcManager {

    private final Map<String, NpcRecord> recordsById = new LinkedHashMap<>();
    private final Map<String, RuntimeNpc> runtimeById = new LinkedHashMap<>();

    /*
     * Fügt einen persistenten NPC-Record hinzu.
     * Gleichzeitig wird ein leerer RuntimeNpc-Eintrag vorbereitet.
     */
    public void addRecord(NpcRecord record)
	{
    String npcId = requireValidRecord(record);

    if (recordsById.containsKey(npcId)) {
        throw new IllegalStateException("NPC record already exists: " + npcId);
    }

    recordsById.put(npcId, record);
    runtimeById.putIfAbsent(npcId, new RuntimeNpc(npcId));
	}

    /*
     * Sucht einen persistenten NPC-Record per npcId.
     */
    public Optional<NpcRecord> findRecord(String npcId) {
        if (npcId == null || npcId.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(recordsById.get(npcId));
    }

    /*
     * Sucht Runtime-Daten per npcId.
     */
    public Optional<RuntimeNpc> findRuntime(String npcId) {
        if (npcId == null || npcId.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(runtimeById.get(npcId));
    }

	/*
	 * Holt Runtime-Daten oder erstellt sie nur für einen existierenden Record.
	 */
	public RuntimeNpc runtimeOrCreate(String npcId) {

		requireText(npcId, "npcId");

	    if (!recordsById.containsKey(npcId)) {
	        throw new IllegalStateException("Cannot create runtime without persistent record: " + npcId);
	    }

	    return runtimeById.computeIfAbsent(npcId, RuntimeNpc::new);
	}

    /*
     * Gibt alle persistenten NPC-Records zurück.
     * Diese Daten dürfen später gespeichert werden.
     */
    public Collection<NpcRecord> allRecords() {
        return Collections.unmodifiableCollection(recordsById.values());
    }

    /*
     * Gibt alle RuntimeNpc-Objekte zurück.
     * Diese Daten dürfen niemals gespeichert werden.
     */
    public Collection<RuntimeNpc> allRuntimeNpcs() {
        return Collections.unmodifiableCollection(runtimeById.values());
    }

    /*
     * Entfernt einen NPC vollständig aus der RAM-Verwaltung.
     * Das löscht hier nur aus dem Manager, nicht automatisch aus state.json.
     */
    public boolean removeNpc(String npcId) {
        if (npcId == null || npcId.isBlank()) {
            return false;
        }

        RuntimeNpc runtime = runtimeById.remove(npcId);
        if (runtime != null) {
            runtime.clearRuntime();
        }

        return recordsById.remove(npcId) != null;
    }

    /*
     * Entfernt nur Runtime-Daten, aber behält den persistenten Record.
     * Das ist wichtig bei Restart, Relink-Failure oder EntityRef-Verlust.
     */
    public void clearRuntime(String npcId) {
        if (npcId == null || npcId.isBlank()) {
            return;
        }

        RuntimeNpc runtime = runtimeById.get(npcId);
        if (runtime != null) {
            runtime.clearRuntime();
        }
    }

    /*
     * Entfernt alle Runtime-Daten aller NPCs.
     * Die persistenten Records bleiben erhalten.
     */
    public void clearAllRuntime() {
        for (RuntimeNpc runtime : runtimeById.values()) {
            runtime.clearRuntime();
        }
    }


	/*
	 * Lädt mehrere NPC-Records in den Manager.
	 * Erst wird alles geprüft, dann wird der alte RAM-Zustand ersetzt.
	 */

	public void restoreRecords(Collection<NpcRecord> records) {
	    if (records == null) {
	        throw new IllegalArgumentException("records must not be null.");
	    }

	    Map<String, NpcRecord> newRecordsById = new LinkedHashMap<>();
	    Map<String, RuntimeNpc> newRuntimeById = new LinkedHashMap<>();

	    for (NpcRecord record : records) {
	        String npcId = requireValidRecord(record);

	        if (newRecordsById.containsKey(npcId)) {
	            throw new IllegalStateException("Duplicate NPC record in restore data: " + npcId);
	        }

	        newRecordsById.put(npcId, record);
	        newRuntimeById.put(npcId, new RuntimeNpc(npcId));
	    }

	    recordsById.clear();
	    runtimeById.clear();

	    recordsById.putAll(newRecordsById);
	    runtimeById.putAll(newRuntimeById);
	}

	/*
     * Prüft, ob ein NPC-Record existiert.
     */
    public boolean hasRecord(String npcId) {
        return findRecord(npcId).isPresent();
    }

    /*
     * Gibt zurück, wie viele persistente NPC-Records geladen sind.
     */
    public int recordCount() {
        return recordsById.size();
    }

    /*
     * Gibt zurück, ob der Manager keine NPC-Records enthält.
     */
    public boolean isEmpty() {
        return recordsById.isEmpty();
    }


	/*
	 * Prüft, ob ein NpcRecord vorhanden ist und eine gültige npcId besitzt.
	 */
	private static String requireValidRecord(NpcRecord record) {
	    if (record == null) {
	        throw new IllegalArgumentException("record must not be null.");
	    }

	    String npcId = record.npcId();
	    requireText(npcId, "record.npcId");

	    return npcId;
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