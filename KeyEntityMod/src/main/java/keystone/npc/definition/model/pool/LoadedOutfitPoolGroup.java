package keystone.npc.definition.model.pool;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * LoadedOutfitPoolGroup beschreibt eine geladene OutfitPool-Gruppe aus resources.
 *
 * Diese Klasse sagt:
 * Für welches OutfitTheme sind welche OutfitPools erlaubt?
 *
 * Beispiel:
 * poolGroupId = human_outfit_pool_group
 *
 * entriesByTheme:
 * sand -> sand_worker_outfit_pool, sand_trader_outfit_pool
 * forest -> forest_worker_outfit_pool, forest_trader_outfit_pool
 *
 * Wichtig:
 * Diese Klasse würfelt kein Outfit aus.
 * Sie wählt keine Kleidung.
 * Sie speichert keine state.json-Daten.
 * Sie hält nur Bauplan-Daten aus resources.
 */
public final class LoadedOutfitPoolGroup {

	private final String poolGroupId;						// Eindeutige ID der OutfitPoolGroup.
	private final Map<String, List<String>> entriesByTheme;	// OutfitTheme -> Liste von OutfitPool-IDs oder OutfitPool-Pfaden.

	/*
	 * Erstellt eine geladene OutfitPoolGroup.
	 */
	public LoadedOutfitPoolGroup(
		String poolGroupId,							// ID der PoolGroup.
		Map<String, List<String>> entriesByTheme		// Zuordnung OutfitTheme -> OutfitPools.
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
	 * Gibt alle OutfitPool-Einträge nach Theme zurück.
	 */
	public Map<String, List<String>> entriesByTheme() {
		return entriesByTheme;
	}

	/*
	 * Gibt die OutfitPools für ein bestimmtes OutfitTheme zurück.
	 */
	public List<String> entriesForTheme(String outfitTheme) {
		String checkedOutfitTheme = cleanOptionalText(outfitTheme);

		if (checkedOutfitTheme == null) {
			return List.of();
		}

		return entriesByTheme.getOrDefault(checkedOutfitTheme, List.of());
	}

	/*
	 * Prüft, ob diese PoolGroup Einträge für ein OutfitTheme besitzt.
	 */
	public boolean hasTheme(String outfitTheme) {
		String checkedOutfitTheme = cleanOptionalText(outfitTheme);

		if (checkedOutfitTheme == null) {
			return false;
		}

		return entriesByTheme.containsKey(checkedOutfitTheme);
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
			String theme = requireText(entry.getKey(), "outfitTheme");
			List<String> poolEntries = copyPoolEntries(entry.getValue(), "entriesByTheme[" + theme + "]");

			if (copy.containsKey(theme)) {
				throw new IllegalArgumentException("duplicate outfitTheme in OutfitPoolGroup: " + theme);
			}

			if (poolEntries.isEmpty()) {
				throw new IllegalArgumentException("OutfitPoolGroup theme has no entries: " + theme);
			}

			copy.put(theme, poolEntries);
		}

		return Collections.unmodifiableMap(copy);
	}

	/*
	 * Kopiert eine OutfitPool-Liste sicher und verhindert leere oder doppelte Einträge.
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