package keystone.npc.skills;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import keystone.npc.definition.NpcDefinitionRegistry;
import keystone.npc.definition.NpcProfileRefs;

public final class SkillResolver {

    public static final Gson GSON = new GsonBuilder().create();

    private final NpcDefinitionRegistry definitionRegistry;
    private final Map<String, SkillSet> cache = new ConcurrentHashMap<>();

    public SkillResolver(NpcDefinitionRegistry definitionRegistry) {
        this.definitionRegistry = Objects.requireNonNull(definitionRegistry, "definitionRegistry");
    }

    public SkillSet resolve(String definitionId, NpcProfileRefs profileRefs) {
        if (profileRefs == null) {
            return SkillSet.empty();
        }

        String rawPath = profileRefs.resolveSkillsPath();
        if (rawPath == null || rawPath.isBlank()) {
            return SkillSet.empty();
        }

        String resolvedPath = definitionRegistry.resolveProfilePath(definitionId, rawPath);
        if (resolvedPath == null || resolvedPath.isBlank()) {
            return SkillSet.empty();
        }

        String normalizedPath = NpcDefinitionRegistry.normalizeRelativePath(resolvedPath);
        return cache.computeIfAbsent(normalizedPath, this::loadSkillSet);
    }

    public void clearCache() {
        cache.clear();
    }

    private SkillSet loadSkillSet(String relativePath) {
        Optional<String> raw = definitionRegistry.readText(relativePath);
        if (raw.isEmpty()) {
            return SkillSet.empty();
        }

        try {
            SkillProfile profile = GSON.fromJson(raw.get(), SkillProfile.class);
            if (profile == null) {
                return SkillSet.empty();
            }
            return SkillSet.fromBooleanMap(profile.skills());
        } catch (JsonParseException | IllegalStateException ex) {
            System.err.println("[KeystoneNPC] Failed to parse skill profile " + relativePath + ": " + ex.getMessage());
            return SkillSet.empty();
        }
    }
}