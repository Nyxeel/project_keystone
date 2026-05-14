package keystone.npc.model.identity;

import java.util.Objects;

/*
 * NpcIdentity enthält die stabile Mod-Identität eines NPCs.
 *
 * Diese Daten sagen:
 * - welche konkrete NPC-Instanz ist das?
 * - wie heißt sie?
 * - welche Keystone-roleId hat sie?
 *
 * Wichtig:
 * Das ist nicht die Hytale-Entity-UUID.
 */
public record NpcIdentity(
        String npcId,
        String npcName,
        String roleId
) {

    /*
     * Prüft beim Erstellen, ob alle Pflichtwerte vorhanden sind.
     */
    public NpcIdentity {
        npcId = requireText(npcId, "npcId");
        npcName = requireText(npcName, "npcName");
        roleId = requireText(roleId, "roleId");
    }

    /*
     * Prüft, ob ein Pflicht-Text gültig ist.
     */
    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return value;
    }
}
