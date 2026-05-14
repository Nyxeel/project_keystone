package keystone.npc;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import keystone.npc.bootstrap.NpcPluginBootstrap;
import keystone.npc.service.NpcServices;

/**
 * Main plugin entrypoint for NPCMod / KeystoneNPC.
 *
 * Aufgabe dieser Klasse:
 * - Services erstellen
 * - Definitionen laden
 * - State laden
 * - Commands registrieren
 * - Runtime starten
 * - Shutdown sauber speichern
 *
 * WICHTIG:
 * Keine große NPC-Fachlogik hier einbauen.
 * Fachlogik gehört in NpcServices und die einzelnen NPC-Abteilungen.
 */
public class KeystoneNPCPlugin extends JavaPlugin {

    private NpcServices service;
    private boolean initialRespawnQueued;


    public KeystoneNPCPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    /**
     * Wird beim Registrieren/Konstruieren des Plugins aufgerufen.
     * Hier nur Grundsysteme vorbereiten.
     */
	@Override
	protected void setup() {
	    this.service = new NpcPluginBootstrap(
	            this,
	            this::queueInitialRespawnIfNeeded
	    ).setupNpcMod();
	}

    /**
     * Wird aufgerufen, wenn der Server bereit ist.
     * Hier Runtime-Systeme starten.
     */
    @Override
    protected void start() {
        System.out.println("[KeystoneNPC] start...");

        NpcServices services = requireServices();

        // 1) Tick / Runtime starten
        services.tick().start();

        // 2) Restore/Relink vorbereiten
        services.relink().prepareRelinkAfterStartup();

        // 3) Initialer Respawn/Relink-Check als Sicherheitsnetz
        queueInitialRespawnIfNeeded("plugin-start");

        System.out.println("[KeystoneNPC] started.");
    }

    /**
     * Initialen Respawn nur einmal auslösen.
     * Mehrere Events dürfen nicht mehrfach Ersatzspawns starten.
     */
    private synchronized void queueInitialRespawnIfNeeded(String trigger) {
        if (initialRespawnQueued) {
            System.out.println("[KeystoneNPC] Initial respawn already queued; skipping duplicate trigger " + trigger + ".");
            return;
        }

        initialRespawnQueued = true;

        NpcServices services = requireServices();
        services.respawn().queueInitialRespawnCheck();

        System.out.println("[KeystoneNPC] Initial respawn trigger queued by " + trigger + ".");
    }

    /**
     * Wird beim Server-Shutdown / Plugin-Unload aufgerufen.
     */
    @Override
    protected void shutdown() {
        System.out.println("[KeystoneNPC] shutdown...");

        if (service == null) {
            System.out.println("[KeystoneNPC] shutdown skipped: services were not initialized.");
            return;
        }

        try {
            service.shutdown();
        } catch (RuntimeException e) {
            System.err.println("[KeystoneNPC][PLUGIN_SHUTDOWN_RUNTIME_ERROR] "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
        } catch (LinkageError e) {
            System.err.println("[KeystoneNPC][PLUGIN_SHUTDOWN_LINKAGE_ERROR] "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        System.out.println("[KeystoneNPC] stopped.");
    }

    private NpcServices requireServices() {
        if (service == null) {
            throw new IllegalStateException("NpcServices not initialized");
        }
        return service;
    }
}