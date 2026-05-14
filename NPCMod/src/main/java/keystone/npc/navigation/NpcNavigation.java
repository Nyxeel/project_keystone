package keystone.npc.navigation;

import java.util.Objects;

import keystone.npc.marker.MarkerAssignment;

/*
 * NpcNavigation ist die Skeleton-Schicht für NPC-Navigation.
 *
 * Später soll diese Klasse NPCs zu Markern bewegen.
 * Dafür wird MarkerAssignment benutzt, um Marker nur lesend aufzulösen.
 *
 * Wichtig:
 * Diese Klasse darf keine Navigation persistieren.
 * Navigation ist Runtime-State und darf niemals in state.json gespeichert werden.
 */
public final class NpcNavigation {

    private final MarkerAssignment markerAssignment;

    /*
     * Erstellt die Navigation-Schicht mit Zugriff auf Marker-Zuweisungen.
     */
    public NpcNavigation(MarkerAssignment markerAssignment) {
        this.markerAssignment = Objects.requireNonNull(markerAssignment, "markerAssignment must not be null");
    }

    /*
     * Startet später Navigation zu einem Marker.
     * Im Skeleton ist echte Navigation noch nicht implementiert.
     */
    public void startNavigation(String npcId, String markerName) {
        requireText(npcId, "npcId");
        requireText(markerName, "markerName");

        // TODO: Marker später über markerAssignment.resolveMarkerReadOnly(...) nur lesend auflösen.
        // TODO: Danach echte Hytale-Navigation starten, ohne state.json zu verändern.
        throw new UnsupportedOperationException("NPC navigation start is not implemented yet.");
    }

    /*
     * Stoppt später die Runtime-Navigation eines NPCs.
     * Im Skeleton gibt es noch keine aktive Navigation.
     */
    public void stopNavigation(String npcId) {
        requireText(npcId, "npcId");

        // TODO: Später aktive Runtime-Navigation dieses NPCs stoppen.
    }

    /*
     * Aktualisiert später die laufende Runtime-Navigation eines NPCs.
     * Im Skeleton ist noch keine Navigation aktiv.
     */
    public void updateNavigation(String npcId) {
        requireText(npcId, "npcId");

        // TODO: Später Navigation nur aktualisieren, wenn der NPC ACTIVE ist und eine gültige EntityRef hat.
    }

    /*
     * Löscht später alle Runtime-Navigationsdaten.
     * Das ist wichtig bei Restart, Relink-Failure oder Remove.
     */
    public void clearRuntimeNavigation() {
        // TODO: Später alle aktiven Runtime-Navigationshandles leeren.
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