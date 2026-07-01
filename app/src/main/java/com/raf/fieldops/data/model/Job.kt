package com.raf.fieldops.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Job(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val address: String = "",
    val scheduledStart: Timestamp? = null,
    val scheduledEnd: Timestamp? = null,
    val priority: String = Priority.Medium.name,
    val status: String = JobStatus.Assigned.name,
    val assignedTo: String = "",
    val assignedEngineerName: String = "",
    val createdBy: String = "",
    val rejectionReason: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
