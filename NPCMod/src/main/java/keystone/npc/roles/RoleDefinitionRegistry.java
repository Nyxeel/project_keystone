package keystone.npc.roles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import keystone.npc.domain.NpcRole;
import keystone.npc.markers.MarkerType;

/**
 * Hybrid role registry:
 * - code defaults from NpcRole
 * - optional JSON overrides/new roles from keystone-npc/roles.json
 */
public final class RoleDefinitionRegistry {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path path;
    private final RoleDefinitionParsingSupport parsingSupport = new RoleDefinitionParsingSupport();
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
        if (file == null || file.roles() == null) {
            return;
        }

        for (PersistedRole persistedRole : file.roles()) {
            if (persistedRole == null || persistedRole.roleId() == null || persistedRole.roleId().isBlank()) {
                continue;
            }

            String roleId = RoleDefinition.normalizeRoleId(persistedRole.roleId());
            RoleDefinition base = byRoleId.get(roleId);

            String npcPluginRoleName = parsingSupport.firstNonBlank(
                persistedRole.npcPluginRoleName(),
                base != null ? base.npcPluginRoleName() : null,
                parsingSupport.toTitleCase(roleId)
            );

            Set<MarkerType> requiredMarkers = parsingSupport.parseMarkers(persistedRole.requiredMarkers());
            if (requiredMarkers == null) {
                requiredMarkers = base != null ? base.requiredMarkers() : EnumSet.noneOf(MarkerType.class);
            }

            DailyRoutine schedule = parsingSupport.mergeSchedule(base, persistedRole.schedule());
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
}
