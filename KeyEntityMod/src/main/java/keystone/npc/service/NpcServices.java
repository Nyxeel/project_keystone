package keystone.npc.service;

import java.util.Objects;

import keystone.npc.KeystoneNpcPlugin;
import keystone.npc.command.NpcCommands;
import keystone.npc.core.NpcManager;
import keystone.npc.definition.NpcDefinition;
import keystone.npc.lifecycle.NpcRelink;
import keystone.npc.lifecycle.NpcRemoval;
import keystone.npc.lifecycle.NpcRespawn;
import keystone.npc.lifecycle.NpcSpawn;
import keystone.npc.marker.MarkerAssignment;
import keystone.npc.marker.MarkerRegistry;
import keystone.npc.navigation.NpcNavigation;
import keystone.npc.runtime.NpcTick;
import keystone.npc.state.NpcStateStore;
import keystone.npc.world.WorldManager;

/*
 * NpcServices ist die zentrale Service-Schaltzentrale der Mod.
 *
 * Diese Klasse baut alle großen NPC-Systeme einmalig zusammen:
 * - WorldManager
 * - NPC-Manager
 * - Marker-System
 * - StateStore
 * - Definitionen
 * - Spawn / Relink / Respawn / Removal
 * - Tick / Navigation
 * - Commands
 *
 * Wichtig:
 * Diese Klasse soll keine tiefe NPC-Logik selbst ausführen.
 * Sie verbindet nur die Services miteinander.
 */
public final class NpcServices {

	private final WorldManager worldManager;              // Erkennt Server-Spielwelten und erzeugt sichere WorldKeys.
	private final NpcManager npcManager;                  // Verwaltet alle NPC-Records und RuntimeNpc-Daten im RAM.
	private final MarkerRegistry markerRegistry;          // Speichert/kennt alle Marker: markerId, Typ, Welt, Position.
	private final NpcStateStore stateStore;               // Lädt und speichert NPC-State, später pro Welt: state.json.
	private final NpcDefinition definition;               // Lädt und prüft NPC-Baupläne aus JSON: Rollen, Marker, Profile.
	private final MarkerAssignment markerAssignment;      // Weist Marker sicher konkreten NPCs zu und prüft Typ/Welt/Erlaubnis.
	private final NpcSpawn spawn;                         // Erstellt neue NPCs kontrolliert und mit Save-/Rollback-Schutz.
	private final NpcRelink relink;                       // Verbindet NPC-Records nach Restart wieder mit Live-Entities.
	private final NpcRespawn respawn;                     // Ersetzt fehlende NPC-Entities nur nach strengen Safety-Checks.
	private final NpcRemoval removal;                     // Entfernt NPCs sicher, ohne falsche Entities oder Records zu löschen.
	private final NpcTick tick;                           // Führt pro Tick NPC-Logik aus, aber nur bei gültiger EntityRef.
	private final NpcNavigation navigation;               // Verwaltet Bewegung, Ziele, Runtime-Navigation und später Door-Routen.
	private final NpcCommands commands;                   // Registriert Commands und leitet sie nur an die passenden Services weiter.

    /*
     * Erstellt die Service-Schaltzentrale.
     * Alle Services müssen vorhanden sein, sonst wird direkt abgebrochen.
     */
    private NpcServices(
            WorldManager worldManager,
            NpcManager npcManager,
            MarkerRegistry markerRegistry,
            NpcStateStore stateStore,
            NpcDefinition definition,
            MarkerAssignment markerAssignment,
            NpcSpawn spawn,
            NpcRelink relink,
            NpcRespawn respawn,
            NpcRemoval removal,
            NpcNavigation navigation,
            NpcTick tick,
            NpcCommands commands
    ) {
        this.worldManager = Objects.requireNonNull(worldManager, "worldManager must not be null");
        this.npcManager = Objects.requireNonNull(npcManager, "npcManager must not be null");
        this.markerRegistry = Objects.requireNonNull(markerRegistry, "markerRegistry must not be null");
        this.stateStore = Objects.requireNonNull(stateStore, "stateStore must not be null");
        this.definition = Objects.requireNonNull(definition, "definition must not be null");
        this.markerAssignment = Objects.requireNonNull(markerAssignment, "markerAssignment must not be null");
        this.spawn = Objects.requireNonNull(spawn, "spawn must not be null");
        this.relink = Objects.requireNonNull(relink, "relink must not be null");
        this.respawn = Objects.requireNonNull(respawn, "respawn must not be null");
        this.removal = Objects.requireNonNull(removal, "removal must not be null");
        this.navigation = Objects.requireNonNull(navigation, "navigation must not be null");
        this.tick = Objects.requireNonNull(tick, "tick must not be null");
        this.commands = Objects.requireNonNull(commands, "commands must not be null");
    }

