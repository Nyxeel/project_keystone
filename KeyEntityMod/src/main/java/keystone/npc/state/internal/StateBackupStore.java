package keystone.npc.state.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import keystone.npc.state.StateSaveResult;

/*
 * StateBackupStore erstellt Sicherheitskopien von state.json.
 *
 * Aufgabe:
 * - vor riskanten Saves oder Migrationen Backups anlegen
 * - Backup-Fehler sichtbar machen
 *
 * Wichtig:
 * Diese Klasse darf Backup-Fehler nicht still schlucken.
 * Wenn ein Backup nötig ist und fehlschlägt, muss der Save blockiert werden.
 */
public final class StateBackupStore {

    private static final DateTimeFormatter BACKUP_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private final StatePathResolver pathResolver;

    /*
     * Erstellt den BackupStore mit Zugriff auf die State-Pfade.
     */
    public StateBackupStore(StatePathResolver pathResolver) {
        this.pathResolver = Objects.requireNonNull(pathResolver, "pathResolver must not be null");
    }

    /*
     * Legt vor dem Speichern ein Backup an, falls schon eine state.json existiert.
     */
    public StateSaveResult backupBeforeSave(String worldKey, Path stateFile) {
        if (worldKey == null || worldKey.isBlank()) {
            return StateSaveResult.failed("Cannot create backup: worldKey is null or blank.");
        }

        if (stateFile == null) {
            return StateSaveResult.failed("Cannot create backup: stateFile is null.");
        }

        if (!Files.exists(stateFile)) {
            return StateSaveResult.success("No existing state.json. Backup skipped.");
        }

        if (!Files.isRegularFile(stateFile)) {
            return StateSaveResult.failed("Cannot create backup: stateFile is not a regular file: " + stateFile);
        }

		try {
		    String safeWorldKey = pathResolver.sanitizeWorldKey(worldKey);
		    String timestamp = LocalDateTime.now().format(BACKUP_TIMESTAMP_FORMAT);

		    Path worldBackupDir = pathResolver.backupsDir().resolve(safeWorldKey);
		    Path backupFile = uniqueBackupFile(worldBackupDir, timestamp);

		    Files.createDirectories(worldBackupDir);
		    Files.copy(stateFile, backupFile, StandardCopyOption.COPY_ATTRIBUTES);

		    return StateSaveResult.success("Backup created: " + backupFile);
		}
		catch (IOException | RuntimeException e) {
		    return StateSaveResult.failed("Failed to create backup for world " + worldKey + ": " + e.getMessage());
		}
	}

    /*
     * Erzeugt einen Backup-Dateinamen, der vorhandene Backups nicht überschreibt.
     */
    private static Path uniqueBackupFile(Path worldBackupDir, String timestamp) {
        Path backupFile = worldBackupDir.resolve("state_" + timestamp + ".json.bak");

        if (!Files.exists(backupFile)) {
            return backupFile;
        }

        for (int index = 1; index <= 999; index++) {
            Path indexedBackupFile = worldBackupDir.resolve(
                    "state_" + timestamp + "_" + index + ".json.bak"
            );

            if (!Files.exists(indexedBackupFile)) {
                return indexedBackupFile;
            }
        }

        throw new IllegalStateException("Cannot create unique backup filename for timestamp: " + timestamp);
    }
}