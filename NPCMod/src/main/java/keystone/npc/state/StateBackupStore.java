package keystone.npc.state;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * StateBackupStore erstellt Sicherheitskopien von state.json.
 *
 * Aufgabe:
 * - vor riskanten Saves oder Migrationen Backups anlegen
 * - Backup-Fehler sichtbar machen
 *
 * Im Skeleton wird vor jedem Save ein Backup angelegt, falls die Datei existiert.
 */
public final class StateBackupStore {

    private final StatePathResolver pathResolver;

    /*
     * Erstellt den BackupStore mit Zugriff auf die State-Pfade.
     */
    public StateBackupStore(StatePathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    /*
     * Legt vor dem Speichern ein Backup an, falls schon eine state.json existiert.
     */
    public StateSaveResult backupBeforeSave(String worldId, Path stateFile) {
        if (stateFile == null || !Files.exists(stateFile)) {
            return StateSaveResult.success("No existing state.json. Backup skipped.");
        }

        String safeWorldId = pathResolver.sanitizeWorldId(worldId);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path worldBackupDir = pathResolver.backupsDir().resolve(safeWorldId);
        Path backupFile = worldBackupDir.resolve("state_" + timestamp + ".json.bak");

        try {
            Files.createDirectories(worldBackupDir);
            Files.copy(stateFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
            return StateSaveResult.success("Backup created: " + backupFile);
        } catch (IOException e) {
            return StateSaveResult.failed("Failed to create backup for world " + worldId + ": " + e.getMessage());
        }
    }
}