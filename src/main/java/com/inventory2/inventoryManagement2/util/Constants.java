package com.inventory2.inventoryManagement2.util;

public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException(
                "Utility class cannot be instantiated");
    }

    // ================= Cache Keys =================

    public static final String PRODUCT_KEY_PREFIX = "product:";

    // ================= Redis =================

    public static final long REDIS_TTL_MINUTES = 30L;

    // ================= Caffeine Cache =================

    public static final long CAFFEINE_MAX_SIZE = 500L;
    public static final long CAFFEINE_EXPIRE_MINUTES = 10L;

    // ================= LRU Cache =================

    public static final int LRU_CACHE_MAX_SIZE = 100;
    public static final float LRU_CACHE_LOAD_FACTOR = 0.75F;



}