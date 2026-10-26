package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ProjectEntity::class, ClipEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ClipNexDatabase : RoomDatabase() {
    abstract fun clipNexDao(): ClipNexDao

    companion object {
        @Volatile
        private var INSTANCE: ClipNexDatabase? = null

        fun getDatabase(context: Context): ClipNexDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClipNexDatabase::class.java,
                    "clipnex_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
