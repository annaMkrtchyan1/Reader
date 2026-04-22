package com.example.reader_epub.data

import com.example.reader_epub.data.local.ProgressDao
import com.example.reader_epub.data.local.ReadingProgress
import com.example.reader_epub.domain.ProgressRepository
import javax.inject.Inject

class ProgressRepositoryImpl @Inject constructor(private val dao: ProgressDao): ProgressRepository {
    override suspend fun getProgress(bookId: String) = dao.getProgress(bookId)
    override suspend fun saveProgress(progress: ReadingProgress) = dao.saveProgress(progress)
}