package keystone.npc.role;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import keystone.npc.model.NpcRole;
import keystone.npc.world.MarkerType;

/**
 * Hybrid role registry:
 * - code defaults from NpcRole
 * - optional JSON overrides/new roles from keystone-npc/roles.json
 */
public final class RoleDefinitionRegistry {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path path;
    private final Map<String, RoleDefinition> byRoleId = new LinkedHashMap<>();

    public RoleDefinitionRegistry(String relativePath) {
        this.path = Paths.get(relativePath);
    }

    public synchronized void load() {
        byRoleId.clear();

        for (NpcRole role : NpcRole.values()) {
            RoleDefinition definition = role.toDefinition();
            byRoleId.put(definition.roleId(), definition);
        }

        PersistedRoleFile file = readRoleFile();
        if (file == null || file.roles == null) {
            return;
        }

        for (PersistedRole persistedRole : file.roles) {
            if (persistedRole == null || persistedRole.roleId == null || persistedRole.roleId.isBlank()) {
                continue;
            }

            String roleId = RoleDefinition.normalizeRoleId(persistedRole.roleId);
            RoleDefinition base = byRoleId.get(roleId);

            String npcPluginRoleName = firstNonBlank(
                persistedRole.npcPluginRoleName,
                base != null ? base.npcPluginRoleName() : null,
                toTitleCase(roleId)
            );

            Set<MarkerType> requiredMarkers = parseMarkers(persistedRole.requiredMarkers);
            if (requiredMarkers == null) {
                requiredMarkers = base != null ? base.requiredMarkers() : EnumSet.noneOf(MarkerType.class);
            }

            RoleSchedule schedule = mergeSchedule(base, persistedRole.schedule);
            RoleDefinition merged = new RoleDefinition(roleId, npcPluginRoleName, requiredMarkers, schedule);
            byRoleId.put(roleId, merged);
        }
    }

    public synchronized Optional<RoleDefinition> findByRoleId(String roleId) {
        if (roleId == null || roleId.isBlank()) {
            return Optional.empty();
        }

        String normalized = RoleDefinition.normalizeRoleId(roleId);
        return Optional.ofNullable(byRoleId.get(normalized));
    }

    public synchronized List<RoleDefinition> list() {
        return List.copyOf(byRoleId.values());
    }

    public synchronized List<String> roleIds() {
        return byRoleId.keySet().stream().toList();
    }

    public synchronized void ensureExampleFileExists() {
        if (Files.exists(path)) {
            return;
        }

        try {
            Files.createDirectories(path.getParent() == null ? Paths.get(".") : path.getParent());
            PersistedRoleFile example = new PersistedRoleFile(List.of(
                new PersistedRole(
                    "lumberjack",
                    "Lumberjack",
                    List.of("BED", "DOOR", "WORK"),
                    new PersistedSchedule(21, 7)
                )
            ));
            Files.writeString(
                path,
                GSON.toJson(example),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            System.err.println("[KeystoneNPC] Failed to create example roles file: " + path);
            System.err.println("[KeystoneNPC] " + e.getMessage());
        }
    }

    private PersistedRoleFile readRoleFile() {
        if (!Files.exists(path)) {
            return null;
        }

        try {
            String raw = Files.readString(path, StandardCharsets.UTF_8);
            if (raw.isBlank()) {
                return null;
            }
            return GSON.fromJson(raw, PersistedRoleFile.class);
        } catch (IOException | JsonParseException | IllegalStateException e) {
            System.err.println("[KeystoneNPC] Failed to load role definitions from: " + path);
            System.err.println("[KeystoneNPC] " + e.getMessage());
            return null;
        }
    }

    private RoleSchedule mergeSchedule(RoleDefinition base, PersistedSchedule override) {
        int sleepStart = base != null ? base.schedule().sleepStartHour() : RoleSchedule.DEFAULT.sleepStartHour();
        int wakeHour = base != null ? base.schedule().wakeHour() : RoleSchedule.DEFAULT.wakeHour();

        if (override != null) {
            if (override.sleepStartHour != null) {
                sleepStart = override.sleepStartHour;
            }
            if (override.wakeHour != null) {
                wakeHour = override.wakeHour;
            }
        }

        return new RoleSchedule(sleepStart, wakeHour);
    }

    private Set<MarkerType> parseMarkers(List<String> rawMarkers) {
        if (rawMarkers == null) {
            return null;
        }

        if (rawMarkers.isEmpty()) {
            return Collections.emptySet();
        }

        EnumSet<MarkerType> markers = EnumSet.noneOf(MarkerType.class);
        for (String marker : rawMarkers) {
            if (marker == null || marker.isBlank()) {
                continue;
            }
            try {
                markers.add(MarkerType.valueOf(marker.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ex) {
                System.err.println("[KeystoneNPC] Ignoring unknown marker type in role file: " + marker);
            }
        }
        return markers;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "Unknown";
    }

    private static String toTitleCase(String roleId) {
        String[] parts = roleId.split("[_-]");
        List<String> out = new ArrayList<>(parts.length);
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            out.add(part.substring(0, 1).toUpperCase(Locale.ROOT) + part.substring(1));
        }
        return out.stream().collect(Collectors.joining(" "));
    }

    private record PersistedRoleFile(List<PersistedRole> roles) {
    }

    private record PersistedRole(
        String roleId,
        String npcPluginRoleName,
        List<String> requiredMarkers,
        PersistedSchedule schedule
    ) {
    }

    private record PersistedSchedule(
        Integer sleepStartHour,
        Integer wakeHour
    ) {
    }
}
