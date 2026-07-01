package com.raf.fieldops.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.raf.fieldops.data.local.CachedJobDao
import com.raf.fieldops.data.local.FieldOpsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = Firebase.firestore
        val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(
                com.google.firebase.firestore.persistentCacheSettings {
                    setSizeBytes(100 * 1024 * 1024)
                }
            )
            .build()
        firestore.firestoreSettings = settings
        return firestore
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FieldOpsDatabase =
        Room.databaseBuilder(
            context,
            FieldOpsDatabase::class.java,
            "fieldops_db"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideCachedJobDao(database: FieldOpsDatabase): CachedJobDao =
        database.cachedJobDao()
}
