package keystone.npc.definition.model.npc;

/*
 * NpcDebugDefinition beschreibt Debug-Einstellungen für eine NPC-Definition.
 *
 * Diese Klasse hält role-spezifische Debug-Schalter.
 * Globale Debug-Einstellungen bleiben davon getrennt.
 *
 * Beispiel:
 * LogRoutine
 * LogMarkers
 * LogNavigation
 * LogActions
 *
 * Wichtig:
 * Global Debug ist der Master-Schalter.
 * Role-Debug verfeinert nur, was für diese RoleId geloggt werden darf.
 *
 * Diese Klasse loggt selbst nichts.
 * Sie speichert nur Bauplan-/Config-Daten aus resources.
 */
public final class NpcDebugDefinition {

	private final boolean enabled;			// Ob Debug für diese NPC-Definition grundsätzlich erlaubt ist.
	private final boolean logRoutine;		// Ob Routine-Entscheidungen geloggt werden dürfen.
	private final boolean logMarkers;		// Ob Marker-Prüfungen geloggt werden dürfen.
	private final boolean logNavigation;		// Ob Navigation geloggt werden darf.
	private final boolean logActions;		// Ob Actions geloggt werden dürfen.

	/*
	 * Erstellt die Debug-Einstellungen für einen NPC-Bauplan.
	 */
	public NpcDebugDefinition(
		boolean enabled,			// Ob Debug für diese Definition aktiviert ist.
		boolean logRoutine,			// Ob Routine-Logs erlaubt sind.
		boolean logMarkers,			// Ob Marker-Logs erlaubt sind.
		boolean logNavigation,		// Ob Navigations-Logs erlaubt sind.
		boolean logActions			// Ob Action-Logs erlaubt sind.
	) {
		this.enabled = enabled;
		this.logRoutine = logRoutine;
		this.logMarkers = logMarkers;
		this.logNavigation = logNavigation;
		this.logActions = logActions;
	}

	/*
	 * Gibt zurück, ob Debug für diese NPC-Definition grundsätzlich aktiviert ist.
	 */
	public boolean enabled() {
		return enabled;
	}

	/*
	 * Gibt zurück, ob Routine-Logs für diese NPC-Definition erlaubt sind.
	 */
	public boolean logRoutine() {
		return logRoutine;
	}

	/*
	 * Gibt zurück, ob Marker-Logs für diese NPC-Definition erlaubt sind.
	 */
	public boolean logMarkers() {
		return logMarkers;
	}

	/*
	 * Gibt zurück, ob Navigations-Logs für diese NPC-Definition erlaubt sind.
	 */
	public boolean logNavigation() {
		return logNavigation;
	}

	/*
	 * Gibt zurück, ob Action-Logs für diese NPC-Definition erlaubt sind.
	 */
	public boolean logActions() {
		return logActions;
	}
}