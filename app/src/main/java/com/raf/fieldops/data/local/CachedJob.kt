package com.raf.fieldops.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.raf.fieldops.data.model.Job

@Entity(tableName = "cached_jobs")
data class CachedJob(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val address: String,
    val scheduledStartMillis: Long?,
    val scheduledEndMillis: Long?,
    val priority: String,
    val status: String,
    val assignedTo: String,
    val assignedEngineerName: String,
    val createdBy: String,
    val rejectionReason: String,
    val createdAtMillis: Long?,
    val updatedAtMillis: Long?,
    val cachedAt: Long
)

fun Job.toCachedJob(): CachedJob = CachedJob(
    id = id,
    title = title,
    description = description,
    address = address,
    scheduledStartMillis = scheduledStart?.toDate()?.time,
    scheduledEndMillis = scheduledEnd?.toDate()?.time,
    priority = priority,
    status = status,
    assignedTo = assignedTo,
    assignedEngineerName = assignedEngineerName,
    createdBy = createdBy,
    rejectionReason = rejectionReason,
    createdAtMillis = createdAt?.toDate()?.time,
    updatedAtMillis = updatedAt?.toDate()?.time,
    cachedAt = System.currentTimeMillis()
)

fun CachedJob.toJob(): Job = Job(
    id = id,
    title = title,
    description = description,
    address = address,
    scheduledStart = scheduledStartMillis?.let { Timestamp(java.util.Date(it)) },
    scheduledEnd = scheduledEndMillis?.let { Timestamp(java.util.Date(it)) },
    priority = priority,
    status = status,
    assignedTo = assignedTo,
    assignedEngineerName = assignedEngineerName,
    createdBy = createdBy,
    rejectionReason = rejectionReason,
    createdAt = createdAtMillis?.let { Timestamp(java.util.Date(it)) },
    updatedAt = updatedAtMillis?.let { Timestamp(java.util.Date(it)) }
)
