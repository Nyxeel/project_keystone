package keystone.npc.definition.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * NpcMarkerDefinition beschreibt die logischen Marker-Regeln einer NPC-Definition.
 *
 * Diese Klasse enthält keine echten Marker-IDs und keine Koordinaten.
 * Sie beschreibt nur, welche Marker eine RoleId später braucht oder benutzen darf.
 *
 * Beispiel:
 * requiredMarkers:
 * - bed
 * - work
 * - door
 *
 * markerRoles:
 * bed  -> SLEEP
 * work -> WORK
 * door -> DOOR
 *
 * Wichtig:
 * - Kein MarkerRecord.
 * - Keine MarkerAssignments.
 * - Keine Weltposition.
 * - Keine state.json-Daten.
 * - Keine automatische Marker-Suche.
 */
public final class NpcMarkerDefinition {

	private final List<String> requiredMarkers;	// Pflichtmarker, ohne die der NPC später nicht sauber starten darf.
	private final Map<String, String> markerRoles;	// MarkerName -> MarkerType/MarkerRole als Text, z. B. bed -> SLEEP.
	private final List<String> routineMarkers;	// Marker, die der Tagesablauf benutzen darf.
	private final List<String> eventMarkers;		// Marker, die Event-Systeme benutzen dürfen.
	private final List<String> safetyMarkers;		// Marker für Flucht, Schutz oder sichere Orte.
	private final List<String> optionalMarkers;	// Marker, die fehlen dürfen.

	/*
	 * Erstellt die Marker-Regeln für einen NPC-Bauplan.
	 */
	public NpcMarkerDefinition(
		List<String> requiredMarkers,
		Map<String, String> markerRoles,
		List<String> routineMarkers,
		List<String> eventMarkers,
		List<String> safetyMarkers,
		List<String> optionalMarkers
	) {
		this.requiredMarkers = copyMarkerNames(requiredMarkers, "requiredMarkers");
		this.markerRoles = copyMarkerRoles(markerRoles);
		this.routineMarkers = copyMarkerNames(routineMarkers, "routineMarkers");
		this.eventMarkers = copyMarkerNames(eventMarkers, "eventMarkers");
		this.safetyMarkers = copyMarkerNames(safetyMarkers, "safetyMarkers");
		this.optionalMarkers = copyMarkerNames(optionalMarkers, "optionalMarkers");

		validateRequiredMarkerRoles(this.requiredMarkers, this.markerRoles);
	}

	/*
	 * Gibt alle Pflichtmarker zurück.
	 */
	public List<String> requiredMarkers() {
		return requiredMarkers;
	}

	/*
	 * Gibt die Marker-Rollen zurück.
	 */
	public Map<String, String> markerRoles() {
		return markerRoles;
	}

	/*
	 * Gibt alle Marker zurück, die die Routine benutzen darf.
	 */
	public List<String> routineMarkers() {
		return routineMarkers;
	}

	/*
	 * Gibt alle Marker zurück, die Events benutzen dürfen.
	 */
	public List<String> eventMarkers() {
		return eventMarkers;
	}

	/*
	 * Gibt alle Sicherheitsmarker zurück.
	 */
	public List<String> safetyMarkers() {
		return safetyMarkers;
	}

	/*
	 * Gibt alle optionalen Marker zurück.
	 */
	public List<String> optionalMarkers() {
		return optionalMarkers;
	}

	/*
	 * Prüft, ob ein Marker irgendwo in dieser Definition bekannt ist.
	 */
	public boolean hasMarker(String markerName) {
		String checkedMarkerName = cleanOptionalText(markerName);

		if (checkedMarkerName == null) {
			return false;
		}

		return requiredMarkers.contains(checkedMarkerName)
			|| routineMarkers.contains(checkedMarkerName)
			|| eventMarkers.contains(checkedMarkerName)
			|| safetyMarkers.contains(checkedMarkerName)
			|| optionalMarkers.contains(checkedMarkerName)
			|| markerRoles.containsKey(checkedMarkerName);
	}

	/*
	 * Gibt die Marker-Rolle für einen Marker zurück.
	 */
	public String markerRoleFor(String markerName) {
		String checkedMarkerName = cleanOptionalText(markerName);

		if (checkedMarkerName == null) {
			return null;
		}

		return markerRoles.get(checkedMarkerName);
	}

	/*
	 * Kopiert Marker-Namen sicher und verhindert leere oder doppelte Namen.
	 */
	private static List<String> copyMarkerNames(List<String> markerNames, String fieldName) {
		if (markerNames == null || markerNames.isEmpty()) {
			return List.of();
		}

		Map<String, Boolean> seen = new LinkedHashMap<>();

		for (String markerName : markerNames) {
			String checkedMarkerName = requireText(markerName, fieldName + " entry");

			if (seen.containsKey(checkedMarkerName)) {
				throw new IllegalArgumentException(fieldName + " contains duplicate marker: " + checkedMarkerName);
			}

			seen.put(checkedMarkerName, Boolean.TRUE);
		}

		return List.copyOf(seen.keySet());
	}

	/*
	 * Kopiert Marker-Rollen sicher und verhindert leere oder doppelte Marker-Keys.
	 */
	private static Map<String, String> copyMarkerRoles(Map<String, String> markerRoles) {
		if (markerRoles == null || markerRoles.isEmpty()) {
			return Map.of();
		}

		Map<String, String> copy = new LinkedHashMap<>();

		for (Map.Entry<String, String> entry : markerRoles.entrySet()) {
			String markerName = requireText(entry.getKey(), "markerRoles key");
			String markerRole = requireText(entry.getValue(), "markerRoles value");

			if (copy.containsKey(markerName)) {
				throw new IllegalArgumentException("markerRoles contains duplicate marker: " + markerName);
			}

			copy.put(markerName, markerRole);
		}

		return Collections.unmodifiableMap(copy);
	}

	/*
	 * Prüft, ob jeder Pflichtmarker auch eine Marker-Rolle besitzt.
	 */
	private static void validateRequiredMarkerRoles(List<String> requiredMarkers, Map<String, String> markerRoles) {
		for (String requiredMarker : requiredMarkers) {
			if (!markerRoles.containsKey(requiredMarker)) {
				throw new IllegalArgumentException("required marker has no marker role: " + requiredMarker);
			}
		}
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