package com.levelup.backend.util;

import java.util.Locale;

public final class RunUtils {
    private RunUtils() {
    }

    public static String normalizeRun(String run) {
        if (run == null) {
            return "";
        }
        return run.replaceAll("[^0-9kK]", "").toUpperCase(Locale.ROOT);
    }
}
