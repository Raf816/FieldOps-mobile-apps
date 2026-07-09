package com.raf.fieldops.data.repo

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.raf.fieldops.data.model.Response
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepo {

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override val isEmailVerified: Boolean
        get() = auth.currentUser?.isEmailVerified ?: false

    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Response {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            // reload to get email verification status
            auth.currentUser?.reload()?.await()
            Response.Success
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    override suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String
    ): Response {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            Response.Success
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    override suspend fun sendEmailVerification(): Response {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            Response.Success
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Response {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Response.Success
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Response {
        return try {
            val user = auth.currentUser ?: return Response.Failure(Exception("No user signed in"))
            val email = user.email ?: return Response.Failure(Exception("No email on account"))

            // re-auth required by Firebase before changes
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()

            user.updatePassword(newPassword).await()

            Response.Success
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    override fun signOut() = auth.signOut()
}
