package com.oblivi0n_arch.flowwithnoir.data

import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class ProfileStats(
    val sessionCount: Int,
    val totalMeditatedMinutes: Int,
    val mostCommonFeeling: Feeling?
)

class JournalRepository(private val dao: JournalDao) {

    fun getEntries(): Flow<List<JournalEntry>> = dao.getAll()

    fun getEntry(id: Long): Flow<JournalEntry?> = dao.getById(id)

    fun getLatestEntry(): Flow<JournalEntry?> = dao.getLatest()

    suspend fun logSession(
        sessionName: String,
        plannedDurationSeconds: Int,
        actualDurationSeconds: Int,
        isCompleted: Boolean,
        feeling: Feeling? = null,
        note: String = "",
        tags: List<JournalTag> = emptyList()
    ) {
        dao.insert(
            JournalEntry(
                sessionName = sessionName,
                plannedDurationSeconds = plannedDurationSeconds,
                actualDurationSeconds = actualDurationSeconds,
                timestampMillis = System.currentTimeMillis(),
                isCompleted = isCompleted,
                feeling = feeling,
                note = note,
                tags = tags
            )
        )
    }

    suspend fun updateEntry(
        entry: JournalEntry,
        feeling: Feeling?,
        note: String,
        tags: List<JournalTag>
    ) {
        dao.update(entry.copy(feeling = feeling, note = note, tags = tags))
    }

    suspend fun deleteEntry(entry: JournalEntry) {
        dao.delete(entry)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }

    suspend fun getCurrentStreakDays(zone: ZoneId = ZoneId.systemDefault()): Int {
        val days = dao.getCompletedTimestamps()
            .map { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
            .distinct()
            .sortedDescending()

        if (days.isEmpty()) return 0

        val today = Instant.now().atZone(zone).toLocalDate()
        var expected = today
        var streak = 0

        for (day in days) {
            if (day == expected) {
                streak++
                expected = expected.minus(1, ChronoUnit.DAYS)
            } else if (day.isBefore(expected)) {
                break
            }
        }
        return streak
    }

    suspend fun getProfileStats(): ProfileStats {
        val sessionCount = dao.getSessionCount()
        val totalMinutes = (dao.getTotalMeditatedSeconds() ?: 0) / 60
        val mostCommonFeeling = dao.getAllFeelings()
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

        return ProfileStats(
            sessionCount = sessionCount,
            totalMeditatedMinutes = totalMinutes,
            mostCommonFeeling = mostCommonFeeling
        )
    }
}

fun List<JournalEntry>.journalOnly(): List<JournalEntry> = filter { it.isJournalEntry }

fun List<JournalEntry>.daysWithEntries(
    days: Int,
    today: LocalDate = LocalDate.now(),
    zone: ZoneId = ZoneId.systemDefault()
): List<LocalDate> {
    val start = today.minusDays((days - 1).toLong())
    val present = journalOnly()
        .map { Instant.ofEpochMilli(it.timestampMillis).atZone(zone).toLocalDate() }
        .toSet()
    return (0 until days).map { start.plusDays(it.toLong()) }.filter { it in present }
}

fun List<JournalEntry>.feelingCounts(
    days: Int,
    today: LocalDate = LocalDate.now(),
    zone: ZoneId = ZoneId.systemDefault()
): Map<Feeling, Int> {
    val start = today.minusDays((days - 1).toLong())
    return journalOnly()
        .filter {
            val date = Instant.ofEpochMilli(it.timestampMillis).atZone(zone).toLocalDate()
            !date.isBefore(start) && !date.isAfter(today)
        }
        .mapNotNull { it.feeling }
        .groupingBy { it }
        .eachCount()
}
