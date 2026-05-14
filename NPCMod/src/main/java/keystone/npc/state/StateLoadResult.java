package keystone.npc.state;

/*
 * StateLoadResult beschreibt das Ergebnis eines Load-Vorgangs.
 *
 * Warum nicht nur boolean?
 * Weil Laden mehrere Zustände haben kann:
 * - erfolgreich
 * - teilweise geladen
 * - komplett fehlgeschlagen
 * - blockiert wegen kaputtem JSON
 */
public record StateLoadResult(
        boolean success,
        boolean partial,
        boolean loadFailed,
        String message
) {

    /*
     * Erstellt ein erfolgreiches Load-Ergebnis.
     */
    public static StateLoadResult success(String message) {
        return new StateLoadResult(true, false, false, message);
    }

    /*
     * Erstellt ein teilweise erfolgreiches Load-Ergebnis.
     */
    public static StateLoadResult partial(String message) {
        return new StateLoadResult(false, true, false, message);
    }

    /*
     * Erstellt ein fehlgeschlagenes Load-Ergebnis.
     */
    public static StateLoadResult failed(String message) {
        return new StateLoadResult(false, false, true, message);
    }
}