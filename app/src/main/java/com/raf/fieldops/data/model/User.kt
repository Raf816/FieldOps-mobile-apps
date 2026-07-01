package com.raf.fieldops.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    @DocumentId val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val role: String = "",
    val status: String = "pending",
    val emailVerified: Boolean = false,
    @ServerTimestamp val createdAt: Date? = null
)
