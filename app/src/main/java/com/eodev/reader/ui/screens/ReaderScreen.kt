package com.eodev.reader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eodev.reader.ui.viewmodel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId: Long,
    viewModel: BookViewModel,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val book by viewModel.getBookById(bookId).collectAsStateWithLifecycle()
    val readingState by viewModel.readingState.collectAsStateWithLifecycle()

    DisposableEffect(bookId) {
        viewModel.loadBookForReading(context, bookId)
        onDispose {
            viewModel.clearReadingState()
        }
    }
    //Text("READ LIST")
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = book!!.book.title ?: "Чтение",
                            maxLines = 1,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (readingState.hasContent) {
                            val content = readingState.content!!
                            Text(
                                text = "Глава ${readingState.currentChapter + 1} из ${content.totalChapters}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        bottomBar = {
            if (readingState.hasContent) {
                val content = readingState.content!!
                ReaderBottomBar(
                    currentChapter = readingState.currentChapter,
                    totalChapters = content.totalChapters,
                    progress = readingState.progress,
                    onPreviousChapter = { viewModel.previousChapter(bookId) },
                    onNextChapter = { viewModel.nextChapter(bookId) },
                    canGoPrevious = readingState.currentChapter > 0,
                    canGoNext = readingState.currentChapter < content.totalChapters - 1
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                readingState.isLoading -> {
                    LoadingView()
                }

                readingState.error != null -> {
                    ErrorView(error = readingState.error!!)
                }

                readingState.hasContent -> {
                    val chapter = readingState.currentChapterData
                    if (chapter != null) {
                        ChapterView(chapter = chapter)
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Подготовка к чтению...")
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Загрузка книги...")
        }
    }
}

@Composable
fun ErrorView(error: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Ошибка",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun ChapterView(chapter: com.eodev.reader.reader.EpubChapter) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = chapter.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = cleanHtmlContent(chapter.content),
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5f,
            textAlign = TextAlign.Justify
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ReaderBottomBar(
    currentChapter: Int,
    totalChapters: Int,
    progress: Float,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit,
    canGoPrevious: Boolean,
    canGoNext: Boolean
) {
    Surface(
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Прогресс ${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = onPreviousChapter,
                    enabled = canGoPrevious
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Предыдущая глава"
                    )
                }

                IconButton(
                    onClick = onNextChapter,
                    enabled = canGoNext
                ) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "Следующая глава"
                    )
                }
            }
        }
    }
}

private fun cleanHtmlContent(htmlContent: String): String {
    return htmlContent
        .replace("<br>", "\n")
        .replace("<br/>", "\n")
        .replace("<br />", "\n")
        .replace("<p>", "\n")
        .replace("</p>", "\n")
        .replace("<div>", "\n")
        .replace("</div>", "\n")
        .replace("<h1>", "\n\n")
        .replace("</h1>", "\n")
        .replace("<h2>", "\n\n")
        .replace("</h2>", "\n")
        .replace("<h3>", "\n\n")
        .replace("</h3>", "\n")
        .replace("<h4>", "\n\n")
        .replace("</h4>", "\n")
        .replace("<h5>", "\n\n")
        .replace("</h5>", "\n")
        .replace("<h6>", "\n\n")
        .replace("</h6>", "\n")
        .replace("<li>", "\n• ")
        .replace("</li>", "")
        .replace("<ul>", "\n")
        .replace("</ul>", "\n")
        .replace("<ol>", "\n")
        .replace("</ol>", "\n")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("<[^>]*>".toRegex(), "")
        .replace("\\s+".toRegex(), " ")
        .replace("\n\n\n+".toRegex(), "\n\n")
        .trim()
}