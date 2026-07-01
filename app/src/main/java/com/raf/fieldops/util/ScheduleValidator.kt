package com.raf.fieldops.util

import com.raf.fieldops.data.model.Job
import com.raf.fieldops.data.model.JobStatus
import com.raf.fieldops.data.model.toJobStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object ScheduleValidator {

    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.UK)

    fun findConflict(
        existingJobs: List<Job>,
        proposedStartMillis: Long,
        proposedEndMillis: Long,
        excludeJobId: String? = null
    ): ConflictResult? {
        val activeJobs = existingJobs.filter { job ->

            val status = job.status.toJobStatus()
            val isActive = status == JobStatus.Assigned ||
                status == JobStatus.Accepted ||
                status == JobStatus.InProgress

            val isNotExcluded = job.id != excludeJobId

            isActive && isNotExcluded &&
                job.scheduledStart != null && job.scheduledEnd != null
        }

        for (job in activeJobs) {
            val existingStart = job.scheduledStart!!.toDate().time
            val existingEnd = job.scheduledEnd!!.toDate().time

            if (proposedStartMillis < existingEnd && proposedEndMillis > existingStart) {
                return ConflictResult(
                    conflictingJob = job,
                    conflictingStartTime = timeFormatter.format(Date(existingStart)),
                    conflictingEndTime = timeFormatter.format(Date(existingEnd))
                )
            }
        }

        return null
    }

    fun findFreeSlots(
        existingJobs: List<Job>,
        dayMillis: Long,
        minimumDurationMinutes: Int = 60,
        excludeJobId: String? = null
    ): List<FreeSlot> {

        val cal = Calendar.getInstance()
        cal.timeInMillis = dayMillis
        cal.set(Calendar.HOUR_OF_DAY, 7)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val dayStart = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 18)
        val dayEnd = cal.timeInMillis

        val now = System.currentTimeMillis()

        val dayJobs = existingJobs
            .filter { job ->
                val status = job.status.toJobStatus()
                val isActive = status == JobStatus.Assigned ||
                    status == JobStatus.Accepted ||
                    status == JobStatus.InProgress
                val isNotExcluded = job.id != excludeJobId
                val isOnDay = job.scheduledStart?.toDate()?.let { startDate ->
                    val jobCal = Calendar.getInstance()
                    jobCal.time = startDate
                    jobCal.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().apply {
                        timeInMillis = dayMillis
                    }.get(Calendar.DAY_OF_YEAR) &&
                    jobCal.get(Calendar.YEAR) == Calendar.getInstance().apply {
                        timeInMillis = dayMillis
                    }.get(Calendar.YEAR)
                } ?: false

                isActive && isNotExcluded && isOnDay &&
                    job.scheduledStart != null && job.scheduledEnd != null
            }
            .sortedBy { it.scheduledStart }

        val freeSlots = mutableListOf<FreeSlot>()
        var currentStart = dayStart

        for (job in dayJobs) {
            val jobStart = job.scheduledStart!!.toDate().time
            val jobEnd = job.scheduledEnd!!.toDate().time

            if (jobStart > currentStart) {
                addHourlySlots(freeSlots, currentStart, jobStart, now)
            }

            if (jobEnd > currentStart) {
                currentStart = jobEnd
            }
        }

        if (currentStart < dayEnd) {
            addHourlySlots(freeSlots, currentStart, dayEnd, now)
        }

        return freeSlots
    }

    private fun addHourlySlots(
        slots: MutableList<FreeSlot>,
        gapStart: Long,
        gapEnd: Long,
        now: Long
    ) {
        val oneHour = 3600000L
        var slotStart = gapStart

        while (slotStart + oneHour <= gapEnd) {
            val slotEnd = slotStart + oneHour

            if (slotStart >= now) {
                slots.add(
                    FreeSlot(
                        startMillis = slotStart,
                        endMillis = slotEnd,
                        startFormatted = timeFormatter.format(Date(slotStart)),
                        endFormatted = timeFormatter.format(Date(slotEnd))
                    )
                )
            }

            slotStart += oneHour
        }
    }
}

data class ConflictResult(
    val conflictingJob: Job,
    val conflictingStartTime: String,
    val conflictingEndTime: String
) {

    fun toMessage(): String =
        "${conflictingJob.assignedEngineerName} has '${conflictingJob.title}' from $conflictingStartTime–$conflictingEndTime"
}

data class FreeSlot(
    val startMillis: Long,
    val endMillis: Long,
    val startFormatted: String,
    val endFormatted: String
) {

    fun toDisplayText(): String = "$startFormatted – $endFormatted"
}
