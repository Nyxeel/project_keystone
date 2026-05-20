package keystone.npc.definition.model;

/*
 * ProfileTypeRule beschreibt die Regel für einen Profil-Typ.
 *
 * Beispiel:
 * Routine ist required.
 * Combat ist optional.
 * CustomSomething ist custom und wird nur basic validiert.
 *
 * Diese Klasse lädt kein Profil.
 * Sie sagt nur, wie ein Profil später geprüft werden soll.
 */
public final class ProfileTypeRule {

	private final String 	profileKey;			// Name des Profil-Typs, z. B. Routine, Actions oder Combat.
	private final boolean 	required; 			// True, wenn dieses Profil Pflicht ist.
	private final String 	expectedType; 		// Optionaler erwarteter JSON-Type, z. B. RoutineProfile.
	private final String 	validationMode; 	// Gibt an, wie streng dieses Profil geprüft wird.
	private final boolean 	knownProfileType; 	// True, wenn der Profil-Typ fest bekannt ist.
	private final String 	handlerKey; 		// Optionaler späterer Handler-Key für ausführbare Systeme.

	/*
	 * Erstellt eine Regel für einen Profil-Typ.
	 */
	public ProfileTypeRule(
		String	profileKey,			// Name des Profil-Typs.
		boolean required,			// Ob dieses Profil Pflicht ist.
		String	expectedType,		// Optionaler erwarteter JSON-Type.
		String	validationMode,		// Validierungsart.
		boolean knownProfileType, 	// Ob der Profil-Typ bekannt ist.
		String	handlerKey 			// Optionaler späterer Handler-Key.
	) {
		this.profileKey = requireText(profileKey, "profileKey");
		this.required = required;
		this.expectedType = cleanOptionalText(expectedType);
		this.validationMode = requireText(validationMode, "validationMode");
		this.knownProfileType = knownProfileType;
		this.handlerKey = cleanOptionalText(handlerKey);
	}

	/*
	 * Gibt den Profil-Key zurück.
	 */
	public String profileKey() {
		return profileKey;
	}

	/*
	 * Gibt zurück, ob dieses Profil Pflicht ist.
	 */
	public boolean required() {
		return required;
	}

	/*
	 * Gibt den erwarteten JSON-Type zurück, falls einer festgelegt wurde.
	 */
	public String expectedType() {
		return expectedType;
	}

	/*
	 * Gibt die Validierungsart zurück.
	 */
	public String validationMode() {
		return validationMode;
	}

	/*
	 * Gibt zurück, ob dieser Profil-Typ bekannt ist.
	 */
	public boolean knownProfileType() {
		return knownProfileType;
	}

	/*
	 * Gibt den optionalen späteren Handler-Key zurück.
	 */
	public String handlerKey() {
		return handlerKey;
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