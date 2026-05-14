package keystone.npc.state;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import keystone.npc.KeystoneNPCPlugin;

/*
 * StatePathResolver ist nur für Pfade und Ordner zuständig.
 *
 * Aufgabe:
 * - Basisordner vorbereiten
 * - Weltordner vorbereiten
 * - Pfad zu state.json pro Welt erzeugen
 * - worldId sicher für Dateipfade machen
 */
public final class StatePathResolver {

    private final KeystoneNPCPlugin plugin;

    private final Path baseDir;
    private final Path worldsDir;
    private final Path backupsDir;

    /*
     * Erstellt den Resolver.
     * Der Plugin-Parameter bleibt erhalten, falls später ein echter Hytale-Datenpfad genutzt wird.
     */
    public StatePathResolver(KeystoneNPCPlugin plugin) {
        this.plugin = plugin;

        this.baseDir = Path.of("keystone-npc");
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

        String safeWorldId = sanitizeWorldId(worldId);
        Path worldDir = worldsDir.resolve(safeWorldId);

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
            throw new IllegalArgumentException("worldId must not be empty.");
        }

        return worldId
                .replace("\\", "_")
                .replace("/", "_")
                .replace("..", "_")
                .replace(":", "_")
                .trim();
    }

    /*
     * Gibt das Plugin zurück, falls später Hytale-Pfade darüber ermittelt werden.
     */
    public KeystoneNPCPlugin plugin() {
        return plugin;
    }
}