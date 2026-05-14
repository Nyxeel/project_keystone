package keystone.npc.state;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

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
 */
public final class WorldStateStore {

    private final StatePathResolver pathResolver;
    private final StateFileIO fileIO;
    private final StateJsonCodec jsonCodec;
    private final StateBackupStore backupStore;

    private final Map<String, String> loadedWorldJson = new LinkedHashMap<>();

    /*
     * Erstellt den WorldStateStore mit seinen unteren Hilfsschichten.
     */
    public WorldStateStore(
            StatePathResolver pathResolver,
            StateFileIO fileIO,
            StateJsonCodec jsonCodec,
            StateBackupStore backupStore
    ) {
        this.pathResolver = pathResolver;
        this.fileIO = fileIO;
        this.jsonCodec = jsonCodec;
        this.backupStore = backupStore;
    }

    /*
     * Lädt später alle bekannten Welten.
     * Im Skeleton macht diese Methode noch keinen echten World-Scan.
     */
    public StateLoadResult loadAllKnownWorlds() {
        pathResolver.prepareBaseDirectories();
        return StateLoadResult.success("World state store prepared. No automatic world scan implemented yet.");
    }

    /*
     * Lädt den State für genau eine Server-Spielwelt.
     */
    public StateLoadResult loadWorld(String worldId) {
        Path stateFile = pathResolver.stateFile(worldId);

        if (!fileIO.exists(stateFile)) {
            String defaultJson = jsonCodec.emptyWorldStateJson();
            loadedWorldJson.put(worldId, defaultJson);
            return StateLoadResult.success("No state.json found. Empty world state prepared for world: " + worldId);
        }

        String json = fileIO.readString(stateFile);
        if (json == null) {
            return StateLoadResult.failed("Failed to read state.json for world: " + worldId);
        }

        if (!jsonCodec.isValidStateJson(json)) {
            return StateLoadResult.failed("Invalid state.json for world: " + worldId);
        }

        loadedWorldJson.put(worldId, json);
        return StateLoadResult.success("Loaded state.json for world: " + worldId);
    }

    /*
     * Speichert den State für genau eine Server-Spielwelt.
     */
    public StateSaveResult saveWorld(String worldId) {
        Path stateFile = pathResolver.stateFile(worldId);
        String json = loadedWorldJson.getOrDefault(worldId, jsonCodec.emptyWorldStateJson());

        StateSaveResult backupResult = backupStore.backupBeforeSave(worldId, stateFile);
        if (!backupResult.success()) {
            return backupResult;
        }

        boolean saved = fileIO.writeAtomic(stateFile, json);
        if (!saved) {
            return StateSaveResult.failed("Failed to save state.json for world: " + worldId);
        }

        return StateSaveResult.success("Saved state.json for world: " + worldId);
    }

    /*
     * Speichert alle Welten, die aktuell im Speicher geladen sind.
     */
    public StateSaveResult saveAllLoadedWorlds() {
        for (String worldId : loadedWorldJson.keySet()) {
            StateSaveResult result = saveWorld(worldId);

            if (!result.success()) {
                return result;
            }
        }

        return StateSaveResult.success("Saved all loaded world states.");
    }

    /*
     * Merkt rohen JSON-State für eine Welt.
     * Später wird das durch echte PersistedWorldState-Objekte ersetzt.
     */
    public void putRawWorldJson(String worldId, String json) {
        loadedWorldJson.put(worldId, json);
    }
}