package com.eodev.reader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eodev.reader.ui.components.BookCover
import com.eodev.reader.ui.components.RatingDialog
import com.eodev.reader.ui.components.RatingItem
import com.eodev.reader.ui.viewmodel.BookViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(
    bookId: Long,
    viewModel: BookViewModel,
    onBackPressed: () -> Unit,
    onReadBook: () -> Unit,

) {
    val book by viewModel.getBookById(bookId).collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    val ratings by viewModel.getBookRatings(bookId).collectAsStateWithLifecycle(emptyList())
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Информация о книге") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        book?.let { currentBook ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BookCover(
                    coverPath = currentBook.book.coverImagePath,
                    modifier = Modifier.size(150.dp, 220.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = currentBook.book.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentBook.authorName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentBook.book.bookUri,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                if (!currentBook.seriesName.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Серия: ${currentBook.seriesName}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (currentBook.book.readProgressPercent > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Прогресс чтения",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = currentBook.book.readProgressPercent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${(currentBook.book.readProgressPercent * 100).toInt()}% прочитано",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                   Spacer(modifier = Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),//.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onReadBook,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text( text = "Читать")
                    }
                    OutlinedButton(
                        onClick = {
                            if (currentBook.book.isRead) {
                                showRatingDialog = true
                            } else {
                                viewModel.markAsRead(bookId)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (currentBook.book.isRead) "Оценить книгу" else "Добавить в прочитанное",
                            maxLines = 2,
                            textAlign = TextAlign.Center
                        )
                    }
                    Button(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Text(
                            text="Удалить"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!currentBook.book.description.isNullOrEmpty()) {
                    Text(
                        text = "Описание",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start).padding(start = 32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                        ) {
                            Text(
                                text = currentBook.book.description,
                                textAlign = TextAlign.Justify,
                                style = TextStyle(
                                    textIndent = TextIndent(24.sp, 8.sp),
                                    fontFamily = FontFamily.Default,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    letterSpacing = 0.25.sp)
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Описание отсутствует",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if(ratings.isNotEmpty()){
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Оценки и отзывы",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start).padding(start = 32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ratings.forEach { rating ->
                            RatingItem(rating = rating)
                        }
                    }
                }
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Удаление книги") },
                    text = { Text("Вы действительно хотите удалить книгу?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteBook(bookId)
                                showDeleteDialog = false
                                onBackPressed()
                            }
                        ) {
                            Text("Удалить", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDeleteDialog = false }
                        ) {
                            Text("Отмена")
                        }
                    }
                )
            }

            if(showRatingDialog){
                RatingDialog(
                    onDismiss = { showRatingDialog = false },
                    onSave = { rating, review ->
                        coroutineScope.launch {
                            viewModel.addBookRating(bookId, rating, review)
                            showRatingDialog = false
                        }
                    }
                )
            }
        } ?: run {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                if (bookId != -1L) {
                    CircularProgressIndicator()
                } else {
                    Text(
                        text = "Книга не найдена",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}