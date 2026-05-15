package keystone.npc.world;

import java.util.Objects;

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
    public void prepare() {
        // TODO: Später Hytale-Welten / Save-Infos prüfen, wenn API final klar ist.
		/////API fuer worldkey ist bekannt!!
		///
		///
		/// WorldKey.fromAPI() !!!



        // TODO: Keine Welt scannen, keine Chunks laden und keine NPCs spawnen.
    }

    /*
     * Erstellt einen WorldKey aus einem bekannten Welt-Namen.
     * Das ist aktuell der sichere Minimal-Fallback.
     */
    public WorldKey worldKeyFromName(String worldName) {
        return WorldKey.fromName(worldName);
    }

    /*
     * Prüft, ob zwei WorldKeys dieselbe Server-Spielwelt beschreiben.
     */
    public boolean isSameWorld(WorldKey first, WorldKey second) {
        if (first == null || second == null) {
            return false;
        }

        return first.key().equals(second.key());
    }

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
     * Macht einen Welt-Namen sicher für Datei- und Ordnernamen.
     */
    public String sanitizeWorldKey(String value) {
        return WorldKey.sanitize(value);
    }

    /*
     * Gibt das Plugin zurück, falls spätere World-API-Zugriffe nötig sind.
     */
    public KeystoneNpcPlugin plugin() {
        return plugin;
    }
}