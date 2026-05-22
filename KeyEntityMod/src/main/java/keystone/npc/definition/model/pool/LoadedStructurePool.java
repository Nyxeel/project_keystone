package keystone.npc.definition.model.pool;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * LoadedStructurePool beschreibt einen geladenen StructurePool aus resources.
 *
 * Diese Klasse sagt:
 * Für welches StructureTheme sind welche Structures oder Prefabs erlaubt?
 *
 * Beispiel:
 * poolId = sand_structure_pool
 * structureTheme = sand
 * entries = sand_house_trader, sand_house_blacksmith, sand_guard_post
 *
 * Wichtig:
 * Diese Klasse platziert kein Gebäude.
 * Sie wählt kein Gebäude aus.
 * Sie fragt kein echtes Biom ab.
 * Sie speichert keine state.json-Daten.
 * Sie hält nur Bauplan-Daten aus resources.
 */
public final class LoadedStructurePool {

	private final String poolId;			// Eindeutige ID dieses StructurePools.
	private final String structureTheme;	// Theme, für das dieser Pool gilt, z. B. sand.
	private final List<String> entries;		// Structure-/Prefab-IDs oder Pfade, die zu diesem Theme passen.

	/*
	 * Erstellt einen geladenen StructurePool.
	 */
	public LoadedStructurePool(
		String poolId,			// ID dieses StructurePools.
		String structureTheme,	// StructureTheme dieses Pools.
		List<String> entries	// Erlaubte Structure-/Prefab-Einträge.
	) {
		this.poolId = requireText(poolId, "poolId");
		this.structureTheme = requireText(structureTheme, "structureTheme");
		this.entries = copyEntries(entries);
	}

	/*
	 * Gibt die ID dieses StructurePools zurück.
	 */
	public String poolId() {
		return poolId;
	}

	/*
	 * Gibt das StructureTheme dieses Pools zurück.
	 */
	public String structureTheme() {
		return structureTheme;
	}

	/*
	 * Gibt alle Structure-/Prefab-Einträge zurück.
	 */
	public List<String> entries() {
		return entries;
	}

	/*
	 * Prüft, ob dieser Pool einen bestimmten Structure-/Prefab-Eintrag enthält.
	 */
	public boolean hasEntry(String entryId) {
		String checkedEntryId = cleanOptionalText(entryId);

		if (checkedEntryId == null) {
			return false;
		}

		return entries.contains(checkedEntryId);
	}

	/*
	 * Gibt zurück, ob dieser Pool keine Einträge besitzt.
	 */
	public boolean isEmpty() {
		return entries.isEmpty();
	}

	/*
	 * Gibt die Anzahl der Einträge zurück.
	 */
	public int size() {
		return entries.size();
	}

	/*
	 * Kopiert die Einträge sicher und verhindert leere oder doppelte Werte.
	 */
	private static List<String> copyEntries(List<String> entries) {
		if (entries == null || entries.isEmpty()) {
			return List.of();
		}

		Map<String, Boolean> seen = new LinkedHashMap<>();

		for (String entry : entries) {
			String checkedEntry = requireText(entry, "entries entry");

			if (seen.containsKey(checkedEntry)) {
				throw new IllegalArgumentException("StructurePool contains duplicate entry: " + checkedEntry);
			}

			seen.put(checkedEntry, Boolean.TRUE);
		}

		return List.copyOf(seen.keySet());
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