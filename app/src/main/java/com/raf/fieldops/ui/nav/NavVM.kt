package com.raf.fieldops.ui.nav

import androidx.lifecycle.ViewModel
import com.raf.fieldops.data.model.User
import com.raf.fieldops.data.repo.AuthRepo
import com.raf.fieldops.data.repo.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavVM @Inject constructor(
    val authRepo: AuthRepo,
    private val userRepo: UserRepo
) : ViewModel() {

    val currentUserUid: String?
        get() = authRepo.currentUser?.uid

    suspend fun getUserRole(uid: String): String? = userRepo.getUserRole(uid)

    suspend fun getUser(uid: String): User? = userRepo.getUserById(uid)

    suspend fun markEmailVerified(uid: String) = userRepo.markEmailVerified(uid)

    fun signOut() = authRepo.signOut()
}
