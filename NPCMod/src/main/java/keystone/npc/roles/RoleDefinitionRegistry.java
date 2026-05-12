package keystone.npc.roles;

import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import keystone.npc.definition.EffectiveNpcDefinition;
import keystone.npc.definition.NpcDefinition;
import keystone.npc.definition.NpcTemplateResolver;
import keystone.npc.markers.MarkerType;

/**
 * In-memory role registry derived from loaded NPC JSON definitions.
 */
public final class RoleDefinitionRegistry {

    private static final Pattern NON_ALNUM = Pattern.compile("[^A-Za-z0-9]+");

    private final NpcTemplateResolver templateResolver;

    private final Map<String, RoleDefinition> byRoleId = new LinkedHashMap<>();
    private final Map<String, List<String>> invalidRequiredMarkerNamesByRoleId = new LinkedHashMap<>();
    private final Map<String, List<String>> invalidRoleReasonsByRoleId = new LinkedHashMap<>();
    private final Map<String, List<String>> definitionIdsByRoleId = new LinkedHashMap<>();

    public RoleDefinitionRegistry(NpcTemplateResolver templateResolver) {
        this.templateResolver = java.util.Objects.requireNonNull(templateResolver, "templateResolver");
    }

    public synchronized void load() {
        byRoleId.clear();
        invalidRequiredMarkerNamesByRoleId.clear();
        invalidRoleReasonsByRoleId.clear();
        definitionIdsByRoleId.clear();

        for (String roleId : templateResolver.roleIdsWithValidationIssues()) {
            addDefinitionIds(roleId, templateResolver.definitionIdsForRole(roleId));
            addInvalidRoleReasons(roleId, templateResolver.roleInvalidReasons(roleId));
        }

        Map<String, String> firstDefinitionIdByRoleId = new LinkedHashMap<>();
        Map<String, String> firstDefinitionIdByEngineRoleName = new LinkedHashMap<>();
        Map<String, String> firstRoleIdByEngineRoleName = new LinkedHashMap<>();

        for (String definitionId : templateResolver.definitionIds()) {
            Optional<EffectiveNpcDefinition> resolved = templateResolver.resolveById(definitionId);
            if (resolved.isEmpty()) {
                continue;
            }

            NpcDefinition definition = resolved.get().definition();
            String roleId = resolved.get().roleId();
            if (roleId == null || roleId.isBlank()) {
                continue;
            }

            addDefinitionId(roleId, definitionId);

            String firstDefinitionId = firstDefinitionIdByRoleId.putIfAbsent(roleId, definitionId);
            if (firstDefinitionId != null) {
                addInvalidRoleReason(roleId,
                    "duplicate roleId '" + roleId + "' found in multiple NPC role definitions: "
                        + firstDefinitionId + ", " + definitionId);
                addInvalidRoleReason(roleId, "RoleDefinition conflict: refusing to merge requiredMarkers.");
                addInvalidRoleReason(roleId, "Spawn blocked for role " + roleId + ".");
                byRoleId.remove(roleId);
                continue;
            }

            if (isRoleInvalid(roleId)) {
                byRoleId.remove(roleId);
                continue;
            }

            // Spawn model source of truth:
            // NPCPlugin.spawnEntity uses this engine role name, which resolves to
            // Server/NPC/Roles/<RoleName>.json on the engine side.
            // Keystone appearance JSON under Server/NPC/Keystone/... is configuration only
            // unless explicit runtime apply code mutates the live entity after spawn.
            String roleSource = firstNonBlank(definition.role(), definition.id(), roleId);
            String npcPluginRoleName = firstNonBlank(definition.hytaleRole(), toPascalCase(roleSource), null);
            if (npcPluginRoleName == null || npcPluginRoleName.isBlank()) {
                addInvalidRoleReason(roleId,
                    "invalid role name source for NPCPlugin role mapping: " + String.valueOf(roleSource));
                continue;
            }

            ParsedRequiredMarkers parsed = parseRequiredMarkers(definition.requiredMarkers(), definition.markerRoles());

            if (!parsed.invalidRequiredMarkerNames().isEmpty()) {
                addInvalidRequiredMarkerNames(roleId, parsed.invalidRequiredMarkerNames());
                for (String invalidMarker : parsed.invalidRequiredMarkerNames()) {
                    addInvalidRoleReason(roleId,
                        "requiredMarkers contains unknown marker type: " + invalidMarker);
                }
                byRoleId.remove(roleId);
                continue;
            }

            if (parsed.requiredMarkerTypes().isEmpty()) {
                addInvalidRoleReason(roleId, "requiredMarkers is missing or empty");
                byRoleId.remove(roleId);
                continue;
            }

            String normalizedEngineRoleName = normalizeEngineRoleName(npcPluginRoleName);
            String firstRoleIdForEngineRole = firstRoleIdByEngineRoleName.putIfAbsent(normalizedEngineRoleName, roleId);
            String firstDefinitionIdForEngineRole = firstDefinitionIdByEngineRoleName.putIfAbsent(normalizedEngineRoleName, definitionId);
            if (firstRoleIdForEngineRole != null && !firstRoleIdForEngineRole.equals(roleId)) {
                String duplicateReason = "duplicate hytaleRole '" + npcPluginRoleName
                    + "' found in multiple NPC role definitions: "
                    + firstDefinitionIdForEngineRole + ", " + definitionId;

                addInvalidRoleReason(roleId, duplicateReason);
                addInvalidRoleReason(roleId, "Spawn blocked for role " + roleId + ".");

                addInvalidRoleReason(firstRoleIdForEngineRole, duplicateReason);
                addInvalidRoleReason(firstRoleIdForEngineRole, "Spawn blocked for role " + firstRoleIdForEngineRole + ".");

                byRoleId.remove(firstRoleIdForEngineRole);
                byRoleId.remove(roleId);
                continue;
            }

            byRoleId.put(roleId, new RoleDefinition(roleId, npcPluginRoleName, parsed.requiredMarkerTypes()));
        }

        for (RoleDefinition definition : byRoleId.values()) {
            String roleId = definition.roleId();
            System.out.println("[KeystoneNPC] Loaded required markers for role " + roleId + ": "
                + formatMarkerList(definition.requiredMarkers()));

            List<String> invalid = invalidRequiredMarkerNamesByRoleId.getOrDefault(roleId, List.of());
            for (String invalidMarker : invalid) {
                System.err.println("[KeystoneNPC][ROLE_DEF_INVALID_REQUIRED_MARKER] roleId=" + roleId
                    + " marker=" + invalidMarker.toUpperCase());
            }
        }

        for (Map.Entry<String, List<String>> invalidRole : invalidRoleReasonsByRoleId.entrySet()) {
            System.err.println("[KeystoneNPC][ROLE_INVALID] Role " + invalidRole.getKey() + " is invalid:");
            for (String reason : invalidRole.getValue()) {
                System.err.println("[KeystoneNPC][ROLE_INVALID] - " + reason);
            }
        }
    }

