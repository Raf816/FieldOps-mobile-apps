package com.raf.fieldops.data.repo

import com.raf.fieldops.data.model.User
import com.raf.fieldops.data.remote.UserDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

//wrapper delegates to UserDao, no caching needed for user data
@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) : UserRepo {

    override suspend fun createUser(user: User) = userDao.createUser(user)

    override suspend fun getUserById(uid: String): User? = userDao.getUserById(uid)

    override suspend fun getUserRole(uid: String): String? = userDao.getUserRole(uid)

    override fun getAllEngineers(): Flow<List<User>> = userDao.getAllEngineers()

    override suspend fun updateDisplayName(uid: String, newName: String) =
        userDao.updateDisplayName(uid, newName)

    override suspend fun markEmailVerified(uid: String) =
        userDao.markEmailVerified(uid)

    override fun observeUser(uid: String): Flow<User?> =
        userDao.observeUser(uid)

    override fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()

    override suspend fun updateUserStatus(uid: String, status: String) =
        userDao.updateUserStatus(uid, status)

    override suspend fun updateUserRole(uid: String, role: String) =
        userDao.updateUserRole(uid, role)

    override suspend fun deleteUser(uid: String) =
        userDao.deleteUser(uid)
}
