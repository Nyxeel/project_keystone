package keystone.npc.model.binding;

import java.util.Objects;

/*
 * NpcAppearanceSelection speichert, welche Appearance-Variante gewählt wurde.
 *
 * Beispiel:
 * selectedAppearanceId = lumberjack_male_green_eyes_01
 *
 * Diese Klasse wendet keine Appearance an.
 * Sie merkt nur die persistente Auswahl.
 */
public final class NpcAppearanceSelection {

    private String selectedAppearanceId;

    /*
     * Erstellt eine leere Appearance-Auswahl.
     */
    public NpcAppearanceSelection() {
        this.selectedAppearanceId = null;
    }

    /*
     * Gibt die ausgewählte Appearance-Variante zurück.
     */
    public String selectedAppearanceId() {
        return selectedAppearanceId;
    }

    /*
     * Setzt die ausgewählte Appearance-Variante.
     */
    public void setSelectedAppearanceId(String selectedAppearanceId) {
        this.selectedAppearanceId = optionalText(selectedAppearanceId, "selectedAppearanceId");
    }

    /*
     * Prüft einen optionalen Text.
     * Null ist erlaubt, aber leerer oder blanker Text nicht.
     */
    private static String optionalText(String value, String fieldName) {
        if (value == null) {
            return null;
        }

        Objects.requireNonNull(value, fieldName + " must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return value;
    }
}
