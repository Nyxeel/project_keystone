package keystone.npc.capabilities;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import keystone.npc.definition.NpcDefinitionRegistry;
import keystone.npc.definition.NpcProfileRefs;

public final class CapabilityResolver {

    public static final Gson GSON = new GsonBuilder().create();

    private final NpcDefinitionRegistry definitionRegistry;
    private final Map<String, CapabilitySet> cache = new ConcurrentHashMap<>();

    public CapabilityResolver(NpcDefinitionRegistry definitionRegistry) {
        this.definitionRegistry = Objects.requireNonNull(definitionRegistry, "definitionRegistry");
    }

    public CapabilitySet resolve(NpcProfileRefs profileRefs) {
        if (profileRefs == null || profileRefs.capabilities() == null || profileRefs.capabilities().isBlank()) {
            return CapabilitySet.empty();
        }
        String normalizedPath = NpcDefinitionRegistry.normalizeRelativePath(profileRefs.capabilities());
        return cache.computeIfAbsent(normalizedPath, this::loadCapabilitySet);
    }

    public void clearCache() {
        cache.clear();
    }

    private CapabilitySet loadCapabilitySet(String relativePath) {
        Optional<String> raw = definitionRegistry.readText(relativePath);
        if (raw.isEmpty()) {
            return CapabilitySet.empty();
        }

        try {
            CapabilityProfile profile = GSON.fromJson(raw.get(), CapabilityProfile.class);
            if (profile == null) {
                return CapabilitySet.empty();
            }
            return CapabilitySet.fromBooleanMap(profile.capabilities());
        } catch (JsonParseException | IllegalStateException ex) {
            System.err.println("[KeystoneNPC] Failed to parse capability profile " + relativePath + ": " + ex.getMessage());
            return CapabilitySet.empty();
        }
    }
}
