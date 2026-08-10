package com.oblivi0n_arch.flowwithnoir.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Feeling(val label: String) {
    CALM("Spokój"),
    NEUTRAL("Neutralnie"),
    STRESSED("Napięcie"),
    TIRED("Zmęczenie")
}

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionName: String,
    val plannedDurationSeconds: Int,
    val actualDurationSeconds: Int,
    val timestampMillis: Long,
    val isCompleted: Boolean,
    val feeling: Feeling?,
    val note: String
)
