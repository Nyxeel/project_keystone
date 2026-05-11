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

import keystone.npc.domain.NpcRecord;
import keystone.npc.domain.NpcState;
import keystone.npc.navigation.NavigationTarget;
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

    @Override
    public PluginState load() {
        if (!Files.exists(path)) {
            return PluginState.empty();
        }

        try {
            String raw = Files.readString(path, StandardCharsets.UTF_8);
            if (raw.isBlank()) {
                return PluginState.empty();
            }

            PersistedState persisted = GSON.fromJson(raw, PersistedState.class);
            if (persisted == null) {
                return PluginState.empty();
            }

            List<MarkerRecord> markers = new ArrayList<>();
            if (persisted.markers() != null) {
                for (PersistedMarker marker : persisted.markers()) {
                    if (marker != null) {
                        markers.add(toMarkerRecord(marker));
                    }
                }
            }

            List<NpcRecord> npcs = new ArrayList<>();
            if (persisted.npcs() != null) {
                for (PersistedNpc npc : persisted.npcs()) {
                    if (npc != null) {
                        npcs.add(toNpcRecord(npc));
                    }
                }
            }

            Map<MarkerType, String> activeMarkerIds = activeMarkerIdMapper.toActiveMarkerIds(persisted.activeMarkerIds());

            return new PluginState(markers, npcs, activeMarkerIds);
        } catch (JsonParseException | IllegalStateException | IllegalArgumentException e) {
            System.err.println("[KeystoneNPC][STATE_LOAD_PARSE_ERROR] Failed to parse state file: " + path);
            System.err.println("[KeystoneNPC][STATE_LOAD_PARSE_ERROR] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return PluginState.empty();
        } catch (IOException e) {
            System.err.println("[KeystoneNPC][STATE_LOAD_IO_ERROR] Failed to read state file: " + path);
            System.err.println("[KeystoneNPC][STATE_LOAD_IO_ERROR] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return PluginState.empty();
        } catch (RuntimeException e) {
            System.err.println("[KeystoneNPC][STATE_LOAD_RUNTIME_ERROR] Failed to load state: " + path);
            System.err.println("[KeystoneNPC][STATE_LOAD_RUNTIME_ERROR] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return PluginState.empty();
        } catch (LinkageError e) {
            System.err.println("[KeystoneNPC][STATE_LOAD_LINKAGE_ERROR] Failed to load state due to class linkage issue: " + path);
            System.err.println("[KeystoneNPC][STATE_LOAD_LINKAGE_ERROR] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return PluginState.empty();
        }
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
        } catch (RuntimeException e) {
            System.err.println("[KeystoneNPC][STATE_SAVE_RUNTIME_ERROR] Failed to serialize/save state: " + path);
            System.err.println("[KeystoneNPC][STATE_SAVE_RUNTIME_ERROR] " + e.getClass().getSimpleName() + ": " + e.getMessage());
        } catch (LinkageError e) {
            System.err.println("[KeystoneNPC][STATE_SAVE_LINKAGE_ERROR] Save skipped due to classloader/linkage issue: " + path);
            System.err.println("[KeystoneNPC][STATE_SAVE_LINKAGE_ERROR] " + e.getClass().getSimpleName() + ": " + e.getMessage());
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
        Vec3 position = npc.currentPosition();
        PersistedNavigation navigation = toPersistedNavigation(npc.navigationState());
        return new PersistedNpc(
                npc.npcId(),
                npc.npcName(),
                npc.roleId(),
                npc.state().name(),
                npc.worldId().value(),
                position == null ? null : new PersistedVec3(position.x(), position.y(), position.z()),
                npc.homeInstanceId(),
                npc.workInstanceId(),
                npc.bedMarkerId(),
                npc.doorMarkerId(),
                npc.workMarkerId(),
                npc.entityUuid(),
                navigation
        );
    }

    private PersistedNavigation toPersistedNavigation(NavigationTarget navigationState) {
        if (navigationState == null || !navigationState.hasTarget()) {
            return null;
        }

        Vec3 targetPosition = navigationState.getTargetPosition();
        NpcState targetState = navigationState.getTargetState();
        long remainingMs = navigationState.getRemainingTimeMs();

        if (targetPosition == null || targetState == null || remainingMs <= 0) {
            return null;
        }

        return new PersistedNavigation(
                new PersistedVec3(targetPosition.x(), targetPosition.y(), targetPosition.z()),
                targetState.name(),
                remainingMs
        );
    }

    private MarkerRecord toMarkerRecord(PersistedMarker marker) {
        return new MarkerRecord(
                Objects.requireNonNull(marker.markerId()),
                new WorldId(Objects.requireNonNull(marker.worldId())),
                toVec3(marker.position()),
                MarkerType.valueOf(Objects.requireNonNull(marker.type()))
        );
    }

    private NpcRecord toNpcRecord(PersistedNpc npc) {
        NpcRecord record = new NpcRecord(
                Objects.requireNonNull(npc.npcId()),
                Objects.requireNonNull(npc.npcName()),
            Objects.requireNonNull(npc.role()),
                new WorldId(Objects.requireNonNull(npc.worldId()))
        );

        if (npc.state() != null) {
            record.state(NpcState.valueOf(npc.state()));
        }

        if (npc.currentPosition() != null) {
            record.currentPosition(toVec3(npc.currentPosition()));
        }

        record.homeInstanceId(npc.homeInstanceId());
        record.workInstanceId(npc.workInstanceId());
        record.bedMarkerId(npc.bedMarkerId());
        record.doorMarkerId(npc.doorMarkerId());
        record.workMarkerId(npc.workMarkerId());
        record.entityUuid(npc.entityUuid());

        restorePersistedNavigation(record, npc.navigation());

        // EntityRef intentionally not restored from disk.
        return record;
    }

    private void restorePersistedNavigation(NpcRecord record, PersistedNavigation persistedNavigation) {
        if (persistedNavigation == null || persistedNavigation.targetPosition() == null) {
            return;
        }

        if (record.state() == null || !record.state().isWalking()) {
            return;
        }

        if (persistedNavigation.targetState() == null || persistedNavigation.remainingMs() <= 0) {
            return;
        }

        NpcState targetState;
        try {
            targetState = NpcState.valueOf(persistedNavigation.targetState());
        } catch (IllegalArgumentException ex) {
            System.err.println("[KeystoneNPC] Ignoring invalid persisted navigation target state: "
                + persistedNavigation.targetState());
            return;
        }

        Vec3 targetPosition = toVec3(persistedNavigation.targetPosition());
        Vec3 currentPosition = record.currentPosition();
        record.navigationState().resumeNavigation(currentPosition, targetPosition, persistedNavigation.remainingMs(), targetState);
    }

    private Vec3 toVec3(PersistedVec3 vec3) {
        return new Vec3(vec3.x(), vec3.y(), vec3.z());
    }
}
