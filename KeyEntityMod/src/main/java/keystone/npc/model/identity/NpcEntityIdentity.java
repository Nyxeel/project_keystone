package keystone.npc.model.identity;

import java.util.Objects;

/*
 * NpcEntityIdentity enthält die persistierte Hytale-Entity-Identität.
 *
 * Wichtig:
 * Hier wird nur die entityUuid gespeichert.
 * Hier wird keine EntityRef und kein Hytale-Entity-Objekt gespeichert.
 */
public final class NpcEntityIdentity {

    private String entityUuid;

    /*
     * Erstellt eine leere Entity-Identität.
     * Null bedeutet: Es ist noch keine sichere Hytale-Entity bekannt.
     */
    public NpcEntityIdentity() {
        this.entityUuid = null;
    }

    /*
     * Gibt die persistierte Hytale Entity UUID zurück.
     */
    public String entityUuid() {
        return entityUuid;
    }

    /*
     * Setzt die persistierte Hytale Entity UUID.
     * Null ist erlaubt, wenn noch keine sichere Entity bekannt ist.
     */
    public void setEntityUuid(String entityUuid) {
        this.entityUuid = optionalText(entityUuid, "entityUuid");
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
