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
    private final Path worldsDir;
    private final Path backupsDir;

    /*
     * Erstellt den Resolver.
     * Der Plugin-Parameter bleibt erhalten, falls später ein echter Hytale-Datenpfad genutzt wird.
     */
    public StatePathResolver(KeystoneNpcPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null");



		/*
 		* TODO Hytale-API:
 		* Dieser Pfad ist aktuell nur ein Skeleton-/Entwicklungspfad.
 		*
 		* Später darf die Mod den Speicherordner nicht selbst raten.
 		* Stattdessen soll der offizielle Plugin-Datenordner von Hytale benutzt werden,
 		* z. B. über plugin.getDataDirectory().
 		*
 		* Grund:
 		* Hytale soll entscheiden, wo Mod-Daten sicher gespeichert werden.
 		* Dadurch landen state.json und Backups später am richtigen Server-Ort.
 		*/

        this.baseDir = Path.of("key-entity-mod");
        this.worldsDir = baseDir;
        this.backupsDir = baseDir.resolve("backups");
    }

    /*
     * Erstellt die globalen Basisordner der Mod.
     */
    public void prepareBaseDirectories() {
        try {
            Files.createDirectories(baseDir);
            Files.createDirectories(worldsDir);
            Files.createDirectories(backupsDir);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to prepare KeystoneNPC state directories.", e);
        }
    }


    /*
	 * Baut den Ordner für genau eine Welt.
	 *
	 * Zielstruktur:
	 * key-entity-mod/<worldKey>/state.json
	 */
	public Path worldDirectory(String worldKey) {
		if (worldKey == null || worldKey.isBlank()) {
			throw new IllegalArgumentException("worldKey must not be null or blank.");
		}

		String safeWorldKey = worldKey.trim();

		Path basePath = baseDir.toAbsolutePath().normalize();
		Path worldDirectory = basePath.resolve(safeWorldKey).normalize();

		if (!worldDirectory.startsWith(basePath)) {
			throw new IllegalArgumentException("worldKey escapes state directory: " + worldKey);
		}

		if (worldDirectory.equals(backupsDir.toAbsolutePath().normalize())) {
			throw new IllegalArgumentException("worldKey is reserved: " + worldKey);
		}

		return worldDirectory;
	}

    /*
     * Gibt den Pfad zur state.json einer konkreten Server-Spielwelt zurück.
     */
    public Path stateFile(String worldKey) {
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