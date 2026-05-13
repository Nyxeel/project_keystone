package keystone.npc.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import keystone.npc.domain.NpcEntityStatus;
import keystone.npc.domain.NpcRecord;
import keystone.npc.domain.NpcState;
import keystone.npc.markers.MarkerRecord;
import keystone.npc.markers.MarkerType;
import keystone.npc.markers.Vec3;
import keystone.npc.markers.WorldId;

/**
 * MVP A: simpelstes Persistenz-Skeleton.
 *
 * Speichert Marker + NPC-Daten als JSON auf Disk.
 */
public final class JsonFileStateStore implements StateStore {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path path;
    private final ActiveMarkerIdMapper activeMarkerIdMapper = new ActiveMarkerIdMapper();

    public JsonFileStateStore(String relativePath) {
        this.path = Paths.get(relativePath);
    }

    public static final class LoadResult {
        private final PluginState state;
        private final boolean loadSuccessful;
        private final boolean stateFileMissing;
        private final boolean partialLoad;

        private LoadResult(PluginState state, boolean loadSuccessful, boolean stateFileMissing, boolean partialLoad) {
            this.state = state;
            this.loadSuccessful = loadSuccessful;
            this.stateFileMissing = stateFileMissing;
            this.partialLoad = partialLoad;
        }

        public static LoadResult success(PluginState state, boolean stateFileMissing, boolean partialLoad) {
            return new LoadResult(state, true, stateFileMissing, partialLoad);
        }

        public static LoadResult failure() {
            return new LoadResult(PluginState.empty(), false, false, false);
        }

        public PluginState state() {
            return state;
        }

        public boolean loadSuccessful() {
            return loadSuccessful;
        }

        public boolean stateFileMissing() {
            return stateFileMissing;
        }

        public boolean partialLoad() {
            return partialLoad;
        }
    }

    private static final class ParseFlags {
        private boolean partialLoad;

        private void markPartial() {
            partialLoad = true;
        }

        private boolean partialLoad() {
            return partialLoad;
        }
    }

