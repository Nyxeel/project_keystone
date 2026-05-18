package keystone.npc.bootstrap;

import java.util.Objects;
import java.util.function.Consumer;

import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;
import com.hypixel.hytale.server.npc.AllNPCsLoadedEvent;

import keystone.npc.KeystoneNpcPlugin;
import keystone.npc.service.NpcServices;

public final class NpcPluginBootstrap {

    private final KeystoneNpcPlugin plugin;
    private final Consumer<String> queueInitialRespawn;

    private NpcServices service;


    public NpcPluginBootstrap(KeystoneNpcPlugin plugin, Consumer<String> queueInitialRespawn)
	{
     	this.plugin = Objects.requireNonNull(plugin, "plugin must not be null");
		this.queueInitialRespawn = Objects.requireNonNull(queueInitialRespawn, "queueInitialRespawn must not be null");
    }


    public NpcServices setupNpcMod()
	{
		if (this.service != null) {
    		throw new IllegalStateException("NpcPluginBootstrap setupNpcMod() was already called.");
		}
        System.out.println("[KeystoneNPC] setup...");

		// 1) Services bauen
		// Hier wird die zentrale Service-Schaltzentrale erstellt.
		// NpcServices erzeugt und verbindet alle NPC-Abteilungen:
		// - StateStore für state.json
		// - Definitionen für NPC-Baupläne
		// - MarkerAssignment für Marker-Zuweisungen
		// - Spawn / Relink / Respawn / Removal
		// - Tick / Navigation
		// - Commands
	    createServices();
		

		////////////////////////////////////////////////////////
		// 2) World States aus den state.json laden
 		// Bereitet das World-System vor.
		// Hier wird der gespeicherte Zustand geladen.
		// Also z. B.:
		// - holt Worlds ueber hytale API
		// - welche NPCs existierten vorher?
		// - welche npcId hatten sie?
		// - welche entityUuid hatten sie?
		// - welche Marker waren ihnen zugewiesen?
		// - in welcher Welt waren sie?
		prepareWorldSystem();
		//TODOS in BootstrapJava.md


		// 3) Definitionen laden
		// Hier werden die NPC-Baupläne geladen.
		// Also z. B.:
		// - Welche NPC-Rollen existieren?
		// - Welche Hytale-Role nutzt ein NPC?
		// - Welche Marker braucht ein NPC?
		// - Sind die JSON-Dateien gültig?
		//
		// Wichtig:
		// Das sind nur Baupläne.
		// Hier werden noch keine echten NPCs gespawnt.
        loadDefinitions();




		// 4) Geladene Welt-Daten gegen Definitionen prüfen
		// Hier wird geprüft, ob die bereits geladene state.json
		// noch zu den aktuell geladenen NPC-Bauplänen passt.
		// Also z. B.:
		// - Existiert die gespeicherte roleId noch?
		// - Darf dieser NPC diese Marker verwenden?
		// - Passen markerAssignments zu den requiredMarkers?
		// - Ist die gespeicherte Welt-/NPC-Struktur noch gültig?
		// - Gibt es alte oder ungültige Daten in der state.json?
		//
		// Wichtig:
		// Hier werden echte gespeicherte NPC-Daten nur geprüft.
		// Hier wird noch nichts gespawnt.
		// Hier wird noch kein Relink gemacht.
		// Hier wird noch keine kaputte state.json automatisch repariert oder überschrieben.
		validateStateAgainstDefinitions();





		// 5) Commands registrieren
		// Hier werden Admin-Befehle registriert.
		// Zum Beispiel später:
		// /knpc spawn
		// /knpc remove
		// /knpc marker set
		// /knpc respawn-missing
		//
		// Wichtig:
		// Commands sollen selbst keine tiefe Logik enthalten.
		// Sie sollen nur den passenden Service aufrufen.
        registerCommands();



		// 6) Events registrieren
		// Diese Events sagen: Die Welten oder NPC-Daten der Engine sind jetzt geladen.
		// Erst dann darf ein sicherer Relink/Respawn-Check geplant werden.
		//
		// Warum?
		// Vorher könnten NPC-Entities noch nicht verfügbar sein.
		// Wenn man zu früh respawnt, könnte man aus Versehen Duplikate erzeugen.
		// Wird ausgelöst, wenn alle Welten geladen sind.
		// Dann kann geprüft werden, ob gespeicherte NPCs wiedergefunden werden können.
        registerStartupEvents();


        System.out.println("[KeystoneNPC] setup complete.");
        return service;
    }


    private void createServices()
	{
        this.service = NpcServices.create(plugin);
    }


	private void validateStateAgainstDefinitions()
	{





    }

    private void loadDefinitions()
	{
        service.definition().loadDefinitions();
    }

    private void registerCommands()
	{
        service.commands().registerCommands();
    }


	private void prepareWorldSystem() {
    	service.worldManager().prepare();		// extract serverdata
		service.stateStore().loadWorldState();	//
	}

	private void registerStartupEvents()
	{
    	var worldsRegistration = plugin.getEventRegistry().registerGlobal(AllWorldsLoadedEvent.class, event -> {
        queueInitialRespawn.accept("all-worlds-loaded-event");
    	});

    	if (worldsRegistration == null) {
    	    throw new IllegalStateException("Failed to register AllWorldsLoadedEvent.");
    	}

    	var npcsRegistration = plugin.getEventRegistry().registerGlobal(AllNPCsLoadedEvent.class, event -> {
    	    queueInitialRespawn.accept("all-npcs-loaded-event");
    	});

    	if (npcsRegistration == null) {
    	    worldsRegistration.unregister();
    	    throw new IllegalStateException("Failed to register AllNPCsLoadedEvent.");
    	}
	}
}