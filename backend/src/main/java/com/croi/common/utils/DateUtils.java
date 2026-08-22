package com.croi.common.utils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class DateUtils {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private DateUtils() {
    }

    public static String toIsoString(Instant instant) {
        return instant == null ? null : ISO_FORMATTER.format(instant.atZone(ZoneOffset.UTC));
    }

    public static boolean isBefore(Instant a, Instant b) {
        return a != null && b != null && a.isBefore(b);
    }
}
