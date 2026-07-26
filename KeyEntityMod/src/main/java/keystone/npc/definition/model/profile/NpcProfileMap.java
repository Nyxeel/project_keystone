package keystone.npc.definition.model.profile;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/*
 * NpcProfileMap sammelt alle Profil-Verweise einer NPC-Definition.
 *
 * Diese Klasse enthält keine festen Felder wie routine, actions oder movement.
 * Stattdessen nutzt sie eine Map.
 *
 * Dadurch können später neue Profiltypen ergänzt werden,
 * ohne dass diese Klasse jedes Mal umgebaut werden muss.
 */
public final class NpcProfileMap {

	private final Map<String, NpcProfile> profiles; // Alle Profil-Verweise, Key = Profilname, Value = einzelner Profil-Verweis.

	/*
	 * Erstellt eine sichere Kopie aller Profil-Verweise.
	 */
	public NpcProfileMap(
		Map<String, NpcProfile> profiles // Profil-Map aus der geladenen NPC-Definition.
	) {
		this.profiles = copyProfiles(profiles);
	}

	/*
	 * Gibt alle Profile als unveränderliche Map zurück.
	 */
	public Map<String, NpcProfile> profiles() {
		return profiles;
	}

	/*
	 * Gibt einen einzelnen Profil-Verweis über seinen Profil-Key zurück.
	 */
	public NpcProfile get(String profileKey) {
		if (profileKey == null || profileKey.isBlank()) {
			return null;
		}

		return profiles.get(profileKey.trim());
	}

	/*
	 * Prüft, ob ein Profil-Key vorhanden ist.
	 */
	public boolean has(String profileKey) {
		if (profileKey == null || profileKey.isBlank()) {
			return false;
		}

		return profiles.containsKey(profileKey.trim());
	}

	/*
	 * Gibt zurück, ob keine Profile eingetragen sind.
	 */
	public boolean isEmpty() {
		return profiles.isEmpty();
	}

	/*
	 * Gibt die Anzahl der eingetragenen Profile zurück.
	 */
	public int size() {
		return profiles.size();
	}

	/*
	 * Kopiert die Profile sicher, damit niemand von außen die interne Map verändern kann.
	 */
	private static Map<String, NpcProfile> copyProfiles(Map<String, NpcProfile> profiles) {
		if (profiles == null || profiles.isEmpty()) {
			return Map.of();
		}

		Map<String, NpcProfile> copy = new LinkedHashMap<>();

		for (Map.Entry<String, NpcProfile> entry : profiles.entrySet()) {
			String key = requireProfileKey(entry.getKey());
			NpcProfile value = Objects.requireNonNull(entry.getValue(), "profile must not be null");

			if (!key.equals(value.profileKey())) {
				throw new IllegalArgumentException("profile map key does not match profile.profileKey: " + key + " != " + value.profileKey());
			}

			if (copy.containsKey(key)) {
				throw new IllegalArgumentException("duplicate profile key: " + key);
			}

			copy.put(key, value);
		}

		return Collections.unmodifiableMap(copy);
	}

	/*
	 * Prüft und bereinigt einen Profil-Key.
	 */
	private static String requireProfileKey(String profileKey) {
		if (profileKey == null || profileKey.isBlank()) {
			throw new IllegalArgumentException("profileKey must not be null or blank");
		}

		return profileKey.trim();
	}
}