package com.example.reader_epub.presentation.reader

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import java.io.File

class ReaderJsInterface(private val onToggleUi: () -> Unit) {
    @JavascriptInterface
    fun toggleUi() {
        Handler(Looper.getMainLooper()).post {
            onToggleUi()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "SetJavaScriptEnabled")
@Composable
fun EpubReaderScreen(
    viewModel: ReaderViewModel = hiltViewModel(),
    file: File
) {
    val state by viewModel.uiState.collectAsState()
    var isUiVisible by remember { mutableStateOf(false) }

    LaunchedEffect(file) {
        viewModel.loadBook(file)
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = isUiVisible,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it })
            ) {
                TopAppBar(
                    title = {
                        val titleText = if (state is ReaderState.Success) {
                            (state as ReaderState.Success).book.title
                        } else "Загрузка"

                        Text(text = titleText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isUiVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                if (state is ReaderState.Success) {
                    val successState = state as ReaderState.Success
                    BottomAppBar(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { viewModel.previousChapter(successState.book.id) },
                                enabled = successState.currentChapterIndex > 0
                            ) { Text("Назад") }

                            Text(
                                text = "${successState.currentChapterIndex + 1} / ${successState.book.chapters.size}",
                                style = MaterialTheme.typography.labelLarge
                            )

                            Button(
                                onClick = { viewModel.nextChapter(successState.book.id) },
                                enabled = successState.currentChapterIndex < successState.book.chapters.size - 1
                            ) { Text("Вперед") }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        isUiVisible = !isUiVisible
                    }
                ),
        ) {
            when (val currentState = state) {
                is ReaderState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ReaderState.Error -> {
                    Text(
                        text = "Ошибка: ${currentState.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                is ReaderState.Success -> {
                    val chapter = currentState.book.chapters.getOrNull(currentState.currentChapterIndex)

                    if (chapter != null) {
                        val optimizedHtml = injectMobileCss(chapter.htmlContent)
                        val chapterFile = File(currentState.book.extractedPath, chapter.href)
                        val safeBaseUrl = "file://${chapterFile.parentFile?.absolutePath}/"

                        AndroidView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center),
                            factory = { context ->
                                WebView(context).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.defaultTextEncodingName = "utf-8"
                                    settings.allowFileAccess = true

                                    addJavascriptInterface(
                                        ReaderJsInterface {
                                            isUiVisible = !isUiVisible
                                        },
                                        "AndroidInterface"
                                    )

                                    webViewClient = object : WebViewClient() {
                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            view?.scrollTo(0, currentState.scrollY)
                                        }
                                    }

                                    setOnScrollChangeListener { _, _, scrollY, _, _ ->
                                        viewModel.saveProgress(currentState.book.id, currentState.currentChapterIndex, scrollY)
                                    }
                                }
                            },
                            update = { webView ->
                                webView.loadDataWithBaseURL(
                                    safeBaseUrl, optimizedHtml, "text/html", "UTF-8", null
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun injectMobileCss(rawHtml: String): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                body {
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                    font-size: 18px;
                    line-height: 1.6;
                    padding: 24px 16px;
                    color: #333333;
                    background-color: #FFFFFF;
                    word-wrap: break-word;
                    max-width: 100%;
                    display: flex; 
                    justify-content: center; 
                    align-items: center;
                    overflow-x: hidden;
                }
                img {
                    max-width: 100%;
                    height: auto;
                    display: block;
                    margin: 0 auto;
                    display: flex; 
                    justify-content: center; 
                    align-items: center;
                    border-radius: 8px;
                }
                a {
                    color: #0066cc;
                    text-decoration: none;
                }
            </style>
            <script>
                document.addEventListener('click', function(event) {
                    if (event.target.tagName.toLowerCase() !== 'a') {
                        AndroidInterface.toggleUi();
                    }
                });
            </script>
        </head>
        <body>
            $rawHtml
        </body>
        </html>
    """.trimIndent()
}