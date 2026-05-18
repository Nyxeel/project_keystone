package keystone.npc.state.internal;

import java.util.regex.Pattern;

import keystone.npc.model.PersistedWorldState;




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
 *
 * Wichtig:
 * Kaputtes oder leeres JSON darf niemals automatisch zu leerem Default-State werden.
 * Sonst könnte eine echte state.json versehentlich überschrieben werden.
 */
public final class StateJsonCodec {

    private static final Pattern VERSION_FIELD = Pattern.compile("\"version\"\\s*:");
    private static final Pattern NPCS_FIELD = Pattern.compile("\"npcs\"\\s*:");
    private static final Pattern MARKERS_FIELD = Pattern.compile("\"markers\"\\s*:");
	private static final Pattern EMPTY_NPCS_FIELD = Pattern.compile("\"npcs\"\\s*:\\s*\\[\\s*\\]");
	// MORE FIELDS LATER

	//TODO: JSON bei Bedarf erweitern

	// npcId
	// roleId
	// worldKey / worldUuid
	// entityUuid
	// state
	// markerAssignments
	// currentPosition
	// structureInstanceId
	// slotId
	// selectedAppearanceId


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



	private static String escapeJson(String value) {
   		return value
        	.replace("\\", "\\\\")
            .replace("\"", "\\\"");
	}


	public String WorldStateToJson(PersistedWorldState worldState) {
		if (worldState == null)
			return null;

		String worldKey = worldState.worldKey(); //

		if (worldKey == null || worldKey.isBlank())
			return null;


		// TODO: FORMAT NPC RECORDS TO JSON and RETURN JSON

		return """
           {
             "version": 1,
             "worldKey": "%s",
             "npcs": [],
             "markers": []
           }
           """.formatted(escapeJson(worldKey));
		// TODO: Muss spaeter die NPC Json eintraege als skelett returnen!
	}


	/*
	 * Liest den JSON-Text einer state.json und baut daraus einen PersistedWorldState.
	 *
	 * Wichtig:
	 * Diese Methode darf kaputtes JSON niemals still als leeren State behandeln.
	 * Wenn etwas fehlt oder ungültig ist, gibt sie null zurück.
	 *
	 * Phase 2 Minimal-Version:
	 * - leere npc-Liste ist erlaubt
	 * - echte NPC-Records werden später sauber geparst
	 */


	public PersistedWorldState JsonToWorldState(String worldKey, String json) {
		if (worldKey == null || worldKey.isBlank()) {
			return null;
		}

		if (!isValidStateJson(json)) {
			return null;
		}

		String trimmedJson = json.trim();
		if (!EMPTY_NPCS_FIELD.matcher(trimmedJson).find()) { //Empty fields skelett!
			return null;									// Eintraege anlegen
		}

		return new PersistedWorldState(worldKey.trim());
		// TODO: nur nen Skelett return, muss in NPC Record die eintraege speichern
		// Wenn Server schon ne state.json hat, dann auslesen um NPC laden zu koennen!
		//TODO: es ist wichtig das NPC auck korrekt gefunden werden, wenn in
		// resources/Server/NPC/Roles in deren role.json aenderungen gab die
		// nicht mehr der state.json entsprechen!

		//Aktuell schreibt WorldStateToJson() immer:

		// "npcs": []
		// "markers": []
			//
		// Egal, ob PersistedWorldState echte NpcRecords enthält.


		// PersistedWorldState leer → darf speichern
		// PersistedWorldState enthält NPCs → Save blockieren



	}


    /*
     * Prüft, ob ein JSON-Text als Skeleton-state.json verwendbar ist.
     */
    public boolean isValidStateJson(String json) {
        if (json == null || json.isBlank()) {
            return false;
        }
	    String trimmed = json.trim();

	    if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
	        return false;
	    }

	    if (!new JsonSyntaxParser(trimmed).isValidJson()) {
	        return false;
	    }

