package com.raf.fieldops.data.repo

import com.google.firebase.auth.FirebaseUser
import com.raf.fieldops.data.model.Response

interface AuthRepo {

    val currentUser: FirebaseUser?

    val isEmailVerified: Boolean

    suspend fun signInWithEmailAndPassword(email: String, password: String): Response

    suspend fun signUpWithEmailAndPassword(email: String, password: String): Response

    suspend fun sendEmailVerification(): Response

    suspend fun sendPasswordResetEmail(email: String): Response

    suspend fun changePassword(currentPassword: String, newPassword: String): Response

    fun signOut()
}
