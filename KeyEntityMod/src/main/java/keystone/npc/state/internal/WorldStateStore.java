package keystone.npc.state.internal;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import keystone.npc.model.PersistedWorldState;
import keystone.npc.state.StateLoadResult;
import keystone.npc.state.StateSaveResult;

/*
 * WorldStateStore verwaltet den NPC-State pro Server-Spielwelt.
 *
 * Diese Klasse entscheidet nicht, WO Dateien liegen und nicht, WIE JSON aufgebaut ist.
 * Dafür gibt es StatePathResolver, StateFileIO und StateJsonCodec.
 *
 * Aufgabe:
 * - Welt-State laden
 * - Welt-State speichern
 * - mehrere geladene Welten merken
 * - Save-Ergebnisse ehrlich zurückgeben
 *
 * Wichtig:
 * Diese Klasse darf niemals unbekannte oder kaputte Welt-Daten als leeren Default-State speichern.
 * Erst laden/prüfen, dann merken, dann speichern.
 */
public final class WorldStateStore {

    private final StatePathResolver pathResolver;
    private final StateFileIO fileIO;
    private final StateJsonCodec jsonCodec;
    private final StateBackupStore backupStore;

    private final Map<String, PersistedWorldState> loadedWorldState = new LinkedHashMap<>();

    /*
     * Erstellt den WorldStateStore mit seinen unteren Hilfsschichten.
     */
    public WorldStateStore(
            StatePathResolver pathResolver,
            StateFileIO fileIO,
            StateJsonCodec jsonCodec,
            StateBackupStore backupStore
    ) {
        this.pathResolver = Objects.requireNonNull(pathResolver, "pathResolver must not be null");
        this.fileIO = Objects.requireNonNull(fileIO, "fileIO must not be null");
        this.jsonCodec = Objects.requireNonNull(jsonCodec, "jsonCodec must not be null");
        this.backupStore = Objects.requireNonNull(backupStore, "backupStore must not be null");
    }

	// TODO Phase später / DataStore<T>-Migration:
	// Beim Laden eines PersistedWorldState in loadWorld prüfen,
	// ob der gespeicherte interne worldKey/worldUuid
	// zum erwarteten Storage-Key passt.
	//
	// Grund:
	// Wenn eine state.json oder ein DataStore-Eintrag versehentlich kopiert, verschoben oder falsch
	// zugeordnet wird, darf der Inhalt nicht still als Zustand einer anderen Welt akzeptiert werden.
	//
	// Regel:
	// - Storage-Key / DataStore-Key bleibt die primäre Adresse.
	// - Wenn PersistedWorldState später ein worldKey/worldUuid-Feld enthält:
	//   expectedWorldKey == persistedWorldState.worldKey()
	// - Bei mismatch:
	//   Load failed
	//   kein Save
	//   kein Auto-Repair
	//   kein Überschreiben mit leerem Default-State

    /*
     * Lädt den State für genau eine Server-Spielwelt.
     */
    public StateLoadResult loadWorld(String worldKey) {
        String checkedWorldKey;

        try {
            checkedWorldKey = requireWorldKey(worldKey);
        } catch (IllegalArgumentException e) {
            return StateLoadResult.failed("Cannot load world state: " + e.getMessage());
        }

        Path stateFile;

		// Baut Pfad key-entity-mod/<worldKey>/state.json
        try {
            stateFile = pathResolver.stateFile(checkedWorldKey);
        } catch (RuntimeException e) {
            return StateLoadResult.failed("Failed to resolve state.json path for world " + checkedWorldKey + ": " + e.getMessage());
        }



		// Wenn keine Server noch keine state.json hat (weil erster Start)
        if (!fileIO.exists(stateFile)) {
			String emptyJson = jsonCodec.emptyWorldStateJson();


			// Hier wird defualt json file eingelesen (derzeit nur worldkey weil skelett)
			PersistedWorldState defaultState = jsonCodec.JsonToWorldState(checkedWorldKey, emptyJson); //check ob json format stimmt
            if (defaultState == null) {
				return StateLoadResult.failed("Internal error: default world state JSON is invalid.");
			}

            loadedWorldState.put(checkedWorldKey, defaultState);
            return StateLoadResult.success(
			"No state.json found. Empty world state prepared for world: " + checkedWorldKey,
			defaultState);
        }

		boolean loadedFromBackup = false;

        String stateJson = fileIO.readString(stateFile);
        if (stateJson == null) {

			stateJson = backupStore.loadBackup(checkedWorldKey);
			loadedFromBackup = true;
			if (stateJson == null)
				return StateLoadResult.failed("Failed to read state.json for world: " + checkedWorldKey);
        }

		// Hier werden die Eintraege aus state.json in NPC Record eingelesen (derzeit nur worldkey weil skelett)
        PersistedWorldState decodedState = jsonCodec.JsonToWorldState(checkedWorldKey, stateJson);
    	if (decodedState == null && !loadedFromBackup) {

			stateJson = backupStore.loadBackup(checkedWorldKey);
			loadedFromBackup = true;
			if (stateJson == null)
				return StateLoadResult.failed("Invalid state.json and no backup found for world: " + checkedWorldKey);

			decodedState = jsonCodec.JsonToWorldState(checkedWorldKey, stateJson);
		}
		if (decodedState == null)
				return StateLoadResult.failed("Invalid state.json and no backup found for world: " + checkedWorldKey);

        loadedWorldState.put(checkedWorldKey, decodedState);
        return StateLoadResult.success(
			loadedFromBackup
				? "Loaded Backup state.json for world: " + checkedWorldKey
				: "Loaded state.json for world: " + checkedWorldKey,
				decodedState);
    }





