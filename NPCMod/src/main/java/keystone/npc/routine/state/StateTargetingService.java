package keystone.npc.routine.state;

import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;

import keystone.npc.actions.ActionProfile;
import keystone.npc.debug.NpcDebugSupport;
import keystone.npc.definition.EffectiveNpcDefinition;
import keystone.npc.definition.NpcTemplateResolver;
import keystone.npc.domain.NpcRecord;
import keystone.npc.domain.NpcState;
import keystone.npc.domain.TargetRole;
import keystone.npc.doorway.ActiveDoorPass;
import keystone.npc.doorway.PendingDoorAttempt;
import keystone.npc.markers.MarkerRecord;
import keystone.npc.markers.MarkerType;
import keystone.npc.markers.RequiredMarkerResolver;
import keystone.npc.markers.Vec3;
import keystone.npc.movement.InstructionDefinition;
import keystone.npc.movement.MotionControllerDefinition;
import keystone.npc.movement.MovementProfile;
import keystone.npc.navigation.EngineNavigationController;
import keystone.npc.navigation.NpcNavigation;
import keystone.npc.navigation.NpcNavigationProfile;
import keystone.npc.roles.RoleDefinition;
import keystone.npc.routine.RoutineDefinition;
import keystone.npc.routine.RoutineEntry;
import keystone.npc.routine.RoutineRunner;
import keystone.npc.routine.marker.MarkerResolver;
import keystone.npc.routine.pathfinding.PathfindingSupport;

public final class StateTargetingService {
    private final MarkerResolver markerResolver;
    private final PathfindingSupport pathfindingSupport;
    private final EngineNavigationController engineNavigation;
    private final Map<String, Deque<ActiveDoorPass>> activeDoorPasses;
    private final Map<String, PendingDoorAttempt> pendingDoorAttempts;
    private final Map<String, PendingDoorAttempt> pendingDoorCloseAttempts;
    private final NpcTemplateResolver templateResolver;
    private final RequiredMarkerResolver requiredMarkerResolver;
    private final RoutineRunner routineRunner;
    private final boolean engineNavigationEnabled;
    private final Predicate<NpcRecord> routineChatEnabled;
    private final BiConsumer<NpcRecord, String> routineChatSink;

    public record DesiredTarget(
        NpcState targetState,
        MarkerType markerType,
        String markerId,
        Vec3 targetPosition,
        String actionId,
        String source
    ) {
    }

    public StateTargetingService(
        MarkerResolver markerResolver,
        PathfindingSupport pathfindingSupport,
        EngineNavigationController engineNavigation,
        Map<String, Deque<ActiveDoorPass>> activeDoorPasses,
        Map<String, PendingDoorAttempt> pendingDoorAttempts,
        Map<String, PendingDoorAttempt> pendingDoorCloseAttempts,
        NpcTemplateResolver templateResolver,
        RequiredMarkerResolver requiredMarkerResolver,
        RoutineRunner routineRunner,
        boolean engineNavigationEnabled,
        Predicate<NpcRecord> routineChatEnabled,
        BiConsumer<NpcRecord, String> routineChatSink
    ) {
        this.markerResolver = markerResolver;
        this.pathfindingSupport = pathfindingSupport;
        this.engineNavigation = engineNavigation;
        this.activeDoorPasses = activeDoorPasses;
        this.pendingDoorAttempts = pendingDoorAttempts;
        this.pendingDoorCloseAttempts = pendingDoorCloseAttempts;
        this.templateResolver = Objects.requireNonNull(templateResolver);
        this.requiredMarkerResolver = Objects.requireNonNull(requiredMarkerResolver);
        this.routineRunner = Objects.requireNonNull(routineRunner);
        this.engineNavigationEnabled = engineNavigationEnabled;
        this.routineChatEnabled = Objects.requireNonNull(routineChatEnabled);
        this.routineChatSink = Objects.requireNonNull(routineChatSink);
    }

    public boolean startNavigationToBed(NpcRecord npc) {
        return startNavigationToMarker(npc, MarkerType.BED, NpcState.SLEEPING);
    }

    public boolean startNavigationToWork(NpcRecord npc) {
        return startNavigationToMarker(npc, MarkerType.WORK, NpcState.WORKING);
    }

    public boolean startNavigationToTarget(NpcRecord npc, DesiredTarget desiredTarget) {
        if (desiredTarget == null
            || desiredTarget.targetState() == null
            || desiredTarget.markerType() == null
            || desiredTarget.targetPosition() == null) {
            return false;
        }

        return startNavigationToResolvedTarget(
            npc,
            desiredTarget.markerType(),
            desiredTarget.markerId(),
            desiredTarget.targetPosition(),
            desiredTarget.targetState(),
            desiredTarget.source()
        );
    }

