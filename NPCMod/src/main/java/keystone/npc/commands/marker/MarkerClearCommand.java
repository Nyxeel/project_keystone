package keystone.npc.commands.marker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import keystone.npc.KeystoneNpcPlugin;
import keystone.npc.domain.NpcRecord;
import keystone.npc.markers.MarkerRegistry;
import keystone.npc.markers.MarkerType;
import keystone.npc.routine.NpcRoutineRunner;

public final class MarkerClearCommand extends CommandBase {
    private final KeystoneNpcPlugin plugin;
    private final MarkerRegistry markerRegistry;
    private final NpcRoutineRunner scheduler;

    public MarkerClearCommand(KeystoneNpcPlugin plugin, MarkerRegistry markerRegistry, NpcRoutineRunner scheduler) {
        super("clear", "keystone.commands.knpc.marker.clear");
        this.plugin = Objects.requireNonNull(plugin);
        this.markerRegistry = Objects.requireNonNull(markerRegistry);
        this.scheduler = Objects.requireNonNull(scheduler);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        List<keystone.npc.markers.MarkerRecord> markersBefore = markerRegistry.snapshot();
        Map<MarkerType, String> activeBefore = markerRegistry.snapshotActiveMarkerIds();

        boolean hasPersistedMarkers = !markersBefore.isEmpty();
        boolean hasActiveSelections = !activeBefore.isEmpty();
        if (!hasPersistedMarkers && !hasActiveSelections) {
            context.sendMessage(Message.raw("[knpc] No markers to clear."));
            return;
        }

        if (hasPersistedMarkers) {
            Set<String> existingMarkerIds = new HashSet<>();
            for (var marker : markersBefore) {
                existingMarkerIds.add(marker.markerId());
            }

            List<String> referencingNpcIds = new ArrayList<>();
            for (NpcRecord npc : scheduler.snapshot()) {
                if (referencesAnyMarker(npc, existingMarkerIds)) {
                    referencingNpcIds.add(npc.npcId());
                }
            }

            if (!referencingNpcIds.isEmpty()) {
                context.sendMessage(Message.raw("[knpc] Marker clear blocked: " + referencingNpcIds.size()
                    + " NPC(s) still reference persisted markers."));
                context.sendMessage(Message.raw("[knpc] Remove marker assignments first (e.g. /knpc status) before clearing markers."));
                return;
            }
        }

        markerRegistry.clear();
        if (!plugin.saveStateSafely()) {
            markerRegistry.restore(markersBefore, activeBefore);
            context.sendMessage(Message.raw("[knpc] Marker clear aborted: state save failed, runtime markers were restored."));
            return;
        }

        if (hasPersistedMarkers) {
            context.sendMessage(Message.raw("[knpc] Cleared markers (bed/door/chest/food/work/chill) and persisted state."));
            return;
        }

        context.sendMessage(Message.raw("[knpc] Cleared stale active marker selections and persisted state."));
    }

    private boolean referencesAnyMarker(NpcRecord npc, Set<String> existingMarkerIds) {
        return hasMarkerReference(existingMarkerIds, npc.bedMarkerId())
            || hasMarkerReference(existingMarkerIds, npc.doorMarkerId())
            || hasMarkerReference(existingMarkerIds, npc.chestMarkerId())
            || hasMarkerReference(existingMarkerIds, npc.foodMarkerId())
            || hasMarkerReference(existingMarkerIds, npc.workMarkerId())
            || hasMarkerReference(existingMarkerIds, npc.chillMarkerId());
    }

    private boolean hasMarkerReference(Set<String> existingMarkerIds, String markerId) {
        return markerId != null && !markerId.isBlank() && existingMarkerIds.contains(markerId);
    }
}
