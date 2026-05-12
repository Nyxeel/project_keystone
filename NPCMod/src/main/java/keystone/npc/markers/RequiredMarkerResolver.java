package keystone.npc.markers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import keystone.npc.definition.NpcTemplateResolver;
import keystone.npc.roles.RoleDefinition;
import keystone.npc.roles.RoleDefinitionRegistry;

public final class RequiredMarkerResolver {

    public record Requirement(String name, MarkerType markerType) {
        public Requirement {
            name = normalizeName(name);
            Objects.requireNonNull(name, "name");
        }
    }

    private final RoleDefinitionRegistry roleDefinitions;

    public RequiredMarkerResolver(NpcTemplateResolver templateResolver, RoleDefinitionRegistry roleDefinitions) {
        Objects.requireNonNull(templateResolver, "templateResolver");
        this.roleDefinitions = Objects.requireNonNull(roleDefinitions, "roleDefinitions");
    }

    public List<Requirement> resolveRequirements(String roleId) {
        if (roleId == null || roleId.isBlank()) {
            return List.of();
        }

        LinkedHashMap<String, Requirement> ordered = new LinkedHashMap<>();

        RoleDefinition roleDefinition = roleDefinitions.findByRoleId(roleId).orElse(null);
        if (roleDefinition != null) {
            for (MarkerType markerType : roleDefinition.requiredMarkers()) {
                String normalized = normalizeName(markerType.name());
                if (normalized != null) {
                    ordered.putIfAbsent(normalized, new Requirement(normalized, markerType));
                }
            }

            for (String invalidMarker : roleDefinitions.invalidRequiredMarkerNames(roleDefinition.roleId())) {
                String normalized = normalizeName(invalidMarker);
                if (normalized != null) {
                    ordered.putIfAbsent(normalized, new Requirement(normalized, null));
                }
            }

            return List.copyOf(ordered.values());
        }

        return List.copyOf(ordered.values());
    }

    public List<MarkerType> resolveSupportedRequiredMarkerTypes(String roleId) {
        LinkedHashSet<MarkerType> types = new LinkedHashSet<>();
        for (Requirement requirement : resolveRequirements(roleId)) {
            if (requirement.markerType() != null) {
                types.add(requirement.markerType());
            }
        }
        return new ArrayList<>(types);
    }

    public List<String> resolveRequiredMarkerNames(String roleId) {
        List<String> names = new ArrayList<>();
        for (Requirement requirement : resolveRequirements(roleId)) {
            names.add(requirement.name());
        }
        return names;
    }

    private static String normalizeName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return null;
        }
        return rawName.trim().toLowerCase(Locale.ROOT);
    }
}
