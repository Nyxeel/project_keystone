package keystone.npc.state.internal;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

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

    /*
     * Lädt später alle bekannten Welten.
     * Im Skeleton macht diese Methode noch keinen echten World-Scan.
     */
    public StateLoadResult loadAllKnownWorlds() {
        try {
            pathResolver.prepareBaseDirectories();
            return StateLoadResult.success("World state store prepared. No automatic world scan implemented yet.");
        } catch (RuntimeException e) {
            return StateLoadResult.failed("Failed to prepare world state store: " + e.getMessage());
        }
    }

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

        try {
            stateFile = pathResolver.stateFile(checkedWorldKey);
        } catch (RuntimeException e) {
            return StateLoadResult.failed("Failed to resolve state.json path for world " + checkedWorldKey + ": " + e.getMessage());
        }

        if (!fileIO.exists(stateFile)) {
			String emptyJson = jsonCodec.emptyWorldStateJson();
            PersistedWorldState defaultState = jsonCodec.decodeWorldState(checkedWorldKey, emptyJson);

            if (defaultState == null) {
				return StateLoadResult.failed("Internal error: default world state JSON is invalid.");
			}

            loadedWorldState.put(checkedWorldKey, defaultState);
            return StateLoadResult.success("No state.json found. Empty world state prepared for world: " + checkedWorldKey);
        }

        String stateJson = fileIO.readString(stateFile);
        if (stateJson == null) {
            return StateLoadResult.failed("Failed to read state.json for world: " + checkedWorldKey);
        }

        PersistedWorldState decodedState = jsonCodec.decodeWorldState(checkedWorldKey, stateJson);
    	if (decodedState == null) {
			return StateLoadResult.failed("Internal error: default world state JSON is invalid.");
		}

        loadedWorldState.put(checkedWorldKey, decodedState);
        return StateLoadResult.success("Loaded state.json for world: " + checkedWorldKey);
    }

    /*
     * Speichert den State für genau eine Server-Spielwelt.
     */
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

        String rawJson = loadedWorldState.get(checkedWorldKey);
        String encodedJson = jsonCodec.encodeRaw(rawJson);

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


		/*
        loadedWorldState.put(checkedWorldKey, encodedJson);
        return StateSaveResult.success("Saved state.json for world: " + checkedWorldKey);
    	 */

	}

    /*
     * Speichert alle Welten, die aktuell im Speicher geladen sind.
     */
    public StateSaveResult saveAllLoadedWorlds() {
        for (String worldKey : new ArrayList<>(loadedWorldState.keySet())) {
            StateSaveResult result = saveWorld(worldKey);

            if (!result.success()) {
                return result;
            }
        }

        return StateSaveResult.success("Saved all loaded world states.");
    }

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

        String decodedJson = jsonCodec.decodeRaw(json);
        if (decodedJson == null) {
            return StateLoadResult.failed("Cannot put raw world JSON: json is invalid for world: " + checkedWorldKey);
        }

        loadedWorldState.put(checkedWorldKey, decodedJson);
        return StateLoadResult.success("Raw world JSON accepted for world: " + checkedWorldKey);
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
}