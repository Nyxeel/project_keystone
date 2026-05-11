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
import com.google.gson.JsonParseException;

public final class NpcDefinitionRegistry {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path externalRoot;
    private final String classpathRoot;
    private final Map<String, NpcDefinition> byId = new LinkedHashMap<>();

    public NpcDefinitionRegistry(Path externalRoot, String classpathRoot) {
        this.externalRoot = Objects.requireNonNull(externalRoot, "externalRoot").toAbsolutePath().normalize();
        this.classpathRoot = normalizeRelativePath(Objects.requireNonNull(classpathRoot, "classpathRoot"));
    }

    public synchronized void load() {
        byId.clear();
        for (String relativeNpcPath : loadNpcIndexPaths()) {
            Optional<String> raw = readText(relativeNpcPath);
            if (raw.isEmpty()) {
                System.err.println("[KeystoneNPC] NPC definition missing: " + relativeNpcPath);
                continue;
            }

            try {
                NpcDefinition definition = GSON.fromJson(raw.get(), NpcDefinition.class);
                if (definition == null || definition.id() == null || definition.id().isBlank()) {
                    System.err.println("[KeystoneNPC] NPC definition without id ignored: " + relativeNpcPath);
                    continue;
                }
                byId.put(definition.id(), definition);
            } catch (JsonParseException | IllegalStateException e) {
                System.err.println("[KeystoneNPC] Failed to parse NPC definition: " + relativeNpcPath + " -> " + e.getMessage());
            }
        }
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

    private List<String> loadNpcIndexPaths() {
        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        ordered.addAll(readIndexEntries("npc/index.json", "classpath"));
        ordered.addAll(readIndexEntries("npc/index.json", "external"));

        List<String> normalized = new ArrayList<>();
        for (String entry : ordered) {
            String cleaned = normalizeRelativePath(entry);
            if (!cleaned.startsWith("npc/")) {
                cleaned = "npc/" + cleaned;
            }
            normalized.add(cleaned);
        }
        return normalized;
    }

    private List<String> readIndexEntries(String relativeIndexPath, String sourceLabel) {
        Optional<String> raw = "external".equals(sourceLabel)
            ? readExternalOnly(relativeIndexPath)
            : readClasspathOnly(relativeIndexPath);
        if (raw.isEmpty()) {
            return List.of();
        }

        try {
            String[] entries = GSON.fromJson(raw.get(), String[].class);
            if (entries == null) {
                return List.of();
            }
            List<String> out = new ArrayList<>(entries.length);
            for (String entry : entries) {
                if (entry != null && !entry.isBlank()) {
                    out.add(entry);
                }
            }
            return out;
        } catch (JsonParseException | IllegalStateException e) {
            System.err.println("[KeystoneNPC] Failed to parse " + sourceLabel + " NPC index: "
                + relativeIndexPath + " -> " + e.getMessage());
            return List.of();
        }
    }

    private Optional<String> readExternalOnly(String relativePath) {
        String normalized = normalizeRelativePath(relativePath);
        Path externalPath = externalRoot.resolve(normalized).normalize();
        if (!Files.isRegularFile(externalPath)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readString(externalPath, StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("[KeystoneNPC] Failed to read external JSON: " + externalPath + " -> " + e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> readClasspathOnly(String relativePath) {
        String normalized = normalizeRelativePath(relativePath);
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
