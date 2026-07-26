package keystone.npc.state;

/*
 * StateSaveResult beschreibt das Ergebnis eines Save-Vorgangs.
 *
 * Wichtig:
 * Save-Fehler dürfen niemals als Erfolg gelten.
 * Andere Services können dadurch sauber prüfen:
 * result.success()
 */
public record StateSaveResult(
        boolean success,
        String message
) {

	public StateSaveResult {
        message = requireText(message, "message");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank.");
        }

        return value;
    }

    /*
     * Erstellt ein erfolgreiches Save-Ergebnis.
     */
    public static StateSaveResult success(String message) {
        return new StateSaveResult(true, message);
    }

    /*
     * Erstellt ein fehlgeschlagenes Save-Ergebnis.
     */
    public static StateSaveResult failed(String message) {
        return new StateSaveResult(false, message);
    }
}