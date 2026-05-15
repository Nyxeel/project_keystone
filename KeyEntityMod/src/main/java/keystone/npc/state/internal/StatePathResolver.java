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
 * - worldId sicher für Dateipfade machen
 *
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

        this.baseDir = Path.of("key-entity-mod");
        this.worldsDir = baseDir.resolve("worlds");
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
     * Gibt den Ordner für eine konkrete Server-Spielwelt zurück.
     */
    public Path worldDirectory(String worldId) {
        prepareBaseDirectories();

		if (worldId == null || worldId.isBlank()) {
			throw new IllegalArgumentException("worldId must not be null or blank.");
		}


        String safeWorldId = sanitizeWorldId(worldId);
        Path worldDir = worldsDir.resolve(safeWorldId).normalize();

        if (!worldDir.startsWith(worldsDir.normalize())) {
            throw new IllegalArgumentException("Resolved world directory escaped worldsDir: " + worldId);
        }

        try {
            Files.createDirectories(worldDir);
            return worldDir;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to prepare world directory: " + safeWorldId, e);
        }
    }

    /*
     * Gibt den Pfad zur state.json einer konkreten Server-Spielwelt zurück.
     */
    public Path stateFile(String worldId) {
        return worldDirectory(worldId).resolve("state.json");
    }

    /*
     * Gibt den globalen Backup-Ordner zurück.
     */
    public Path backupsDir() {
        prepareBaseDirectories();
        return backupsDir;
    }

    /*
     * Macht eine worldId sicher für Dateipfade.
     */
    public String sanitizeWorldId(String worldId) {
        if (worldId == null || worldId.isBlank()) {
            throw new IllegalArgumentException("worldId must not be null or blank.");
        }

        String safeWorldId = worldId.trim()
                .replace('\\', '_')
                .replace('/', '_')
                .replace(':', '_')
                .replaceAll("[^A-Za-z0-9._-]", "_")
                .replaceAll("\\.{2,}", "_")
                .replaceAll("^\\.+", "_")
                .replaceAll("_+", "_");

        if (safeWorldId.isBlank() || ".".equals(safeWorldId) || "..".equals(safeWorldId)) {
            throw new IllegalArgumentException("worldId cannot be converted to a safe path name.");
        }

        return safeWorldId;
    }

    /*
     * Gibt das Plugin zurück, falls später Hytale-Pfade darüber ermittelt werden.
     */
    public KeystoneNpcPlugin plugin() {
        return plugin;
    }
}