	    return VERSION_FIELD.matcher(trimmed).find()
	           && NPCS_FIELD.matcher(trimmed).find()
	           && MARKERS_FIELD.matcher(trimmed).find();
			   // TODO: JSON Marker die aus der File gelesen werden soll hier und oben eintragen!
	}

	/*
	* Bereitet rohes JSON vor dem Speichern vor.
	* Ungültiges JSON wird nicht durch Default-State ersetzt.
	*/
	public String encodeRaw(String json) {
	    if (!isValidStateJson(json)) {
	        return null;
	    }
        return json.trim();
    }

    /*
     * Bereitet JSON nach dem Laden vor.
     * Ungültiges JSON wird blockiert.
     */
    public String decodeRaw(String json) {
        if (!isValidStateJson(json)) {
            return null;
        }

        return json.trim();
    }

    /*
     * Kleiner JSON-Syntaxprüfer ohne externe Bibliothek.
     * Er baut keine Java-Objekte, sondern prüft nur die Grundform.
     */
    private static final class JsonSyntaxParser {

        private final String text;
        private int index;

        /*
         * Erstellt den Parser für einen JSON-Text.
         */
        private JsonSyntaxParser(String text) {
            this.text = text;
        }

        /*
         * Prüft, ob der gesamte Text gültige JSON-Grundsyntax hat.
         */
        private boolean isValidJson() {
            try {
                skipWhitespace();

                if (!parseValue()) {
                    return false;
                }

                skipWhitespace();
                return index == text.length();
            } catch (RuntimeException ignored) {
                return false;
            }
        }

        /*
         * Prüft einen JSON-Wert.
         */
        private boolean parseValue() {
            skipWhitespace();

            if (index >= text.length()) {
                return false;
            }

            char current = text.charAt(index);

            if (current == '{') {
                return parseObject();
            }

            if (current == '[') {
                return parseArray();
            }

            if (current == '"') {
                return parseString();
            }

            if (current == '-' || Character.isDigit(current)) {
                return parseNumber();
            }

            return parseLiteral("true")
                    || parseLiteral("false")
                    || parseLiteral("null");
        }

        /*
         * Prüft ein JSON-Objekt.
         */
        private boolean parseObject() {
            index++;
            skipWhitespace();

            if (consume('}')) {
                return true;
            }

            while (true) {
                skipWhitespace();

                if (!parseString()) {
                    return false;
                }

                skipWhitespace();

                if (!consume(':')) {
                    return false;
                }

                if (!parseValue()) {
                    return false;
                }

                skipWhitespace();

                if (consume('}')) {
                    return true;
                }

                if (!consume(',')) {
                    return false;
                }
            }
        }

        /*
         * Prüft ein JSON-Array.
         */
        private boolean parseArray() {
            index++;
            skipWhitespace();

            if (consume(']')) {
                return true;
            }

            while (true) {
                if (!parseValue()) {
                    return false;
                }

                skipWhitespace();

                if (consume(']')) {
                    return true;
                }

                if (!consume(',')) {
                    return false;
                }
            }
        }

        /*
         * Prüft einen JSON-String inklusive Escape-Zeichen.
         */
        private boolean parseString() {
            if (!consume('"')) {
                return false;
            }

            while (index < text.length()) {
                char current = text.charAt(index++);

                if (current == '"') {
                    return true;
                }

                if (current == '\\') {
                    if (!parseEscape()) {
                        return false;
                    }
                } else if (current < 0x20) {
                    return false;
                }
            }

            return false;
        }

        /*
         * Prüft ein Escape-Zeichen in einem JSON-String.
         */
        private boolean parseEscape() {
            if (index >= text.length()) {
                return false;
            }

            char escaped = text.charAt(index++);

            if (escaped == '"' || escaped == '\\' || escaped == '/'
                    || escaped == 'b' || escaped == 'f'
                    || escaped == 'n' || escaped == 'r' || escaped == 't') {
                return true;
            }

            if (escaped != 'u') {
                return false;
            }

            for (int i = 0; i < 4; i++) {
                if (index >= text.length() || !isHex(text.charAt(index++))) {
                    return false;
                }
            }

            return true;
        }

        /*
         * Prüft eine JSON-Zahl.
         */
        private boolean parseNumber() {
            int start = index;

            if (consume('-') && index >= text.length()) {
                return false;
            }

            if (consume('0')) {
                // Einzelne 0 ist erlaubt.
            } else if (index < text.length() && isDigitOneToNine(text.charAt(index))) {
                index++;

                while (index < text.length() && Character.isDigit(text.charAt(index))) {
                    index++;
                }
            } else {
                index = start;
                return false;
            }

            if (consume('.')) {
                if (index >= text.length() || !Character.isDigit(text.charAt(index))) {
                    return false;
                }

                while (index < text.length() && Character.isDigit(text.charAt(index))) {
                    index++;
                }
            }

            if (index < text.length() && (text.charAt(index) == 'e' || text.charAt(index) == 'E')) {
                index++;

                if (index < text.length() && (text.charAt(index) == '+' || text.charAt(index) == '-')) {
                    index++;
                }

                if (index >= text.length() || !Character.isDigit(text.charAt(index))) {
                    return false;
                }

                while (index < text.length() && Character.isDigit(text.charAt(index))) {
                    index++;
                }
            }

            return true;
        }

        /*
         * Prüft ein festes JSON-Literal wie true, false oder null.
         */
        private boolean parseLiteral(String literal) {
            if (!text.startsWith(literal, index)) {
                return false;
            }

            index += literal.length();
            return true;
        }

        /*
         * Überspringt Leerzeichen.
         */
        private void skipWhitespace() {
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
        }

        /*
         * Verbraucht ein erwartetes Zeichen, wenn es an der aktuellen Stelle steht.
         */
        private boolean consume(char expected) {
            if (index < text.length() && text.charAt(index) == expected) {
                index++;
                return true;
            }

            return false;
        }

        /*
         * Prüft, ob ein Zeichen eine Hex-Ziffer ist.
         */
        private static boolean isHex(char value) {
            return (value >= '0' && value <= '9')
                    || (value >= 'a' && value <= 'f')
                    || (value >= 'A' && value <= 'F');
        }

        /*
         * Prüft, ob ein Zeichen eine Ziffer von 1 bis 9 ist.
         */
        private static boolean isDigitOneToNine(char value) {
            return value >= '1' && value <= '9';
        }
    }
}