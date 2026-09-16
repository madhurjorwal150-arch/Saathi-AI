package com.sathii.ai.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NoteEntity::class], version = 1, exportSchema = false)
abstract class SathiDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: SathiDatabase? = null

        fun getDatabase(context: Context): SathiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SathiDatabase::class.java,
                    "sathi_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
