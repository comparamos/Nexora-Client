package com.nexora.client;

public final class FpsUnlocker {
    private static boolean enabled;

    private FpsUnlocker() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
    }
}
