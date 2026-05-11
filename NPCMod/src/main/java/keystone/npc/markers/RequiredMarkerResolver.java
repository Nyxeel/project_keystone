package keystone.npc.markers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import keystone.npc.definition.EffectiveNpcDefinition;
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

    private final NpcTemplateResolver templateResolver;
    private final RoleDefinitionRegistry roleDefinitions;

    public RequiredMarkerResolver(NpcTemplateResolver templateResolver, RoleDefinitionRegistry roleDefinitions) {
        this.templateResolver = Objects.requireNonNull(templateResolver, "templateResolver");
        this.roleDefinitions = Objects.requireNonNull(roleDefinitions, "roleDefinitions");
    }

    public List<Requirement> resolveRequirements(String roleId) {
        LinkedHashMap<String, Requirement> ordered = new LinkedHashMap<>();

        templateResolver.resolveById(roleId)
            .map(EffectiveNpcDefinition::definition)
            .ifPresent(definition -> {
                Map<String, String> markerRoles = definition.markerRoles();
                for (String requiredName : definition.requiredMarkers()) {
                    String normalized = normalizeName(requiredName);
                    if (normalized == null) {
                        continue;
                    }
                    MarkerType markerType = resolveMarkerType(normalized, markerRoles.get(normalized));
                    ordered.putIfAbsent(normalized, new Requirement(normalized, markerType));
                }
            });

        if (!ordered.isEmpty()) {
            return List.copyOf(ordered.values());
        }

        RoleDefinition fallbackRole = roleDefinitions.findByRoleId(roleId).orElse(null);
        if (fallbackRole == null) {
            return List.of();
        }

        for (MarkerType markerType : fallbackRole.requiredMarkers()) {
            String normalized = markerType.name().toLowerCase(Locale.ROOT);
            ordered.putIfAbsent(normalized, new Requirement(normalized, markerType));
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

    private MarkerType resolveMarkerType(String markerName, String mappedType) {
        MarkerType fromMapping = parseMarkerType(mappedType);
        if (fromMapping != null) {
            return fromMapping;
        }
        return parseMarkerType(markerName);
    }

    private MarkerType parseMarkerType(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            MarkerType parsed = MarkerType.valueOf(rawValue.trim().toUpperCase(Locale.ROOT));
            if (parsed == MarkerType.DOOR) {
                // Doors are handled by runtime detection/capabilities and should not require marker assignment.
                return null;
            }
            return parsed;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static String normalizeName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return null;
        }
        return rawName.trim().toLowerCase(Locale.ROOT);
    }
}
