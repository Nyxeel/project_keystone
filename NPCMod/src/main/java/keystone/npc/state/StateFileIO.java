package keystone.npc.state;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/*
 * StateFileIO ist nur für Datei-Lesen und Datei-Schreiben zuständig.
 *
 * Diese Klasse kennt keine NPC-Logik.
 * Sie weiß nicht, was ein NPC oder Marker ist.
 *
 * Aufgabe:
 * - Datei prüfen
 * - Text lesen
 * - Text sicher/atomar schreiben
 */
public final class StateFileIO {

    /*
     * Prüft, ob eine Datei existiert.
     */
    public boolean exists(Path file) {
        return file != null && Files.exists(file);
    }

    /*
     * Liest eine Datei als UTF-8 Text.
     * Bei Fehler wird null zurückgegeben.
     */
    public String readString(Path file) {
        if (file == null || !Files.exists(file)) {
            return null;
        }

        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("[KeystoneNPC][STATE_READ_FAILED] " + e.getMessage());
            return null;
        }
    }

    /*
     * Schreibt Inhalt zuerst in eine temporäre Datei und ersetzt dann die echte Datei.
     * Dadurch wird state.json nicht halb kaputt geschrieben.
     */
    public boolean writeAtomic(Path file, String content) {
        if (file == null || content == null) {
            return false;
        }

        Path tempFile = file.resolveSibling(file.getFileName() + ".tmp");

        try {
            Files.createDirectories(file.getParent());
            Files.writeString(tempFile, content, StandardCharsets.UTF_8);

            try {
                Files.move(
                        tempFile,
                        file,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (IOException atomicMoveFailed) {
                Files.move(
                        tempFile,
                        file,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            return true;
        } catch (IOException | RuntimeException e) {
            System.err.println("[KeystoneNPC][STATE_WRITE_FAILED] " + e.getMessage());
            return false;
        }
    }
}