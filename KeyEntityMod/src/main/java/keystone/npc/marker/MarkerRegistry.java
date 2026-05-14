package keystone.npc.marker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
 * MarkerRegistry ist die zentrale RAM-Liste für alle bekannten Marker.
 *
 * Diese Klasse speichert Marker nicht direkt in state.json.
 * Sie merkt nur während der Laufzeit:
 * - markerId
 * - markerName
 * - markerType
 * - worldId
 * - position
 *
 * Wichtig:
 * Marker dürfen nicht still überschrieben werden.
 * Beim Restore wird erst alles geprüft und erst danach ersetzt.
 */
public final class MarkerRegistry {

    private final Map<String, MarkerRecord> markersById = new LinkedHashMap<>();

    /*
     * Registriert einen neuen Marker.
     * Ungültige Marker oder doppelte markerIds werden abgelehnt.
     */
    public boolean register(MarkerRecord marker) {
        if (!isValidMarker(marker)) {
            return false;
        }

        String markerId = marker.markerId();
        if (markersById.containsKey(markerId)) {
            return false;
        }

        markersById.put(markerId, marker);
        return true;
    }

    /*
     * Sucht einen Marker über seine eindeutige markerId.
     */
    public Optional<MarkerRecord> findById(String markerId) {
        if (markerId == null || markerId.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(markersById.get(markerId));
    }

    /*
     * Prüft, ob ein Marker mit dieser markerId existiert.
     */
    public boolean exists(String markerId) {
        return findById(markerId).isPresent();
    }

    /*
     * Entfernt einen Marker aus der RAM-Registry.
     * Diese Methode verändert nicht direkt state.json.
     */
    public boolean remove(String markerId) {
        if (markerId == null || markerId.isBlank()) {
            return false;
        }

        return markersById.remove(markerId) != null;
    }

    /*
     * Findet alle Marker in einer bestimmten Welt.
     */
    public List<MarkerRecord> findByWorld(String worldId) {
        if (worldId == null || worldId.isBlank()) {
            return Collections.emptyList();
        }

        List<MarkerRecord> result = new ArrayList<>();

        for (MarkerRecord marker : markersById.values()) {
            if (worldId.equals(marker.worldId())) {
                result.add(marker);
            }
        }

        return Collections.unmodifiableList(result);
    }

    /*
     * Findet alle Marker in einer bestimmten Welt mit einem bestimmten MarkerType.
     */
    public List<MarkerRecord> findByWorldAndType(String worldId, MarkerType markerType) {
        if (worldId == null || worldId.isBlank() || markerType == null) {
            return Collections.emptyList();
        }

        List<MarkerRecord> result = new ArrayList<>();

        for (MarkerRecord marker : markersById.values()) {
            if (worldId.equals(marker.worldId()) && markerType == marker.markerType()) {
                result.add(marker);
            }
        }

        return Collections.unmodifiableList(result);
    }

    /*
     * Gibt eine sichere Kopie aller Marker zurück.
     * Außenstehender Code kann die Registry dadurch nicht direkt verändern.
     */
    public Collection<MarkerRecord> snapshot() {
        return Collections.unmodifiableCollection(new ArrayList<>(markersById.values()));
    }

    /*
     * Lädt mehrere Marker in die Registry.
     * Erst wird alles geprüft, dann wird der alte RAM-Zustand ersetzt.
     */
    public void restore(Collection<MarkerRecord> markers) {
        if (markers == null) {
            throw new IllegalArgumentException("markers must not be null.");
        }

        Map<String, MarkerRecord> newMarkersById = new LinkedHashMap<>();

        for (MarkerRecord marker : markers) {
            String markerId = requireValidMarker(marker);

            if (newMarkersById.containsKey(markerId)) {
                throw new IllegalStateException("Duplicate markerId in restore data: " + markerId);
            }

            newMarkersById.put(markerId, marker);
        }

        markersById.clear();
        markersById.putAll(newMarkersById);
    }

    /*
     * Leert die Registry im RAM.
     * Diese Methode sollte später nur in sicheren Admin-/Reload-Kontexten genutzt werden.
     */
    public void clear() {
        markersById.clear();
    }

    /*
     * Prüft, ob ein Marker gültig ist.
     */
    private static boolean isValidMarker(MarkerRecord marker) {
        if (marker == null) {
            return false;
        }

        if (marker.markerId() == null || marker.markerId().isBlank()) {
            return false;
        }

        if (marker.markerName() == null || marker.markerName().isBlank()) {
            return false;
        }

        if (marker.markerType() == null) {
            return false;
        }

        if (marker.worldId() == null || marker.worldId().isBlank()) {
            return false;
        }

        return marker.position() != null && marker.position().isFinite();
    }

    /*
     * Prüft einen Marker streng und gibt seine markerId zurück.
     * Wird beim Restore genutzt, damit kein kaputter Zwischenzustand entsteht.
     */
    private static String requireValidMarker(MarkerRecord marker) {
        if (!isValidMarker(marker)) {
            throw new IllegalArgumentException("marker must be valid.");
        }

        return marker.markerId();
    }
}
