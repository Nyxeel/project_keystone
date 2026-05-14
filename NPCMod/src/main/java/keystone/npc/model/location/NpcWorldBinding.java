package keystone.npc.model.location;

import java.util.Objects;

/*
 * NpcWorldBinding sagt, zu welcher Server-Spielwelt ein NPC gehört.
 *
 * Später wird worldKey für Pfade wie diese genutzt:
 * keystone-npc/worlds/<worldKey>/state.json
 */
public final class NpcWorldBinding {

    private String worldKey;

    /*
     * Erstellt eine Welt-Bindung für einen NPC.
     */
    public NpcWorldBinding(String worldKey) {
        this.worldKey = requireText(worldKey, "worldKey");
    }

    /*
     * Gibt den stabilen Welt-Key zurück.
     */
    public String worldKey() {
        return worldKey;
    }

    /*
     * Setzt den stabilen Welt-Key.
     */
    public void setWorldKey(String worldKey) {
        this.worldKey = requireText(worldKey, "worldKey");
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
