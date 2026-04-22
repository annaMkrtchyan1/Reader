package com.example.reader_epub.di

import android.content.Context
import androidx.room.Room
import com.example.reader_epub.data.local.AppDatabase
import com.example.reader_epub.data.local.ProgressDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "reader_database"
        ).build()
    }

    @Provides
    fun provideProgressDao(database: AppDatabase): ProgressDao {
        return database.progressDao()
    }
}