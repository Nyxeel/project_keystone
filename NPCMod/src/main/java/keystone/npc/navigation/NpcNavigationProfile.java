package keystone.npc.navigation;

public record NpcNavigationProfile(
    String id,
    Integer version,
    String pathStyle,
    String doorPolicy,
    String dangerPolicy,
    String targetPolicy,
    String shortcutPolicy,
    String climbPolicy
) {
}
