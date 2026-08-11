package com.oblivi0n_arch.flowwithnoir.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

enum class Feeling(val label: String) {
    CALM("Spokój"),
    NEUTRAL("Neutralnie"),
    STRESSED("Napięcie"),
    TIRED("Zmęczenie")
}

enum class JournalTag(val label: String) {
    WORK("Praca"),
    SLEEP("Sen"),
    RELATIONS("Relacje"),
    HEALTH("Zdrowie"),
    BODY("Ciało"),
    ANXIETY("Lęk"),
    GRATITUDE("Wdzięczność"),
    REST("Odpoczynek")
}

class JournalConverters {

    @TypeConverter
    fun tagsToString(tags: List<JournalTag>): String = tags.joinToString(",") { it.name }

    @TypeConverter
    fun stringToTags(value: String): List<JournalTag> =
        if (value.isBlank()) {
            emptyList()
        } else {
            value.split(",").mapNotNull { name ->
                runCatching { JournalTag.valueOf(name.trim()) }.getOrNull()
            }
        }
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
    val note: String,
    @ColumnInfo(defaultValue = "") val tags: List<JournalTag> = emptyList()
) {
    val isJournalEntry: Boolean
        get() = feeling != null || note.isNotBlank() || tags.isNotEmpty()

    val isSession: Boolean
        get() = plannedDurationSeconds > 0
}
