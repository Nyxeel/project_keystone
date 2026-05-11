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
import keystone.npc.capabilities.CapabilityResolver;
import keystone.npc.capabilities.CapabilitySet;
import keystone.npc.capabilities.NpcCapability;
import keystone.npc.domain.NpcState;
import keystone.npc.markers.MarkerType;
import keystone.npc.movement.InstructionDefinition;
import keystone.npc.movement.MotionControllerDefinition;
import keystone.npc.movement.MovementProfile;
import keystone.npc.navigation.NpcNavigationProfile;
import keystone.npc.routine.RoutineDefinition;
import keystone.npc.routine.RoutineEntry;

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
    private final CapabilityResolver capabilityResolver;
    private final Map<String, EffectiveNpcDefinition> byId = new LinkedHashMap<>();
    private final Map<String, RoutineDefinition> routineByDefinitionId = new LinkedHashMap<>();
    private final Map<String, ActionProfile> actionByDefinitionId = new LinkedHashMap<>();
    private final Map<String, MovementProfile> movementByDefinitionId = new LinkedHashMap<>();
    private final Map<String, NpcNavigationProfile> navigationByDefinitionId = new LinkedHashMap<>();

    public NpcTemplateResolver(NpcDefinitionRegistry definitions, CapabilityResolver capabilityResolver) {
        this.definitions = Objects.requireNonNull(definitions, "definitions");
        this.capabilityResolver = Objects.requireNonNull(capabilityResolver, "capabilityResolver");
    }

    public synchronized void reload() {
        byId.clear();
        routineByDefinitionId.clear();
        actionByDefinitionId.clear();
        movementByDefinitionId.clear();
        navigationByDefinitionId.clear();
        capabilityResolver.clearCache();

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

    public synchronized List<String> definitionIds() {
        return List.copyOf(byId.keySet());
    }

    public synchronized Optional<RoutineDefinition> resolveRoutine(String definitionId) {
        if (definitionId == null || definitionId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(routineByDefinitionId.get(NpcDefinition.normalizeId(definitionId)));
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

        NpcProfileRefs profiles = merged.profiles();
        validateProfilePathsExist(profiles, errors);
        validateDefaultState(merged, errors);
        validateCapabilityProfileKeys(profiles, errors);

        Optional<RoutineDefinition> routine = parseProfile(profiles != null ? profiles.routine() : null, RoutineDefinition.class, errors);
        Optional<ActionProfile> actionProfile = parseProfile(profiles != null ? profiles.actions() : null, ActionProfile.class, errors);
        Optional<MovementProfile> movement = parseProfile(profiles != null ? profiles.movement() : null, MovementProfile.class, errors);
        Optional<NpcNavigationProfile> navigationProfile = parseProfile(
            profiles != null ? profiles.navigation() : null,
            NpcNavigationProfile.class,
            errors
        );
        validateNavigationProfile(navigationProfile, profiles != null ? profiles.navigation() : null, errors);

        merged = reconcileRequiredMarkersFromRoutine(merged, routine);

        if (movement.isEmpty()) {
            movement = buildInlineMovementProfile(merged);
        }

        validateMarkerMapping(merged, errors);
        validateRoutineSchedule(routine, errors);
        validateRoutineActions(routine, actionProfile, errors);

        if (!errors.isEmpty()) {
            for (String error : errors) {
                System.err.println("[KeystoneNPC] NPC definition rejected: id=" + concrete.id() + " reason=" + error);
            }
            return;
        }

        CapabilitySet capabilities = capabilityResolver.resolve(merged.profiles());
        EffectiveNpcDefinition effective = new EffectiveNpcDefinition(merged, capabilities);
        byId.put(effective.id(), effective);
        routine.ifPresent(value -> routineByDefinitionId.put(effective.id(), value));
        actionProfile.ifPresent(value -> actionByDefinitionId.put(effective.id(), value));
        movement.ifPresent(value -> movementByDefinitionId.put(effective.id(), value));
        navigationProfile.ifPresent(value -> navigationByDefinitionId.put(effective.id(), value));
    }

    private void validateProfilePathsExist(NpcProfileRefs profiles, List<String> errors) {
        if (profiles == null) {
            return;
        }

        validateProfilePathExists("routine", profiles.routine(), errors);
        validateProfilePathExists("capabilities", profiles.capabilities(), errors);
        validateProfilePathExists("actions", profiles.actions(), errors);
        validateProfilePathExists("movement", profiles.movement(), errors);
        validateProfilePathExists("navigation", profiles.navigation(), errors);
        validateProfilePathExists("combat", profiles.combat(), errors);
        validateProfilePathExists("spawn", profiles.spawn(), errors);
        validateProfilePathExists("structure", profiles.structure(), errors);
        validateProfilePathExists("persistence", profiles.persistence(), errors);
    }

    private void validateProfilePathExists(String profileKey, String relativePath, List<String> errors) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        if (definitions.readText(relativePath).isEmpty()) {
            errors.add("profiles." + profileKey + " not found: " + relativePath);
        }
    }

    private void validateDefaultState(NpcDefinition merged, List<String> errors) {
        String defaultState = merged.defaultState();
        if (defaultState == null || defaultState.isBlank()) {
            return;
        }
        try {
            NpcState.valueOf(defaultState.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            errors.add("unknown defaultState: " + defaultState);
        }
    }

    private void validateMarkerMapping(NpcDefinition merged, List<String> errors) {
        Set<String> required = new HashSet<>();
        for (String markerName : merged.requiredMarkers()) {
            if (markerName != null && !markerName.isBlank()) {
                required.add(markerName.trim().toLowerCase());
            }
        }

        Map<String, String> markerRoles = merged.markerRoles();
        for (String requiredMarker : required) {
            if (!markerRoles.containsKey(requiredMarker)) {
                errors.add("requiredMarkers entry has no markerRoles mapping: " + requiredMarker);
            }
        }

        for (Map.Entry<String, String> entry : markerRoles.entrySet()) {
            String markerTypeRaw = entry.getValue();
            if (markerTypeRaw == null || markerTypeRaw.isBlank()) {
                errors.add("markerRoles." + entry.getKey() + " is blank");
                continue;
            }
            try {
                MarkerType.valueOf(markerTypeRaw.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                errors.add("markerRoles." + entry.getKey() + " has unknown marker type: " + markerTypeRaw);
            }
        }
    }

    private void validateCapabilityProfileKeys(NpcProfileRefs profiles, List<String> errors) {
        if (profiles == null || profiles.capabilities() == null || profiles.capabilities().isBlank()) {
            return;
        }

        Optional<String> raw = definitions.readText(profiles.capabilities());
        if (raw.isEmpty()) {
            return;
        }

        try {
            JsonObject root = GSON.fromJson(raw.get(), JsonObject.class);
            if (root == null || !root.has("capabilities") || !root.get("capabilities").isJsonObject()) {
                return;
            }

            JsonObject capabilities = root.getAsJsonObject("capabilities");
            for (Map.Entry<String, JsonElement> capabilityEntry : capabilities.entrySet()) {
                if (NpcCapability.tryParse(capabilityEntry.getKey()).isEmpty()) {
                    errors.add("capabilities profile contains unknown capability key: " + capabilityEntry.getKey());
                }
            }
        } catch (RuntimeException ex) {
            errors.add("failed to parse capabilities profile: " + profiles.capabilities());
        }
    }

    private NpcDefinition reconcileRequiredMarkersFromRoutine(NpcDefinition merged, Optional<RoutineDefinition> routine) {
        LinkedHashSet<String> scheduledMarkers = new LinkedHashSet<>();
        if (routine.isPresent()) {
            for (RoutineEntry entry : routine.get().schedule()) {
                if (entry == null || entry.targetMarker() == null || entry.targetMarker().isBlank()) {
                    continue;
                }
                String markerName = normalizeMarkerName(entry.targetMarker());
                if (markerName != null) {
                    scheduledMarkers.add(markerName);
                }
            }
        }

        LinkedHashSet<String> declaredMarkers = new LinkedHashSet<>();
        for (String markerName : merged.requiredMarkers()) {
            String normalized = normalizeMarkerName(markerName);
            if (normalized != null) {
                declaredMarkers.add(normalized);
            }
        }

        LinkedHashSet<String> reconciledRequiredMarkers = new LinkedHashSet<>(declaredMarkers);
        if (reconciledRequiredMarkers.isEmpty()) {
            // Backward compatibility: older definitions may omit requiredMarkers and only declare routine markers.
            reconciledRequiredMarkers.addAll(scheduledMarkers);
        }

        LinkedHashMap<String, String> reconciledMarkerRoles = new LinkedHashMap<>(merged.markerRoles());
        for (String markerName : reconciledRequiredMarkers) {
            if (reconciledMarkerRoles.containsKey(markerName)) {
                continue;
            }
            try {
                MarkerType markerType = MarkerType.valueOf(markerName.toUpperCase(Locale.ROOT));
                reconciledMarkerRoles.put(markerName, markerType.name());
            } catch (IllegalArgumentException ignored) {
                // Keep unresolved markers for validation to report clearly.
            }
        }

        return new NpcDefinition(
            merged.id(),
            merged.version(),
            merged.type(),
            merged.template(),
            merged.displayName(),
            merged.nameTranslationKey(),
            merged.npcType(),
            merged.faction(),
            merged.role(),
            merged.appearance(),
            merged.stats(),
            merged.drops(),
            merged.attitude(),
            merged.profiles(),
            List.copyOf(reconciledRequiredMarkers),
            reconciledMarkerRoles,
            merged.motionControllerList(),
            merged.instructions(),
            merged.defaultState(),
            merged.debug()
        );
    }

    private void validateRoutineSchedule(Optional<RoutineDefinition> routine, List<String> errors) {
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

    private <T> Optional<T> parseProfile(String relativePath, Class<T> clazz, List<String> errors) {
        if (relativePath == null || relativePath.isBlank()) {
            return Optional.empty();
        }

        Optional<String> raw = definitions.readText(relativePath);
        if (raw.isEmpty()) {
            return Optional.empty();
        }

        try {
            T parsed = GSON.fromJson(raw.get(), clazz);
            if (parsed == null) {
                errors.add("profile parsed to null: " + relativePath);
                return Optional.empty();
            }
            return Optional.of(parsed);
        } catch (RuntimeException ex) {
            errors.add("failed to parse profile: " + relativePath + " as " + clazz.getSimpleName());
            return Optional.empty();
        }
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
            choose(concrete.capabilities(), base.capabilities()),
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