package keystone.npc.world;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

import keystone.npc.KeystoneNpcPlugin;

/*
 * WorldManager ist die zentrale Stelle für Welt-Erkennung.
 *
 * Aufgabe:
 * - WorldKey erzeugen
 * - Welt-Namen sicher machen
 * - später Hytale World/API-Daten auswerten
 * - später prüfen, ob NPC und Marker in derselben Welt sind
 *
 * Wichtig:
 * Diese Klasse macht noch kein Worldgen.
 * Diese Klasse lädt keine Chunks.
 * Diese Klasse spawnt keine NPCs.
 * Diese Klasse speichert keine Runtime-Entity und keine EntityRef.
 */
public final class WorldManager {

    private final KeystoneNpcPlugin plugin;
   	private Map<String, WorldData> worldData = Map.of();

    /*
     * Erstellt den WorldManager.
     * Das Plugin wird behalten, weil spätere Hytale-API-Zugriffe darüber laufen können.
     */
    public WorldManager(KeystoneNpcPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null");
    }

    /*
     * Bereitet den WorldManager vor.
     * Im Skeleton gibt es noch nichts zu laden.
     */
   			// Eine World ist NICHT das ganze Universe.
			// Eine World ist z. B.:
			//
			// - lobby
			// - farmwelt
			// - bauwelt
			// - dungeon_instance_001
			// jede World hat eigene WorlUuid
			//	Map<String, World> worldsfd
			//	Map<UUID, World> worldsByUuid


	public void prepare() {


		Map<String, World> worlds = Universe.get().getWorlds();
		Map<String, WorldData> localWorldData = new LinkedHashMap<>();

		for (World world : worlds.values()) {

			// get WorldUuid
			UUID uuid = world.getWorldConfig().getUuid();

			//Duplicate check!
			if (localWorldData.containsKey(uuid.toString())) {
				throw new IllegalStateException("Duplicate WorldUuid found: " + uuid);
			}

			// Extract World Data for each World
			WorldData extractedWorldData  = WorldData.fromWorld(uuid.toString(), world);
			localWorldData.put(uuid.toString(), extractedWorldData);
		}
		this.worldData = Map.copyOf(localWorldData);
	}


	/*	this.worldKeys = Map.copyOf(loadedWorldKeys);
	if (this.worldKeys.isEmpty()) {
		throw new IllegalStateException("No worlds found — aborting WorldManager.prepare()");
	} */
	// Wichtig:
	// Hier keine Chunks laden.
	// Hier keine NPCs spawnen.
	// Hier nur bekannte Worlds merken.


	//get worlddata
	public Map<String, WorldData> worldData() {
		return worldData;
	}

	public WorldData getWorldData(String worldKey) {
    	return worldData.get(worldKey);
	}


	public Set<String> worldKeys() {
		return worldData.keySet();
	}

	//service.worldManager.getWorldData(worldKey).getSavePath()
















   /*  this.worldKey = getWorldKeyFromAPI();

	if (this.worldKey == null || this.worldKey.isBlank()) {
       	throw new IllegalStateException("worldKey could not be resolved — aborting WorldManager.prepare()");
    }

	//Holt den Servername
	this.worldName = getWorldNameFromAPI();
	if (this.worldName == null || this.worldName.isBlank()) {
       	throw new IllegalStateException("worldName could not be resolved — aborting WorldManager.prepare()");
    }
 	*/




    /*
     * Prüft, ob ein NPC und ein Marker zur selben Welt gehören.
     * Beide Werte sind hier einfache worldKey-Strings.
     */
    public boolean isSameWorld(String npcWorldKey, String markerWorldKey) {
        if (npcWorldKey == null || npcWorldKey.isBlank()) {
            return false;
        }

        if (markerWorldKey == null || markerWorldKey.isBlank()) {
            return false;
        }

        return npcWorldKey.trim().equals(markerWorldKey.trim());
    }

    /*
     * Gibt das Plugin zurück, falls spätere World-API-Zugriffe nötig sind.
     */
    public KeystoneNpcPlugin plugin() {
        return plugin;
    }
}