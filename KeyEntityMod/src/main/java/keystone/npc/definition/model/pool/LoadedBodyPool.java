package keystone.npc.definition.model.pool;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * LoadedBodyPool beschreibt einen geladenen BodyPool aus resources.
 *
 * Diese Klasse sagt:
 * Welche Body-Einträge gibt es für ein bestimmtes BodyTheme?
 *
 * Beispiel:
 * poolId = sand_male_body_pool
 * bodyTheme = sand
 * entries = sand_male_01, sand_male_02
 *
 * Wichtig:
 * BodyPool enthält Körper-/Identitätsoptik.
 * Dazu gehören später z. B. Körper, Haut, Haare, Gesicht und Augen.
 *
 * Diese Klasse würfelt keinen Körper aus.
 * Sie erzeugt keinen NPC.
 * Sie speichert keine state.json-Daten.
 * Sie hält nur Bauplan-Daten aus resources.
 */
public final class LoadedBodyPool {

	private final String poolId;		// Eindeutige ID dieses BodyPools.
	private final String bodyTheme;		// BodyTheme dieses Pools, z. B. sand.
	private final List<String> entries;	// BodyEntry-IDs oder BodyProfile-IDs in diesem Pool.

	/*
	 * Erstellt einen geladenen BodyPool.
	 */
	public LoadedBodyPool(
		String poolId,		// ID dieses BodyPools.
		String bodyTheme,	// BodyTheme dieses Pools.
		List<String> entries	// Erlaubte Body-Einträge.
	) {
		this.poolId = requireText(poolId, "poolId");
		this.bodyTheme = requireText(bodyTheme, "bodyTheme");
		this.entries = copyEntries(entries);
	}

	/*
	 * Gibt die ID dieses BodyPools zurück.
	 */
	public String poolId() {
		return poolId;
	}

	/*
	 * Gibt das BodyTheme dieses Pools zurück.
	 */
	public String bodyTheme() {
		return bodyTheme;
	}

	/*
	 * Gibt alle Body-Einträge zurück.
	 */
	public List<String> entries() {
		return entries;
	}

	/*
	 * Prüft, ob dieser Pool einen bestimmten Body-Eintrag enthält.
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
				throw new IllegalArgumentException("BodyPool contains duplicate entry: " + checkedEntry);
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