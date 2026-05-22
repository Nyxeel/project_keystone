package keystone.npc.definition.model.pool;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * LoadedNamePoolGroup beschreibt eine geladene NamePool-Gruppe aus resources.
 *
 * Diese Klasse sagt:
 * Für welches NameTheme sind welche NamePools erlaubt?
 *
 * Beispiel:
 * poolGroupId = human_name_pool_group
 *
 * entriesByTheme:
 * sand -> sand_male_name_pool, sand_female_name_pool
 * forest -> forest_male_name_pool, forest_female_name_pool
 *
 * Wichtig:
 * Diese Klasse würfelt keinen Namen aus.
 * Sie wählt keinen Anzeigenamen.
 * Sie speichert keine state.json-Daten.
 * Sie hält nur Bauplan-Daten aus resources.
 */
public final class LoadedNamePoolGroup {

	private final String poolGroupId;						// Eindeutige ID der NamePoolGroup.
	private final Map<String, List<String>> entriesByTheme;	// NameTheme -> Liste von NamePool-IDs oder NamePool-Pfaden.

	/*
	 * Erstellt eine geladene NamePoolGroup.
	 */
	public LoadedNamePoolGroup(
		String poolGroupId,							// ID der PoolGroup.
		Map<String, List<String>> entriesByTheme		// Zuordnung NameTheme -> NamePools.
	) {
		this.poolGroupId = requireText(poolGroupId, "poolGroupId");
		this.entriesByTheme = copyEntriesByTheme(entriesByTheme);
	}

	/*
	 * Gibt die ID der PoolGroup zurück.
	 */
	public String poolGroupId() {
		return poolGroupId;
	}

	/*
	 * Gibt alle NamePool-Einträge nach Theme zurück.
	 */
	public Map<String, List<String>> entriesByTheme() {
		return entriesByTheme;
	}

	/*
	 * Gibt die NamePools für ein bestimmtes NameTheme zurück.
	 */
	public List<String> entriesForTheme(String nameTheme) {
		String checkedNameTheme = cleanOptionalText(nameTheme);

		if (checkedNameTheme == null) {
			return List.of();
		}

		return entriesByTheme.getOrDefault(checkedNameTheme, List.of());
	}

	/*
	 * Prüft, ob diese PoolGroup Einträge für ein NameTheme besitzt.
	 */
	public boolean hasTheme(String nameTheme) {
		String checkedNameTheme = cleanOptionalText(nameTheme);

		if (checkedNameTheme == null) {
			return false;
		}

		return entriesByTheme.containsKey(checkedNameTheme);
	}

	/*
	 * Gibt zurück, ob diese PoolGroup keine Einträge besitzt.
	 */
	public boolean isEmpty() {
		return entriesByTheme.isEmpty();
	}

	/*
	 * Kopiert die Theme-Zuordnung sicher und verhindert leere Themes oder leere Pool-Einträge.
	 */
	private static Map<String, List<String>> copyEntriesByTheme(Map<String, List<String>> entriesByTheme) {
		if (entriesByTheme == null || entriesByTheme.isEmpty()) {
			return Map.of();
		}

		Map<String, List<String>> copy = new LinkedHashMap<>();

		for (Map.Entry<String, List<String>> entry : entriesByTheme.entrySet()) {
			String theme = requireText(entry.getKey(), "nameTheme");
			List<String> poolEntries = copyPoolEntries(entry.getValue(), "entriesByTheme[" + theme + "]");

			if (copy.containsKey(theme)) {
				throw new IllegalArgumentException("duplicate nameTheme in NamePoolGroup: " + theme);
			}

			if (poolEntries.isEmpty()) {
				throw new IllegalArgumentException("NamePoolGroup theme has no entries: " + theme);
			}

			copy.put(theme, poolEntries);
		}

		return Collections.unmodifiableMap(copy);
	}

	/*
	 * Kopiert eine NamePool-Liste sicher und verhindert leere oder doppelte Einträge.
	 */
	private static List<String> copyPoolEntries(List<String> poolEntries, String fieldName) {
		if (poolEntries == null || poolEntries.isEmpty()) {
			return List.of();
		}

		Map<String, Boolean> seen = new LinkedHashMap<>();

		for (String poolEntry : poolEntries) {
			String checkedPoolEntry = requireText(poolEntry, fieldName + " entry");

			if (seen.containsKey(checkedPoolEntry)) {
				throw new IllegalArgumentException(fieldName + " contains duplicate entry: " + checkedPoolEntry);
			}

			seen.put(checkedPoolEntry, Boolean.TRUE);
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