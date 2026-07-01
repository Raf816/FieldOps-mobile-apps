package com.raf.fieldops.util

import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.repo.JobRepo

object JobActions {

    suspend fun accept(job: Job, jobRepo: JobRepo) {
        val updatedJob = job.copy(
            status = JobStatus.Accepted.name,
            updatedAt = null
        )
        jobRepo.updateJob(updatedJob)
    }

    suspend fun reject(job: Job, reason: String, jobRepo: JobRepo) {
        val rejectionReason = InputSanitiser.sanitiseLong(reason).ifBlank { "No reason provided" }
        val updatedJob = job.copy(
            status = JobStatus.Assigned.name,
            assignedTo = "",
            assignedEngineerName = "",
            rejectionReason = rejectionReason,
            updatedAt = null
        )
        jobRepo.updateJob(updatedJob)
    }

    suspend fun dismiss(job: Job, jobRepo: JobRepo) {
        val updatedJob = job.copy(
            status = JobStatus.Dismissed.name,
            updatedAt = null
        )
        jobRepo.updateJob(updatedJob)
    }
}
