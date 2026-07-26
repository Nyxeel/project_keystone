package keystone.npc.definition.model.npc;

/*
 * NpcDisplayDefinition beschreibt Anzeige-Daten einer NPC-Definition.
 *
 * Diese Klasse ist nur für Namen / Anzeige gedacht.
 * Sie ist keine technische Identität.
 *
 * Wichtig:
 * RoleId ist die technische Keystone-ID.
 * hytaleRole ist die technische Hytale-Engine-Role.
 * fallbackName ist nur ein lesbarer Name für Anzeige oder Debug.
 */
public final class NpcDisplayDefinition {

	private final String fallbackName; 			// Lesbarer Ersatzname, z. B. Lumberjack.
	private final String nameTranslationKey;	// Optionaler Translation-Key für spätere Übersetzungen.

	/*
	 * Erstellt die Anzeige-Daten für einen NPC-Bauplan.
	 */
	public NpcDisplayDefinition(
		String fallbackName,		// Lesbarer Ersatzname, falls keine Übersetzung genutzt wird.
		String nameTranslationKey	// Optionaler Übersetzungs-Key für UI / Sprache.
	) {
		this.fallbackName = requireText(fallbackName, "fallbackName");
		this.nameTranslationKey = cleanOptionalText(nameTranslationKey);
	}

	/*
	 * Gibt den lesbaren Ersatznamen zurück.
	 */
	public String fallbackName() {
		return fallbackName;
	}

	/*
	 * Gibt den optionalen Translation-Key zurück.
	 */
	public String nameTranslationKey() {
		return nameTranslationKey;
	}

	/*
	 * Prüft Pflicht-Textfelder und entfernt unnötige Leerzeichen.
	 */
	private static String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " must not be null or blank");
		}

		return value.trim();
	}

	/*
	 * Bereinigt optionale Textfelder, ohne sie verpflichtend zu machen.
	 */
	private static String cleanOptionalText(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		return value.trim();
	}
}