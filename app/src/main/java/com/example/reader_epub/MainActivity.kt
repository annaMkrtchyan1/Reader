package com.example.reader_epub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.reader_epub.presentation.reader.EpubReaderScreen
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val epubFile = remember { mutableStateOf<File?>(null) }
                    LaunchedEffect(Unit) {
                        epubFile.value = copyAssetToInternalStorage("ebook.demo.epub")
                    }
                    epubFile.value?.let { file ->
                        EpubReaderScreen(file = file)
                    }
                }
            }
        }
    }

    private fun copyAssetToInternalStorage(fileName: String): File? {
        return try {
            val file = File(filesDir, fileName)
            if (!file.exists()) {
                assets.open(fileName).use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}