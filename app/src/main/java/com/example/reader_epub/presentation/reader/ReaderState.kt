package com.example.reader_epub.presentation.reader

import com.example.reader_epub.domain.model.EpubBook

sealed interface ReaderState {
    data object Loading : ReaderState 

    data class Success(
        val book: EpubBook,
        val currentChapterIndex: Int,
        val scrollY: Int
    ) : ReaderState

    data class Error(val message: String) : ReaderState 
}