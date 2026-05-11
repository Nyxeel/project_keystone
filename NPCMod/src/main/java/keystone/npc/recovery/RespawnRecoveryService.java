package keystone.npc.recovery;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import com.hypixel.hytale.server.core.universe.world.World;

import keystone.npc.domain.NpcRecord;
import keystone.npc.roles.RoleDefinition;
import keystone.npc.roles.RoleDefinitionRegistry;
import keystone.npc.routine.marker.MarkerResolver;
import keystone.npc.markers.MarkerRecord;
import keystone.npc.markers.MarkerType;

public final class RespawnRecoveryService {
    @FunctionalInterface
    public interface SpawnContextFormatter {
        String format(NpcRecord npc, String trigger, World world, RoleDefinition roleDefinition, Integer roleIndex);
    }

    private final Map<String, NpcRecord> npcs;
    private final Set<String> spawnRequestsInFlight;
    private final Map<String, Long> respawnRetryAtMs;
    private final Map<String, Integer> respawnFailureCounts;
    private final Map<String, Integer> uuidRelinkMissCounts;
    private final Map<String, Long> uuidRelinkFirstMissAtMs;
    private final RoleDefinitionRegistry roleDefinitions;
    private final MarkerResolver markerResolver;
    private final long respawnRetryBaseMs;
    private final long respawnRetryMaxMs;
    private final int respawnMaxFailures;
    private final BiConsumer<String, String> logSevereSink;
    private final SpawnContextFormatter spawnContextFormatter;

    public RespawnRecoveryService(
        Map<String, NpcRecord> npcs,
        Set<String> spawnRequestsInFlight,
        Map<String, Long> respawnRetryAtMs,
        Map<String, Integer> respawnFailureCounts,
        Map<String, Integer> uuidRelinkMissCounts,
        Map<String, Long> uuidRelinkFirstMissAtMs,
        RoleDefinitionRegistry roleDefinitions,
        MarkerResolver markerResolver,
        long respawnRetryBaseMs,
        long respawnRetryMaxMs,
        int respawnMaxFailures,
        BiConsumer<String, String> logSevereSink,
        SpawnContextFormatter spawnContextFormatter
    ) {
        this.npcs = npcs;
        this.spawnRequestsInFlight = spawnRequestsInFlight;
        this.respawnRetryAtMs = respawnRetryAtMs;
        this.respawnFailureCounts = respawnFailureCounts;
        this.uuidRelinkMissCounts = uuidRelinkMissCounts;
        this.uuidRelinkFirstMissAtMs = uuidRelinkFirstMissAtMs;
        this.roleDefinitions = roleDefinitions;
        this.markerResolver = markerResolver;
        this.respawnRetryBaseMs = respawnRetryBaseMs;
        this.respawnRetryMaxMs = respawnRetryMaxMs;
        this.respawnMaxFailures = respawnMaxFailures;
        this.logSevereSink = logSevereSink;
        this.spawnContextFormatter = spawnContextFormatter;
    }

    public void clearRespawnFailureState(String npcId) {
        respawnFailureCounts.remove(npcId);
        respawnRetryAtMs.remove(npcId);
        uuidRelinkMissCounts.remove(npcId);
        uuidRelinkFirstMissAtMs.remove(npcId);
    }

    public void scheduleRespawnRetry(String npcId, String reason) {
        int failureCount = respawnFailureCounts.getOrDefault(npcId, 0) + 1;
        respawnFailureCounts.put(npcId, failureCount);

        NpcRecord npc = npcs.get(npcId);

        if (failureCount >= respawnMaxFailures && !"world-missing".equals(reason)) {
            if (npc != null) {
                npcs.remove(npcId);
                clearRespawnFailureState(npcId);
                spawnRequestsInFlight.remove(npcId);

                logSevere("RESPAWN_HARD_CLEAN", "Removing NPC after repeated respawn failures: "
                    + spawnContextFormatter.format(npc, "tick-retry", null, null, null)
                    + " reason=" + reason + " failures=" + failureCount + " threshold=" + respawnMaxFailures);
            }
            return;
        }

        long delay = Math.min(respawnRetryMaxMs, respawnRetryBaseMs << Math.min(5, failureCount - 1));
        long retryAt = System.currentTimeMillis() + delay;
        respawnRetryAtMs.put(npcId, retryAt);

        if (failureCount == 1 || failureCount % 10 == 0) {
            if (npc != null) {
                logSevere("RESPAWN_RETRY_SCHEDULED", "Respawn retry scheduled: "
                    + spawnContextFormatter.format(npc, "tick-retry", null, null, null)
                    + " reason=" + reason + " failures=" + failureCount + " delayMs=" + delay);
            } else {
                logSevere("RESPAWN_RETRY_SCHEDULED", "Respawn retry scheduled for unknown NPC "
                    + "npcId=" + npcId + " reason=" + reason + " failures=" + failureCount + " delayMs=" + delay);
            }
        }
    }

    public String staleReasonForRestore(NpcRecord npc) {
        Optional<RoleDefinition> roleDefinition = roleDefinitions.findByRoleId(npc.roleId());
        if (roleDefinition.isEmpty()) {
            return "unknown-role roleId=" + npc.roleId();
        }

        for (MarkerType markerType : roleDefinition.get().requiredMarkers()) {
            String markerId = markerResolver.markerIdForType(npc, markerType);
            if (markerId == null || markerId.isBlank()) {
                return "missing-marker-id markerType=" + markerType;
            }

            Optional<MarkerRecord> marker = markerResolver.resolveRequiredMarkerWithFallback(npc, markerType);
            if (marker.isEmpty()) {
                return "missing-marker-record markerType=" + markerType + " markerId=" + markerId + " fallback=none";
            }
        }

        return null;
    }

    private void logSevere(String eventKey, String message) {
        logSevereSink.accept(eventKey, message);
    }
}
