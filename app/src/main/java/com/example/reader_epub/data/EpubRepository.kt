package com.example.reader_epub.data

import com.example.reader_epub.domain.model.EpubBook
import com.example.reader_epub.domain.model.EpubChapter
import com.example.reader_epub.domain.EpubRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.siegmann.epublib.epub.EpubReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

class EpubRepositoryImpl @Inject constructor(): EpubRepository {

    override suspend fun parseEpub(file: File): EpubBook = withContext(Dispatchers.IO) {
        val epubReader = EpubReader()
        val book = FileInputStream(file).use { epubReader.readEpub(it) }
        val extractDir = File(file.parentFile, "extracted_${file.nameWithoutExtension}")
        if (!extractDir.exists()) {
            extractDir.mkdirs()
            book.resources.all.forEach { resource ->
                val resFile = File(extractDir, resource.href)
                resFile.parentFile?.mkdirs()

                try {
                    FileOutputStream(resFile).use { fos ->
                        fos.write(resource.data)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        val bookTitle = book.metadata.titles.firstOrNull() ?: "Неизвестная книга"

        val chapters = book.spine.spineReferences.mapIndexed { index, spineReference ->
            val resource = spineReference.resource
            val htmlContent = String(resource.data, Charsets.UTF_8)

            EpubChapter(
                id = resource.id ?: index.toString(),
                title = resource.title ?: "Глава ${index + 1}",
                htmlContent = htmlContent,
                href = resource.href
            )
        }

        EpubBook(
            id = file.name,
            title = bookTitle,
            chapters = chapters,
            extractedPath = extractDir.absolutePath
        )
    }
}