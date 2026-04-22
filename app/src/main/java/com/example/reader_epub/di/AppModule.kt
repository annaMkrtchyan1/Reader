package com.example.reader_epub.di

import com.example.reader_epub.data.EpubRepositoryImpl
import com.example.reader_epub.data.ProgressRepositoryImpl
import com.example.reader_epub.domain.EpubRepository
import com.example.reader_epub.domain.ProgressRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {
    @Binds
    fun bindEpubRepository(epubRepositoryImpl: EpubRepositoryImpl): EpubRepository
    @Binds
    fun bindProgressRepository(progressRepositoryImpl: ProgressRepositoryImpl): ProgressRepository
}