    public boolean startNavigationToMarker(NpcRecord npc, MarkerType markerType, NpcState targetState) {
        Optional<MarkerRecord> marker = markerResolver.resolveRequiredMarkerReadOnly(npc, markerType);
        if (marker.isEmpty()) {
            warnOnce(
                npc,
                "marker-missing:" + markerType.name(),
                "[KNPC][Warning] " + npc.npcName() + " routine target marker '"
                    + markerType.name().toLowerCase(Locale.ROOT)
                    + "' has no assigned position."
            );
            npc.state(NpcState.PAUSED_MISSING_MARKER);
            return false;
        }

        MarkerRecord markerRecord = marker.get();
        return startNavigationToResolvedTarget(
            npc,
            markerType,
            markerRecord.markerId(),
            markerRecord.position(),
            targetState,
            npc.movementProfileId() != null ? "json" : "fallback"
        );
    }

    private boolean startNavigationToResolvedTarget(
        NpcRecord npc,
        MarkerType markerType,
        String markerId,
        Vec3 markerPos,
        NpcState targetState,
        String source
    ) {
        Vec3 startPos = pathfindingSupport.resolveNavigationStartPosition(npc, markerPos);
        long durationMs = NpcNavigation.calculateDurationMs(startPos, markerPos);

        npc.navigationState().startNavigation(startPos, markerPos, durationMs, targetState, markerType, markerId);
        npc.state(walkingStateForMarker(markerType));
        clearDoorTracking(npc.npcId());

        if (engineNavigationEnabled) {
            engineNavigation.setTarget(npc.entityRef(), markerPos, npc.motionControllerType());
        }

        String movementSource = source == null || source.isBlank()
            ? (npc.movementProfileId() != null ? "json" : "fallback")
            : source;
        System.out.println("[KNPC][Movement] " + npc.npcName()
            + " target=" + markerType.name().toLowerCase(Locale.ROOT)
            + " markerId=" + nullToDash(markerId)
            + " pos=" + formatPosition(markerPos)
            + " stopDistance=" + nullToDashDouble(npc.stopDistance())
            + " slowDownDistance=" + nullToDashDouble(npc.slowDownDistance())
            + " usePathfinder=" + nullToDashBoolean(npc.usePathfinder())
            + " useSteering=" + nullToDashBoolean(npc.useSteering())
            + " motionControllerType=" + nullToDash(npc.motionControllerType())
            + " source=" + movementSource);
        return true;
    }

