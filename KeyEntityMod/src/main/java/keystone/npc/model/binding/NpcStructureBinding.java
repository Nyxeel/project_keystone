package keystone.npc.model.binding;

import java.util.Objects;

/*
 * NpcStructureBinding verbindet einen NPC mit einer konkreten Strukturinstanz.
 *
 * Beispiel:
 * structureInstanceId = house_007
 * slotId = main_worker
 *
 * Null ist erlaubt, wenn ein NPC nicht an ein Prefab oder eine Struktur gebunden ist.
 */
public final class NpcStructureBinding {

    private String structureInstanceId;
    private String slotId;

    /*
     * Erstellt eine leere Struktur-Bindung.
     */
    public NpcStructureBinding() {
        this.structureInstanceId = null;
        this.slotId = null;
    }

    /*
     * Gibt die Strukturinstanz zurück, falls der NPC an ein Prefab gebunden ist.
     */
    public String structureInstanceId() {
        return structureInstanceId;
    }

    /*
     * Setzt die Strukturinstanz, z. B. house_007.
     */
    public void setStructureInstanceId(String structureInstanceId) {
        this.structureInstanceId = optionalText(structureInstanceId, "structureInstanceId");
    }

    /*
     * Gibt den Slot innerhalb der Struktur zurück, z. B. main_worker oder spouse.
     */
    public String slotId() {
        return slotId;
    }

    /*
     * Setzt den Slot innerhalb der Struktur.
     */
    public void setSlotId(String slotId) {
        this.slotId = optionalText(slotId, "slotId");
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
