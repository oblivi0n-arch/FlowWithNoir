package com.oblivi0n_arch.flowwithnoir.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {

    @Insert
    suspend fun insert(entry: JournalEntry): Long

    @Update
    suspend fun update(entry: JournalEntry)

    @Delete
    suspend fun delete(entry: JournalEntry)

    @Query("SELECT * FROM journal_entries ORDER BY timestampMillis DESC")
    fun getAll(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<JournalEntry?>

    @Query("SELECT * FROM journal_entries ORDER BY timestampMillis DESC LIMIT 1")
    fun getLatest(): Flow<JournalEntry?>

    @Query("SELECT timestampMillis FROM journal_entries WHERE isCompleted = 1 AND plannedDurationSeconds > 0 ORDER BY timestampMillis DESC")
    suspend fun getCompletedTimestamps(): List<Long>

    @Query("SELECT COUNT(*) FROM journal_entries WHERE plannedDurationSeconds > 0")
    suspend fun getSessionCount(): Int

    @Query("SELECT SUM(actualDurationSeconds) FROM journal_entries WHERE plannedDurationSeconds > 0")
    suspend fun getTotalMeditatedSeconds(): Int?

    @Query("SELECT feeling FROM journal_entries WHERE feeling IS NOT NULL")
    suspend fun getAllFeelings(): List<Feeling>

    @Query("DELETE FROM journal_entries")
    suspend fun deleteAll()
}
