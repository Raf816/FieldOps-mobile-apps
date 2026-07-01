package com.raf.fieldops.data.repo

import com.raf.fieldops.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepo {

    suspend fun createUser(user: User)

    suspend fun getUserById(uid: String): User?

    suspend fun getUserRole(uid: String): String?

    fun getAllEngineers(): Flow<List<User>>

    suspend fun updateDisplayName(uid: String, newName: String)

    suspend fun markEmailVerified(uid: String)

    fun observeUser(uid: String): Flow<User?>

    fun getAllUsers(): Flow<List<User>>

    suspend fun updateUserStatus(uid: String, status: String)

    suspend fun updateUserRole(uid: String, role: String)

    suspend fun deleteUser(uid: String)
}