    public synchronized Optional<RoleDefinition> findByRoleId(String roleId) {
        if (roleId == null || roleId.isBlank()) {
            return Optional.empty();
        }

        String normalized = RoleDefinition.normalizeRoleId(roleId);
        if (isRoleInvalid(normalized)) {
            return Optional.empty();
        }
        return Optional.ofNullable(byRoleId.get(normalized));
    }

    public synchronized List<RoleDefinition> list() {
        return List.copyOf(byRoleId.values());
    }

    public synchronized List<String> roleIds() {
        return byRoleId.keySet().stream().toList();
    }

    public synchronized List<String> knownRoleIds() {
        LinkedHashSet<String> known = new LinkedHashSet<>(byRoleId.keySet());
        known.addAll(invalidRoleReasonsByRoleId.keySet());
        return List.copyOf(known);
    }

    public synchronized List<String> invalidRequiredMarkerNames(String roleId) {
        if (roleId == null || roleId.isBlank()) {
            return List.of();
        }

        String normalized = RoleDefinition.normalizeRoleId(roleId);
        return invalidRequiredMarkerNamesByRoleId.getOrDefault(normalized, List.of());
    }

    public synchronized List<String> invalidRoleReasons(String roleId) {
        if (roleId == null || roleId.isBlank()) {
            return List.of();
        }

        String normalized = RoleDefinition.normalizeRoleId(roleId);
        return invalidRoleReasonsByRoleId.getOrDefault(normalized, List.of());
    }

    public synchronized boolean isRoleInvalid(String roleId) {
        if (roleId == null || roleId.isBlank()) {
            return false;
        }

        String normalized = RoleDefinition.normalizeRoleId(roleId);
        return !invalidRoleReasonsByRoleId.getOrDefault(normalized, List.of()).isEmpty();
    }

    private String formatMarkerList(Set<MarkerType> requiredMarkers) {
        if (requiredMarkers == null || requiredMarkers.isEmpty()) {
            return "none";
        }

        return requiredMarkers.stream()
            .map(MarkerType::name)
            .collect(Collectors.joining(", "));
    }

