package com.raf.fieldops.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.raf.fieldops.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDao @Inject constructor(private val firestore: FirebaseFirestore) {

    private val usersCollection = firestore.collection("users")

    suspend fun createUser(user: User) {
        usersCollection.document(user.uid).set(user).await()
    }

    suspend fun getUserById(uid: String): User? {
        val snapshot = usersCollection.document(uid).get().await()
        return snapshot.toObject(User::class.java)
    }

    suspend fun getUserRole(uid: String): String? {
        val user = getUserById(uid)
        return user?.role
    }

    fun getAllEngineers(): Flow<List<User>> {
        return usersCollection
            .whereEqualTo("role", "engineer")
            .whereEqualTo("emailVerified", true)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(User::class.java)
                    .sortedBy { it.displayName.lowercase() }
            }
            .catch { emit(emptyList()) }
    }

    suspend fun updateDisplayName(uid: String, newName: String) {
        usersCollection.document(uid)
            .update("displayName", newName)
            .await()
    }

    suspend fun markEmailVerified(uid: String) {
        usersCollection.document(uid)
            .update("emailVerified", true)
            .await()
    }

    fun observeUser(uid: String): Flow<User?> {
        return usersCollection.document(uid)
            .snapshots()
            .map { snapshot -> snapshot.toObject(User::class.java) }
            .catch { emit(null) }
    }

    fun getAllUsers(): Flow<List<User>> {
        return usersCollection
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(User::class.java)
                    .filter { it.role != "admin" }
                    .sortedBy { it.displayName.lowercase() }
            }
            .catch { emit(emptyList()) }
    }

    suspend fun updateUserStatus(uid: String, status: String) {
        usersCollection.document(uid)
            .update("status", status)
            .await()
    }

    suspend fun updateUserRole(uid: String, role: String) {
        usersCollection.document(uid)
            .update("role", role)
            .await()
    }

    suspend fun deleteUser(uid: String) {
        usersCollection.document(uid)
            .delete()
            .await()
    }
}
