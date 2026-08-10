package com.oblivi0n_arch.flowwithnoir.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {

    @Insert
    suspend fun insert(entry: JournalEntry): Long

    @Query("SELECT * FROM journal_entries ORDER BY timestampMillis DESC")
    fun getAll(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries ORDER BY timestampMillis DESC LIMIT 1")
    fun getLatest(): Flow<JournalEntry?>

    @Query("SELECT timestampMillis FROM journal_entries WHERE isCompleted = 1 AND plannedDurationSeconds > 0 ORDER BY timestampMillis DESC")
    suspend fun getCompletedTimestamps(): List<Long>
}
