package keystone.npc.model.binding;

import java.util.Objects;

/*
 * NpcSpawnSelection speichert, welche Spawn-Auswahl für diesen NPC getroffen wurde.
 *
 * Beispiel:
 * selectedPrefabId = lumberjack_house_family
 * selectedCompositionId = composition_full_family
 *
 * Diese Daten sind wichtig, damit nach Restart klar ist,
 * aus welcher logischen Auswahl der NPC entstanden ist.
 */
public final class NpcSpawnSelection {

    private String selectedCompositionId;
    private String selectedPrefabId;

    /*
     * Erstellt eine leere Spawn-Auswahl.
     */
    public NpcSpawnSelection() {
        this.selectedCompositionId = null;
        this.selectedPrefabId = null;
    }

    /*
     * Gibt die gewählte Spawn-Composition zurück.
     */
    public String selectedCompositionId() {
        return selectedCompositionId;
    }

    /*
     * Setzt die gewählte Spawn-Composition.
     */
    public void setSelectedCompositionId(String selectedCompositionId) {
        this.selectedCompositionId = optionalText(selectedCompositionId, "selectedCompositionId");
    }

    /*
     * Gibt das gewählte Prefab zurück.
     */
    public String selectedPrefabId() {
        return selectedPrefabId;
    }

    /*
     * Setzt das gewählte Prefab.
     */
    public void setSelectedPrefabId(String selectedPrefabId) {
        this.selectedPrefabId = optionalText(selectedPrefabId, "selectedPrefabId");
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
