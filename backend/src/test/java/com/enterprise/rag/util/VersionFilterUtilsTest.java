package com.enterprise.rag.util;

import com.enterprise.rag.dto.SearchResultDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VersionFilterUtilsTest {

    @Test
    @DisplayName("Should compare versions correctly based on highest version number first")
    void testCompareVersionsByVersionNumber() {
        // v2 vs v1 with same date
        assertTrue(VersionFilterUtils.compareVersions(2, LocalDate.of(2024, 1, 1), 1, LocalDate.of(2024, 1, 1)) > 0);
        assertTrue(VersionFilterUtils.compareVersions(1, LocalDate.of(2024, 1, 1), 2, LocalDate.of(2024, 1, 1)) < 0);

        // v2 vs v1 with no dates
        assertTrue(VersionFilterUtils.compareVersions(2, null, 1, null) > 0);

        // v2 vs v1 where v2 has higher version number
        assertTrue(VersionFilterUtils.compareVersions(2, LocalDate.of(2023, 1, 1), 1, LocalDate.of(2024, 1, 1)) > 0);
    }

    @Test
    @DisplayName("Should compare versions based on most recent effective date when version numbers are equal or null")
    void testCompareVersionsByEffectiveDate() {
        // Equal version numbers (v1 vs v1), different dates
        assertTrue(VersionFilterUtils.compareVersions(1, LocalDate.of(2024, 6, 1), 1, LocalDate.of(2024, 1, 1)) > 0);
        assertTrue(VersionFilterUtils.compareVersions(1, LocalDate.of(2024, 1, 1), 1, LocalDate.of(2024, 6, 1)) < 0);

        // Null version numbers, different dates
        assertTrue(VersionFilterUtils.compareVersions(null, LocalDate.of(2024, 5, 1), null, LocalDate.of(2023, 5, 1)) > 0);
        assertTrue(VersionFilterUtils.compareVersions(null, LocalDate.of(2023, 5, 1), null, LocalDate.of(2024, 5, 1)) < 0);

        // Equal version numbers and equal dates
        assertEquals(0, VersionFilterUtils.compareVersions(1, LocalDate.of(2024, 1, 1), 1, LocalDate.of(2024, 1, 1)));
    }

    @Test
    @DisplayName("Should filter multiple versions of same document and keep only the latest version based on highest version number")
    void testFilterKeepLatestByVersionNumber() {
        SearchResultDto chunkV1 = SearchResultDto.builder()
                .id(UUID.randomUUID())
                .documentName("Security Policy")
                .versionNumber(1)
                .effectiveDate(LocalDate.of(2023, 1, 1))
                .chunkText("Old password policy: 8 chars")
                .similarityScore(0.95)
                .build();

        SearchResultDto chunkV2 = SearchResultDto.builder()
                .id(UUID.randomUUID())
                .documentName("Security Policy")
                .versionNumber(2)
                .effectiveDate(LocalDate.of(2024, 1, 1))
                .chunkText("New password policy: 16 chars")
                .similarityScore(0.90)
                .build();

        List<SearchResultDto> filtered = VersionFilterUtils.filterLatestVersions(
                List.of(chunkV1, chunkV2),
                SearchResultDto::getDocumentName,
                SearchResultDto::getVersionNumber,
                SearchResultDto::getEffectiveDate
        );

        assertEquals(1, filtered.size());
        assertEquals(2, filtered.get(0).getVersionNumber());
        assertEquals("New password policy: 16 chars", filtered.get(0).getChunkText());
    }

    @Test
    @DisplayName("Should filter multiple versions of same document and keep only the latest version based on most recent effective date")
    void testFilterKeepLatestByEffectiveDate() {
        SearchResultDto chunkOldDate = SearchResultDto.builder()
                .id(UUID.randomUUID())
                .documentName("Travel Policy")
                .versionNumber(1)
                .effectiveDate(LocalDate.of(2023, 3, 1))
                .chunkText("Per diem is $50")
                .similarityScore(0.92)
                .build();

        SearchResultDto chunkNewDate = SearchResultDto.builder()
                .id(UUID.randomUUID())
                .documentName("Travel Policy")
                .versionNumber(1)
                .effectiveDate(LocalDate.of(2024, 3, 1))
                .chunkText("Per diem is $75")
                .similarityScore(0.88)
                .build();

        List<SearchResultDto> filtered = VersionFilterUtils.filterLatestVersions(
                List.of(chunkOldDate, chunkNewDate),
                SearchResultDto::getDocumentName,
                SearchResultDto::getVersionNumber,
                SearchResultDto::getEffectiveDate
        );

        assertEquals(1, filtered.size());
        assertEquals(LocalDate.of(2024, 3, 1), filtered.get(0).getEffectiveDate());
        assertEquals("Per diem is $75", filtered.get(0).getChunkText());
    }

    @Test
    @DisplayName("Should keep multiple chunks if they belong to the current latest version")
    void testKeepMultipleChunksOfLatestVersion() {
        SearchResultDto oldV1 = SearchResultDto.builder()
                .id(UUID.randomUUID())
                .documentName("HR Handbook")
                .versionNumber(1)
                .effectiveDate(LocalDate.of(2022, 1, 1))
                .chunkText("V1 Section 1")
                .similarityScore(0.96)
                .build();

        SearchResultDto latestV2Chunk1 = SearchResultDto.builder()
                .id(UUID.randomUUID())
                .documentName("HR Handbook")
                .versionNumber(2)
                .effectiveDate(LocalDate.of(2024, 1, 1))
                .chunkText("V2 Section 1")
                .similarityScore(0.91)
                .build();

        SearchResultDto latestV2Chunk2 = SearchResultDto.builder()
                .id(UUID.randomUUID())
                .documentName("HR Handbook")
                .versionNumber(2)
                .effectiveDate(LocalDate.of(2024, 1, 1))
                .chunkText("V2 Section 2")
                .similarityScore(0.85)
                .build();

        List<SearchResultDto> filtered = VersionFilterUtils.filterLatestVersions(
                List.of(oldV1, latestV2Chunk1, latestV2Chunk2),
                SearchResultDto::getDocumentName,
                SearchResultDto::getVersionNumber,
                SearchResultDto::getEffectiveDate
        );

        assertEquals(2, filtered.size());
        assertEquals("V2 Section 1", filtered.get(0).getChunkText());
        assertEquals("V2 Section 2", filtered.get(1).getChunkText());
    }

    @Test
    @DisplayName("Should handle multiple distinct documents independently and preserve similarity order")
    void testMultipleDocumentsFilteredIndependently() {
        SearchResultDto docAOld = SearchResultDto.builder()
                .id(UUID.randomUUID())
                .documentName("DocA")
                .versionNumber(1)
                .chunkText("DocA V1")
                .similarityScore(0.99)
                .build();

        SearchResultDto docB = SearchResultDto.builder()
                .id(UUID.randomUUID())
                .documentName("DocB")
                .versionNumber(1)
                .chunkText("DocB V1")
                .similarityScore(0.95)
                .build();

        SearchResultDto docANew = SearchResultDto.builder()
                .id(UUID.randomUUID())
                .documentName("DocA")
                .versionNumber(2)
                .chunkText("DocA V2")
                .similarityScore(0.90)
                .build();

        List<SearchResultDto> filtered = VersionFilterUtils.filterLatestVersions(
                List.of(docAOld, docB, docANew),
                SearchResultDto::getDocumentName,
                SearchResultDto::getVersionNumber,
                SearchResultDto::getEffectiveDate
        );

        // DocA V1 is ignored. DocB V1 and DocA V2 are kept in their relative similarity order.
        assertEquals(2, filtered.size());
        assertEquals("DocB V1", filtered.get(0).getChunkText());
        assertEquals("DocA V2", filtered.get(1).getChunkText());
    }

    @Test
    @DisplayName("Should handle empty or single-item lists gracefully")
    void testEmptyAndSingleList() {
        assertTrue(VersionFilterUtils.filterLatestVersions(
                Collections.emptyList(),
                SearchResultDto::getDocumentName,
                SearchResultDto::getVersionNumber,
                SearchResultDto::getEffectiveDate
        ).isEmpty());

        SearchResultDto single = SearchResultDto.builder()
                .id(UUID.randomUUID())
                .documentName("Single")
                .versionNumber(1)
                .chunkText("Only chunk")
                .build();

        List<SearchResultDto> result = VersionFilterUtils.filterLatestVersions(
                List.of(single),
                SearchResultDto::getDocumentName,
                SearchResultDto::getVersionNumber,
                SearchResultDto::getEffectiveDate
        );

        assertEquals(1, result.size());
        assertEquals("Only chunk", result.get(0).getChunkText());
    }

    @Test
    @DisplayName("Should partition chunks into current and older versions accurately")
    void testPartitionByVersion() {
        SearchResultDto v1 = SearchResultDto.builder()
                .id(UUID.randomUUID())
                .documentName("Policy")
                .versionNumber(1)
                .chunkText("V1 content")
                .build();

        SearchResultDto v2 = SearchResultDto.builder()
                .id(UUID.randomUUID())
                .documentName("Policy")
                .versionNumber(2)
                .chunkText("V2 content")
                .build();

        VersionFilterUtils.VersionPartitionResult<SearchResultDto> partition =
                VersionFilterUtils.partitionByVersion(
                        List.of(v1, v2),
                        SearchResultDto::getDocumentName,
                        SearchResultDto::getVersionNumber,
                        SearchResultDto::getEffectiveDate
                );

        assertEquals(1, partition.currentVersionChunks().size());
        assertEquals(2, partition.currentVersionChunks().get(0).getVersionNumber());

        assertEquals(1, partition.olderVersionChunks().size());
        assertEquals(1, partition.olderVersionChunks().get(0).getVersionNumber());
    }
}
