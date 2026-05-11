package keystone.npc.doorway;

public record DoorwayConfig(
    double doorRouteMaxDistanceSq,
    double doorDirectionDotEpsilon,
    double doorLocalSearchDistanceSq,
    int doorLocalSearchRadiusBlocks,
    String openDoorIn,
    String openDoorOut,
    String closeDoorIn,
    String closeDoorOut
) {
}
