package com.raf.fieldops.data.repo

import com.raf.fieldops.data.model.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface JobRepo {

    val lastSynced: StateFlow<Long?>

    suspend fun createJob(job: Job)

    suspend fun updateJob(job: Job)

    suspend fun deleteJob(jobId: String)

    fun getAllJobs(): Flow<List<Job>>

    fun getJobsForEngineer(uid: String): Flow<List<Job>>

    fun getJobsForEngineerToday(uid: String): Flow<List<Job>>

    fun getJobsForEngineerUpcoming(uid: String): Flow<List<Job>>

    fun getCompletedJobsForEngineer(uid: String): Flow<List<Job>>

    suspend fun getJobById(jobId: String): Job?
}
