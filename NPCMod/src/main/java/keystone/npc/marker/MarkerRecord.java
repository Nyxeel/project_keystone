package keystone.npc.marker;

import java.util.Objects;

/*
 * MarkerRecord beschreibt einen einzelnen Marker.
 *
 * Diese Daten dürfen später gespeichert werden:
 * - markerId
 * - markerName
 * - markerType
 * - worldId / worldKey
 * - position
 *
 * Wichtig:
 * MarkerRecord enthält keine Runtime-Daten.
 */
public record MarkerRecord(
        String markerId,
        String markerName,
        MarkerType markerType,
        String worldId,
        MarkerPosition position
) {

    /*
     * Prüft beim Erstellen, ob alle Pflichtdaten gültig sind.
     */
    public MarkerRecord {
        requireText(markerId, "markerId");
        requireText(markerName, "markerName");
        Objects.requireNonNull(markerType, "markerType must not be null");
        requireText(worldId, "worldId");
        Objects.requireNonNull(position, "position must not be null");

        if (!position.isFinite()) {
            throw new IllegalArgumentException("position must be finite.");
        }
    }

    /*
     * Prüft, ob ein Textwert vorhanden ist.
     */
    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank.");
        }
    }
}