    public LoadResult loadWithStatus() {
        if (!Files.exists(path)) {
            return LoadResult.success(PluginState.empty(), true, false);
        }

        try {
            String raw = Files.readString(path, StandardCharsets.UTF_8);
            if (raw.isBlank()) {
                System.err.println("[KeystoneNPC][STATE_LOAD_PARSE_ERROR] State file exists but is blank: " + path);
                return LoadResult.failure();
            }

            PersistedState persisted = GSON.fromJson(raw, PersistedState.class);
            if (persisted == null) {
                System.err.println("[KeystoneNPC][STATE_LOAD_PARSE_ERROR] State file produced null payload: " + path);
                return LoadResult.failure();
            }

            ParseFlags parseFlags = new ParseFlags();

            List<MarkerRecord> markers = new ArrayList<>();
            if (persisted.markers() != null) {
                int markerIndex = 0;
                for (PersistedMarker marker : persisted.markers()) {
                    if (marker == null) {
                        parseFlags.markPartial();
                        System.err.println("[KeystoneNPC][STATE_LOAD_MARKER_SKIPPED] Skipping null marker entry at index " + markerIndex + ".");
                    } else {
                        try {
                            markers.add(toMarkerRecord(marker));
                        } catch (IllegalArgumentException | IllegalStateException ex) {
                            parseFlags.markPartial();
                            System.err.println("[KeystoneNPC][STATE_LOAD_MARKER_SKIPPED] Skipping invalid marker entry at index "
                                + markerIndex + ": " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                        }
                    }
                    markerIndex++;
                }
            }

            List<NpcRecord> npcs = new ArrayList<>();
            if (persisted.npcs() != null) {
                int npcIndex = 0;
                for (PersistedNpc npc : persisted.npcs()) {
                    if (npc == null) {
                        parseFlags.markPartial();
                        System.err.println("[KeystoneNPC][STATE_LOAD_NPC_SKIPPED] Skipping null NPC entry at index " + npcIndex + ".");
                    } else {
                        try {
                            npcs.add(toNpcRecord(npc, parseFlags));
                        } catch (IllegalArgumentException | IllegalStateException ex) {
                            parseFlags.markPartial();
                            System.err.println("[KeystoneNPC][STATE_LOAD_NPC_SKIPPED] Skipping invalid NPC entry at index "
                                + npcIndex + ": " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                        }
                    }
                    npcIndex++;
                }
            }

            Map<MarkerType, String> activeMarkerIds = activeMarkerIdMapper.toActiveMarkerIds(persisted.activeMarkerIds());

            return LoadResult.success(new PluginState(markers, npcs, activeMarkerIds), false, parseFlags.partialLoad());
        } catch (JsonParseException | IllegalStateException | IllegalArgumentException e) {
            System.err.println("[KeystoneNPC][STATE_LOAD_PARSE_ERROR] Failed to parse state file: " + path);
            System.err.println("[KeystoneNPC][STATE_LOAD_PARSE_ERROR] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return LoadResult.failure();
        } catch (IOException e) {
            System.err.println("[KeystoneNPC][STATE_LOAD_IO_ERROR] Failed to read state file: " + path);
            System.err.println("[KeystoneNPC][STATE_LOAD_IO_ERROR] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return LoadResult.failure();
        } catch (RuntimeException e) {
            System.err.println("[KeystoneNPC][STATE_LOAD_RUNTIME_ERROR] Failed to load state: " + path);
            System.err.println("[KeystoneNPC][STATE_LOAD_RUNTIME_ERROR] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return LoadResult.failure();
        } catch (LinkageError e) {
            System.err.println("[KeystoneNPC][STATE_LOAD_LINKAGE_ERROR] Failed to load state due to class linkage issue: " + path);
            System.err.println("[KeystoneNPC][STATE_LOAD_LINKAGE_ERROR] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return LoadResult.failure();
        }
    }

    @Override
    public PluginState load() {
        LoadResult loadResult = loadWithStatus();
        if (!loadResult.loadSuccessful()) {
            throw new IllegalStateException("state-load-failed");
        }
        return loadResult.state();
    }

    @Override
    public void save(List<MarkerRecord> markers, List<NpcRecord> npcs, Map<MarkerType, String> activeMarkerIds) {
        try {
            Files.createDirectories(path.getParent() == null ? Paths.get(".") : path.getParent());

            List<PersistedMarker> persistedMarkers = new ArrayList<>(markers.size());
            for (MarkerRecord marker : markers) {
                persistedMarkers.add(toPersistedMarker(marker));
            }

            List<PersistedNpc> persistedNpcs = new ArrayList<>(npcs.size());
            for (NpcRecord npc : npcs) {
                persistedNpcs.add(toPersistedNpc(npc));
            }

            PersistedState persisted = new PersistedState(
                    persistedMarkers,
                    persistedNpcs,
                        activeMarkerIdMapper.toPersistedActiveMarkerIds(activeMarkerIds)
            );

            Files.writeString(path, GSON.toJson(persisted), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            System.err.println("[KeystoneNPC][STATE_SAVE_IO_ERROR] Failed to save state file: " + path);
            System.err.println("[KeystoneNPC][STATE_SAVE_IO_ERROR] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            throw new IllegalStateException("state-save-io-failure", e);
        } catch (RuntimeException e) {
            System.err.println("[KeystoneNPC][STATE_SAVE_RUNTIME_ERROR] Failed to serialize/save state: " + path);
            System.err.println("[KeystoneNPC][STATE_SAVE_RUNTIME_ERROR] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            throw e;
        } catch (LinkageError e) {
            System.err.println("[KeystoneNPC][STATE_SAVE_LINKAGE_ERROR] Save skipped due to classloader/linkage issue: " + path);
            System.err.println("[KeystoneNPC][STATE_SAVE_LINKAGE_ERROR] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            throw e;
        }
    }

    private PersistedMarker toPersistedMarker(MarkerRecord marker) {
        return new PersistedMarker(
                marker.markerId(),
                marker.worldId().value(),
                new PersistedVec3(marker.position().x(), marker.position().y(), marker.position().z()),
                marker.type().name()
        );
    }

    private PersistedNpc toPersistedNpc(NpcRecord npc) {
        Vec3 position = npc.hasKnownCurrentPosition() ? npc.currentPosition() : null;
        NpcState persistedState = normalizePersistedState(npc.state());
        PersistedNavigation navigation = null;
        return new PersistedNpc(
                npc.npcId(),
                npc.npcName(),
                npc.roleId(),
                persistedState.name(),
            npc.entityStatus() == null ? null : npc.entityStatus().name(),
                npc.worldId().value(),
                position == null ? null : new PersistedVec3(position.x(), position.y(), position.z()),
                npc.homeInstanceId(),
                npc.workInstanceId(),
                npc.bedMarkerId(),
                npc.doorMarkerId(),
                npc.chestMarkerId(),
                npc.foodMarkerId(),
                npc.workMarkerId(),
                npc.chillMarkerId(),
                npc.entityUuid(),
                navigation
        );
    }

    private NpcState normalizePersistedState(NpcState state) {
        if (state == null) {
            return NpcState.IDLE;
        }

        return switch (state) {
            case WALKING_TO_DOOR -> NpcState.OPENING_DOOR;
            case WALKING_TO_CHEST -> NpcState.USING_CHEST;
            case WALKING_TO_FOOD -> NpcState.EATING;
            case WALKING_TO_WORK -> NpcState.WORKING;
            case WALKING_TO_CHILL -> NpcState.CHILLING;
            case WALKING_TO_BED -> NpcState.SLEEPING;
            case PAUSED_MISSING_MARKER -> NpcState.IDLE;
            default -> state;
        };
    }

    private MarkerRecord toMarkerRecord(PersistedMarker marker) {
        return new MarkerRecord(
                Objects.requireNonNull(marker.markerId()),
                new WorldId(Objects.requireNonNull(marker.worldId())),
                toVec3(marker.position()),
                MarkerType.valueOf(Objects.requireNonNull(marker.type()))
        );
    }

    private NpcRecord toNpcRecord(PersistedNpc npc, ParseFlags parseFlags) {
        NpcRecord record = new NpcRecord(
                Objects.requireNonNull(npc.npcId()),
                Objects.requireNonNull(npc.npcName()),
            Objects.requireNonNull(npc.role()),
                new WorldId(Objects.requireNonNull(npc.worldId()))
        );

        if (npc.state() != null) {
            try {
                record.state(NpcState.valueOf(npc.state()));
            } catch (IllegalArgumentException ex) {
                parseFlags.markPartial();
                // Keep safe constructor default (IDLE) for unknown persisted states.
                System.err.println("[KeystoneNPC][STATE_LOAD_NPC_STATE_FALLBACK] Invalid persisted NPC state for npcId="
                    + npc.npcId() + " state=" + npc.state() + "; using IDLE.");
            }
        }

        record.entityUuid(npc.entityUuid());

        if (npc.entityStatus() != null) {
            try {
                record.entityStatus(NpcEntityStatus.valueOf(npc.entityStatus()));
            } catch (IllegalArgumentException ex) {
                parseFlags.markPartial();
                System.err.println("[KeystoneNPC] Ignoring invalid persisted entity status: " + npc.entityStatus());
                record.entityStatus(record.entityUuid() == null || record.entityUuid().isBlank()
                    ? NpcEntityStatus.MISSING_ENTITY
                    : NpcEntityStatus.NEEDS_RELINK);
            }
        } else {
            record.entityStatus(record.entityUuid() == null || record.entityUuid().isBlank()
                ? NpcEntityStatus.MISSING_ENTITY
                : NpcEntityStatus.NEEDS_RELINK);
        }

        PersistedVec3 currentPosition = npc.currentPosition();
        if (currentPosition == null) {
            parseFlags.markPartial();
            record.clearCurrentPosition();
            System.err.println("[KeystoneNPC][STATE_LOAD_NPC_POSITION_MISSING] Missing persisted currentPosition for npcId="
                + npc.npcId() + "; auto-respawn position recovery is blocked until explicitly set.");
        } else {
            try {
                record.currentPosition(toVec3(currentPosition));
            } catch (IllegalArgumentException | IllegalStateException ex) {
                parseFlags.markPartial();
                record.clearCurrentPosition();
                System.err.println("[KeystoneNPC][STATE_LOAD_NPC_POSITION_INVALID] Invalid persisted currentPosition for npcId="
                    + npc.npcId() + ": " + ex.getMessage()
                    + "; auto-respawn position recovery is blocked until explicitly set.");
            }
        }

        record.homeInstanceId(npc.homeInstanceId());
        record.workInstanceId(npc.workInstanceId());
        record.bedMarkerId(npc.bedMarkerId());
        record.doorMarkerId(npc.doorMarkerId());
        record.chestMarkerId(npc.chestMarkerId());
        record.foodMarkerId(npc.foodMarkerId());
        record.workMarkerId(npc.workMarkerId());
        record.chillMarkerId(npc.chillMarkerId());

        restorePersistedNavigation(record, npc.navigation());

        // EntityRef intentionally not restored from disk.
        return record;
    }

    private void restorePersistedNavigation(NpcRecord record, PersistedNavigation persistedNavigation) {
        // Restart safety: runtime route state is not trusted across server restarts.
        // Keep persisted identity/state fields, but always reset live navigation/action runtime.
        record.navigationState().clear();
        record.pendingActionId(null);
        record.activeActionId(null);
        record.lastActionNoRestartLog(null);
    }

    private Vec3 toVec3(PersistedVec3 vec3) {
        Objects.requireNonNull(vec3, "persisted-vec3-missing");
        if (!Double.isFinite(vec3.x()) || !Double.isFinite(vec3.y()) || !Double.isFinite(vec3.z())) {
            throw new IllegalArgumentException("persisted-vec3-non-finite");
        }
        return new Vec3(vec3.x(), vec3.y(), vec3.z());
    }
}
