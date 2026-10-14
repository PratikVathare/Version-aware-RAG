package com.enterprise.rag.util;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

public final class VersionFilterUtils {

    private VersionFilterUtils() {
        // Utility class
    }

    /**
     * Compares two document versions based on:
     * 1. Highest version number
     * OR
     * 2. Most recent effective date
     *
     * @param v1 version number of first version
     * @param d1 effective date of first version
     * @param v2 version number of second version
     * @param d2 effective date of second version
     * @return positive if first is newer, negative if first is older, 0 if equal
     */
    public static int compareVersions(Integer v1, LocalDate d1, Integer v2, LocalDate d2) {
        // 1. Highest version number
        if (v1 != null && v2 != null && !v1.equals(v2)) {
            return Integer.compare(v1, v2);
        }

        // 2. Most recent effective date
        if (d1 != null && d2 != null && !d1.equals(d2)) {
            return d1.compareTo(d2);
        }

        // If effective dates differ by presence
        if (d1 != null && d2 == null) {
            return 1;
        }
        if (d1 == null && d2 != null) {
            return -1;
        }

        // If version numbers differ by presence
        if (v1 != null && v2 == null) {
            return 1;
        }
        if (v1 == null && v2 != null) {
            return -1;
        }

        return 0;
    }

    public record VersionPartitionResult<T>(
            List<T> currentVersionChunks,
            List<T> olderVersionChunks
    ) {}

    /**
     * Partitions retrieved chunks into current version chunks and older version chunks.
     * When multiple versions of the same document are retrieved, chunks are separated
     * based on:
     * 1. Highest version number
     * OR
     * 2. Most recent effective date
     *
     * Preserves the original relative similarity ordering in both subsets.
     *
     * @param items list of retrieved chunks
     * @param documentNameExtractor extractor for document name
     * @param versionNumberExtractor extractor for version number
     * @param effectiveDateExtractor extractor for effective date
     * @param <T> chunk type
     * @return VersionPartitionResult containing current and older version chunks
     */
    public static <T> VersionPartitionResult<T> partitionByVersion(
            List<T> items,
            Function<T, String> documentNameExtractor,
            Function<T, Integer> versionNumberExtractor,
            Function<T, LocalDate> effectiveDateExtractor) {

        if (items == null || items.isEmpty()) {
            return new VersionPartitionResult<>(Collections.emptyList(), Collections.emptyList());
        }

        if (items.size() == 1) {
            return new VersionPartitionResult<>(new ArrayList<>(items), Collections.emptyList());
        }

        // Group items by normalized document name
        Map<String, List<T>> groupedByDocument = new LinkedHashMap<>();
        for (T item : items) {
            String docName = documentNameExtractor.apply(item);
            String groupKey = (docName != null && !docName.isBlank())
                    ? docName.trim().toLowerCase()
                    : UUID.randomUUID().toString(); // Untitled chunks stay standalone
            groupedByDocument.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(item);
        }

        record VersionKey(Integer versionNumber, LocalDate effectiveDate) {}

        Set<T> allowedItems = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<T> olderItems = Collections.newSetFromMap(new IdentityHashMap<>());

        for (List<T> group : groupedByDocument.values()) {
            if (group.size() <= 1) {
                allowedItems.addAll(group);
                continue;
            }

            // Identify all distinct versions present for this document
            Set<VersionKey> versionKeys = new HashSet<>();
            for (T item : group) {
                versionKeys.add(new VersionKey(
                        versionNumberExtractor.apply(item),
                        effectiveDateExtractor.apply(item)
                ));
            }

            if (versionKeys.size() <= 1) {
                // All chunks in this document group belong to the same version
                allowedItems.addAll(group);
            } else {
                // Find the latest version according to:
                // 1. Highest version number OR 2. Most recent effective date
                VersionKey latestKey = Collections.max(versionKeys, (k1, k2) -> compareVersions(
                        k1.versionNumber(), k1.effectiveDate(),
                        k2.versionNumber(), k2.effectiveDate()
                ));

                // Separate current version chunks from older version chunks
                for (T item : group) {
                    VersionKey itemKey = new VersionKey(
                            versionNumberExtractor.apply(item),
                            effectiveDateExtractor.apply(item)
                    );
                    if (itemKey.equals(latestKey)) {
                        allowedItems.add(item);
                    } else {
                        olderItems.add(item);
                    }
                }
            }
        }

        List<T> current = items.stream().filter(allowedItems::contains).toList();
        List<T> older = items.stream().filter(olderItems::contains).toList();

        return new VersionPartitionResult<>(current, older);
    }

    /**
     * Filters retrieved chunks so that when multiple versions of the same document are retrieved,
     * only the latest version's chunks are kept based on:
     * 1. Highest version number
     * OR
     * 2. Most recent effective date
     *
     * Older versions are ignored. Returns only current version chunks while preserving
     * the original relative similarity ordering of chunks.
     *
     * @param items list of retrieved chunks
     * @param documentNameExtractor extractor for document name
     * @param versionNumberExtractor extractor for version number
     * @param effectiveDateExtractor extractor for effective date
     * @param <T> chunk type
     * @return filtered list containing only current version chunks
     */
    public static <T> List<T> filterLatestVersions(
            List<T> items,
            Function<T, String> documentNameExtractor,
            Function<T, Integer> versionNumberExtractor,
            Function<T, LocalDate> effectiveDateExtractor) {
        return partitionByVersion(items, documentNameExtractor, versionNumberExtractor, effectiveDateExtractor)
                .currentVersionChunks();
    }
}
