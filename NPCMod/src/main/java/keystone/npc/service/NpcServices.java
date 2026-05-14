package keystone.npc.service;

import keystone.npc.KeystoneNPCPlugin;
import keystone.npc.command.NpcCommands;
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

public final class NpcServices {

	private final MarkerRegistry markerRegistry;      // Speichert/kennt alle Marker: markerId, Typ, Welt, Position.
	private final NpcStateStore stateStore;           // Lädt und speichert NPC-State, später pro Welt: state.json.
	private final NpcDefinition definition;           // Lädt und prüft NPC-Baupläne aus JSON: Rollen, Marker, Profile.
	private final MarkerAssignment markerAssignment;  // Weist Marker sicher konkreten NPCs zu und prüft Typ/Welt/Erlaubnis.
	private final NpcSpawn spawn;                     // Erstellt neue NPCs kontrolliert und mit Save-/Rollback-Schutz.
	private final NpcRelink relink;                   // Verbindet NPC-Records nach Restart wieder mit Live-Entities.
	private final NpcRespawn respawn;                 // Ersetzt fehlende NPC-Entities nur nach strengen Safety-Checks.
	private final NpcRemoval removal;                 // Entfernt NPCs sicher, ohne falsche Entities oder Records zu löschen.
	private final NpcTick tick;                       // Führt pro Tick NPC-Logik aus, aber nur bei gültiger EntityRef.
	private final NpcNavigation navigation;           // Verwaltet Bewegung, Ziele, Runtime-Navigation und später Door-Routen.
	private final NpcCommands commands;               // Registriert Commands und leitet sie nur an die passenden Services weiter.


	//Konstruktor
    private NpcServices(
            MarkerRegistry markerRegistry,
            NpcStateStore stateStore,
            NpcDefinition definition,
            MarkerAssignment markerAssignment,
            NpcSpawn spawn,
            NpcRelink relink,
            NpcRespawn respawn,
            NpcRemoval removal,
            NpcTick tick,
            NpcNavigation navigation,
            NpcCommands commands
    ) {
        this.markerRegistry = markerRegistry;
        this.stateStore = stateStore;
        this.definition = definition;
        this.markerAssignment = markerAssignment;
        this.spawn = spawn;
        this.relink = relink;
        this.respawn = respawn;
        this.removal = removal;
        this.tick = tick;
        this.navigation = navigation;
        this.commands = commands;
    }


    public static NpcServices create(KeystoneNPCPlugin plugin) {

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
                markerRegistry,
                stateStore,
                definition,
                markerAssignment,
                spawn,
                relink,
                respawn,
                removal,
                tick,
                navigation,
                commands
        );
    }

    public MarkerRegistry markerRegistry() {
        return markerRegistry;
    }

    public NpcStateStore stateStore() {
        return stateStore;
    }

    public NpcDefinition definition() {
        return definition;
    }

    public MarkerAssignment markerAssignment() {
        return markerAssignment;
    }

    public NpcSpawn spawn() {
        return spawn;
    }

    public NpcRelink relink() {
        return relink;
    }

    public NpcRespawn respawn() {
        return respawn;
    }

    public NpcRemoval removal() {
        return removal;
    }

    public NpcTick tick() {
        return tick;
    }

    public NpcNavigation navigation() {
        return navigation;
    }

    public NpcCommands commands() {
        return commands;
    }

    public void shutdown() {
        tick.stop();
        navigation.clearRuntimeNavigation();
        stateStore.saveStateSafely();
    }
}