package com.example.reader_epub.domain.model

data class EpubBook(
    val id: String,
    val title: String,
    val chapters: List<EpubChapter>,
    val extractedPath: String,
)

data class EpubChapter(
    val id: String,
    val title: String,
    val htmlContent: String,
    val href: String,
)
