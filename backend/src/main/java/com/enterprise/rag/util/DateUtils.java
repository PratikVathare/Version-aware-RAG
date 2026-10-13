package com.enterprise.rag.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public final class DateUtils {

    public static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private DateUtils() {
        // Utility class
    }

    public static Optional<LocalDate> parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(dateStr.trim(), ISO_FORMATTER));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
}
