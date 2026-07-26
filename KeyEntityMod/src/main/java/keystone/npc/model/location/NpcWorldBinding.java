package keystone.npc.model.location;

import java.util.Objects;

/*
 * NpcWorldBinding speichert, zu welcher Welt ein NPC gehört.
 *
 * Wichtig:
 * worldKey ist bewusst unveränderlich.
 * Ein NPC soll nicht heimlich von Welt A nach Welt B verschoben werden.
 */
public record NpcWorldBinding(String worldKey) {

    /*
     * Prüft beim Erstellen, dass der Welt-Key gültig ist.
     */
    public NpcWorldBinding {
        if (worldKey == null || worldKey.isBlank()) {
            throw new IllegalArgumentException("worldKey must not be null or blank");
        }
    }

}