    public DesiredTarget resolveDesiredTarget(World world, NpcRecord npc, RoleDefinition roleDefinition) {
        try {
            WorldTimeResource worldTimeResource = world.getEntityStore().getStore()
                .getResource(WorldTimeResource.getResourceType());

            applyMovementProfile(npc, templateResolver.resolveMovementProfile(npc.roleId()));
            applyNavigationProfile(npc, templateResolver.resolveNavigationProfile(npc.roleId()));

            int minuteOfDay = resolveMinuteOfDay(worldTimeResource);
            Set<MarkerType> allowedMarkers = requiredMarkerTypes(npc, roleDefinition);
            Optional<RoutineDefinition> routine = templateResolver.resolveRoutineByRoleId(npc.roleId());
            if (routine.isEmpty()) {
                warnOnce(
                    npc,
                    "routine-missing:" + npc.roleId(),
                    "[KNPC][Warning] No routine loaded for role " + npc.roleId() + ". NPC stays IDLE."
                );
                npc.state(NpcState.IDLE);
                return null;
            }

            Optional<RoutineEntry> activeEntry = routineRunner.findActiveEntry(routine.get(), minuteOfDay);
            if (activeEntry.isEmpty()) {
                warnOnce(
                    npc,
                    "routine-no-active-entry",
                    "[KNPC][Warning] " + npc.npcName()
                        + " has a routine profile but no valid active entry at time="
                        + formatMinuteOfDay(minuteOfDay)
                        + "."
                );
                npc.state(NpcState.IDLE);
                return null;
            }

            RoutineEntry entry = activeEntry.get();
            Optional<MarkerType> routineMarker = resolveRoutineMarkerType(npc.roleId(), entry.targetMarker());
            if (routineMarker.isEmpty()) {
                warnOnce(
                    npc,
                    "routine-marker-unresolved:" + nullToDash(entry.targetMarker()),
                    "[KNPC][Warning] " + npc.npcName() + " routine target marker '"
                        + nullToDash(entry.targetMarker())
                        + "' is configured but cannot be mapped to a valid marker type."
                );
                npc.state(NpcState.PAUSED_MISSING_MARKER);
                return null;
            }

            if (!allowedMarkers.contains(routineMarker.get())) {
                warnOnce(
                    npc,
                    "routine-marker-invalid-for-role:" + routineMarker.get().name(),
                    "[KNPC][Warning] Routine marker " + routineMarker.get().name()
                        + " is not valid for role " + npc.roleId() + "."
                        + " Allowed markers: " + formatAllowedMarkers(allowedMarkers)
                );
                npc.state(NpcState.PAUSED_MISSING_MARKER);
                return null;
            }

            String previousMarker = npc.activeRoutineMarker();
            NpcState previousState = npc.activeRoutineState();
            String previousAction = npc.activeRoutineActionId();
            String previousSource = npc.activeRoutineSource();
            String nextMarker = normalizeMarkerName(entry.targetMarker());
            String actionId = normalizeActionId(
                npc,
                entry.action(),
                templateResolver.resolveActionProfile(npc.roleId()),
                "json",
                minuteOfDay
            );
            NpcState entryState = resolveEntryState(entry.state(), routineMarker.get());
            npc.activeRoutineMarker(nextMarker);
            npc.activeRoutineState(entryState);
            npc.activeRoutineActionId(actionId);
            npc.activeRoutineSource("json");
            logRoutineTargetChange(
                npc,
                previousMarker,
                previousState,
                previousAction,
                previousSource,
                nextMarker,
                entryState,
                actionId,
                "json",
                minuteOfDay
            );

            Optional<MarkerRecord> resolvedMarker = markerResolver.resolveRequiredMarkerReadOnly(npc, routineMarker.get());
            if (resolvedMarker.isEmpty()) {
                warnOnce(
                    npc,
                    "routine-marker-missing:" + routineMarker.get().name(),
                    "[KNPC][Warning] " + npc.npcName() + " routine marker '"
                        + routineMarker.get().name().toLowerCase(Locale.ROOT)
                        + "' has no assigned position."
                );
                npc.state(NpcState.PAUSED_MISSING_MARKER);
                return null;
            }

            MarkerRecord markerRecord = resolvedMarker.get();
            return new DesiredTarget(
                entryState,
                routineMarker.get(),
                markerRecord.markerId(),
                markerRecord.position(),
                actionId,
                "json"
            );
        } catch (Exception e) {
            System.err.println("[KNPC][Warning] " + npc.npcName() + " has no valid routine target: time query failed ("
                + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
            npc.state(NpcState.PAUSED_MISSING_MARKER);
            return null;
        }
    }

    public boolean hasRequiredMarkers(NpcRecord npc, RoleDefinition roleDefinition) {
        for (MarkerType markerType : requiredMarkerTypes(npc, roleDefinition)) {
            if (markerResolver.resolveRequiredMarkerReadOnly(npc, markerType).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public String missingRequiredMarkers(NpcRecord npc, RoleDefinition roleDefinition) {
        List<String> missing = new java.util.ArrayList<>();
        for (MarkerType markerType : requiredMarkerTypes(npc, roleDefinition)) {
            if (markerResolver.resolveRequiredMarkerReadOnly(npc, markerType).isEmpty()) {
                missing.add(markerType.name().toLowerCase(Locale.ROOT));
            }
        }
        return String.join(",", missing);
    }

    private Set<MarkerType> requiredMarkerTypes(NpcRecord npc, RoleDefinition roleDefinition) {
        LinkedHashSet<MarkerType> resolved = new LinkedHashSet<>();

        for (RequiredMarkerResolver.Requirement requirement : requiredMarkerResolver.resolveRequirements(npc.roleId())) {
            if (requirement.markerType() != null) {
                resolved.add(requirement.markerType());
            }
        }
        return resolved;
    }

    private void clearDoorTracking(String npcId) {
        activeDoorPasses.remove(npcId);
        pendingDoorAttempts.remove(npcId);
        pendingDoorCloseAttempts.remove(npcId);
    }

    private int resolveMinuteOfDay(WorldTimeResource worldTimeResource) {
        float dayProgress = worldTimeResource.getDayProgress();
        if (!Float.isFinite(dayProgress)) {
            return Math.max(0, Math.min(23, worldTimeResource.getCurrentHour())) * 60;
        }

        int minuteOfDay = (int) Math.floor(dayProgress * 24.0f * 60.0f);
        if (minuteOfDay < 0) {
            minuteOfDay = 0;
        }
        return minuteOfDay % (24 * 60);
    }

    private Optional<MarkerType> resolveRoutineMarkerType(String definitionId, String targetMarker) {
        if (definitionId == null || definitionId.isBlank()) {
            return Optional.empty();
        }

        String markerName = normalizeMarkerName(targetMarker);
        if (markerName == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(MarkerType.valueOf(markerName.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private NpcState resolveEntryState(String rawState, MarkerType markerType) {
        NpcState fallback = idleStateForMarker(markerType);
        if (rawState == null || rawState.isBlank()) {
            return fallback;
        }

        try {
            NpcState parsed = NpcState.valueOf(rawState.trim().toUpperCase(Locale.ROOT));
            TargetRole expectedRole = markerRoleForType(markerType);
            if (parsed.markerRole() == TargetRole.NONE || parsed.markerRole() == expectedRole) {
                return parsed;
            }
            return fallback;
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private TargetRole markerRoleForType(MarkerType markerType) {
        return switch (markerType) {
            case BED -> TargetRole.BED;
            case DOOR -> TargetRole.DOOR;
            case CHEST -> TargetRole.CHEST;
            case FOOD -> TargetRole.FOOD;
            case WORK -> TargetRole.WORK;
            case CHILL -> TargetRole.CHILL;
        };
    }

    private NpcState idleStateForMarker(MarkerType markerType) {
        return switch (markerType) {
            case BED -> NpcState.SLEEPING;
            case DOOR -> NpcState.OPENING_DOOR;
            case CHEST -> NpcState.USING_CHEST;
            case FOOD -> NpcState.EATING;
            case WORK -> NpcState.WORKING;
            case CHILL -> NpcState.CHILLING;
        };
    }

    private NpcState walkingStateForMarker(MarkerType markerType) {
        return switch (markerType) {
            case BED -> NpcState.WALKING_TO_BED;
            case DOOR -> NpcState.WALKING_TO_DOOR;
            case CHEST -> NpcState.WALKING_TO_CHEST;
            case FOOD -> NpcState.WALKING_TO_FOOD;
            case WORK -> NpcState.WALKING_TO_WORK;
            case CHILL -> NpcState.WALKING_TO_CHILL;
        };
    }

    private String normalizeActionId(
        NpcRecord npc,
        String actionId,
        Optional<ActionProfile> actionProfile,
        String source,
        int minuteOfDay
    ) {
        if (actionId == null || actionId.isBlank()) {
            return null;
        }
        if (actionProfile.isEmpty()) {
            warnOnce(
                npc,
                "action-profile-missing",
                "[KNPC][Warning] " + npc.npcName() + " routine action '" + actionId
                    + "' cannot be validated because action profile is missing."
            );
            return null;
        }
        if (actionProfile.isPresent()
            && actionProfile.get().actions() != null
            && !actionProfile.get().actions().containsKey(actionId)) {
            warnOnce(
                npc,
                "action-missing:" + actionId + ":" + source,
                "[KNPC][Warning] " + npc.npcName() + " routine action '" + actionId
                    + "' is not defined in action profile at time=" + formatMinuteOfDay(minuteOfDay) + "."
            );
            return null;
        }
        return actionId;
    }

    private String normalizeMarkerName(String markerName) {
        if (markerName == null || markerName.isBlank()) {
            return null;
        }
        return markerName.trim().toLowerCase(Locale.ROOT);
    }

    private void applyMovementProfile(NpcRecord npc, Optional<MovementProfile> movementProfile) {
        if (movementProfile.isEmpty()) {
            if (!npc.movementProfileMissingWarned()) {
                warnOnce(
                    npc,
                    "movement-profile-missing",
                    "[KNPC][Warning] " + npc.npcName()
                        + " has no movement profile; runtime will use fallback movement tuning."
                );
                npc.movementProfileMissingWarned(true);
            }
            clearMovementTuning(npc);
            return;
        }

        npc.movementProfileMissingWarned(false);
        MovementProfile profile = movementProfile.get();
        String previousProfileId = npc.movementProfileId();
        npc.movementProfileId(profile.id());

        MotionControllerDefinition controller = profile.motionControllerList().isEmpty()
            ? null
            : profile.motionControllerList().get(0);
        npc.motionControllerType(controller != null ? controller.type() : null);
        npc.maxWalkSpeed(controller != null ? controller.maxWalkSpeed() : null);

        InstructionDefinition selectedInstruction = selectMovementInstruction(profile.instructions());
        if (selectedInstruction == null || selectedInstruction.bodyMotion() == null) {
            npc.stopDistance(null);
            npc.slowDownDistance(null);
            npc.usePathfinder(null);
            npc.useSteering(null);
        } else {
            Map<String, Object> bodyMotion = selectedInstruction.bodyMotion();
            npc.stopDistance(positiveOrNull(asDouble(bodyMotion.get("stopDistance"))));
            npc.slowDownDistance(positiveOrNull(asDouble(bodyMotion.get("slowDownDistance"))));
            npc.usePathfinder(asBoolean(bodyMotion.get("usePathfinder")));
            npc.useSteering(asBoolean(bodyMotion.get("useSteering")));
        }

        if (!Objects.equals(previousProfileId, npc.movementProfileId())) {
            System.out.println("[KNPC][Movement] " + npc.npcName()
                + " profile=" + npc.movementProfileId()
                + " motionController=" + nullToDash(npc.motionControllerType())
                + " stopDistance=" + nullToDashDouble(npc.stopDistance())
                + " slowDownDistance=" + nullToDashDouble(npc.slowDownDistance())
                + " usePathfinder=" + nullToDashBoolean(npc.usePathfinder())
                + " useSteering=" + nullToDashBoolean(npc.useSteering())
                + " source=json");
        }
    }

    private void clearMovementTuning(NpcRecord npc) {
        npc.movementProfileId(null);
        npc.motionControllerType(null);
        npc.maxWalkSpeed(null);
        npc.stopDistance(null);
        npc.slowDownDistance(null);
        npc.usePathfinder(null);
        npc.useSteering(null);
    }

    private void applyNavigationProfile(NpcRecord npc, Optional<NpcNavigationProfile> navigationProfile) {
        String previousNavigationProfileId = npc.navigationProfileId();

        if (navigationProfile.isEmpty()) {
            if (previousNavigationProfileId != null) {
                npc.navigationProfileId(null);
                npc.navigationProfileVersion(null);
                npc.navigationPathStyle(null);
                npc.navigationDoorPolicy(null);
                npc.navigationDangerPolicy(null);
                npc.navigationTargetPolicy(null);
                npc.navigationShortcutPolicy(null);
                npc.navigationClimbPolicy(null);
            }
            return;
        }

        NpcNavigationProfile profile = navigationProfile.get();
        String profileId = profile.id() == null || profile.id().isBlank() ? "unnamed" : profile.id();

        npc.navigationProfileId(profileId);
    npc.navigationProfileVersion(profile.version());
    npc.navigationPathStyle(profile.pathStyle());
    npc.navigationDoorPolicy(profile.doorPolicy());
    npc.navigationDangerPolicy(profile.dangerPolicy());
    npc.navigationTargetPolicy(profile.targetPolicy());
    npc.navigationShortcutPolicy(profile.shortcutPolicy());
    npc.navigationClimbPolicy(profile.climbPolicy());

        if (Objects.equals(previousNavigationProfileId, profileId)) {
            return;
        }

        if (isMotionDebugEnabled(npc)) {
            String message = "Navigation profile loaded: " + profileId;
            System.out.println("[KNPC][Movement] " + npc.npcName() + " " + message);
            NpcDebugSupport.sendGlobalChat("[KNPC][Movement] " + npc.npcName() + " " + message);
        }
    }

    private boolean isMotionDebugEnabled(NpcRecord npc) {
        return templateResolver.resolveByRoleId(npc.roleId())
            .map(EffectiveNpcDefinition::definition)
            .map(def -> def.debug())
            .map(debug -> debug != null && Boolean.TRUE.equals(debug.logMotionChanges()))
            .orElse(false);
    }

    private InstructionDefinition selectMovementInstruction(List<InstructionDefinition> instructions) {
        if (instructions == null || instructions.isEmpty()) {
            return null;
        }

        for (InstructionDefinition instruction : instructions) {
            if (instruction == null || instruction.sensor() == null) {
                continue;
            }
            Object sensorType = instruction.sensor().get("type");
            if (sensorType instanceof String sensorTypeString && "Leash".equalsIgnoreCase(sensorTypeString)) {
                return instruction;
            }
        }

        for (InstructionDefinition instruction : instructions) {
            if (instruction == null || instruction.bodyMotion() == null) {
                continue;
            }
            Object bodyType = instruction.bodyMotion().get("type");
            if (bodyType instanceof String bodyTypeString && "Seek".equalsIgnoreCase(bodyTypeString)) {
                return instruction;
            }
        }

        return instructions.get(0);
    }

    private Double asDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            try {
                return Double.valueOf(stringValue);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Boolean asBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            return Boolean.valueOf(stringValue);
        }
        return null;
    }

    private Double positiveOrNull(Double value) {
        if (value == null || !Double.isFinite(value) || value <= 0.0d) {
            return null;
        }
        return value;
    }

    private void logRoutineTargetChange(
        NpcRecord npc,
        String previousMarker,
        NpcState previousState,
        String previousAction,
        String previousSource,
        String nextMarker,
        NpcState targetState,
        String actionId,
        String source,
        int minuteOfDay
    ) {
        if (Objects.equals(previousMarker, nextMarker)
            && previousState == targetState
            && Objects.equals(previousAction, actionId)
            && Objects.equals(previousSource, source)) {
            return;
        }

        System.out.println("[KNPC][Routine] " + npc.npcName()
            + " changed routine: " + markerState(previousMarker, previousState)
            + " -> " + markerState(nextMarker, targetState)
            + " action=" + nullToDash(actionId)
            + " time=" + formatMinuteOfDay(minuteOfDay)
            + " source=" + source
            + " npcId=" + npc.npcId());

        if (routineChatEnabled.test(npc)) {
            routineChatSink.accept(npc, buildRoutineChatMessage(
                npc,
                previousMarker,
                previousState,
                previousAction,
                nextMarker,
                targetState,
                minuteOfDay
            ));
        }
    }

    private String buildRoutineChatMessage(
        NpcRecord npc,
        String previousMarker,
        NpcState previousState,
        String previousAction,
        String nextMarker,
        NpcState targetState,
        int minuteOfDay
    ) {
        String timeLabel = formatMinuteOfDay(minuteOfDay);
        String nextLabel = markerStateForChat(nextMarker, targetState);
        String header = "[KNPC] [Routine] " + npc.npcName();

        if ((previousMarker == null || previousMarker.isBlank())
            && previousState == null
            && (previousAction == null || previousAction.isBlank())) {
            return header + "\n"
                + "startet " + nextLabel + "\n"
                + "time=" + timeLabel;
        }

        return header + "\n"
            + markerStateForChat(previousMarker, previousState)
            + " -> " + nextLabel + "\n"
            + "time=" + timeLabel;
    }

    private String markerStateForChat(String marker, NpcState state) {
        return markerLabelForChat(marker) + "/" + (state == null ? "none" : state.name());
    }

    private String markerLabelForChat(String marker) {
        if (marker == null || marker.isBlank()) {
            return "none";
        }
        return marker.trim().toLowerCase(Locale.ROOT);
    }

    private String markerState(String marker, NpcState state) {
        return markerLabel(marker) + "/" + (state == null ? "-" : state.name());
    }

    private String markerLabel(String marker) {
        if (marker == null || marker.isBlank()) {
            return "-";
        }
        return marker.trim().toUpperCase(Locale.ROOT);
    }

    private String formatAllowedMarkers(Set<MarkerType> allowedMarkers) {
        if (allowedMarkers == null || allowedMarkers.isEmpty()) {
            return "none";
        }

        List<String> values = new java.util.ArrayList<>();
        for (MarkerType markerType : allowedMarkers) {
            values.add(markerType.name());
        }
        return String.join(", ", values);
    }

    private String formatMinuteOfDay(int minuteOfDay) {
        int hour = Math.max(0, Math.min(23, minuteOfDay / 60));
        int minute = Math.max(0, minuteOfDay % 60);
        return String.format(Locale.ROOT, "%02d:%02d", hour, minute);
    }

    private String formatPosition(Vec3 pos) {
        if (pos == null) {
            return "-";
        }
        return String.format(Locale.ROOT, "%.2f,%.2f,%.2f", pos.x(), pos.y(), pos.z());
    }

    private void warnOnce(NpcRecord npc, String key, String message) {
        if (Objects.equals(npc.lastValidationWarningKey(), key)) {
            return;
        }
        npc.lastValidationWarningKey(key);
        System.err.println(message);
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String nullToDashDouble(Double value) {
        return value == null ? "-" : String.format(Locale.ROOT, "%.3f", value);
    }

    private String nullToDashBoolean(Boolean value) {
        return value == null ? "-" : value.toString();
    }
}
