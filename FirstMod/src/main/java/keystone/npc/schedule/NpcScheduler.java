package keystone.npc.schedule;

import keystone.npc.model.NpcRecord;
import keystone.npc.model.NpcRole;
import keystone.npc.model.NpcState;
import keystone.npc.world.MarkerRegistry;
import keystone.npc.world.MarkerType;

import java.time.LocalTime;
import java.util.*;

/**
 * MVP A: minimaler "Scheduler".
 * - verwaltet 1..N NPCs (MVP A: 1)
 * - tickt periodisch
 * - wählt Ziel (work vs bed) anhand Tageszeit
 * - zwingt Routing via door_marker
 */
public final class NpcScheduler {

    private final MarkerRegistry markerRegistry;
    private final Map<String, NpcRecord> npcs = new HashMap<>();

    private volatile boolean running;
    private Thread thread;

    public NpcScheduler(MarkerRegistry markerRegistry) {
        this.markerRegistry = Objects.requireNonNull(markerRegistry);
    }

    public void start() {
        if (running) return;
        running = true;
        thread = new Thread(this::loop, "KeystoneNPC-Scheduler");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        running = false;
        if (thread != null) thread.interrupt();
    }

    public void restore(List<NpcRecord> loaded) {
        npcs.clear();
        for (var npc : loaded) {
            npcs.put(npc.npcId(), npc);
        }
    }

    public List<NpcRecord> snapshot() {
        return npcs.values().stream().toList();
    }

    public NpcRecord spawnLumberjack(String npcId, String name, keystone.npc.world.WorldId worldId) {
        var npc = new NpcRecord(npcId, name, NpcRole.LUMBERJACK, worldId);

        // MVP A: bind to "active" markers (last set)
        npc.bedMarkerId(markerRegistry.getActive(MarkerType.BED).map(m -> m.markerId()).orElse(null));
        npc.doorMarkerId(markerRegistry.getActive(MarkerType.DOOR).map(m -> m.markerId()).orElse(null));
        npc.workMarkerId(markerRegistry.getActive(MarkerType.WORK).map(m -> m.markerId()).orElse(null));

        npcs.put(npc.npcId(), npc);
        return npc;
    }

    private void loop() {
        while (running) {
            try {
                tick();
                Thread.sleep(1000); // TODO: an echten Server Tick binden
            } catch (InterruptedException ie) {
                // exit or continue
            } catch (Exception e) {
                // TODO: proper logger
                e.printStackTrace();
            }
        }
    }

    /** MVP A Tick-Logik (noch ohne echte Navigation). */
    public void tick() {
        for (var npc : npcs.values()) {
            updateNpc(npc);
        }
    }

    private void updateNpc(NpcRecord npc) {
        // 1) Validate markers
        if (npc.bedMarkerId() == null || npc.doorMarkerId() == null || npc.workMarkerId() == null) {
            npc.state(NpcState.PAUSED_MISSING_MARKER);
            // TODO: warn server log: marker missing
            return;
        }

        // 2) Determine desired mode based on time
        var now = LocalTime.now();
        boolean isNight = now.isAfter(LocalTime.of(21, 0)) || now.isBefore(LocalTime.of(7, 0));

        if (isNight) {
            // Desired: bed. Route: door -> bed
            // TODO: implement navigation/waypoints.
            if (npc.state() != NpcState.SLEEPING && npc.state() != NpcState.WALKING_TO_BED) {
                npc.state(NpcState.WALKING_TO_BED);
            }
        } else {
            // Desired: work. Route: door -> work
            // TODO: implement navigation/waypoints.
            if (npc.state() != NpcState.WORKING && npc.state() != NpcState.WALKING_TO_WORK) {
                npc.state(NpcState.WALKING_TO_WORK);
            }
        }

        // 3) Later: when reaching positions, transition to WORKING/SLEEPING
    }
}
