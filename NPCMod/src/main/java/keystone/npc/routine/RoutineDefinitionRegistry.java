package keystone.npc.routine;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class RoutineDefinitionRegistry {
    private final Map<String, RoutineDefinition> byId = new LinkedHashMap<>();

    public synchronized void put(RoutineDefinition definition) {
        if (definition == null || definition.id() == null || definition.id().isBlank()) {
            return;
        }
        byId.put(definition.id(), definition);
    }

    public synchronized Optional<RoutineDefinition> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(byId.get(id));
    }
}
