package keystone.npc.marker;

/*
 * MarkerPosition speichert die Weltposition eines Markers.
 *
 * Diese Klasse ist reine Datenstruktur.
 * Sie enthält keine Hytale-Entity und keine Runtime-Logik.
 */
public record MarkerPosition(
        double x,
        double y,
        double z
) {

    /*
     * Prüft, ob alle Koordinaten echte endliche Zahlen sind.
     */
    public boolean isFinite() {
        return Double.isFinite(x)
                && Double.isFinite(y)
                && Double.isFinite(z);
    }
}
