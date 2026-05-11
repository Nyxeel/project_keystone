package keystone.npc.doorway;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.joml.Vector3i;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.DoorInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;

import keystone.npc.domain.NpcRecord;
import keystone.npc.routine.pathfinding.PathfindingSupport;
import keystone.npc.markers.Vec3;

public final class DoorwayScanner {
    private final PathfindingSupport pathfindingSupport;
    private final DoorwayConfig config;

    public DoorwayScanner(PathfindingSupport pathfindingSupport, DoorwayConfig config) {
        this.pathfindingSupport = Objects.requireNonNull(pathfindingSupport);
        this.config = Objects.requireNonNull(config);
    }

    public BlockPosition resolveApproachDoorBlock(World world, Vec3 currentPos, Vec3 targetPos, BlockPosition markerDoorBlock) {
        BlockPosition best = null;
        double bestDistanceSq = Double.MAX_VALUE;

        if (markerDoorBlock != null) {
            Vec3 markerCenter = toBlockCenter(markerDoorBlock);
            double markerDistanceSq = distanceSq(currentPos, markerCenter);
            if (markerDistanceSq <= config.doorLocalSearchDistanceSq() * 2.0
                && pathfindingSupport.distanceSqToSegment(markerCenter, currentPos, targetPos) <= config.doorRouteMaxDistanceSq()) {
                best = markerDoorBlock;
                bestDistanceSq = markerDistanceSq;
            }
        }

        int baseX = (int) Math.floor(currentPos.x());
        int baseY = (int) Math.floor(currentPos.y());
        int baseZ = (int) Math.floor(currentPos.z());
        Set<String> visited = new HashSet<>();

        for (int dx = -config.doorLocalSearchRadiusBlocks(); dx <= config.doorLocalSearchRadiusBlocks(); dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -config.doorLocalSearchRadiusBlocks(); dz <= config.doorLocalSearchRadiusBlocks(); dz++) {
                    BlockPosition raw = new BlockPosition(baseX + dx, baseY + dy, baseZ + dz);
                    BlockPosition anchor = resolveDoorAnchor(world, raw);
                    if (anchor == null) {
                        continue;
                    }

                    String key = doorBlockKey(anchor);
                    if (!visited.add(key)) {
                        continue;
                    }

                    BlockType type = getDoorBlockType(world, anchor);
                    if (type == null) {
                        continue;
                    }

                    Vec3 center = toBlockCenter(anchor);
                    double distanceSq = distanceSq(currentPos, center);
                    if (distanceSq > config.doorLocalSearchDistanceSq()) {
                        continue;
                    }

                    if (isMovingAwayFromDoor(currentPos, targetPos, center)) {
                        continue;
                    }

                    if (pathfindingSupport.distanceSqToSegment(center, currentPos, targetPos) > config.doorRouteMaxDistanceSq()) {
                        continue;
                    }

                    if (best == null || distanceSq < bestDistanceSq) {
                        best = anchor;
                        bestDistanceSq = distanceSq;
                    }
                }
            }
        }

        return best;
    }

    public boolean isMovingAwayFromDoor(Vec3 currentPos, Vec3 targetPos, Vec3 doorCenter) {
        if (currentPos == null || targetPos == null || doorCenter == null) {
            return false;
        }

        double moveX = targetPos.x() - currentPos.x();
        double moveY = targetPos.y() - currentPos.y();
        double moveZ = targetPos.z() - currentPos.z();

        double toDoorX = doorCenter.x() - currentPos.x();
        double toDoorY = doorCenter.y() - currentPos.y();
        double toDoorZ = doorCenter.z() - currentPos.z();

        double dot = moveX * toDoorX + moveY * toDoorY + moveZ * toDoorZ;
        return dot < -config.doorDirectionDotEpsilon();
    }

    public boolean tryQueueDoorInteractionChain(World world, NpcRecord npc, BlockPosition doorBlock) {
        Ref<EntityStore> entityRef = npc.entityRef();
        if (entityRef == null || !entityRef.isValid()) {
            return false;
        }

        Store<EntityStore> store = entityRef.getStore();
        InteractionManager interactionManager = store.getComponent(entityRef, InteractionModule.get().getInteractionManagerComponent());
        if (interactionManager == null) {
            return false;
        }

        BlockType doorType = getDoorBlockType(world, doorBlock);
        if (doorType == null) {
            return false;
        }

        String rootInteractionId = doorType.getInteractions().get(InteractionType.Use);
        if (rootInteractionId == null || rootInteractionId.isBlank()) {
            return false;
        }

        RootInteraction rootInteraction = RootInteraction.getAssetMap().getAsset(rootInteractionId);
        if (rootInteraction == null) {
            return false;
        }

        InteractionContext context = InteractionContext.forInteraction(interactionManager, entityRef, InteractionType.Use, store);
        context.getMetaStore().putMetaObject(Interaction.TARGET_BLOCK, doorBlock);
        context.getMetaStore().putMetaObject(Interaction.TARGET_BLOCK_RAW, doorBlock);

        InteractionChain chain = interactionManager.initChain(InteractionType.Use, context, rootInteraction, -1, doorBlock, false);
        interactionManager.queueExecuteChain(chain);
        return true;
    }

    public boolean tryOpenDoorFallback(World world, BlockPosition doorBlock) {
        Vector3i pos = new Vector3i(doorBlock.x, doorBlock.y, doorBlock.z);
        BlockType doorType = getDoorBlockType(world, doorBlock);
        if (doorType == null) {
            return false;
        }

        world.setBlockInteractionState(pos, doorType, config.openDoorIn());
        if (isDoorOpened(world, doorBlock)) {
            return true;
        }

        BlockType updatedDoorType = getDoorBlockType(world, doorBlock);
        if (updatedDoorType == null) {
            return false;
        }

        world.setBlockInteractionState(pos, updatedDoorType, config.openDoorOut());
        return isDoorOpened(world, doorBlock);
    }

    public boolean tryCloseDoorFallback(World world, BlockPosition doorBlock) {
        Vector3i pos = new Vector3i(doorBlock.x, doorBlock.y, doorBlock.z);
        BlockType doorType = getDoorBlockType(world, doorBlock);
        if (doorType == null) {
            return false;
        }

        world.setBlockInteractionState(pos, doorType, config.closeDoorIn());
        if (!isDoorOpened(world, doorBlock)) {
            return true;
        }

        BlockType updatedDoorType = getDoorBlockType(world, doorBlock);
        if (updatedDoorType == null) {
            return false;
        }

        world.setBlockInteractionState(pos, updatedDoorType, config.closeDoorOut());
        return !isDoorOpened(world, doorBlock);
    }

    public boolean sameBlock(BlockPosition a, BlockPosition b) {
        if (a == null || b == null) {
            return false;
        }
        return a.x == b.x && a.y == b.y && a.z == b.z;
    }

    public String doorBlockKey(BlockPosition doorBlock) {
        return doorBlock.x + ":" + doorBlock.y + ":" + doorBlock.z;
    }

    public BlockPosition resolveDoorBlock(World world, Vec3 doorPos) {
        int baseX = (int) Math.floor(doorPos.x());
        int baseY = (int) Math.floor(doorPos.y());
        int baseZ = (int) Math.floor(doorPos.z());

        int[][] offsets = new int[][]{
            {0, 0, 0},
            {1, 0, 0},
            {-1, 0, 0},
            {0, 0, 1},
            {0, 0, -1},
            {0, 1, 0},
            {0, -1, 0}
        };

        for (int[] offset : offsets) {
            BlockPosition raw = new BlockPosition(baseX + offset[0], baseY + offset[1], baseZ + offset[2]);
            BlockPosition base = resolveDoorAnchor(world, raw);
            BlockType type = getDoorBlockType(world, base);
            if (type != null) {
                return base;
            }
        }

        return null;
    }

    public BlockType getDoorBlockType(World world, BlockPosition block) {
        if (block == null) {
            return null;
        }

        WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(block.x, block.z));
        if (chunk == null) {
            return null;
        }

        BlockType blockType = chunk.getBlockType(block.x, block.y, block.z);
        if (!isDoorBlockType(blockType)) {
            return null;
        }

        return blockType;
    }

    public BlockPosition resolveDoorAnchor(World world, BlockPosition block) {
        if (block == null) {
            return null;
        }

        WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(block.x, block.z));
        if (chunk == null) {
            return block;
        }

        int filler = chunk.getFiller(block.x, block.y, block.z);
        if (filler == 0) {
            return block;
        }

        return new BlockPosition(
            block.x - FillerBlockUtil.unpackX(filler),
            block.y - FillerBlockUtil.unpackY(filler),
            block.z - FillerBlockUtil.unpackZ(filler)
        );
    }

    public boolean isDoorBlockType(BlockType blockType) {
        if (blockType == null) {
            return false;
        }

        String rootInteractionId = blockType.getInteractions().get(InteractionType.Use);
        if (rootInteractionId == null || rootInteractionId.isBlank()) {
            return false;
        }

        RootInteraction rootInteraction = RootInteraction.getAssetMap().getAsset(rootInteractionId);
        if (rootInteraction == null) {
            return false;
        }

        for (String interactionId : rootInteraction.getInteractionIds()) {
            Interaction interaction = Interaction.getAssetMap().getAsset(interactionId);
            if (interaction instanceof DoorInteraction) {
                return true;
            }
        }

        return false;
    }

    public boolean isDoorOpened(World world, BlockPosition block) {
        BlockType blockType = getDoorBlockType(world, block);
        if (blockType == null) {
            return false;
        }

        String state = blockType.getStateForBlock(blockType);
        return config.openDoorIn().equals(state) || config.openDoorOut().equals(state);
    }

    public String formatBlockPosition(BlockPosition blockPosition) {
        if (blockPosition == null) {
            return "-";
        }
        return blockPosition.x + "," + blockPosition.y + "," + blockPosition.z;
    }

    public Vec3 toBlockCenter(BlockPosition blockPosition) {
        return new Vec3(blockPosition.x + 0.5, blockPosition.y + 0.5, blockPosition.z + 0.5);
    }

    public double distanceSqToSegment(Vec3 point, Vec3 segmentStart, Vec3 segmentEnd) {
        return pathfindingSupport.distanceSqToSegment(point, segmentStart, segmentEnd);
    }

    private double distanceSq(Vec3 a, Vec3 b) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        double dz = a.z() - b.z();
        return dx * dx + dy * dy + dz * dz;
    }
}
