package keystone.npc.state;

import keystone.npc.KeystoneNPCPlugin;

public final class NpcStateStore {

    private final KeystoneNPCPlugin plugin;

    private final StatePathResolver pathResolver;
    private final StateFileIO fileIO;
    private final StateJsonCodec jsonCodec;
    private final StateBackupStore backupStore;
    private final WorldStateStore worldStateStore;

    private boolean dirty;

    public NpcStateStore(KeystoneNPCPlugin plugin) {
        this.plugin = plugin;

        this.pathResolver = new StatePathResolver(plugin);
        this.fileIO = new StateFileIO();
        this.jsonCodec = new StateJsonCodec();
        this.backupStore = new StateBackupStore(pathResolver);

        this.worldStateStore = new WorldStateStore(
                pathResolver,
                fileIO,
                jsonCodec,
                backupStore
        );
    }

    public void prepareBaseDirectories() {
        pathResolver.prepareBaseDirectories();
    }

    public StateLoadResult loadState() {
        // TODO:
        // Später alle bekannten Server-Spielwelten laden.
        // Für MVP eventuell eine Default-Welt oder aktive Welt laden.
        return worldStateStore.loadAllKnownWorlds();
    }

    public StateLoadResult loadWorldState(String worldId) {
        return worldStateStore.loadWorld(worldId);
    }

    public boolean saveStateSafely() {
        StateSaveResult result = worldStateStore.saveAllLoadedWorlds();

        if (result.success()) {
            dirty = false;
            return true;
        }

        dirty = true;
        return false;
    }

    public StateSaveResult saveWorldState(String worldId) {
        StateSaveResult result = worldStateStore.saveWorld(worldId);

        if (!result.success()) {
            dirty = true;
        }

        return result;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public KeystoneNPCPlugin plugin() {
        return plugin;
    }

    public WorldStateStore worldStateStore() {
        return worldStateStore;
    }
}