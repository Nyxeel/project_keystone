package keystone.npc.actions;

import com.google.gson.annotations.SerializedName;

public record ActionDefinition(
    String animation,
    Boolean loop,
    String sound,
    Double soundIntervalSeconds,
    @SerializedName(value = "requiresSkill", alternate = {"requiresCapability"}) String requiresSkill
) {
}
