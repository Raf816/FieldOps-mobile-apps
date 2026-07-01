package com.raf.fieldops.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.raf.fieldops.data.local.CachedJobDao
import com.raf.fieldops.data.remote.AddressLookupService
import com.raf.fieldops.data.repo.AuthRepo
import com.raf.fieldops.data.repo.JobRepo
import com.raf.fieldops.data.repo.NoteRepo
import com.raf.fieldops.data.repo.UserRepo
import com.raf.fieldops.util.NetworkMonitor
import com.raf.fieldops.util.ThemeDataStore
import com.raf.fieldops.util.ThemePreference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestModule {

    @Provides
    @Singleton
    fun provideAuthRepo(): AuthRepo = Mockito.mock(AuthRepo::class.java)

    @Provides
    @Singleton
    fun provideUserRepo(): UserRepo = Mockito.mock(UserRepo::class.java)

    @Provides
    @Singleton
    fun provideJobRepo(): JobRepo {
        val mock = Mockito.mock(JobRepo::class.java)
        whenever(mock.lastSynced).thenReturn(MutableStateFlow(null))
        return mock
    }

    @Provides
    @Singleton
    fun provideNoteRepo(): NoteRepo = Mockito.mock(NoteRepo::class.java)

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Mockito.mock(FirebaseAuth::class.java)

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore =
        Mockito.mock(FirebaseFirestore::class.java)

    @Provides
    @Singleton
    fun provideCachedJobDao(): CachedJobDao = Mockito.mock(CachedJobDao::class.java)

    @Provides
    @Singleton
    fun provideNetworkMonitor(): NetworkMonitor {
        val mock = Mockito.mock(NetworkMonitor::class.java)
        whenever(mock.isOnline).thenReturn(MutableStateFlow(true))
        return mock
    }

    @Provides
    @Singleton
    fun provideThemeDataStore(): ThemeDataStore {
        val mock = Mockito.mock(ThemeDataStore::class.java)
        whenever(mock.themePreference).thenReturn(flowOf(ThemePreference.System))
        return mock
    }

    @Provides
    @Singleton
    fun provideAddressLookupService(): AddressLookupService {
        val mock = Mockito.mock(AddressLookupService::class.java)
        runBlocking {
            whenever(mock.findAddresses(any())).thenReturn(emptyList())
        }
        return mock
    }
}
