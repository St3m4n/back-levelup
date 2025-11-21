package com.levelup.backend.util;

import java.util.Locale;

public final class EmailUtils {
    private EmailUtils() {
    }

    public static String normalizeCorreo(String correo) {
        if (correo == null) {
            return "";
        }
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
