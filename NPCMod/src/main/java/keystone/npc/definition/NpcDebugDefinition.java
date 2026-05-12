package keystone.npc.definition;

import com.google.gson.annotations.SerializedName;

public record NpcDebugDefinition(
    Boolean showMarkers,
    Boolean logRoutineChanges,
    @SerializedName(value = "logSkillChecks", alternate = {"logCapabilityChecks"}) Boolean logSkillChecks,
    Boolean logMotionChanges
) {
}
