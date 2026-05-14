package keystone.npc.state;

/*
 * StateJsonCodec ist nur für JSON-Umwandlung zuständig.
 *
 * Später macht diese Klasse:
 * - Java-State zu JSON
 * - JSON zu Java-State
 * - alte Versionen erkennen
 * - kaputtes JSON blockieren
 *
 * Im Skeleton arbeitet sie erstmal mit rohem JSON-Text.
 */
public final class StateJsonCodec {

    /*
     * Gibt einen leeren, aber gültigen Welt-State als JSON zurück.
     */
    public String emptyWorldStateJson() {
        return """
                {
                  "version": 1,
                  "npcs": [],
                  "markers": []
                }
                """;
    }

    /*
     * Prüft grob, ob ein JSON-Text verwendbar aussieht.
     * Später wird hier echte JSON-Validierung eingebaut.
     */
    public boolean isValidStateJson(String json) {
        if (json == null || json.isBlank()) {
            return false;
        }

        String trimmed = json.trim();
        return trimmed.startsWith("{") && trimmed.endsWith("}");
    }

    /*
     * Bereitet JSON vor dem Speichern vor.
     * Später wird hier echte Serialisierung aus Java-Objekten passieren.
     */
    public String encodeRaw(String json) {
        if (json == null || json.isBlank()) {
            return emptyWorldStateJson();
        }

        return json;
    }

    /*
     * Bereitet JSON nach dem Laden vor.
     * Später wird hier echte Deserialisierung in Java-Objekte passieren.
     */
    public String decodeRaw(String json) {
        if (!isValidStateJson(json)) {
            return null;
        }

        return json;
    }
}