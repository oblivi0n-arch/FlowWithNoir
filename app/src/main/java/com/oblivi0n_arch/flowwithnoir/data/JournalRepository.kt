package com.oblivi0n_arch.flowwithnoir.data

import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class JournalRepository(private val dao: JournalDao) {

    fun getEntries(): Flow<List<JournalEntry>> = dao.getAll()

    fun getLatestEntry(): Flow<JournalEntry?> = dao.getLatest()

    suspend fun addEntry(
        sessionName: String,
        durationSeconds: Int,
        feeling: Feeling,
        note: String
    ) {
        dao.insert(
            JournalEntry(
                sessionName = sessionName,
                durationSeconds = durationSeconds,
                timestampMillis = System.currentTimeMillis(),
                feeling = feeling,
                note = note
            )
        )
    }

    suspend fun getCurrentStreakDays(zone: ZoneId = ZoneId.systemDefault()): Int {
        val days = dao.getAllTimestamps()
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
}
