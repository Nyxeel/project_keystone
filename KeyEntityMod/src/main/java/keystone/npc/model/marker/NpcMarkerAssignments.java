package keystone.npc.model.marker;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/*
 * NpcMarkerAssignments speichert die konkreten Marker-Zuweisungen eines NPCs.
 *
 * Beispiel:
 * bed -> marker_house_007_bed_main
 * work -> marker_house_007_work_01
 *
 * Wichtig:
 * Das ist nicht die globale MarkerRegistry.
 * Diese Klasse sagt nur, welche Marker ein bestimmter NPC benutzt.
 */
public final class NpcMarkerAssignments {

    private final Map<String, String> markerAssignments = new LinkedHashMap<>();

    /*
     * Gibt alle Marker-Zuweisungen als sichere Kopie zurück.
     */
    public Map<String, String> assignments() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(markerAssignments));
    }

    /*
     * Setzt eine konkrete Marker-Zuweisung.
     */
    public void assignMarker(String logicalMarkerName, String markerId) {
        markerAssignments.put(
                requireText(logicalMarkerName, "logicalMarkerName"),
                requireText(markerId, "markerId")
        );
    }

    /*
     * Entfernt eine konkrete Marker-Zuweisung.
     */
    public void removeMarkerAssignment(String logicalMarkerName) {
        markerAssignments.remove(requireText(logicalMarkerName, "logicalMarkerName"));
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
