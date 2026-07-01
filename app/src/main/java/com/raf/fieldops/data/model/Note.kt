package com.raf.fieldops.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Note(
    @DocumentId val id: String = "",
    val text: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val isInternal: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)
