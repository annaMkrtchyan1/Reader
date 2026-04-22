package com.example.reader_epub.domain

import com.example.reader_epub.data.local.ReadingProgress

interface ProgressRepository {
    suspend fun getProgress(bookId: String): ReadingProgress?
    suspend fun saveProgress(progress: ReadingProgress)
}