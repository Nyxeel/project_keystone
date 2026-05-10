package keystone.npc.schedule;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.RemoveReason;
import keystone.npc.model.NpcRecord;
import keystone.npc.model.NpcRole;
import keystone.npc.model.NpcState;
import keystone.npc.navigation.NavigationState;
import keystone.npc.navigation.NpcNavigation;
import keystone.npc.world.MarkerRegistry;
import keystone.npc.world.MarkerType;
import keystone.npc.world.MarkerRecord;
import keystone.npc.world.Vec3;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Vector3d;

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
    private final Set<String> spawnRequestsInFlight = ConcurrentHashMap.newKeySet();

    private volatile boolean running;
    private Thread thread;

    public NpcScheduler(MarkerRegistry markerRegistry) {
        this.markerRegistry = Objects.requireNonNull(markerRegistry);
    }

    public void start() {
        if (running)
			return;
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
        spawnRequestsInFlight.clear();
        for (var npc : loaded) {
            // Entity references are runtime-only and always invalid after restart.
            npc.entityRef(null);
            npc.entityId(0);
            npcs.put(npc.npcId(), npc);
        }
    }

    public List<NpcRecord> snapshot() {
        return npcs.values().stream().toList();
    }

    public NpcRecord getNpc(String npcId) {
        return npcs.get(npcId);
    }

    public boolean removeNpc(String npcId) {
        NpcRecord npc = npcs.remove(npcId);
        if (npc == null) {
            return false;
        }

        spawnRequestsInFlight.remove(npc.npcId());
        removeLiveEntity(npc);
        return true;
    }

    public boolean removeNpcByIndex(int index) {
        var npcList = new ArrayList<>(npcs.values());
        if (index < 0 || index >= npcList.size()) {
            return false;
        }
        NpcRecord npc = npcList.get(index);
        return removeNpc(npc.npcId());
    }

    public int clearNpcs() {
        List<String> npcIds = new ArrayList<>(npcs.keySet());
        for (String npcId : npcIds) {
            removeNpc(npcId);
        }
        return npcIds.size();
    }

    public int spawnRestoredNpcs(String trigger) {
        int queued = 0;
        for (NpcRecord npc : npcs.values()) {
            if (hasLiveEntity(npc)) {
                npc.entityId(1);
                continue;
            }

            if (npc.entityRef() != null || npc.entityId() != 0) {
                npc.entityRef(null);
                npc.entityId(0);
            }

            if (!spawnRequestsInFlight.add(npc.npcId())) {
                continue;
            }

            World world = Universe.get().getWorld(npc.worldId().value());
            if (world == null) {
                if (!"tick-retry".equals(trigger)) {
                    System.err.println("[KeystoneNPC] Delaying NPC respawn (trigger=" + trigger + ") for '"
                        + npc.npcName() + "' (" + npc.npcId() + "): world missing " + npc.worldId());
                }
                spawnRequestsInFlight.remove(npc.npcId());
                continue;
            }

            queued++;
            System.out.println("[KeystoneNPC] Queue respawn (trigger=" + trigger + ") for '"
                + npc.npcName() + "' (" + npc.npcId() + ") in world " + world.getName());
            world.execute(() -> {
                try {
                    spawnNpcEntity(world, npc);
                } finally {
                    spawnRequestsInFlight.remove(npc.npcId());
                }
            });
        }

        return queued;
    }

    public int spawnRestoredNpcs() {
        return spawnRestoredNpcs("manual");
    }

    public NpcRecord spawnLumberjack(String npcId, String name, keystone.npc.world.WorldId worldId, keystone.npc.world.Vec3 position) {
        var npc = new NpcRecord(npcId, name, NpcRole.LUMBERJACK, worldId);

        // Set spawn position
        npc.currentPosition(position);

        // MVP A: bind to "active" markers (last set)
        npc.bedMarkerId(markerRegistry.getActive(MarkerType.BED).map(m -> m.markerId()).orElse(null));
        npc.doorMarkerId(markerRegistry.getActive(MarkerType.DOOR).map(m -> m.markerId()).orElse(null));
        npc.workMarkerId(markerRegistry.getActive(MarkerType.WORK).map(m -> m.markerId()).orElse(null));

        npcs.put(npc.npcId(), npc);
        return npc;
    }

    public NpcRecord linkEntityRef(String npcId, Ref<EntityStore> entityRef) {
        NpcRecord npc = npcs.get(npcId);
        if (npc != null) {
            npc.entityRef(entityRef);
            npc.entityId(1);  // Mark as spawned (persist to JSON)
        }
        return npc;
    }

    private void spawnNpcEntity(World world, NpcRecord npc) {
        if (hasLiveEntity(npc)) {
            npc.entityId(1);
            return;
        }

        npc.entityRef(null);
        npc.entityId(0);

        int roleIndex = switch (npc.role()) {
            case LUMBERJACK -> NPCPlugin.get().getIndex("Lumberjack");
        };

        if (roleIndex < 0) {
            System.err.println("[KeystoneNPC] Role not found while restoring NPC: " + npc.role());
            return;
        }

        Vec3 position = npc.currentPosition() != null ? npc.currentPosition() : new Vec3(0, 0, 0);
        Vector3d spawnPosition = new Vector3d(position.x(), position.y(), position.z());
        Store<EntityStore> store = world.getEntityStore().getStore();

        var pair = NPCPlugin.get().spawnEntity(store, roleIndex, spawnPosition, Rotation3f.IDENTITY, null, null);
        if (pair == null) {
            System.err.println("[KeystoneNPC] Failed to respawn NPC: " + npc.npcId());
            return;
        }

        npc.entityRef(pair.first());
        npc.entityId(1);  // Mark as spawned (persist to JSON)
        System.out.println("[KeystoneNPC] Restored NPC entity: " + npc.npcId() + " in world " + world.getName());
    }

    private void removeLiveEntity(NpcRecord npc) {
        if (npc.entityRef() == null) {
            npc.entityId(0);
            return;
        }

        World world = Universe.get().getWorld(npc.worldId().value());
        if (world == null) {
            System.err.println("[KeystoneNPC] Cannot remove entity for NPC '" + npc.npcName()
                + "' (" + npc.npcId() + "): world not found");
            npc.entityRef(null);
            npc.entityId(0);
            return;
        }

        world.execute(() -> {
            Ref<EntityStore> liveRef = npc.entityRef();
            if (liveRef != null && liveRef.isValid()) {
                liveRef.getStore().removeEntity(liveRef, RemoveReason.REMOVE);
            }
            npc.entityRef(null);
            npc.entityId(0);
        });
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

    /** MVP A Tick-Logik mit Ingame-Weltzeit. */
    public void tick() {
        spawnRestoredNpcs("tick-retry");

        List<NpcRecord> npcSnapshot = new ArrayList<>(npcs.values());
        for (NpcRecord npc : npcSnapshot) {
            World world = Universe.get().getWorld(npc.worldId().value());
            if (world != null) {
                world.execute(() -> updateNpc(npc, world));
            }
        }
    }

    private void updateNpc(NpcRecord npc, World world) {
        // 1) Validate markers
        if (npc.bedMarkerId() == null || npc.doorMarkerId() == null || npc.workMarkerId() == null) {
            if (npc.state() != NpcState.PAUSED_MISSING_MARKER) {
                System.err.println("[KeystoneNPC] Missing marker assignment for NPC '" + npc.npcName() + "' ("
                    + npc.npcId() + "): bed=" + npc.bedMarkerId() + ", door=" + npc.doorMarkerId()
                    + ", work=" + npc.workMarkerId());
            }
            npc.state(NpcState.PAUSED_MISSING_MARKER);
            return;
        }

        NpcState desiredTargetState = resolveDesiredTargetState(world, npc);
        if (desiredTargetState == null) {
            return;
        }

        NavigationState navState = npc.navigationState();

        // 2) Handle active navigation.
        if (hasActiveNavigation(navState)) {
            if (navState.isComplete()) {
                finishNavigation(npc, navState);
                return;
            }

            // Update both logical and visible entity position.
            Vec3 currentPos = navState.getCurrentPosition();
            if (currentPos != null) {
                npc.currentPosition(currentPos);
                updateEntityPosition(npc, currentPos);
            }

            // Re-route if day/night target changed while still walking.
            NpcState activeTargetState = navState.getTargetState();
            if (activeTargetState != null && activeTargetState != desiredTargetState) {
                System.out.println("[KeystoneNPC] Navigation reroute: npc=" + npc.npcName()
                    + " activeTarget=" + targetTypeForState(activeTargetState)
                    + " newTarget=" + targetTypeForState(desiredTargetState)
                    + " currentPos=" + npc.currentPosition());
                navState.clear();

                if (desiredTargetState == NpcState.SLEEPING) {
                    startNavigationToBed(npc);
                } else if (desiredTargetState == NpcState.WORKING) {
                    startNavigationToWork(npc);
                }
                return;
            }

            if (navState.isComplete()) {
                finishNavigation(npc, navState);
            }
            return;
        }

        // 3) No active navigation: choose target from world time.
        if (desiredTargetState == NpcState.SLEEPING) {
            if (npc.state() != NpcState.SLEEPING && npc.state() != NpcState.WALKING_TO_BED) {
                startNavigationToBed(npc);
            }
        } else if (desiredTargetState == NpcState.WORKING) {
            if (npc.state() != NpcState.WORKING && npc.state() != NpcState.WALKING_TO_WORK) {
                startNavigationToWork(npc);
            }
        }
    }

    private void startNavigationToBed(NpcRecord npc) {
        // Route: directly to bed (MVP A: simplified)
        Optional<MarkerRecord> bedMarker = markerRegistry.getById(npc.bedMarkerId());
        if (bedMarker.isEmpty()) {
            System.err.println("[KeystoneNPC] Missing bed marker for NPC '" + npc.npcName() + "' (" + npc.npcId() + ")"
                + " markerId=" + npc.bedMarkerId());
            npc.state(NpcState.PAUSED_MISSING_MARKER);
            return;
        }

        Vec3 startPos = npc.currentPosition();
        Vec3 bedPos = bedMarker.get().position();
        long durationMs = NpcNavigation.calculateDurationMs(startPos, bedPos);
        npc.navigationState().startNavigation(startPos, bedPos, durationMs, NpcState.SLEEPING);
        npc.state(NpcState.WALKING_TO_BED);

        System.out.println("[KeystoneNPC] Navigation start: npc=" + npc.npcName()
            + " start=" + startPos
            + " target=" + bedPos
            + " targetType=BED");
    }

    private void startNavigationToWork(NpcRecord npc) {
        // Route: directly to work (MVP A: simplified)
        Optional<MarkerRecord> workMarker = markerRegistry.getById(npc.workMarkerId());
        if (workMarker.isEmpty()) {
            System.err.println("[KeystoneNPC] Missing work marker for NPC '" + npc.npcName() + "' (" + npc.npcId() + ")"
                + " markerId=" + npc.workMarkerId());
            npc.state(NpcState.PAUSED_MISSING_MARKER);
            return;
        }

        Vec3 startPos = npc.currentPosition();
        Vec3 workPos = workMarker.get().position();
        long durationMs = NpcNavigation.calculateDurationMs(startPos, workPos);
        npc.navigationState().startNavigation(startPos, workPos, durationMs, NpcState.WORKING);
        npc.state(NpcState.WALKING_TO_WORK);

        System.out.println("[KeystoneNPC] Navigation start: npc=" + npc.npcName()
            + " start=" + startPos
            + " target=" + workPos
            + " targetType=WORK");
    }

    private NpcState resolveDesiredTargetState(World world, NpcRecord npc) {
        try {
            WorldTimeResource worldTimeResource = world.getEntityStore().getStore()
                .getResource(WorldTimeResource.getResourceType());
            int currentHour = worldTimeResource.getCurrentHour();
            boolean isNight = currentHour >= 21 || currentHour < 7;
            return isNight ? NpcState.SLEEPING : NpcState.WORKING;
        } catch (Exception e) {
            System.err.println("[KeystoneNPC] Error getting world time for NPC '" + npc.npcName() + "': " + e.getMessage());
            npc.state(NpcState.PAUSED_MISSING_MARKER);
            return null;
        }
    }

    private boolean hasActiveNavigation(NavigationState navState) {
        return navState.getTargetPosition() != null;
    }

    private void finishNavigation(NpcRecord npc, NavigationState navState) {
        Vec3 targetPos = navState.getTargetPosition();
        if (targetPos != null) {
            npc.currentPosition(targetPos);
            updateEntityPosition(npc, targetPos);
        }

        NpcState targetState = navState.getTargetState();
        if (targetState != null) {
            npc.state(targetState);
        }

        System.out.println("[KeystoneNPC] Navigation reached: npc=" + npc.npcName()
            + " reachedTarget=" + targetTypeForState(targetState)
            + " newState=" + npc.state());
        navState.clear();
    }

    private String targetTypeForState(NpcState state) {
        if (state == NpcState.WORKING) {
            return "WORK";
        }
        if (state == NpcState.SLEEPING) {
            return "BED";
        }
        return "UNKNOWN";
    }

    private boolean hasLiveEntity(NpcRecord npc) {
        Ref<EntityStore> entityRef = npc.entityRef();
        return entityRef != null && entityRef.isValid();
    }

    private void updateEntityPosition(NpcRecord npc, Vec3 newPosition) {
        Ref<EntityStore> entityRef = npc.entityRef();
        if (entityRef == null || !entityRef.isValid()) {
            System.err.println("[KeystoneNPC] Cannot move NPC '" + npc.npcName() + "' (" + npc.npcId()
                + "): missing or invalid EntityRef");
            return;
        }

        Store<EntityStore> store = entityRef.getStore();
        TransformComponent transform = store.getComponent(entityRef, TransformComponent.getComponentType());
        if (transform == null) {
            System.err.println("[KeystoneNPC] Cannot move NPC '" + npc.npcName() + "' (" + npc.npcId()
                + "): missing TransformComponent");
            return;
        }

        Vector3d newPos = new Vector3d(newPosition.x(), newPosition.y(), newPosition.z());
        transform.setPosition(newPos);
    }
}
