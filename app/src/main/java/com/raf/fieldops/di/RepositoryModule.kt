package com.raf.fieldops.di

import com.raf.fieldops.data.remote.AddressLookupService
import com.raf.fieldops.data.remote.GooglePlacesAddressLookup
import com.raf.fieldops.data.repo.AuthRepo
import com.raf.fieldops.data.repo.AuthRepository
import com.raf.fieldops.data.repo.JobRepo
import com.raf.fieldops.data.repo.JobRepository
import com.raf.fieldops.data.repo.NoteRepo
import com.raf.fieldops.data.repo.NoteRepository
import com.raf.fieldops.data.repo.UserRepo
import com.raf.fieldops.data.repo.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(authRepository: AuthRepository): AuthRepo

    @Binds
    @Singleton
    abstract fun bindUserRepository(userRepository: UserRepository): UserRepo

    @Binds
    @Singleton
    abstract fun bindJobRepository(jobRepository: JobRepository): JobRepo

    @Binds
    @Singleton
    abstract fun bindNoteRepository(noteRepository: NoteRepository): NoteRepo

    @Binds
    @Singleton
    abstract fun bindAddressLookupService(
        googlePlacesAddressLookup: GooglePlacesAddressLookup
    ): AddressLookupService
}