	////////////////////////////////////////////////////////////////////////
	/////////////////////////  S    A    V    E  ///////////////////////////
	////////////////////////////////////////////////////////////////////////



	public boolean saveLoadedWorlds() {

		if (getWorldKeys().isEmpty()) {
			System.err.println("[WORLD_SAVE_FAILED] dirty save requested, but no loaded world states exist.");
		return false;
	}
		Map<String, String> failedSaveWorlds = new LinkedHashMap<>();

		for (String worldKey : getWorldKeys()){

			StateSaveResult result = saveWorld(worldKey);
        	if (!result.success()) {
				failedSaveWorlds.put(worldKey, result.message());
			}
		}
		if (!failedSaveWorlds.isEmpty())
		{
			System.err.println("[KeystoneNPC] ");
			for (Map.Entry<String, String> entry : failedSaveWorlds.entrySet())
			{
				System.err.println("[WORLD_SAVE_FAILED] "
						+ entry.getKey() + ": " + entry.getValue());
			}
			return false;
		}
		return true;
	}

    /*
     * Speichert den State für genau eine Server-Spielwelt.
     */
	// saveWorld nur fuer shutdown() , zwischen saves nur bei saveStatSafeley()
    public StateSaveResult saveWorld(String worldKey) {

		String checkedWorldKey;
        try {
            checkedWorldKey = requireWorldKey(worldKey);
        } catch (IllegalArgumentException e) {
            return StateSaveResult.failed("Cannot save world state: " + e.getMessage());
        }

        if (!loadedWorldState.containsKey(checkedWorldKey)) {
            return StateSaveResult.failed("Cannot save world state: world not loaded: " + checkedWorldKey);
        }

        PersistedWorldState worldState = loadedWorldState.get(checkedWorldKey);
		if (worldState == null) {
			return StateSaveResult.failed("World state is not loaded.");
		}

        String encodedJson = jsonCodec.WorldStateToJson(worldState);
        if (encodedJson == null) {
            return StateSaveResult.failed("Cannot save world state: loaded JSON is invalid for world: " + checkedWorldKey);
        }

        Path stateFile;
        try {
            stateFile = pathResolver.stateFile(checkedWorldKey);
        } catch (RuntimeException e) {
            return StateSaveResult.failed("Failed to resolve state.json path for world " + checkedWorldKey + ": " + e.getMessage());
        }

        StateSaveResult backupResult;
        try {
            backupResult = backupStore.backupBeforeSave(checkedWorldKey, stateFile);
        } catch (RuntimeException e) {
            return StateSaveResult.failed("Failed to create backup for world " + checkedWorldKey + ": " + e.getMessage());
        }

        if (!backupResult.success()) {
            return backupResult;
        }

        boolean saved = fileIO.writeAtomic(stateFile, encodedJson);
        if (!saved) {
            return StateSaveResult.failed("Failed to save state.json for world: " + checkedWorldKey);
        }
        return StateSaveResult.success("Saved state.json for world: " + checkedWorldKey);
    }

    /*
     * Speichert alle Welten, die aktuell im Speicher geladen sind.
     */



    /*
     * Merkt rohen JSON-State für eine Welt.
     * Gibt ehrlich zurück, ob der JSON-State angenommen wurde.
     */
    public StateLoadResult putRawWorldJson(String worldKey, String json) {
        String checkedWorldKey;

        try {
            checkedWorldKey = requireWorldKey(worldKey);
        } catch (IllegalArgumentException e) {
            return StateLoadResult.failed("Cannot put raw world JSON: " + e.getMessage());
        }

        PersistedWorldState decodedState = jsonCodec.JsonToWorldState(checkedWorldKey, json);
    	if (decodedState == null) {
			return StateLoadResult.failed("Cannot put raw world JSON: json is invalid for world: " + checkedWorldKey);
		}

        loadedWorldState.put(checkedWorldKey, decodedState);
        return StateLoadResult.success("Raw world JSON accepted for world: " + checkedWorldKey,
																decodedState);
    }

    /*
     * Prüft, ob ein worldKey vorhanden ist.
     */
    private static String requireWorldKey(String worldKey) {
        if (worldKey == null || worldKey.isBlank()) {
            throw new IllegalArgumentException("worldKey must not be null or blank.");
        }

        return worldKey.trim();
    }



	public Set<String> getWorldKeys() {
		return loadedWorldState.keySet();
	}
}