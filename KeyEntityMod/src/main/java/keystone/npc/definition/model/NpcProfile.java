package keystone.npc.definition.model;

/*
 * NpcProfile beschreibt einen einzelnen Profil-Verweis einer NPC-Definition.
 *
 * Wichtig:
 * Diese Klasse enthält NICHT den Inhalt der Profil-Datei.
 * Sie merkt sich nur, wo das Profil liegt und wie es validiert werden soll.
 *
 * Beispiel:
 * ProfileKey = Routine
 * Path = npc/lumberjack/routines/lumberjack_day.json
 * ExpectedType = RoutineProfile
 */
public final class NpcProfile {

	private final String profileKey;		// Name des Profils, z. B. Routine, Actions oder Movement.
	private final String path;				// Resource-Pfad zur Profil-Datei.
	private final String expectedType;		// Erwarteter JSON-Type, z. B. RoutineProfile. Optional für Custom-Profile.
	private final String namespace;			// Optionaler Namespace, z. B. keystone.
	private final String assetId;			// Optionale spätere Asset-ID.
	private final boolean knownProfileType;	// True, wenn der Profiltyp dem Loader bekannt ist.
	private final boolean required;			// True, wenn dieses Profil Pflicht ist.
	private final String validationMode;		// Regel, wie streng dieses Profil geprüft wird.
	private final String handlerKey;			// Optionaler späterer Handler-Key für ausführbare Systeme.

	/*
	 * Erstellt einen einzelnen Profil-Verweis aus Resource-Daten.
	 */
	public NpcProfile(
		String profileKey,			// Name des Profils.
		String path,				// Resource-Pfad zur Profil-Datei.
		String expectedType,		// Erwarteter JSON-Type. Darf bei Custom-Profilen null sein.
		String namespace,			// Optionaler Namespace.
		String assetId,				// Optionale spätere Asset-ID.
		boolean knownProfileType,	// Ob der Profiltyp bekannt ist.
		boolean required,			// Ob dieses Profil Pflicht ist.
		String validationMode,		// Validierungsart.
		String handlerKey			// Optionaler späterer Handler-Key.
	) {
		this.profileKey = requireText(profileKey, "profileKey");
		this.path = requireText(path, "path");
		this.expectedType = cleanOptionalText(expectedType);
		this.namespace = cleanOptionalText(namespace);
		this.assetId = cleanOptionalText(assetId);
		this.knownProfileType = knownProfileType;
		this.required = required;
		this.validationMode = requireText(validationMode, "validationMode");
		this.handlerKey = cleanOptionalText(handlerKey);
	}

	/*
	 * Gibt den Profil-Key zurück, zum Beispiel Routine.
	 */
	public String profileKey() {
		return profileKey;
	}

	/*
	 * Gibt den Resource-Pfad zur Profil-Datei zurück.
	 */
	public String path() {
		return path;
	}

	/*
	 * Gibt den erwarteten JSON-Type zurück, zum Beispiel RoutineProfile.
	 */
	public String expectedType() {
		return expectedType;
	}

	/*
	 * Gibt den optionalen Namespace zurück.
	 */
	public String namespace() {
		return namespace;
	}

	/*
	 * Gibt die optionale Asset-ID zurück.
	 */
	public String assetId() {
		return assetId;
	}

	/*
	 * Gibt zurück, ob dieser Profiltyp bekannt ist.
	 */
	public boolean knownProfileType() {
		return knownProfileType;
	}

	/*
	 * Gibt zurück, ob dieses Profil Pflicht ist.
	 */
	public boolean required() {
		return required;
	}

	/*
	 * Gibt die Validierungsart zurück.
	 */
	public String validationMode() {
		return validationMode;
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