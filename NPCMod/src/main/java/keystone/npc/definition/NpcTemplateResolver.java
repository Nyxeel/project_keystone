package keystone.npc.definition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import keystone.npc.actions.ActionProfile;
import keystone.npc.domain.NpcState;
import keystone.npc.markers.MarkerType;
import keystone.npc.movement.InstructionDefinition;
import keystone.npc.movement.MotionControllerDefinition;
import keystone.npc.movement.MovementProfile;
import keystone.npc.navigation.NpcNavigationProfile;
import keystone.npc.persistence.profile.PersistenceProfile;
import keystone.npc.routine.RoutineDefinition;
import keystone.npc.routine.RoutineEntry;
import keystone.npc.skills.NpcSkill;
import keystone.npc.skills.SkillResolver;
import keystone.npc.skills.SkillSet;

public final class NpcTemplateResolver {

    private static final Gson GSON = new GsonBuilder().create();
    private static final List<String> NAV_PATH_STYLES = List.of(
        "safe",
        "practical",
        "patrol",
        "direct",
        "aggressive"
    );
    private static final List<String> NAV_DOOR_POLICIES = List.of(
        "use_if_needed",
        "avoid",
        "never"
    );
    private static final List<String> NAV_DANGER_POLICIES = List.of(
        "avoid",
        "approach",
        "ignore"
    );
    private static final List<String> NAV_TARGET_POLICIES = List.of(
        "routine_only",
        "defend_area",
        "chase_target",
        "cut_off_target"
    );
    private static final List<String> NAV_SHORTCUT_POLICIES = List.of(
        "avoid",
        "allow_small_shortcuts",
        "allow",
        "prefer",
        "prefer_natural"
    );
    private static final List<String> NAV_CLIMB_POLICIES = List.of(
        "never",
        "if_needed",
        "if_useful",
        "if_possible"
    );

    private final NpcDefinitionRegistry definitions;
    private final SkillResolver skillResolver;
    private final Map<String, EffectiveNpcDefinition> byId = new LinkedHashMap<>();
    private final Map<String, RoutineDefinition> routineByDefinitionId = new LinkedHashMap<>();
    private final Map<String, ActionProfile> actionByDefinitionId = new LinkedHashMap<>();
    private final Map<String, MovementProfile> movementByDefinitionId = new LinkedHashMap<>();
    private final Map<String, NpcNavigationProfile> navigationByDefinitionId = new LinkedHashMap<>();
    private final Map<String, PersistenceProfile> persistenceByDefinitionId = new LinkedHashMap<>();
    private final Map<String, List<String>> invalidRoleReasonsByRoleId = new LinkedHashMap<>();
    private final Map<String, List<String>> definitionIdsByRoleId = new LinkedHashMap<>();

    public NpcTemplateResolver(NpcDefinitionRegistry definitions, SkillResolver skillResolver) {
        this.definitions = Objects.requireNonNull(definitions, "definitions");
        this.skillResolver = Objects.requireNonNull(skillResolver, "skillResolver");
    }

    public synchronized void reload() {
        byId.clear();
        routineByDefinitionId.clear();
        actionByDefinitionId.clear();
        movementByDefinitionId.clear();
        navigationByDefinitionId.clear();
        persistenceByDefinitionId.clear();
        invalidRoleReasonsByRoleId.clear();
        definitionIdsByRoleId.clear();
        skillResolver.clearCache();

        List<String> definitionIds = definitions.definitionIds();
        for (String id : definitionIds) {
            definitions.findById(id).ifPresent(this::resolveDefinition);
        }
    }

