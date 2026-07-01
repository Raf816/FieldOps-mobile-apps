package com.raf.fieldops.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedJobDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(jobs: List<CachedJob>)

    @Query("SELECT * FROM cached_jobs WHERE assignedTo = :uid")
    fun getByEngineer(uid: String): Flow<List<CachedJob>>

    @Query("DELETE FROM cached_jobs")
    suspend fun clearAll()

    @Query("DELETE FROM cached_jobs WHERE cachedAt < :cutoff")
    suspend fun clearStale(cutoff: Long)
}
