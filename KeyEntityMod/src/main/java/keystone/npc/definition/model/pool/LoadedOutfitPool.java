package keystone.npc.definition.model.pool;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * LoadedOutfitPool beschreibt einen geladenen OutfitPool aus resources.
 *
 * Diese Klasse sagt:
 * Welche Outfit-Einträge gibt es für ein bestimmtes OutfitTheme?
 *
 * Beispiel:
 * poolId = sand_worker_outfit_pool
 * outfitTheme = sand
 * entries = sand_worker_robe_01, sand_worker_robe_02
 *
 * Wichtig:
 * OutfitPool enthält Kleidung.
 * Er enthält nicht Körper, Haut, Haare, Gesicht oder Augen.
 *
 * Diese Klasse würfelt kein Outfit aus.
 * Sie zieht dem NPC keine Kleidung an.
 * Sie speichert keine state.json-Daten.
 * Sie hält nur Bauplan-Daten aus resources.
 */
public final class LoadedOutfitPool {

	private final String poolId;			// Eindeutige ID dieses OutfitPools.
	private final String outfitTheme;		// OutfitTheme dieses Pools, z. B. sand.
	private final List<String> entries;		// OutfitEntry-IDs oder OutfitProfile-IDs in diesem Pool.

	/*
	 * Erstellt einen geladenen OutfitPool.
	 */
	public LoadedOutfitPool(
		String poolId,		// ID dieses OutfitPools.
		String outfitTheme,	// OutfitTheme dieses Pools.
		List<String> entries	// Erlaubte Outfit-Einträge.
	) {
		this.poolId = requireText(poolId, "poolId");
		this.outfitTheme = requireText(outfitTheme, "outfitTheme");
		this.entries = copyEntries(entries);
	}

	/*
	 * Gibt die ID dieses OutfitPools zurück.
	 */
	public String poolId() {
		return poolId;
	}

	/*
	 * Gibt das OutfitTheme dieses Pools zurück.
	 */
	public String outfitTheme() {
		return outfitTheme;
	}

	/*
	 * Gibt alle Outfit-Einträge zurück.
	 */
	public List<String> entries() {
		return entries;
	}

	/*
	 * Prüft, ob dieser Pool einen bestimmten Outfit-Eintrag enthält.
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
				throw new IllegalArgumentException("OutfitPool contains duplicate entry: " + checkedEntry);
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