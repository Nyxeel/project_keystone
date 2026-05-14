package keystone.npc.state;

import java.util.List;

import keystone.npc.model.PersistedWorldState;

/*
 * StateLoadResult beschreibt das Ergebnis eines Load-Vorgangs.
 *
 * Diese Klasse sagt nicht nur, ob Laden erfolgreich war,
 * sondern kann später auch geladene PersistedWorldState-Daten zurückgeben.
 *
 * Warum?
 * NpcStateStore lädt state.json.
 * Danach soll NpcManager daraus die NpcRecords übernehmen.
 *
 * Wichtig:
 * Ein Ergebnis darf nicht gleichzeitig success, partial und failed sein.
 * worldStates ist niemals null.
 */
public record StateLoadResult(
        boolean success,
        boolean partial,
        boolean loadFailed,
        String message,
        List<PersistedWorldState> worldStates
) {

    /*
     * Erstellt ein Load-Ergebnis und prüft die Grundregeln.
     */
    public StateLoadResult {
        validateResultFlags(success, partial, loadFailed);

        message = requireText(message, "message");
        worldStates = worldStates == null ? List.of() : List.copyOf(worldStates);
    }

    /*
     * Erstellt ein erfolgreiches Load-Ergebnis ohne geladene Welt-Daten.
     */
    public static StateLoadResult success(String message) {
        return new StateLoadResult(true, false, false, message, List.of());
    }

    /*
     * Erstellt ein erfolgreiches Load-Ergebnis mit genau einem geladenen Welt-State.
     */
    public static StateLoadResult success(String message, PersistedWorldState worldState) {
        if (worldState == null) {
            return success(message);
        }

        return new StateLoadResult(true, false, false, message, List.of(worldState));
    }

    /*
     * Erstellt ein erfolgreiches Load-Ergebnis mit mehreren geladenen Welt-States.
     */
    public static StateLoadResult successMany(String message, List<PersistedWorldState> worldStates) {
        return new StateLoadResult(true, false, false, message, worldStates);
    }

    /*
     * Erstellt ein teilweise erfolgreiches Load-Ergebnis.
     * Das wird später wichtig, wenn manche Welten geladen wurden und andere nicht.
     */
    public static StateLoadResult partial(String message, List<PersistedWorldState> worldStates) {
        return new StateLoadResult(false, true, false, message, worldStates);
    }

    /*
     * Erstellt ein fehlgeschlagenes Load-Ergebnis.
     * Bei Fehler werden keine Welt-Daten zurückgegeben.
     */
    public static StateLoadResult failed(String message) {
        return new StateLoadResult(false, false, true, message, List.of());
    }

    /*
     * Gibt true zurück, wenn mindestens ein Welt-State geladen wurde.
     */
    public boolean hasWorldStates() {
        return !worldStates.isEmpty();
    }

    /*
     * Prüft, ob genau ein Ergebnis-Zustand aktiv ist.
     */
    private static void validateResultFlags(boolean success, boolean partial, boolean loadFailed) {
        int activeFlags = 0;

        if (success) {
            activeFlags++;
        }

        if (partial) {
            activeFlags++;
        }

        if (loadFailed) {
            activeFlags++;
        }

        if (activeFlags != 1) {
            throw new IllegalArgumentException("Exactly one load result flag must be true.");
        }
    }

    /*
     * Prüft, ob ein Pflicht-Text vorhanden ist.
     */
    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank.");
        }

        return value;
    }
}