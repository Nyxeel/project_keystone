package keystone.npc;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import keystone.npc.bootstrap.NpcPluginBootstrap;
import keystone.npc.service.NpcServices;
import keystone.npc.logging.KeyNpcLogger;


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
public class KeystoneNpcPlugin extends JavaPlugin {

    private NpcServices service;
    private boolean initialRespawnQueued;

    /*
     * Erstellt das Plugin mit Hytale-Init-Daten.
     */
    public KeystoneNpcPlugin(@Nonnull JavaPluginInit init) {
        super(Objects.requireNonNull(init, "init must not be null"));
    }

    /*
     * Wird beim Registrieren/Konstruieren des Plugins aufgerufen.
     * Hier werden nur Services vorbereitet und Events registriert.
     */
    @Override
    protected void setup() {
        if (service != null) {
            throw new IllegalStateException("setup() was already called.");
        }

        try {
			this.service = new NpcPluginBootstrap(
                this,
                this::queueInitialRespawnIfNeeded
        	).setupNpcMod();
		} catch (IllegalStateException e) {
			KeyNpcLogger.error("[SETUP FAILED] ", e.getMessage(), e);
			throw e;
		}


	}

    /*
     * Wird aufgerufen, wenn der Server bereit ist.
     * Hier werden Runtime-Systeme gestartet.
     */
    @Override
    protected void start() {
		KeyNpcLogger.info("[LOADING SETUP FINISHED] ", " starting ...");

        NpcServices services = requireServices();

        services.tick().start();
        services.relink().prepareRelinkAfterStartup();

        /*
         * Wichtig:
         * Initialer Relink/Respawn wird nicht blind bei plugin-start gestartet.
         * Er soll über AllWorldsLoadedEvent / AllNPCsLoadedEvent kommen,
         * damit Welten und NPC-Daten der Engine wirklich geladen sind.
         */

		KeyNpcLogger.info("[KEYSTONE STARTED] ", "Keystone Entity Mod is running ... ");
    }

    /*
     * Initialen Respawn nur einmal auslösen.
     * Mehrere Events dürfen nicht mehrfach Ersatzspawns starten.
     */
    private synchronized void queueInitialRespawnIfNeeded(String trigger) {
        String checkedTrigger = requireText(trigger, "trigger");

        if (initialRespawnQueued) {
            System.out.println("[KeystoneNPC] Initial respawn already queued; skipping duplicate trigger "
                    + cFAILEDheckedTrigger + ".");
            return;
        }

        initialRespawnQueued = true;

        NpcServices services = requireServices();
        services.respawn().queueInitialRespawnCheck();

        System.out.println("[KeystoneNPC] Initial respawn trigger queued by " + checkedTrigger + ".");
    }

    /*
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

    /*
     * Gibt die Services zurück oder bricht klar ab, wenn setup() noch nicht gelaufen ist.
     */
    private NpcServices requireServices() {
        if (service == null) {
            throw new IllegalStateException("NpcServices not initialized.");
        }

        return service;
    }

    /*
     * Prüft, ob ein Pflicht-Text vorhanden ist.
     */
    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank.");
        }

        return value;
    }
}