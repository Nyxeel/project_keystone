package keystone.npc.recovery;

public record RespawnPolicyConfig(
    boolean enableAutoRespawnMissingNpc,
    int relinkRetryCount,
    long relinkRetryDelayMs
) {
    public static RespawnPolicyConfig loadFromSystemProperties() {
        boolean enableAutoRespawnMissingNpc = Boolean.parseBoolean(
            System.getProperty("knpc.enableAutoRespawnMissingNpc", "false")
        );

        int relinkRetryCount = parseInt("knpc.relinkRetryCount", 5, 1, 1000);
        long relinkRetryDelayMs = parseLong("knpc.relinkRetryDelayMs", 1000L, 1L, 120_000L);

        return new RespawnPolicyConfig(enableAutoRespawnMissingNpc, relinkRetryCount, relinkRetryDelayMs);
    }

    private static int parseInt(String key, int fallback, int min, int max) {
        try {
            int value = Integer.parseInt(System.getProperty(key, Integer.toString(fallback)).trim());
            return Math.max(min, Math.min(max, value));
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private static long parseLong(String key, long fallback, long min, long max) {
        try {
            long value = Long.parseLong(System.getProperty(key, Long.toString(fallback)).trim());
            return Math.max(min, Math.min(max, value));
        } catch (RuntimeException ex) {
            return fallback;
        }
    }
}