    /*
     * Baut alle NPC-Services in sicherer Reihenfolge.
     */
    public static NpcServices create(KeystoneNpcPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin must not be null");

        WorldManager worldManager = new WorldManager(plugin);
        NpcManager npcManager = new NpcManager();
        MarkerRegistry markerRegistry = new MarkerRegistry();
        NpcStateStore stateStore = new NpcStateStore(plugin);
        NpcDefinition definition = new NpcDefinition(plugin);

        MarkerAssignment markerAssignment =
                new MarkerAssignment(markerRegistry, stateStore, definition);

        NpcRelink relink =
                new NpcRelink(stateStore, definition);

        NpcRemoval removal =
                new NpcRemoval(stateStore, markerAssignment);

        NpcSpawn spawn =
                new NpcSpawn(stateStore, definition, markerAssignment, removal);

        NpcRespawn respawn =
                new NpcRespawn(stateStore, definition, relink, spawn);

        NpcNavigation navigation =
                new NpcNavigation(markerAssignment);

        NpcTick tick =
                new NpcTick(stateStore, definition, relink, navigation);

        NpcCommands commands =
                new NpcCommands(spawn, removal, markerAssignment, respawn, stateStore);

        return new NpcServices(
                worldManager,
                npcManager,
                markerRegistry,
                stateStore,
                definition,
                markerAssignment,
                spawn,
                relink,
                respawn,
                removal,
                navigation,
                tick,
                commands
        );
    }

    /*
     * Gibt die Marker-Registry zurück.
     */
    public MarkerRegistry markerRegistry() {
        return markerRegistry;
    }

    /*
     * Gibt den StateStore zurück.
     */
    public NpcStateStore stateStore() {
        return stateStore;
    }

    /*
     * Gibt die NPC-Definitionen zurück.
     */
    public NpcDefinition definition() {
        return definition;
    }

    /*
     * Gibt den MarkerAssignment-Service zurück.
     */
    public MarkerAssignment markerAssignment() {
        return markerAssignment;
    }

    /*
     * Gibt den Spawn-Service zurück.
     */
    public NpcSpawn spawn() {
        return spawn;
    }

    /*
     * Gibt den Relink-Service zurück.
     */
    public NpcRelink relink() {
        return relink;
    }

    /*
     * Gibt den Respawn-Service zurück.
     */
    public NpcRespawn respawn() {
        return respawn;
    }

    /*
     * Gibt den Removal-Service zurück.
     */
    public NpcRemoval removal() {
        return removal;
    }

    /*
     * Gibt den Tick-Service zurück.
     */
    public NpcTick tick() {
        return tick;
    }

    /*
     * Gibt den Navigation-Service zurück.
     */
    public NpcNavigation navigation() {
        return navigation;
    }

    /*
     * Gibt die Command-Schicht zurück.
     */
    public NpcCommands commands() {
        return commands;
    }

    /*
     * Gibt den WorldManager zurück.
     */
    public WorldManager worldManager() {
        return worldManager;
    }

    /*
     * Gibt den NpcManager zurück.
     */
    public NpcManager npcManager() {
        return npcManager;
    }

    /*
     * Stoppt Runtime-Systeme und speichert danach den State.
     * Save-Fehler werden nicht still geschluckt.
     */
    public void shutdown() {
        tick.stop();
        navigation.clearRuntimeNavigation();

        boolean saved = stateStore.saveStateSafely();
        if (!saved) {
            System.err.println("[KeystoneNPC] Failed to save NPC state during shutdown.");
        }
    }
}