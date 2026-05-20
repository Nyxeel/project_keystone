package keystone.npc.definition.model;

import java.util.Objects;

/*
 * LoadedNpcDefinition ist der fertige NPC-Bauplan nach dem Laden aus den Resource-JSONs.
 *
 * Diese Klasse beschreibt keine echte NPC-Instanz in der Welt.
 * Sie enthält nur Definition-Daten aus resources.
 *
 * Wichtig:
 * Kein npcId.
 * Keine EntityRef.
 * Keine RuntimeNpc.
 * Keine state.json-Daten.
 * Keine zufällig ausgewählten Pool-Ergebnisse.
 */
public final class LoadedNpcDefinition {

	private final String namespacedRoleId;
	private final String localRoleId;
	private final String groupId;
	private final String namespace;
	private final NpcEngineDefinition engine;
	private final NpcDisplayDefinition display;
	private final NpcProfile profileRefs;
	private final NpcMarkerDefinition markers;
	private final NpcDebugDefinition debug;
	private final String sourcePath;

	/*
	 * Erstellt einen geladenen NPC-Bauplan aus bereits geprüften Definition-Daten.
	 */
	public LoadedNpcDefinition(

		String namespacedRoleId, 		// Eindeutige RoleId mit Namespace, z. B. keystone:lumberjack.
		String localRoleId, 			// Lokale RoleId ohne Namespace, z. B. lumberjack.
		String groupId, 				// Id der Group-Datei, aus der diese Definition kommt.
		String namespace, 				// Namespace der Mod oder Resource, z. B. keystone.
		NpcEngineDefinition engine, 	// Hytale-Engine-Anbindung, z. B. echte HytaleRole.
		NpcDisplayDefinition display, 	// Anzeige-Daten, z. B. Fallback-Name oder Translation-Key.
		NpcProfile profileRefs, 	// Verweise auf Profile wie Routine, Actions, Movement.
		NpcMarkerDefinition markers, 	// Marker-Regeln wie requiredMarkers und markerRoles.
		NpcDebugDefinition debug, 		// Debug-Schalter für diese NPC-Definition.
		String sourcePath 				// Optionaler Resource-Pfad für Fehlermeldungen oder Diagnose.

	) {
		this.namespacedRoleId = requireText(namespacedRoleId, "namespacedRoleId");
		this.localRoleId = requireText(localRoleId, "localRoleId");
		this.groupId = requireText(groupId, "groupId");
		this.namespace = requireText(namespace, "namespace");
		this.engine = Objects.requireNonNull(engine, "engine must not be null");
		this.display = Objects.requireNonNull(display, "display must not be null");
		this.profileRefs = Objects.requireNonNull(profileRefs, "profileRefs must not be null");
		this.markers = Objects.requireNonNull(markers, "markers must not be null");
		this.debug = Objects.requireNonNull(debug, "debug must not be null");
		this.sourcePath = cleanOptionalText(sourcePath);
	}

	/*
	 * Gibt die eindeutige RoleId mit Namespace zurück, zum Beispiel keystone:lumberjack.
	 */
	public String namespacedRoleId() {
		return namespacedRoleId;
	}

	/*
	 * Gibt die lokale RoleId ohne Namespace zurück, zum Beispiel lumberjack.
	 */
	public String localRoleId() {
		return localRoleId;
	}

	/*
	 * Gibt die Group zurück, aus der diese Definition geladen wurde.
	 */
	public String groupId() {
		return groupId;
	}

	/*
	 * Gibt den Namespace der Definition zurück, zum Beispiel keystone.
	 */
	public String namespace() {
		return namespace;
	}

	/*
	 * Gibt die Engine-Anbindung zurück, zum Beispiel die echte HytaleRole.
	 */
	public NpcEngineDefinition engine() {
		return engine;
	}

	/*
	 * Gibt Anzeige-Daten zurück, zum Beispiel Fallback-Name oder Translation-Key.
	 */
	public NpcDisplayDefinition display() {
		return display;
	}

	/*
	 * Gibt alle Profil-Verweise zurück, zum Beispiel Routine, Actions und Movement.
	 */
	public NpcProfile profileRefs() {
		return profileRefs;
	}

	/*
	 * Gibt Marker-Regeln der Definition zurück, zum Beispiel requiredMarkers.
	 */
	public NpcMarkerDefinition markers() {
		return markers;
	}

	/*
	 * Gibt Debug-Regeln dieser Definition zurück.
	 */
	public NpcDebugDefinition debug() {
		return debug;
	}

	/*
	 * Gibt optional den Resource-Pfad zurück, aus dem diese Definition kam.
	 */
	public String sourcePath() {
		return sourcePath;
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