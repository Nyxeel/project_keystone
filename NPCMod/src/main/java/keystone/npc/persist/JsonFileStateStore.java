package keystone.npc.persist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import keystone.npc.model.NpcRecord;
import keystone.npc.model.NpcRole;
import keystone.npc.model.NpcState;
import keystone.npc.world.MarkerRecord;
import keystone.npc.world.MarkerType;
import keystone.npc.world.Vec3;
import keystone.npc.world.WorldId;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * MVP A: simpelstes Persistenz-Skeleton.
 *
 * Speichert Marker + NPC-Daten als JSON auf Disk.
 */
public final class JsonFileStateStore implements StateStore {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path path;

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
            if (persisted.markers != null) {
                for (PersistedMarker marker : persisted.markers) {
                    if (marker != null) {
                        markers.add(toMarkerRecord(marker));
                    }
                }
            }

            List<NpcRecord> npcs = new ArrayList<>();
            if (persisted.npcs != null) {
                for (PersistedNpc npc : persisted.npcs) {
                    if (npc != null) {
                        npcs.add(toNpcRecord(npc));
                    }
                }
            }

            Map<MarkerType, String> activeMarkerIds = toActiveMarkerIds(persisted.activeMarkerIds);

            return new PluginState(markers, npcs, activeMarkerIds);
        } catch (JsonParseException | IllegalStateException | IllegalArgumentException e) {
            System.err.println("[KeystoneNPC] Failed to parse state file: " + path);
            System.err.println("[KeystoneNPC] " + e.getMessage());
            return PluginState.empty();
        } catch (IOException e) {
            System.err.println("[KeystoneNPC] Failed to read state file: " + path);
            System.err.println("[KeystoneNPC] " + e.getMessage());
            return PluginState.empty();
        }
    }

    @Override
    public void save(List<MarkerRecord> markers, List<NpcRecord> npcs, Map<MarkerType, String> activeMarkerIds) {
        try {
            Files.createDirectories(path.getParent() == null ? Paths.get(".") : path.getParent());

            PersistedState persisted = new PersistedState(
                    markers.stream().map(this::toPersistedMarker).toList(),
                    npcs.stream().map(this::toPersistedNpc).toList(),
                    toPersistedActiveMarkerIds(activeMarkerIds)
            );

            Files.writeString(path, GSON.toJson(persisted), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            System.err.println("[KeystoneNPC] Failed to save state file: " + path);
            e.printStackTrace();
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
        return new PersistedNpc(
                npc.npcId(),
                npc.npcName(),
                npc.role().name(),
                npc.state().name(),
                npc.worldId().value(),
                position == null ? null : new PersistedVec3(position.x(), position.y(), position.z()),
                npc.homeInstanceId(),
                npc.workInstanceId(),
                npc.bedMarkerId(),
                npc.doorMarkerId(),
                npc.workMarkerId()
        );
    }

    private MarkerRecord toMarkerRecord(PersistedMarker marker) {
        return new MarkerRecord(
                Objects.requireNonNull(marker.markerId),
                new WorldId(Objects.requireNonNull(marker.worldId)),
                toVec3(marker.position),
                MarkerType.valueOf(Objects.requireNonNull(marker.type))
        );
    }

    private NpcRecord toNpcRecord(PersistedNpc npc) {
        NpcRecord record = new NpcRecord(
                Objects.requireNonNull(npc.npcId),
                Objects.requireNonNull(npc.npcName),
                NpcRole.valueOf(Objects.requireNonNull(npc.role)),
                new WorldId(Objects.requireNonNull(npc.worldId))
        );

        if (npc.state != null) {
            record.state(NpcState.valueOf(npc.state));
        }

        if (npc.currentPosition != null) {
            record.currentPosition(toVec3(npc.currentPosition));
        }

        record.homeInstanceId(npc.homeInstanceId);
        record.workInstanceId(npc.workInstanceId);
        record.bedMarkerId(npc.bedMarkerId);
        record.doorMarkerId(npc.doorMarkerId);
        record.workMarkerId(npc.workMarkerId);

        // EntityRef intentionally not restored from disk.
        return record;
    }

    private Map<String, String> toPersistedActiveMarkerIds(Map<MarkerType, String> activeMarkerIds) {
        Map<String, String> persisted = new java.util.LinkedHashMap<>();
        if (activeMarkerIds == null) {
            return persisted;
        }

        for (var entry : activeMarkerIds.entrySet()) {
            MarkerType type = entry.getKey();
            String markerId = entry.getValue();
            if (type == null || markerId == null || markerId.isBlank()) {
                continue;
            }
            persisted.put(type.name(), markerId);
        }

        return persisted;
    }

    private Map<MarkerType, String> toActiveMarkerIds(Map<String, String> persistedActiveMarkerIds) {
        if (persistedActiveMarkerIds == null) {
            return null;
        }

        Map<MarkerType, String> activeMarkerIds = new EnumMap<>(MarkerType.class);

        for (var entry : persistedActiveMarkerIds.entrySet()) {
            String rawType = entry.getKey();
            String markerId = entry.getValue();
            if (rawType == null || markerId == null || markerId.isBlank()) {
                continue;
            }

            try {
                MarkerType type = MarkerType.valueOf(rawType);
                activeMarkerIds.put(type, markerId);
            } catch (IllegalArgumentException ex) {
                System.err.println("[KeystoneNPC] Ignoring unknown active marker type in state: " + rawType);
            }
        }

        return activeMarkerIds;
    }

    private Vec3 toVec3(PersistedVec3 vec3) {
        return new Vec3(vec3.x, vec3.y, vec3.z);
    }

    private record PersistedState(
            List<PersistedMarker> markers,
            List<PersistedNpc> npcs,
            Map<String, String> activeMarkerIds
    ) {
    }

    private record PersistedMarker(String markerId, String worldId, PersistedVec3 position, String type) {
    }

    private record PersistedNpc(
            String npcId,
            String npcName,
            String role,
            String state,
            String worldId,
            PersistedVec3 currentPosition,
            String homeInstanceId,
            String workInstanceId,
            String bedMarkerId,
            String doorMarkerId,
            String workMarkerId
    ) {
    }

    private record PersistedVec3(double x, double y, double z) {
    }
}