    public synchronized Optional<EffectiveNpcDefinition> resolveById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(byId.get(NpcDefinition.normalizeId(id)));
    }

    public synchronized Optional<EffectiveNpcDefinition> resolveByRoleId(String roleId) {
        String normalizedRoleId = normalizeRoleId(roleId);
        if (normalizedRoleId == null || roleHasValidationIssues(normalizedRoleId)) {
            return Optional.empty();
        }

        EffectiveNpcDefinition resolved = null;
        for (EffectiveNpcDefinition definition : byId.values()) {
            if (!normalizedRoleId.equals(definition.roleId())) {
                continue;
            }

            if (resolved != null) {
                return Optional.empty();
            }
            resolved = definition;
        }

        return Optional.ofNullable(resolved);
    }

    public synchronized List<String> definitionIds() {
        return List.copyOf(byId.keySet());
    }

    public synchronized List<String> roleIdsWithValidationIssues() {
        return List.copyOf(invalidRoleReasonsByRoleId.keySet());
    }

    public synchronized List<String> roleInvalidReasons(String roleId) {
        String normalizedRoleId = normalizeRoleId(roleId);
        if (normalizedRoleId == null) {
            return List.of();
        }
        return invalidRoleReasonsByRoleId.getOrDefault(normalizedRoleId, List.of());
    }

    public synchronized List<String> definitionIdsForRole(String roleId) {
        String normalizedRoleId = normalizeRoleId(roleId);
        if (normalizedRoleId == null) {
            return List.of();
        }
        return definitionIdsByRoleId.getOrDefault(normalizedRoleId, List.of());
    }

    public synchronized Optional<RoutineDefinition> resolveRoutine(String definitionId) {
        if (definitionId == null || definitionId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(routineByDefinitionId.get(NpcDefinition.normalizeId(definitionId)));
    }

    public synchronized Optional<RoutineDefinition> resolveRoutineByRoleId(String roleId) {
        return resolveByRoleId(roleId)
            .map(EffectiveNpcDefinition::id)
            .flatMap(this::resolveRoutine);
    }

    public synchronized Optional<ActionProfile> resolveActionProfile(String definitionId) {
        if (definitionId == null || definitionId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(actionByDefinitionId.get(NpcDefinition.normalizeId(definitionId)));
    }

    public synchronized Optional<MovementProfile> resolveMovementProfile(String definitionId) {
        if (definitionId == null || definitionId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(movementByDefinitionId.get(NpcDefinition.normalizeId(definitionId)));
    }

    public synchronized Optional<NpcNavigationProfile> resolveNavigationProfile(String definitionId) {
        if (definitionId == null || definitionId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(navigationByDefinitionId.get(NpcDefinition.normalizeId(definitionId)));
    }

    public synchronized Optional<PersistenceProfile> resolvePersistenceProfile(String definitionId) {
        if (definitionId == null || definitionId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(persistenceByDefinitionId.get(NpcDefinition.normalizeId(definitionId)));
    }

    public synchronized Optional<PersistenceProfile> resolvePersistenceProfileByRoleId(String roleId) {
        return resolveByRoleId(roleId)
            .map(EffectiveNpcDefinition::id)
            .flatMap(this::resolvePersistenceProfile);
    }

    public synchronized boolean respawnAfterRestartEnabledForRole(String roleId) {
        return resolvePersistenceProfileByRoleId(roleId)
            .map(PersistenceProfile::respawnAfterRestartEnabled)
            .orElse(Boolean.FALSE);
    }

    private void resolveDefinition(NpcDefinition concrete) {
        NpcDefinition merged = concrete;
        List<String> errors = new ArrayList<>();

        boolean concreteType = "concrete".equalsIgnoreCase(nullSafe(concrete.type()));
        if (concreteType && (concrete.template() == null || concrete.template().isBlank())) {
            errors.add("type=concrete requires a non-empty template reference");
        }

        if (concrete.template() != null && !concrete.template().isBlank()) {
            Optional<String> templateJson = definitions.readText(concrete.template());
            if (templateJson.isPresent()) {
                Optional<NpcDefinition> templateDefinition = definitionsFromJson(templateJson.get());
                if (templateDefinition.isPresent()) {
                    merged = merge(templateDefinition.get(), concrete);
                } else {
                    errors.add("failed to parse template definition: " + concrete.template());
                }
            } else {
                errors.add("template reference not found: " + concrete.template());
            }
        }

        String resolvedRoleId = normalizeRoleId(merged.effectiveRoleId());
        registerRoleDefinition(resolvedRoleId, merged.id());
        if (resolvedRoleId == null) {
            errors.add("roleId could not be resolved from definition");
        }

        NpcProfileRefs profiles = merged.profiles();
        String definitionId = merged.id();
        validateProfilePathsExist(definitionId, profiles, errors);
        validateDefaultState(merged, errors);
        validateSkillProfileKeys(definitionId, profiles, errors);

        Optional<RoutineDefinition> routine = parseProfile(
            definitionId,
            profiles != null ? profiles.routine() : null,
            RoutineDefinition.class,
            errors
        );
        Optional<ActionProfile> actionProfile = parseProfile(
            definitionId,
            profiles != null ? profiles.actions() : null,
            ActionProfile.class,
            errors
        );
        Optional<MovementProfile> movement = parseProfile(
            definitionId,
            profiles != null ? profiles.movement() : null,
            MovementProfile.class,
            errors
        );
        Optional<NpcNavigationProfile> navigationProfile = parseProfile(
            definitionId,
            profiles != null ? profiles.navigation() : null,
            NpcNavigationProfile.class,
            errors
        );
        Optional<PersistenceProfile> persistenceProfile = parseProfile(
            definitionId,
            profiles != null ? profiles.persistence() : null,
            PersistenceProfile.class,
            errors
        );
        String resolvedNavigationProfilePath = resolveProfilePath(definitionId, profiles != null ? profiles.navigation() : null);
        validateNavigationProfile(navigationProfile, resolvedNavigationProfilePath, errors);

        Set<String> requiredMarkerNames = normalizeRequiredMarkerNames(merged.requiredMarkers());

        if (movement.isEmpty()) {
            movement = buildInlineMovementProfile(merged);
        }

        validateMarkerMapping(merged, requiredMarkerNames, errors);
        validateRoutineSchedule(routine, requiredMarkerNames, errors);
        validateRoutineActions(routine, actionProfile, errors);

        if (!errors.isEmpty()) {
            recordRoleValidationErrors(resolvedRoleId, errors);
            for (String error : errors) {
                System.err.println("[KeystoneNPC] NPC definition rejected: id=" + concrete.id() + " reason=" + error);
            }
            return;
        }

        if (roleHasValidationIssues(resolvedRoleId)) {
            return;
        }

        // Appearance note:
        // This resolver validates and stores appearance configuration only.
        // The actual spawned model is selected by the engine role resolved in
        // RoleDefinitionRegistry and loaded from Server/NPC/Roles/<RoleName>.json.
        // No automatic apply-to-live-entity appearance logic is executed here.
        SkillSet skills = skillResolver.resolve(definitionId, merged.profiles());
        EffectiveNpcDefinition effective = new EffectiveNpcDefinition(merged, skills);
        byId.put(effective.id(), effective);
        routine.ifPresent(value -> routineByDefinitionId.put(effective.id(), value));
        actionProfile.ifPresent(value -> actionByDefinitionId.put(effective.id(), value));
        movement.ifPresent(value -> movementByDefinitionId.put(effective.id(), value));
        navigationProfile.ifPresent(value -> navigationByDefinitionId.put(effective.id(), value));
        persistenceProfile.ifPresent(value -> persistenceByDefinitionId.put(effective.id(), value));
    }

    private void validateProfilePathsExist(String definitionId, NpcProfileRefs profiles, List<String> errors) {
        if (profiles == null) {
            return;
        }

        validateProfilePathExists(definitionId, "routine", profiles.routine(), errors);
        String skillsPath = profiles.resolveSkillsPath();
        if (profiles.usesLegacySkillsFallback()) {
            validateProfilePathExists(definitionId, "skills (legacy capabilities)", skillsPath, errors);
        } else {
            validateProfilePathExists(definitionId, "skills", skillsPath, errors);
        }
        validateProfilePathExists(definitionId, "actions", profiles.actions(), errors);
        validateProfilePathExists(definitionId, "movement", profiles.movement(), errors);
        validateProfilePathExists(definitionId, "navigation", profiles.navigation(), errors);
        validateProfilePathExists(definitionId, "combat", profiles.combat(), errors);
        validateProfilePathExists(definitionId, "spawn", profiles.spawn(), errors);
        validateProfilePathExists(definitionId, "structure", profiles.structure(), errors);
        validateProfilePathExists(definitionId, "persistence", profiles.persistence(), errors);
    }

    private void validateProfilePathExists(String definitionId, String profileKey, String relativePath, List<String> errors) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        String resolvedPath = resolveProfilePath(definitionId, relativePath);
        if (resolvedPath == null || resolvedPath.isBlank() || definitions.readText(resolvedPath).isEmpty()) {
            errors.add("profiles." + profileKey + " not found: " + resolvedPath);
        }
    }

    private void validateDefaultState(NpcDefinition merged, List<String> errors) {
        String defaultState = merged.defaultState();
        if (defaultState == null || defaultState.isBlank()) {
            return;
        }
        try {
            NpcState.valueOf(defaultState.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            errors.add("unknown defaultState: " + defaultState);
        }
    }

    private void validateMarkerMapping(NpcDefinition merged, Set<String> requiredMarkers, List<String> errors) {
        if (requiredMarkers.isEmpty()) {
            errors.add("requiredMarkers is missing or empty");
            errors.add("Cannot infer requiredMarkers from routine JSON");
        }

        Map<String, String> normalizedMarkerRoles = normalizeMarkerRoles(merged.markerRoles(), errors);

        for (String requiredMarker : requiredMarkers) {
            MarkerType requiredType = parseMarkerType(requiredMarker);
            if (requiredType == null) {
                errors.add("requiredMarkers contains unknown marker type: " + requiredMarker);
                continue;
            }

            String mappedRawValue = normalizedMarkerRoles.get(requiredMarker);
            if (mappedRawValue == null) {
                errors.add("required marker '" + requiredMarker + "' has no markerRoles mapping");
                continue;
            }

            MarkerType mappedType = parseMarkerType(mappedRawValue);
            if (mappedType == null) {
                errors.add("markerRoles." + requiredMarker + " has unknown marker type: " + mappedRawValue);
                continue;
            }

            if (mappedType != requiredType) {
                errors.add("required marker '" + requiredMarker + "' maps to " + mappedType.name()
                    + " but expected " + requiredType.name());
            }
        }

        for (Map.Entry<String, String> entry : normalizedMarkerRoles.entrySet()) {
            if (!requiredMarkers.contains(entry.getKey())) {
                errors.add("markerRoles contains '" + entry.getKey() + "', but it is not listed in requiredMarkers");
            }
        }
    }

    private void validateSkillProfileKeys(String definitionId, NpcProfileRefs profiles, List<String> errors) {
        if (profiles == null) {
            return;
        }

        String resolvedSkillsPath = resolveProfilePath(definitionId, profiles.resolveSkillsPath());
        if (resolvedSkillsPath == null || resolvedSkillsPath.isBlank()) {
            return;
        }

        Optional<String> raw = definitions.readText(resolvedSkillsPath);
        if (raw.isEmpty()) {
            return;
        }

        try {
            JsonObject root = GSON.fromJson(raw.get(), JsonObject.class);
            if (root == null || !root.has("capabilities") || !root.get("capabilities").isJsonObject()) {
                return;
            }

            JsonObject skillFlags = root.getAsJsonObject("capabilities");
            for (Map.Entry<String, JsonElement> skillEntry : skillFlags.entrySet()) {
                if (NpcSkill.tryParse(skillEntry.getKey()).isEmpty()) {
                    errors.add("skills profile contains unknown skill key: " + skillEntry.getKey());
                }
            }
        } catch (RuntimeException ex) {
            errors.add("failed to parse skills profile: " + resolvedSkillsPath);
        }
    }

    private void validateRoutineSchedule(
        Optional<RoutineDefinition> routine,
        Set<String> requiredMarkers,
        List<String> errors
    ) {
        if (routine.isEmpty()) {
            return;
        }

        if (routine.get().schedule() == null || routine.get().schedule().isEmpty()) {
            errors.add("routine schedule must not be empty");
            return;
        }

        Set<Integer> seenStartMinutes = new HashSet<>();
        int index = 0;
        for (RoutineEntry entry : routine.get().schedule()) {
            if (entry == null) {
                errors.add("routine schedule contains null entry at index " + index);
                index++;
                continue;
            }

            if (entry.targetMarker() == null || entry.targetMarker().isBlank()) {
                errors.add("routine entry missing targetMarker at index " + index);
            } else {
                String normalizedTargetMarker = normalizeMarkerName(entry.targetMarker());
                if (normalizedTargetMarker == null || !requiredMarkers.contains(normalizedTargetMarker)) {
                    errors.add("routine targetMarker '" + entry.targetMarker() + "' is not listed in requiredMarkers");
                }
            }

            if (entry.time() == null || entry.time().isBlank()) {
                errors.add("routine entry missing time at index " + index);
            } else {
                Optional<Integer> startMinute = parseTimeToMinuteOfDay(entry.time());
                if (startMinute.isEmpty()) {
                    errors.add("routine entry has invalid time format at index " + index + ": " + entry.time());
                } else if (!seenStartMinutes.add(startMinute.get())) {
                    errors.add("routine contains duplicate start time: " + entry.time());
                }
            }

            if (entry.state() != null && !entry.state().isBlank()) {
                try {
                    NpcState.valueOf(entry.state().trim().toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException ex) {
                    errors.add("routine entry has unknown state at index " + index + ": " + entry.state());
                }
            }

            if (entry.durationMinutes() != null && entry.durationMinutes() <= 0) {
                errors.add("routine entry has non-positive durationMinutes at index " + index + ": " + entry.durationMinutes());
            }

            index++;
        }
    }

    private Optional<Integer> parseTimeToMinuteOfDay(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }

        String[] parts = raw.trim().split(":");
        if (parts.length != 2) {
            return Optional.empty();
        }

        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                return Optional.empty();
            }
            return Optional.of((hour * 60) + minute);
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private String normalizeMarkerName(String markerName) {
        if (markerName == null || markerName.isBlank()) {
            return null;
        }
        return markerName.trim().toLowerCase(Locale.ROOT);
    }

    private Set<String> normalizeRequiredMarkerNames(List<String> requiredMarkers) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        if (requiredMarkers == null) {
            return Set.copyOf(normalized);
        }

        for (String markerName : requiredMarkers) {
            String normalizedName = normalizeMarkerName(markerName);
            if (normalizedName != null) {
                normalized.add(normalizedName);
            }
        }

        return Set.copyOf(normalized);
    }

    private Map<String, String> normalizeMarkerRoles(Map<String, String> markerRoles, List<String> errors) {
        LinkedHashMap<String, String> normalized = new LinkedHashMap<>();
        if (markerRoles == null || markerRoles.isEmpty()) {
            return normalized;
        }

        for (Map.Entry<String, String> entry : markerRoles.entrySet()) {
            String normalizedKey = normalizeMarkerName(entry.getKey());
            if (normalizedKey == null) {
                errors.add("markerRoles contains blank marker key");
                continue;
            }

            String value = entry.getValue();
            if (value == null || value.isBlank()) {
                errors.add("markerRoles." + normalizedKey + " is blank");
                continue;
            }

            String normalizedValue = value.trim().toUpperCase(Locale.ROOT);
            String previous = normalized.putIfAbsent(normalizedKey, normalizedValue);
            if (previous != null && !previous.equals(normalizedValue)) {
                errors.add("markerRoles contains conflicting mappings for '" + normalizedKey + "'");
            }
        }

        return normalized;
    }

    private MarkerType parseMarkerType(String markerTypeRaw) {
        if (markerTypeRaw == null || markerTypeRaw.isBlank()) {
            return null;
        }

        try {
            return MarkerType.valueOf(markerTypeRaw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private void validateRoutineActions(Optional<RoutineDefinition> routine, Optional<ActionProfile> actionProfile, List<String> errors) {
        if (routine.isEmpty()) {
            return;
        }

        Set<String> knownActions = actionProfile
            .map(profile -> profile.actions() != null ? profile.actions().keySet() : Set.<String>of())
            .orElse(Set.of());

        for (RoutineEntry entry : routine.get().schedule()) {
            if (entry == null || entry.action() == null || entry.action().isBlank()) {
                continue;
            }
            if (!knownActions.contains(entry.action())) {
                errors.add("routine action not found in action profile: " + entry.action());
            }
        }
    }

    private void validateNavigationProfile(
        Optional<NpcNavigationProfile> navigationProfile,
        String profilePath,
        List<String> errors
    ) {
        if (navigationProfile.isEmpty()) {
            return;
        }

        NpcNavigationProfile profile = navigationProfile.get();
        String source = profilePath == null || profilePath.isBlank() ? "inline" : profilePath;

        if (profile.id() == null || profile.id().isBlank()) {
            errors.add("navigation profile missing id: " + source);
        }

        if (profile.version() == null || profile.version() <= 0) {
            errors.add("navigation profile version must be a positive integer: " + source);
        }

        validateNavigationPolicyField("pathStyle", profile.pathStyle(), NAV_PATH_STYLES, source, errors);
        validateNavigationPolicyField("doorPolicy", profile.doorPolicy(), NAV_DOOR_POLICIES, source, errors);
        validateNavigationPolicyField("dangerPolicy", profile.dangerPolicy(), NAV_DANGER_POLICIES, source, errors);
        validateNavigationPolicyField("targetPolicy", profile.targetPolicy(), NAV_TARGET_POLICIES, source, errors);
        validateNavigationPolicyField("shortcutPolicy", profile.shortcutPolicy(), NAV_SHORTCUT_POLICIES, source, errors);
        validateNavigationPolicyField("climbPolicy", profile.climbPolicy(), NAV_CLIMB_POLICIES, source, errors);
    }

    private void validateNavigationPolicyField(
        String fieldName,
        String fieldValue,
        List<String> allowedValues,
        String source,
        List<String> errors
    ) {
        if (fieldValue == null || fieldValue.isBlank()) {
            errors.add("navigation profile missing " + fieldName + ": " + source);
            return;
        }

        String normalized = fieldValue.trim().toLowerCase(Locale.ROOT);
        if (!allowedValues.contains(normalized)) {
            errors.add("navigation profile invalid " + fieldName
                + "='" + fieldValue + "' in " + source
                + ", allowed=" + String.join(",", allowedValues));
        }
    }

    private Optional<MovementProfile> buildInlineMovementProfile(NpcDefinition definition) {
        if (definition.motionControllerList().isEmpty() && definition.instructions().isEmpty()) {
            return Optional.empty();
        }

        List<MotionControllerDefinition> controllers = new ArrayList<>();
        for (NpcMotionControllerDefinition controller : definition.motionControllerList()) {
            controllers.add(new MotionControllerDefinition(
                controller.type(),
                controller.maxWalkSpeed(),
                controller.gravity(),
                controller.maxFallSpeed(),
                controller.acceleration()
            ));
        }

        List<InstructionDefinition> instructions = new ArrayList<>();
        for (NpcInstructionDefinition instruction : definition.instructions()) {
            instructions.add(new InstructionDefinition(instruction.sensor(), instruction.bodyMotion()));
        }

        return Optional.of(new MovementProfile("inline:" + definition.id(), definition.version(), controllers, instructions));
    }

    private <T> Optional<T> parseProfile(String definitionId, String relativePath, Class<T> clazz, List<String> errors) {
        if (relativePath == null || relativePath.isBlank()) {
            return Optional.empty();
        }

        String resolvedPath = resolveProfilePath(definitionId, relativePath);
        if (resolvedPath == null || resolvedPath.isBlank()) {
            return Optional.empty();
        }

        Optional<String> raw = definitions.readText(resolvedPath);
        if (raw.isEmpty()) {
            return Optional.empty();
        }

        try {
            T parsed = GSON.fromJson(raw.get(), clazz);
            if (parsed == null) {
                errors.add("profile parsed to null: " + resolvedPath);
                return Optional.empty();
            }
            return Optional.of(parsed);
        } catch (RuntimeException ex) {
            errors.add("failed to parse profile: " + resolvedPath + " as " + clazz.getSimpleName());
            return Optional.empty();
        }
    }

    private String resolveProfilePath(String definitionId, String profilePath) {
        if (profilePath == null || profilePath.isBlank()) {
            return null;
        }
        return definitions.resolveProfilePath(definitionId, profilePath);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private Optional<NpcDefinition> definitionsFromJson(String rawJson) {
        try {
            return Optional.ofNullable(GSON.fromJson(rawJson, NpcDefinition.class));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    private void recordRoleValidationErrors(String roleId, List<String> errors) {
        if (roleId == null) {
            return;
        }

        for (String error : errors) {
            addInvalidRoleReason(roleId, error);
        }
    }

    private void addInvalidRoleReason(String roleId, String reason) {
        if (roleId == null || reason == null || reason.isBlank()) {
            return;
        }

        LinkedHashSet<String> mergedReasons = new LinkedHashSet<>(invalidRoleReasonsByRoleId.getOrDefault(roleId, List.of()));
        mergedReasons.add(reason);
        invalidRoleReasonsByRoleId.put(roleId, List.copyOf(mergedReasons));
    }

    private boolean roleHasValidationIssues(String roleId) {
        if (roleId == null) {
            return false;
        }
        return !invalidRoleReasonsByRoleId.getOrDefault(roleId, List.of()).isEmpty();
    }

    private void registerRoleDefinition(String roleId, String definitionId) {
        if (roleId == null) {
            return;
        }

        LinkedHashSet<String> definitionIds = new LinkedHashSet<>(definitionIdsByRoleId.getOrDefault(roleId, List.of()));
        if (definitionId != null && !definitionId.isBlank()) {
            definitionIds.add(NpcDefinition.normalizeId(definitionId));
        }
        definitionIdsByRoleId.put(roleId, List.copyOf(definitionIds));

        if (definitionIds.size() > 1) {
            addInvalidRoleReason(roleId, "duplicate roleId '" + roleId + "' found in multiple NPC role definitions");
            addInvalidRoleReason(roleId, "RoleDefinition conflict: refusing to merge requiredMarkers");
            addInvalidRoleReason(roleId, "Spawn blocked for role " + roleId);
            removeResolvedDefinitionsForRole(roleId);
        }
    }

    private void removeResolvedDefinitionsForRole(String roleId) {
        List<String> matchingDefinitionIds = new ArrayList<>();
        for (Map.Entry<String, EffectiveNpcDefinition> entry : byId.entrySet()) {
            if (roleId.equals(entry.getValue().roleId())) {
                matchingDefinitionIds.add(entry.getKey());
            }
        }

        for (String definitionId : matchingDefinitionIds) {
            byId.remove(definitionId);
            routineByDefinitionId.remove(definitionId);
            actionByDefinitionId.remove(definitionId);
            movementByDefinitionId.remove(definitionId);
            navigationByDefinitionId.remove(definitionId);
            persistenceByDefinitionId.remove(definitionId);
        }
    }

    private String normalizeRoleId(String roleId) {
        if (roleId == null || roleId.isBlank()) {
            return null;
        }

        try {
            return NpcDefinition.normalizeId(roleId);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private NpcDefinition merge(NpcDefinition base, NpcDefinition concrete) {
        return new NpcDefinition(
            choose(concrete.id(), base.id()),
            choose(concrete.version(), base.version()),
            choose(concrete.type(), base.type()),
            choose(concrete.template(), base.template()),
            choose(concrete.displayName(), base.displayName()),
            choose(concrete.nameTranslationKey(), base.nameTranslationKey()),
            choose(concrete.npcType(), base.npcType()),
            choose(concrete.faction(), base.faction()),
            choose(concrete.role(), base.role()),
            choose(concrete.hytaleRole(), base.hytaleRole()),
            choose(concrete.appearance(), base.appearance()),
            choose(concrete.stats(), base.stats()),
            choose(concrete.drops(), base.drops()),
            choose(concrete.attitude(), base.attitude()),
            mergeProfiles(base.profiles(), concrete.profiles()),
            concrete.requiredMarkers().isEmpty() ? base.requiredMarkers() : concrete.requiredMarkers(),
            concrete.markerRoles().isEmpty() ? base.markerRoles() : concrete.markerRoles(),
            concrete.motionControllerList().isEmpty() ? base.motionControllerList() : concrete.motionControllerList(),
            concrete.instructions().isEmpty() ? base.instructions() : concrete.instructions(),
            choose(concrete.defaultState(), base.defaultState()),
            choose(concrete.debug(), base.debug())
        );
    }

    private NpcProfileRefs mergeProfiles(NpcProfileRefs base, NpcProfileRefs concrete) {
        if (base == null) {
            return concrete;
        }
        if (concrete == null) {
            return base;
        }

        return new NpcProfileRefs(
            choose(concrete.routine(), base.routine()),
            choose(concrete.skills(), base.skills()),
            choose(concrete.legacySkills(), base.legacySkills()),
            choose(concrete.actions(), base.actions()),
            choose(concrete.movement(), base.movement()),
            choose(concrete.navigation(), base.navigation()),
            choose(concrete.combat(), base.combat()),
            choose(concrete.spawn(), base.spawn()),
            choose(concrete.structure(), base.structure()),
            choose(concrete.persistence(), base.persistence())
        );
    }

    private <T> T choose(T first, T fallback) {
        return first != null ? first : fallback;
    }
}
