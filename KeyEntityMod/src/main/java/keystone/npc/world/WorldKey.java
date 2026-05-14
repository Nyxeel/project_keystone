package keystone.npc.world;

/*
 * WorldKey beschreibt eine Server-Spielwelt eindeutig genug für unsere NPCMod.
 *
 * Diese Klasse ist wichtig, weil state.json später pro Welt gespeichert wird.
 * Beispiel:
 * keystone-npc/worlds/<worldKey>/state.json
 *
 * Später kann WorldKey aus Hytale-Daten wie World-Name, SavePath oder stabiler World-ID gebaut werden.
 *
 * Wichtig:
 * Der key muss sicher für Dateipfade sein.
 * displayName und savePath sind nur Zusatzinformationen.
 */
public record WorldKey(
        String key,
        String displayName,
        String savePath
) {

    /*
     * Prüft und normalisiert die Werte beim Erstellen.
     */
    public WorldKey {
        key = sanitize(key);

        if (displayName == null || displayName.isBlank()) {
            displayName = key;
        } else {
            displayName = displayName.trim();
        }

        savePath = optionalText(savePath, "savePath");
    }

    /*
     * Erstellt einen WorldKey nur aus einem Welt-Namen.
     * Das ist ein Fallback, bis wir die beste Hytale-API für stabile World-IDs nutzen.
     */
    public static WorldKey fromName(String worldName) {
        String checkedWorldName = requireText(worldName, "worldName");

        return new WorldKey(
                sanitize(checkedWorldName),
                checkedWorldName.trim(),
                null
        );
    }

    /*
     * Macht einen Welt-Namen sicher für Ordnernamen.
     */
    public static String sanitize(String value) {
        String checkedValue = requireText(value, "world key");

        String safeValue = checkedValue.trim()
                .replace('\\', '_')
                .replace('/', '_')
                .replace(':', '_')
                .replaceAll("[^A-Za-z0-9._-]", "_")
                .replaceAll("\\.{2,}", "_")
                .replaceAll("^\\.+", "_")
                .replaceAll("_+", "_");

        if (safeValue.isBlank() || ".".equals(safeValue) || "..".equals(safeValue)) {
            throw new IllegalArgumentException("world key cannot be converted to a safe path name.");
        }

        return safeValue;
    }

    /*
     * Prüft, ob ein Pflicht-Text vorhanden ist.
     */
    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank.");
        }

        return value;
    }

    /*
     * Prüft einen optionalen Text.
     * Null ist erlaubt, aber leerer oder blanker Text nicht.
     */
    private static String optionalText(String value, String fieldName) {
        if (value == null) {
            return null;
        }

        return requireText(value, fieldName).trim();
    }
}