package keystone.npc.skills;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

record SkillProfile(
    String id,
    Integer version,
    @SerializedName("capabilities") Map<String, Boolean> skills
) {
}