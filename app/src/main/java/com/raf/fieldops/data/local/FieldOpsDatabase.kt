package com.raf.fieldops.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CachedJob::class],
    version = 3,
    exportSchema = false
)
abstract class FieldOpsDatabase : RoomDatabase() {

    abstract fun cachedJobDao(): CachedJobDao
}
