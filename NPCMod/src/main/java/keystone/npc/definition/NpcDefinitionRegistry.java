package keystone.npc.definition;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public final class NpcDefinitionRegistry {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String[] GROUP_PROFILE_KEYS = {
        "routine",
        "skills",
        "capabilities",
        "actions",
        "movement",
        "navigation",
        "combat",
        "spawn",
        "structure",
        "persistence"
    };
    private static final String[] GROUP_SHARED_PASSTHROUGH_KEYS = {
        "version",
        "type",
        "template",
        "displayName",
        "nameTranslationKey",
        "npcType",
        "faction",
        "appearance",
        "stats",
        "drops",
        "attitude",
        "requiredMarkers",
        "markerRoles",
        "motionControllerList",
        "instructions",
        "defaultState",
        "debug"
    };

    private final Path externalRoot;
    private final String classpathRoot;
    private final Map<String, NpcDefinition> byId = new LinkedHashMap<>();
    private final Map<String, String> sourcePathByDefinitionId = new LinkedHashMap<>();
    private final LinkedHashSet<String> blockedDuplicateDefinitionIds = new LinkedHashSet<>();

    public NpcDefinitionRegistry(Path externalRoot, String classpathRoot) {
        this.externalRoot = Objects.requireNonNull(externalRoot, "externalRoot").toAbsolutePath().normalize();
        this.classpathRoot = normalizeRelativePath(Objects.requireNonNull(classpathRoot, "classpathRoot"));
    }

    public synchronized void load() {
        byId.clear();
        sourcePathByDefinitionId.clear();
        blockedDuplicateDefinitionIds.clear();
        for (String relativeNpcPath : loadNpcIndexPaths()) {
            Optional<String> raw = readText(relativeNpcPath);
            if (raw.isEmpty()) {
                System.err.println("[KeystoneNPC] NPC definition missing: " + relativeNpcPath);
                continue;
            }

            List<NpcDefinition> parsedDefinitions = parseDefinitionEntries(relativeNpcPath, raw.get());
            for (NpcDefinition definition : parsedDefinitions) {
                if (definition == null || definition.id() == null || definition.id().isBlank()) {
                    System.err.println("[KeystoneNPC] NPC definition without id ignored: " + relativeNpcPath);
                    continue;
                }

                String normalizedId = NpcDefinition.normalizeId(definition.id());
                String normalizedSourcePath = normalizeRelativePath(relativeNpcPath);

                if (blockedDuplicateDefinitionIds.contains(normalizedId)) {
                    System.err.println("[KeystoneNPC][DUPLICATE_DEFINITION_ID_BLOCKED] Duplicate definition id remains blocked: id="
                        + normalizedId + " source=" + normalizedSourcePath);
                    continue;
                }

                String existingSourcePath = sourcePathByDefinitionId.get(normalizedId);
                if (existingSourcePath != null) {
                    byId.remove(normalizedId);
                    sourcePathByDefinitionId.remove(normalizedId);
                    blockedDuplicateDefinitionIds.add(normalizedId);

                    System.err.println("[KeystoneNPC][DUPLICATE_DEFINITION_ID_BLOCKED] Duplicate definition id detected; "
                        + "all definitions with this id are blocked: id=" + normalizedId
                        + " firstSource=" + existingSourcePath
                        + " duplicateSource=" + normalizedSourcePath);
                    continue;
                }

                byId.put(normalizedId, definition);
                sourcePathByDefinitionId.put(normalizedId, normalizedSourcePath);
            }
        }
    }

    private List<NpcDefinition> parseDefinitionEntries(String relativeNpcPath, String rawJson) {
        JsonObject root;
        try {
            root = GSON.fromJson(rawJson, JsonObject.class);
        } catch (RuntimeException ex) {
            System.err.println("[KeystoneNPC] Failed to parse NPC definition JSON: "
                + relativeNpcPath + " -> " + ex.getMessage());
            return List.of();
        }

        if (root == null) {
            System.err.println("[KeystoneNPC] NPC definition root is null: " + relativeNpcPath);
            return List.of();
        }

        String type = stringValue(root, "type");
        if (type != null && "npc_group".equalsIgnoreCase(type)) {
            return parseNpcGroupDefinitions(relativeNpcPath, root);
        }

        try {
            NpcDefinition definition = GSON.fromJson(root, NpcDefinition.class);
            if (definition == null) {
                System.err.println("[KeystoneNPC] NPC definition parsed to null: " + relativeNpcPath);
                return List.of();
            }
            return List.of(definition);
        } catch (JsonParseException | IllegalStateException ex) {
            System.err.println("[KeystoneNPC] Failed to parse NPC definition: "
                + relativeNpcPath + " -> " + ex.getMessage());
            return List.of();
        }
    }

    private List<NpcDefinition> parseNpcGroupDefinitions(String relativeNpcPath, JsonObject root) {
        JsonArray variants = root.has("variants") && root.get("variants").isJsonArray()
            ? root.getAsJsonArray("variants")
            : null;
        if (variants == null) {
            System.err.println("[KeystoneNPC][NPC_GROUP_INVALID] Missing or invalid variants array: " + relativeNpcPath);
            return List.of();
        }

        if (variants.isEmpty()) {
            System.err.println("[KeystoneNPC][NPC_GROUP_INVALID] Variants array is empty: " + relativeNpcPath);
            return List.of();
        }

        JsonObject shared = objectValue(root, "shared");

        List<NpcDefinition> out = new ArrayList<>();
        for (int i = 0; i < variants.size(); i++) {
            JsonElement variantElement = variants.get(i);
            if (variantElement == null || !variantElement.isJsonObject()) {
                System.err.println("[KeystoneNPC][NPC_GROUP_INVALID] Variant entry is not an object: "
                    + relativeNpcPath + " index=" + i);
                continue;
            }

            JsonObject variant = variantElement.getAsJsonObject();
            String variantId = stringValue(variant, "id");
            String variantLabel = variantId == null || variantId.isBlank() ? "index=" + i : "id=" + variantId;

            String variantHytaleRole = stringValue(variant, "hytaleRole");
            if (variantHytaleRole == null || variantHytaleRole.isBlank()) {
                System.err.println("[KeystoneNPC][NPC_GROUP_VARIANT_MISSING_HYTALE_ROLE] Skipping variant: "
                    + relativeNpcPath + " " + variantLabel);
                continue;
            }

            JsonObject merged = mergeNpcGroupVariant(root, shared, variant);
            try {
                NpcDefinition definition = GSON.fromJson(merged, NpcDefinition.class);
                if (definition == null || definition.id() == null || definition.id().isBlank()) {
                    System.err.println("[KeystoneNPC][NPC_GROUP_VARIANT_INVALID] Variant without id ignored: "
                        + relativeNpcPath + " " + variantLabel);
                    continue;
                }
                if (definition.hytaleRole() == null || definition.hytaleRole().isBlank()) {
                    System.err.println("[KeystoneNPC][NPC_GROUP_VARIANT_MISSING_HYTALE_ROLE] Skipping variant after merge: "
                        + relativeNpcPath + " " + variantLabel);
                    continue;
                }
                out.add(definition);
            } catch (RuntimeException ex) {
                System.err.println("[KeystoneNPC][NPC_GROUP_VARIANT_PARSE_FAILED] Failed to parse merged variant: "
                    + relativeNpcPath + " " + variantLabel + " -> " + ex.getMessage());
            }
        }

        return List.copyOf(out);
    }

    private JsonObject mergeNpcGroupVariant(JsonObject groupRoot, JsonObject shared, JsonObject variant) {
        JsonObject merged = variant.deepCopy();

        if (groupRoot != null && groupRoot.has("version") && !merged.has("version")) {
            merged.add("version", groupRoot.get("version").deepCopy());
        }

        if (shared != null) {
            for (String key : GROUP_SHARED_PASSTHROUGH_KEYS) {
                applySharedIfMissing(merged, shared, key);
            }
        }

        JsonObject mergedProfiles = new JsonObject();
        if (shared != null) {
            copyProfilesObject(mergedProfiles, objectValue(shared, "profiles"));
            copyProfilesAliases(mergedProfiles, shared);
        }
        copyProfilesObject(mergedProfiles, objectValue(merged, "profiles"));
        copyProfilesAliases(mergedProfiles, variant);
        if (!mergedProfiles.entrySet().isEmpty()) {
            merged.add("profiles", mergedProfiles);
        }

        return merged;
    }

    private void applySharedIfMissing(JsonObject merged, JsonObject shared, String key) {
        if (merged == null || shared == null || key == null || key.isBlank()) {
            return;
        }

        if (!merged.has(key) && shared.has(key)) {
            merged.add(key, shared.get(key).deepCopy());
        }
    }

    private void copyProfilesObject(JsonObject targetProfiles, JsonObject sourceProfiles) {
        if (targetProfiles == null || sourceProfiles == null) {
            return;
        }

        for (String key : GROUP_PROFILE_KEYS) {
            if (sourceProfiles.has(key)) {
                targetProfiles.add(key, sourceProfiles.get(key).deepCopy());
            }
        }
    }

    private void copyProfilesAliases(JsonObject targetProfiles, JsonObject source) {
        if (targetProfiles == null || source == null) {
            return;
        }

        for (String key : GROUP_PROFILE_KEYS) {
            if (source.has(key)) {
                targetProfiles.add(key, source.get(key).deepCopy());
            }
        }
    }

    private String stringValue(JsonObject object, String key) {
        if (object == null || key == null || key.isBlank()) {
            return null;
        }

        JsonElement value = object.get(key);
        if (value == null || value.isJsonNull() || !value.isJsonPrimitive()) {
            return null;
        }

        try {
            return value.getAsString();
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private JsonObject objectValue(JsonObject object, String key) {
        if (object == null || key == null || key.isBlank()) {
            return null;
        }

        JsonElement value = object.get(key);
        if (value == null || value.isJsonNull() || !value.isJsonObject()) {
            return null;
        }

        return value.getAsJsonObject();
    }

    public synchronized Optional<NpcDefinition> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(byId.get(NpcDefinition.normalizeId(id)));
    }

    public synchronized List<String> definitionIds() {
        return List.copyOf(byId.keySet());
    }

    public Optional<String> readText(String relativePath) {
        String normalized = normalizeRelativePath(relativePath);

        Path externalPath = externalRoot.resolve(normalized).normalize();
        if (Files.isRegularFile(externalPath)) {
            try {
                return Optional.of(Files.readString(externalPath, StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.err.println("[KeystoneNPC] Failed to read external JSON: " + externalPath + " -> " + e.getMessage());
                return Optional.empty();
            }
        }

        String classpathPath = classpathRoot + "/" + normalized;
        InputStream stream = getClass().getClassLoader().getResourceAsStream(classpathPath);
        if (stream == null) {
            return Optional.empty();
        }

        try (stream) {
            return Optional.of(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("[KeystoneNPC] Failed to read classpath JSON: " + classpathPath + " -> " + e.getMessage());
            return Optional.empty();
        }
    }

    public String resolveProfilePath(String definitionId, String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return rawPath;
        }

        String normalized = normalizeRelativePath(rawPath);
        if (isExplicitRootPath(normalized) || !isKeystoneDefinition(definitionId)) {
            return normalized;
        }

        String keystoneCandidate = "Keystone/" + normalized;
        if (readText(keystoneCandidate).isPresent()) {
            return keystoneCandidate;
        }
        if (readText(normalized).isPresent()) {
            return normalized;
        }

        // Prefer Keystone root for unresolved new profile paths to keep error reporting deterministic.
        return keystoneCandidate;
    }

    private List<String> loadNpcIndexPaths() {
        IndexEntriesResult keystoneClasspath = readIndexEntries("Keystone/npc/index.json", "classpath");
        IndexEntriesResult keystoneExternal = readIndexEntries("Keystone/npc/index.json", "external");

        LinkedHashSet<String> keystoneEntries = new LinkedHashSet<>();
        keystoneEntries.addAll(keystoneClasspath.entries());
        keystoneEntries.addAll(keystoneExternal.entries());

        if (!keystoneEntries.isEmpty()) {
            List<String> normalized = new ArrayList<>();
            for (String entry : keystoneEntries) {
                normalized.add(normalizeKeystoneIndexEntry(entry));
            }
            return normalized;
        }

        if (keystoneClasspath.parseFailed() || keystoneExternal.parseFailed()) {
            System.err.println("[KeystoneNPC][KEYSTONE_INDEX_PARSE_FAILED_NO_LEGACY_FALLBACK] "
                + "Keystone/npc/index.json exists but could not be parsed. "
                + "Fix Keystone index; legacy fallback is intentionally disabled in this case.");
            return List.of();
        }

        System.out.println("[KeystoneNPC][KEYSTONE_INDEX_MISSING_FALLBACK_TO_LEGACY] "
            + "No entries found in Keystone/npc/index.json (classpath or external). Falling back to npc/index.json.");

        IndexEntriesResult legacyClasspath = readIndexEntries("npc/index.json", "classpath");
        IndexEntriesResult legacyExternal = readIndexEntries("npc/index.json", "external");

        LinkedHashSet<String> legacyEntries = new LinkedHashSet<>();
        legacyEntries.addAll(legacyClasspath.entries());
        legacyEntries.addAll(legacyExternal.entries());

        List<String> normalized = new ArrayList<>();
        for (String entry : legacyEntries) {
            normalized.add(normalizeLegacyIndexEntry(entry));
        }
        return normalized;
    }

    private String normalizeKeystoneIndexEntry(String entry) {
        String cleaned = normalizeRelativePath(entry);
        if (cleaned.startsWith("Keystone/")) {
            return cleaned;
        }
        if (cleaned.startsWith("npc/")) {
            return "Keystone/" + cleaned;
        }
        return "Keystone/npc/" + cleaned;
    }

    private String normalizeLegacyIndexEntry(String entry) {
        String cleaned = normalizeRelativePath(entry);
        if (!cleaned.startsWith("npc/")) {
            cleaned = "npc/" + cleaned;
        }
        return cleaned;
    }

    private IndexEntriesResult readIndexEntries(String relativeIndexPath, String sourceLabel) {
        String normalized = normalizeRelativePath(relativeIndexPath);
        boolean filePresent;
        String raw;

        if ("external".equals(sourceLabel)) {
            Path externalPath = externalRoot.resolve(normalized).normalize();
            if (!Files.isRegularFile(externalPath)) {
                return new IndexEntriesResult(List.of(), false, false);
            }
            filePresent = true;
            try {
                raw = Files.readString(externalPath, StandardCharsets.UTF_8);
            } catch (IOException e) {
                System.err.println("[KeystoneNPC] Failed to read external JSON: " + externalPath + " -> " + e.getMessage());
                return new IndexEntriesResult(List.of(), true, true);
            }
        } else {
            String classpathPath = classpathRoot + "/" + normalized;
            InputStream stream = getClass().getClassLoader().getResourceAsStream(classpathPath);
            if (stream == null) {
                return new IndexEntriesResult(List.of(), false, false);
            }
            filePresent = true;
            try (stream) {
                raw = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                System.err.println("[KeystoneNPC] Failed to read classpath JSON: " + classpathPath + " -> " + e.getMessage());
                return new IndexEntriesResult(List.of(), true, true);
            }
        }

        try {
            String[] entries = GSON.fromJson(raw, String[].class);
            if (entries == null) {
                return new IndexEntriesResult(List.of(), filePresent, false);
            }
            List<String> out = new ArrayList<>(entries.length);
            for (String entry : entries) {
                if (entry != null && !entry.isBlank()) {
                    out.add(entry);
                }
            }
            return new IndexEntriesResult(List.copyOf(out), filePresent, false);
        } catch (JsonParseException | IllegalStateException e) {
            System.err.println("[KeystoneNPC] Failed to parse " + sourceLabel + " NPC index: "
                + relativeIndexPath + " -> " + e.getMessage());
            return new IndexEntriesResult(List.of(), filePresent, true);
        }
    }

    private boolean isKeystoneDefinition(String definitionId) {
        if (definitionId == null || definitionId.isBlank()) {
            return false;
        }

        String normalizedId;
        try {
            normalizedId = NpcDefinition.normalizeId(definitionId);
        } catch (IllegalArgumentException ex) {
            return false;
        }

        String sourcePath = sourcePathByDefinitionId.get(normalizedId);
        return sourcePath != null && sourcePath.startsWith("Keystone/");
    }

    private boolean isExplicitRootPath(String normalizedPath) {
        return normalizedPath.startsWith("Keystone/")
            || normalizedPath.startsWith("npc/")
            || normalizedPath.startsWith("templates/");
    }

    private record IndexEntriesResult(List<String> entries, boolean filePresent, boolean parseFailed) {
    }

    public static String normalizeRelativePath(String rawPath) {
        Objects.requireNonNull(rawPath, "rawPath");
        String normalized = rawPath.trim().replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("relative path must not be blank");
        }
        return normalized;
    }
}
