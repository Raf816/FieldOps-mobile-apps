package com.raf.fieldops.data.repo

import com.raf.fieldops.data.local.CachedJobDao
import com.raf.fieldops.data.local.toCachedJob
import com.raf.fieldops.data.local.toJob
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.remote.JobDao
import com.raf.fieldops.util.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class JobRepository @Inject constructor(
    private val jobDao: JobDao,
    private val cachedJobDao: CachedJobDao,
    private val networkMonitor: NetworkMonitor
) : JobRepo {

    private val cacheScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _lastSynced = MutableStateFlow<Long?>(null)
    override val lastSynced: StateFlow<Long?> = _lastSynced.asStateFlow()

    init {

        cacheScope.launch {
            clearStaleCache()
        }
    }

    override suspend fun createJob(job: Job) = jobDao.add(job)

    override suspend fun updateJob(job: Job) = jobDao.update(job)

    override suspend fun deleteJob(jobId: String) = jobDao.delete(jobId)

    override fun getAllJobs(): Flow<List<Job>> = jobDao.getAll()

    // switches between Firestore and Room depending on network
    override fun getJobsForEngineer(uid: String): Flow<List<Job>> =
        networkMonitor.isOnline.flatMapLatest { online ->
            if (online) {

                jobDao.getByEngineer(uid).onEach { jobs ->
                    cacheJobs(jobs)
                }
            } else {

                cachedJobDao.getByEngineer(uid).map { cached ->
                    cached.map { it.toJob() }
                }
            }
        }

    override fun getJobsForEngineerToday(uid: String): Flow<List<Job>> =
        networkMonitor.isOnline.flatMapLatest { online ->
            if (online) {
                jobDao.getByEngineerToday(uid).onEach { jobs ->
                    cacheJobs(jobs)
                }
            } else {

                cachedJobDao.getByEngineer(uid).map { cached ->
                    cached.map { it.toJob() }
                }
            }
        }

    override fun getJobsForEngineerUpcoming(uid: String): Flow<List<Job>> =
        networkMonitor.isOnline.flatMapLatest { online ->
            if (online) {
                jobDao.getByEngineerUpcoming(uid).onEach { jobs ->
                    cacheJobs(jobs)
                }
            } else {

                cachedJobDao.getByEngineer(uid).map { cached ->
                    cached.map { it.toJob() }
                }
            }
        }

    override suspend fun getJobById(jobId: String): Job? = jobDao.getById(jobId)

    override fun getCompletedJobsForEngineer(uid: String): Flow<List<Job>> =
        networkMonitor.isOnline.flatMapLatest { online ->
            if (online) {
                jobDao.getCompletedJobsForEngineer(uid).onEach { jobs ->
                    cacheJobs(jobs)
                }
            } else {

                cachedJobDao.getByEngineer(uid).map { cached ->
                    cached.map { it.toJob() }
                        .filter { it.status == "Completed" || it.status == "Cancelled" }
                }
            }
        }

    private fun cacheJobs(jobs: List<Job>) {
        _lastSynced.value = System.currentTimeMillis()
        cacheScope.launch {
            cachedJobDao.insertAll(jobs.map { it.toCachedJob() })
        }
    }

    private suspend fun clearStaleCache() {
        val twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        cachedJobDao.clearStale(twentyFourHoursAgo)
    }
}
