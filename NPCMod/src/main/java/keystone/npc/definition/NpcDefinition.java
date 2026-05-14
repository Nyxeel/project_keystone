package keystone.npc.definition;

import java.util.Objects;

import keystone.npc.KeystoneNpcPlugin;

/*
 * NpcDefinition ist aktuell die Skeleton-Schicht für NPC-Baupläne.
 *
 * Später lädt diese Klasse NPC-Definitionen aus JSON.
 * Aktuell speichert sie noch keine Definitionen und spawnt keine NPCs.
 *
 * Wichtig:
 * Diese Klasse darf keine Runtime-Entity, keine EntityRef und keine Navigation speichern.
 */
public final class NpcDefinition {

    private final KeystoneNpcPlugin plugin;

    /*
     * Erstellt die Definitions-Schicht mit Zugriff auf das Plugin.
     * Der Nullcheck verhindert, dass die Klasse später mit kaputtem Plugin weiterläuft.
     */
    public NpcDefinition(KeystoneNpcPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null");
    }

    /*
     * Lädt später alle NPC-Definitionen aus JSON.
     * Im Skeleton ist diese Methode noch leer.
     */
    public void loadDefinitions() {
        // TODO: NPC-Definitionen laden.
        // Prüft später hytaleRole, roleId, requiredMarkers und markerRoles.
    }

    /*
     * Lädt später NPC-Definitionen neu.
     * Im Skeleton macht diese Methode noch nichts.
     */
    public void reloadDefinitions() {
        // TODO: Definitionen neu laden.
    }

    /*
     * Prüft später, ob eine Rolle spawnbar ist.
     * Im Skeleton ist noch keine Rolle spawnbar.
     */
    public boolean isSpawnable(String roleId) {
        if (roleId == null || roleId.isBlank()) {
            return false;
        }

        // TODO: Prüfen, ob Rolle gültig spawnbar ist.
        return false;
    }

    /*
     * Gibt das Plugin zurück, falls spätere Definition-Logik Zugriff darauf braucht.
     */
    public KeystoneNpcPlugin plugin() {
        return plugin;
    }
}