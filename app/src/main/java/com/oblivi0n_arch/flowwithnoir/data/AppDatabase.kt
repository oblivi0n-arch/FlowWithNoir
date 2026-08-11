package com.oblivi0n_arch.flowwithnoir.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [JournalEntry::class], version = 4, exportSchema = false)
@TypeConverters(JournalConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun journalDao(): JournalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE journal_entries ADD COLUMN tags TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE journal_entries ADD COLUMN title TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flowwithnoir.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigrationFrom(true, 1)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
