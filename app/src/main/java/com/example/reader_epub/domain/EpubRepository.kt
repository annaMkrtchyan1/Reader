package com.example.reader_epub.domain

import com.example.reader_epub.domain.model.EpubBook
import java.io.File

interface EpubRepository {
    suspend fun parseEpub(file: File): EpubBook
}