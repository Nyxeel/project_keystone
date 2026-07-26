package keystone.npc.marker;

/*
 * MarkerType beschreibt die grobe Funktion eines Markers.
 *
 * Wichtig:
 * Das ist NICHT jeder einzelne Markername.
 *
 * Beispiel:
 * bed_main, bed_spouse und bed_child sind logische Namen aus JSON.
 * Ihr grober Typ kann trotzdem BED sein.
 */
public enum MarkerType {
    BED,
    DOOR,
    WORK,
    FOOD,
    CHEST,
    CHILL,
    GUARD,
    PATROL,
    SPAWN
}