    private ParsedRequiredMarkers parseRequiredMarkers(List<String> requiredMarkers, Map<String, String> markerRoles) {
        LinkedHashSet<MarkerType> parsedTypes = new LinkedHashSet<>();
        LinkedHashSet<String> invalidNames = new LinkedHashSet<>();

        for (String requiredName : requiredMarkers == null ? List.<String>of() : requiredMarkers) {
            String normalizedName = normalizeMarkerName(requiredName);
            if (normalizedName == null) {
                continue;
            }

            MarkerType mappedType = resolveMarkerType(normalizedName, markerRoles);
            if (mappedType != null) {
                parsedTypes.add(mappedType);
            } else {
                invalidNames.add(normalizedName);
            }
        }

        return new ParsedRequiredMarkers(Set.copyOf(parsedTypes), List.copyOf(invalidNames));
    }

    private void addDefinitionId(String roleId, String definitionId) {
        if (roleId == null || roleId.isBlank() || definitionId == null || definitionId.isBlank()) {
            return;
        }

        LinkedHashSet<String> definitionIds = new LinkedHashSet<>(definitionIdsByRoleId.getOrDefault(roleId, List.of()));
        definitionIds.add(definitionId);
        definitionIdsByRoleId.put(roleId, List.copyOf(definitionIds));
    }

    private void addDefinitionIds(String roleId, List<String> definitionIds) {
        if (definitionIds == null || definitionIds.isEmpty()) {
            return;
        }

        for (String definitionId : definitionIds) {
            addDefinitionId(roleId, definitionId);
        }
    }

    private void addInvalidRequiredMarkerNames(String roleId, List<String> invalidNames) {
        if (roleId == null || roleId.isBlank() || invalidNames == null || invalidNames.isEmpty()) {
            return;
        }

        List<String> existingInvalid = invalidRequiredMarkerNamesByRoleId.getOrDefault(roleId, List.of());
        LinkedHashSet<String> mergedInvalid = new LinkedHashSet<>(existingInvalid);
        mergedInvalid.addAll(invalidNames);
        invalidRequiredMarkerNamesByRoleId.put(roleId, List.copyOf(mergedInvalid));
    }

    private void addInvalidRoleReason(String roleId, String reason) {
        if (roleId == null || roleId.isBlank() || reason == null || reason.isBlank()) {
            return;
        }

        LinkedHashSet<String> reasons = new LinkedHashSet<>(invalidRoleReasonsByRoleId.getOrDefault(roleId, List.of()));
        reasons.add(reason);
        invalidRoleReasonsByRoleId.put(roleId, List.copyOf(reasons));
    }

    private void addInvalidRoleReasons(String roleId, List<String> reasons) {
        if (reasons == null || reasons.isEmpty()) {
            return;
        }

        for (String reason : reasons) {
            addInvalidRoleReason(roleId, reason);
        }
    }

    private MarkerType resolveMarkerType(String markerName, Map<String, String> markerRoles) {
        MarkerType fromMapping = parseMarkerType(findMarkerRole(markerName, markerRoles));
        if (fromMapping != null) {
            return fromMapping;
        }
        return parseMarkerType(markerName);
    }

    private String findMarkerRole(String markerName, Map<String, String> markerRoles) {
        if (markerRoles == null || markerRoles.isEmpty()) {
            return null;
        }

        String direct = markerRoles.get(markerName);
        if (direct != null && !direct.isBlank()) {
            return direct;
        }

        for (Map.Entry<String, String> entry : markerRoles.entrySet()) {
            String key = normalizeMarkerName(entry.getKey());
            if (markerName.equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private MarkerType parseMarkerType(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        try {
            return MarkerType.valueOf(rawValue.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String normalizeMarkerName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return null;
        }
        return rawName.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private String firstNonBlank(String first, String second, String fallback) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return fallback;
    }

    private String toPascalCase(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String[] tokens = NON_ALNUM.split(raw.trim());
        StringBuilder builder = new StringBuilder();
        for (String token : tokens) {
            if (token == null || token.isBlank()) {
                continue;
            }

            String lower = token.toLowerCase(java.util.Locale.ROOT);
            if (Character.isLetter(lower.charAt(0))) {
                builder.append(Character.toUpperCase(lower.charAt(0)));
                if (lower.length() > 1) {
                    builder.append(lower.substring(1));
                }
            } else {
                builder.append(lower);
            }
        }

        String pascal = builder.toString();
        return pascal.isBlank() ? null : pascal;
    }

    private String normalizeEngineRoleName(String rawRoleName) {
        if (rawRoleName == null || rawRoleName.isBlank()) {
            return null;
        }
        return rawRoleName.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private record ParsedRequiredMarkers(Set<MarkerType> requiredMarkerTypes, List<String> invalidRequiredMarkerNames) {
    }
}
