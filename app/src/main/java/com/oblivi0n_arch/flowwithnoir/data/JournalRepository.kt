package com.oblivi0n_arch.flowwithnoir.data

import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class ProfileStats(
    val sessionCount: Int,
    val totalMeditatedMinutes: Int,
    val mostCommonFeeling: Feeling?
)

class JournalRepository(private val dao: JournalDao) {

    fun getEntries(): Flow<List<JournalEntry>> = dao.getAll()

    fun getLatestEntry(): Flow<JournalEntry?> = dao.getLatest()

    suspend fun logSession(
        sessionName: String,
        plannedDurationSeconds: Int,
        actualDurationSeconds: Int,
        isCompleted: Boolean,
        feeling: Feeling? = null,
        note: String = ""
    ) {
        dao.insert(
            JournalEntry(
                sessionName = sessionName,
                plannedDurationSeconds = plannedDurationSeconds,
                actualDurationSeconds = actualDurationSeconds,
                timestampMillis = System.currentTimeMillis(),
                isCompleted = isCompleted,
                feeling = feeling,
                note = note
            )
        )
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
