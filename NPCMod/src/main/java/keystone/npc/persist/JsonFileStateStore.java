package keystone.npc.persist;

import keystone.npc.model.NpcRecord;
import keystone.npc.world.MarkerRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

/**
 * MVP A: simpelstes Persistenz-Skeleton.
 *
 * TODO: Sobald klar ist, welche JSON-Lib im Hytale-Modding-Setup verfügbar/erlaubt ist (Gson/Jackson),
 *       echte Serialisierung implementieren.
 */
public final class JsonFileStateStore implements StateStore {

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
            // TODO: echte JSON Deserialisierung
            // For now: ignore content and return empty.
            Files.readString(path, StandardCharsets.UTF_8);
            return PluginState.empty();
        } catch (IOException e) {
            // TODO: log warn, return empty
            return PluginState.empty();
        }
    }

    @Override
    public void save(List<MarkerRecord> markers, List<NpcRecord> npcs) {
        try {
            Files.createDirectories(path.getParent() == null ? Paths.get(".") : path.getParent());

            // TODO: echte JSON Serialisierung
            String placeholder = "{\n  \"markers\": " + markers.size() + ",\n  \"npcs\": " + npcs.size() + "\n}\n";
            Files.writeString(path, placeholder, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            // TODO: proper logger
            e.printStackTrace();
        }
    }
}
