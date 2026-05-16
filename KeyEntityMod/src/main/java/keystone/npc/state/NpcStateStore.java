package keystone.npc.state;

import java.util.Objects;

import keystone.npc.KeystoneNpcPlugin;
import keystone.npc.state.internal.StateBackupStore;
import keystone.npc.state.internal.StateFileIO;
import keystone.npc.state.internal.StateJsonCodec;
import keystone.npc.state.internal.StatePathResolver;
import keystone.npc.state.internal.WorldStateStore;
import keystone.npc.world.WorldManager;

/*
 * NpcStateStore ist die obere State-Schicht der Mod.
 *
 * Diese Klasse verbindet die internen State-Hilfsklassen:
 * - StatePathResolver für Pfade
 * - StateFileIO für Datei-Lesen/Schreiben
 * - StateJsonCodec für JSON-Prüfung
 * - StateBackupStore für Backups
 * - WorldStateStore für State pro Welt
 *
 * Wichtig:
 * Diese Klasse speichert keine RuntimeNpc-Objekte.
 * Diese Klasse darf Save-Fehler nicht als Erfolg behandeln.
 */
public final class NpcStateStore {

    private final KeystoneNpcPlugin plugin;

    private final StatePathResolver pathResolver;
    private final StateFileIO fileIO;
    private final StateJsonCodec jsonCodec;
    private final StateBackupStore backupStore;
    private final WorldStateStore worldStateStore;
    private final WorldManager worldManager;

    private boolean dirty;

    /*
     * Erstellt den StateStore und alle internen State-Hilfsschichten.
     */
    public NpcStateStore(KeystoneNpcPlugin plugin, WorldManager worldManager) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null");

        this.pathResolver = new StatePathResolver(plugin);
        this.fileIO = new StateFileIO();
        this.jsonCodec = new StateJsonCodec();
        this.backupStore = new StateBackupStore(pathResolver);
		this.worldManager = worldManager;

        this.worldStateStore = new WorldStateStore(
                pathResolver,
                fileIO,
                jsonCodec,
                backupStore
        );
    }

    /*
     * Bereitet die Basisordner für den NPC-State vor.
     */
    public void prepareBaseDirectories() {
        pathResolver.prepareBaseDirectories();
    }

    /*
     * Lädt später den gesamten bekannten NPC-State.
     * Im Skeleton gibt es noch keinen automatischen World-Scan.
     */
    public StateLoadResult loadState() {

		prepareBaseDirectories();

		String worldKey = worldManager.getWorldKey();
		StateLoadResult result = worldStateStore.loadWorld(worldKey);
        if (!result.success()) {
            dirty = false;
        }
	    return result;
    }

    /*
     * Speichert alle geladenen Welt-States sicher.
     * Nur ein erfolgreicher Save darf dirty wieder auf false setzen.
     */
    public boolean saveStateSafely() {
        if (!dirty) {
            return true;
        }

        StateSaveResult result = worldStateStore.saveAllLoadedWorlds();

        if (result.success()) {
            dirty = false;
            return true;
        }

        dirty = true;
        System.err.println("[KeystoneNPC][STATE_SAVE_FAILED] " + result.message());
        return false;
    }

    /*
     * Speichert den State einer einzelnen Welt.
     * Ein Fehler bleibt sichtbar und setzt dirty auf true.
     */
    public StateSaveResult saveWorldState(String worldKey) {
        StateSaveResult result = worldStateStore.saveWorld(worldKey);

        if (!result.success()) {
            dirty = true;
            System.err.println("[KeystoneNPC][WORLD_STATE_SAVE_FAILED] " + result.message());
        }

        return result;
    }

    /*
     * Markiert den State als verändert.
     * Danach soll saveStateSafely() wirklich speichern.
     */
    public void markDirty() {
        this.dirty = true;
    }

    /*
     * Gibt zurück, ob ungespeicherte Änderungen existieren.
     */
    public boolean isDirty() {
        return dirty;
    }

    /*
     * Gibt das Plugin zurück.
     * Das ist nur Zugriff auf die Plugin-Schicht, kein Runtime-State.
     */
    public KeystoneNpcPlugin plugin() {
        return plugin;
    }

    /*
     * Gibt den internen WorldStateStore zurück.
     * Später sollte direkter Zugriff möglichst selten bleiben.
     */
    public WorldStateStore worldStateStore() {
        return worldStateStore;
    }
	  /*
     * Gibt den internen WorldManager zurück.
     *
     */
    public WorldManager worldManager() {
        return worldManager;
    }
}