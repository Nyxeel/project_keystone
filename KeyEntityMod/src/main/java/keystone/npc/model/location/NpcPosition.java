package keystone.npc.model.location;

/*
 * NpcPosition ist eine einfache persistierbare Position.
 *
 * Wichtig:
 * Das ist kein Hytale-Entity-Objekt.
 * Das ist kein Runtime-Handle.
 * Das ist nur x/y/z für state.json.
 */
public record NpcPosition(
        double x,
        double y,
        double z
) {

    /*
     * Erstellt eine persistierbare Position und blockiert ungültige Zahlen.
     */
    public NpcPosition {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            throw new IllegalArgumentException("NpcPosition coordinates must be finite.");
        }
    }

    /*
     * Prüft, ob alle Koordinaten gültige Zahlen sind.
     */
    public boolean isFinite() {
        return Double.isFinite(x)
                && Double.isFinite(y)
                && Double.isFinite(z);
    }
}
