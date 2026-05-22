package keystone.npc.definition.model.pool;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * LoadedNamePool beschreibt einen geladenen NamePool aus resources.
 *
 * Diese Klasse sagt:
 * Welche Namens-Einträge gibt es für ein bestimmtes NameTheme?
 *
 * Beispiel:
 * poolId = sand_male_name_pool
 * nameTheme = sand
 * entries = sand_male_name_01, sand_male_name_02
 *
 * Wichtig:
 * Diese Klasse würfelt keinen Namen aus.
 * Sie erzeugt keinen Anzeigenamen.
 * Sie speichert keine state.json-Daten.
 * Sie hält nur Bauplan-Daten aus resources.
 */
public final class LoadedNamePool {

	private final String poolId;		// Eindeutige ID dieses NamePools.
	private final String nameTheme;		// NameTheme dieses Pools, z. B. sand.
	private final List<String> entries;	// NameEntry-IDs oder stabile Namen-Keys in diesem Pool.

	/*
	 * Erstellt einen geladenen NamePool.
	 */
	public LoadedNamePool(
		String poolId,		// ID dieses NamePools.
		String nameTheme,	// NameTheme dieses Pools.
		List<String> entries	// Erlaubte Namens-Einträge.
	) {
		this.poolId = requireText(poolId, "poolId");
		this.nameTheme = requireText(nameTheme, "nameTheme");
		this.entries = copyEntries(entries);
	}

	/*
	 * Gibt die ID dieses NamePools zurück.
	 */
	public String poolId() {
		return poolId;
	}

	/*
	 * Gibt das NameTheme dieses Pools zurück.
	 */
	public String nameTheme() {
		return nameTheme;
	}

	/*
	 * Gibt alle Namens-Einträge zurück.
	 */
	public List<String> entries() {
		return entries;
	}

	/*
	 * Prüft, ob dieser Pool einen bestimmten Namens-Eintrag enthält.
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
				throw new IllegalArgumentException("NamePool contains duplicate entry: " + checkedEntry);
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