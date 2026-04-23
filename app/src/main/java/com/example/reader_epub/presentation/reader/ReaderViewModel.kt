package com.example.reader_epub.presentation.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reader_epub.Logger
import com.example.reader_epub.data.local.ReadingProgress
import com.example.reader_epub.domain.EpubRepository
import com.example.reader_epub.domain.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val epubRepository: EpubRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReaderState>(ReaderState.Loading)
    val uiState: StateFlow<ReaderState> = _uiState.asStateFlow()

    fun loadBook(file: File) {
        viewModelScope.launch {
            try {
                Logger.log("Начло загрузки книги")
                val book = epubRepository.parseEpub(file)
                val progress = progressRepository.getProgress(book.id) ?: ReadingProgress(
                    book.id,
                    0,
                    0,
                    System.currentTimeMillis()
                )
                Logger.log("Книга загружена")
                _uiState.value = ReaderState.Success(book, progress.chapterIndex, progress.scrollY)
            } catch (_: Exception) {
                Logger.log("Ошибка загрузки книги")
                _uiState.value = ReaderState.Error("Произошла ошибка")
            }
        }
    }

    fun saveProgress(bookId: String, chapterIndex: Int, scrollY: Int) {
        Logger.log("Сохранение прогресса чтения")
        viewModelScope.launch {
            progressRepository.saveProgress(
                ReadingProgress(
                    bookId,
                    chapterIndex,
                    scrollY,
                    System.currentTimeMillis()
                )
            )
        }
    }

    fun nextChapter(bookId: String) {
        Logger.log("Следующая страница")
        val currentState = _uiState.value
        if (currentState is ReaderState.Success) {
            if (currentState.currentChapterIndex < currentState.book.chapters.size - 1) {
                val newIndex = currentState.currentChapterIndex + 1
                _uiState.value = currentState.copy(currentChapterIndex = newIndex, scrollY = 0)
                saveProgress(bookId, newIndex, 0)
            }
        }
    }

    fun previousChapter(bookId: String) {
        Logger.log("Предыдущая страница")
        val currentState = _uiState.value
        if (currentState is ReaderState.Success) {
            if (currentState.currentChapterIndex > 0) {
                val newIndex = currentState.currentChapterIndex - 1
                _uiState.value = currentState.copy(currentChapterIndex = newIndex, scrollY = 0)
                saveProgress(bookId, newIndex, 0)
            }
        }
    }
}
