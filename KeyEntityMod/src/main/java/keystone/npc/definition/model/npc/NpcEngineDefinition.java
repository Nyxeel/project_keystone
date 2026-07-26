package keystone.npc.definition.model.npc;

/*
 * NpcEngineDefinition beschreibt die Hytale-Engine-Anbindung einer NPC-Definition.
 *
 * Wichtig:
 * hytaleRole ist die echte Hytale Role-Datei unter Server/NPC/Roles/.
 * RoleId bleibt Keystone-Logik und gehört nicht hier hinein.
 *
 * Diese Klasse spawnt nichts.
 * Sie speichert nur Bauplan-Daten aus resources.
 */
public final class NpcEngineDefinition {

	private final String hytaleRole;		// Echte Hytale Engine-Role, z. B. Keystone_Human_Worker.
	private final String templateReference; // Optionaler Template-/Reference-Name für spätere Prüfung.

	/*
	 * Erstellt die Engine-Daten für einen NPC-Bauplan.
	 */
	public NpcEngineDefinition(
		String hytaleRole,			// Echte Hytale Role, die beim Spawn verwendet wird.
		String templateReference	// Optionaler Verweis auf ein Template oder eine Base-Reference.
	) {
		this.hytaleRole = requireText(hytaleRole, "hytaleRole");
		this.templateReference = cleanOptionalText(templateReference);
	}

	/*
	 * Gibt die echte Hytale Engine-Role zurück.
	 */
	public String hytaleRole() {
		return hytaleRole;
	}

	/*
	 * Gibt die optionale Template-/Reference-Angabe zurück.
	 */
	public String templateReference() {
		return templateReference;
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