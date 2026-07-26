package keystone.npc.state.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import keystone.npc.KeystoneNpcPlugin;
/*
 * StatePathResolver ist nur für Pfade und Ordner zuständig.
 *
 * Aufgabe:
 * - Basisordner vorbereiten
 * - Weltordner vorbereiten
 * - Pfad zu state.json pro Welt erzeugen
 * - worldKey sicher für Dateipfade machen
 *
 * Path: key-entit-mod/<worldKey>/state.json
 * Wichtig:
 * Es werden innerhalb einer world verschiedene Dimensionen ins spiel kommen,
 * die später die state.json nach dimension aufteilen muss!! oder zumindest
 * "worldKey": "world_" + dimension string anhaengen! oder in die json den dimension
 * eintrage extra anlegen: "dimension": "overworld"
 * Wichtig:
 * Diese Klasse kennt keine NPC-Logik.
 * Sie entscheidet nur, wo Dateien liegen.
 */
public final class StatePathResolver {

    private final KeystoneNpcPlugin plugin;

    private final Path baseDir;
    private final Path worldDir;
    private final Path backupsDir;

    /*
     * Erstellt den Resolver.
     * Der Plugin-Parameter bleibt erhalten, falls später ein echter Hytale-Datenpfad genutzt wird.
     */
    public StatePathResolver(KeystoneNpcPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null");
      	this.baseDir = plugin.getDataDirectory();
       	this.worldDir = baseDir.resolve("worlds");
    	this.backupsDir = baseDir.resolve("backups");
		// getDataDirectory()
		// -> gibt deiner Mod ihren eigenen Server-Datenordner.
		// -> gut für eigene Dateien wie worlds/<worldUuid>/state.json.
			//
		// withConfig(...)
		// -> Hytale-Config-System für Mod-Einstellungen.
		// -> eher für config.json, nicht für NPC-State.
			//
		// DataStore<T>
		// -> Hytale-System für persistente Daten mit load/save/list/loadAll.
		// -> gut, wenn du saubere Codec-basierte Speicherung willst.
			//
		// DiskDataStoreProvider("keystonenpc/worlds")
		// -> speichert DataStore-Dateien auf Disk im Universe-/Serverbereich.
			//
		// Universe#getWorld(UUID)
		// -> holt die World, zu der dein gespeicherter worldUuid gehört.
    }

    /*
     * Erstellt die globalen Basisordner der Mod.
     */
    public void prepareBaseDirectories() {

        try {
            Files.createDirectories(baseDir);
            Files.createDirectories(worldDir);
            Files.createDirectories(backupsDir);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to prepare KeystoneNPC state directories.", e);
        }
    }


    /*
	 * Baut den Ordner für genau eine Welt.
	 *
	 * Zielstruktur:
	 * key-entity-mod/worldname/<worlduuid>/state.json
	 */
	public Path worldDirectory(String worldKey) {
		if (worldKey == null || worldKey.isBlank()) {
			throw new IllegalArgumentException("worldKey must not be null or blank.");
		}

		String safeWorldKey = sanitizeWorldKey(worldKey);

		Path worldRoot = worldDir.toAbsolutePath().normalize();
		Path worldDirectory = worldRoot.resolve(safeWorldKey).normalize();

		if (!worldDirectory.startsWith(worldRoot)) {
			throw new IllegalArgumentException("worldKey escapes worlds directory: " + worldKey);
		}

		return worldDirectory;
	}

    /*
     * Gibt den Pfad zur state.json einer konkreten Server-Spielwelt zurück.
     */
    public Path	stateFile(String worldKey) {
        return worldDirectory(worldKey).resolve("state.json");
    }

    /*
     * Gibt den globalen Backup-Ordner zurück.
     */
    public Path backupsDir() {
        prepareBaseDirectories();
        return backupsDir;
    }

    /*
     * Macht einen worldKey sicher für Dateipfade.
     */
    public String sanitizeWorldKey(String worldKey) {
        if (worldKey == null || worldKey.isBlank()) {
            throw new IllegalArgumentException("worldKey must not be null or blank.");
        }

        String safeWorldKey = worldKey.trim()
                .replace('\\', '_')
                .replace('/', '_')
                .replace(':', '_')
                .replaceAll("[^A-Za-z0-9._-]", "_")
                .replaceAll("\\.{2,}", "_")
                .replaceAll("^\\.+", "_")
                .replaceAll("_+", "_");

        if (safeWorldKey.isBlank() || ".".equals(safeWorldKey) || "..".equals(safeWorldKey)) {
            throw new IllegalArgumentException("worldKey cannot be converted to a safe path name.");
        }

        return safeWorldKey;
    }

    /*
     * Gibt das Plugin zurück, falls später Hytale-Pfade darüber ermittelt werden.
     */
    public KeystoneNpcPlugin plugin() {
        return plugin;
    }
}