package com.eodev.reader.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eodev.reader.data.model.AuthorWithCount
import com.eodev.reader.data.model.BookRating
import com.eodev.reader.data.model.BookWithDetails
import com.eodev.reader.data.model.MonthlyBook
import com.eodev.reader.data.model.RatingStatistic
import com.eodev.reader.data.model.ReadingState
import com.eodev.reader.data.model.SeriesWithCount
import com.eodev.reader.reader.EpubReader
import com.eodev.reader.reader.EpubContent
import com.eodev.reader.reader.EpubChapter
import com.eodev.reader.repository.BookRepository
import com.eodev.reader.data.model.YearlyStatistic
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class BookViewModel(private val repository: BookRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(BookUiState())
    val uiState: StateFlow<BookUiState> = _uiState.asStateFlow()

    private val _readingState = MutableStateFlow(ReadingState())
    val readingState: StateFlow<ReadingState> = _readingState.asStateFlow()

    private val _screenTitle = MutableStateFlow("Все книги")
    val screenTitle: StateFlow<String> = _screenTitle.asStateFlow()

    private val _currentBooks = MutableStateFlow<List<BookWithDetails>>(emptyList())
    val currentBooks: StateFlow<List<BookWithDetails>> = _currentBooks.asStateFlow()

    val authorsWithCount: StateFlow<List<AuthorWithCount>> = repository.getAuthorsWithCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val seriesWithCount: StateFlow<List<SeriesWithCount>> = repository.getSeriesWithCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadAllBooks()
    }
    //Все книги
    fun loadAllBooks() {
        viewModelScope.launch {
            repository.getAllBooks()
                .collect { books ->
                _currentBooks.value = books
                _screenTitle.value = "Все книги"
            }
        }
    }
    //Прочитанные книги
    fun loadReadBooks() {
        viewModelScope.launch {
            repository.getReadBooks().collect { books ->
                _currentBooks.value = books
                _screenTitle.value = "Прочитанное"
            }
        }
    }
    //Читаю сейчас
    fun loadCurrentlyReadingBooks() {
        viewModelScope.launch {
            repository.getCurrentlyReadingBooks().collect { books ->
                _currentBooks.value = books
                _screenTitle.value = "Читаю сейчас"
            }
        }
    }
    //Книги конкретного автора
    fun loadBooksByAuthor(author: String) {
        viewModelScope.launch {
            repository.getBooksByAuthor(author).collect { books ->
                _currentBooks.value = books
                _screenTitle.value = "Книги: $author"
            }
        }
    }
    //Книги из конкретной серии
    fun loadBooksBySeries(series: String) {
        viewModelScope.launch {
            repository.getBooksBySeries(series).collect { books ->
                _currentBooks.value = books
                _screenTitle.value = "Серия: $series"
            }
        }
    }
    //Книга по ID
    fun getBookById(bookId: Long): StateFlow<BookWithDetails?> {
        return repository.getBookById(bookId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }
    //Удаление книги
    fun deleteBook(bookId: Long){
        viewModelScope.launch {
                repository.deleteBook(bookId)
        }
    }
    //Загрузка книги для чтения
    fun loadBookForReading(context: Context, bookId: Long) {
        viewModelScope.launch {
            _readingState.value = ReadingState(isLoading = true)
            println("BVM start")
            try {
                val book = repository.getBookById(bookId).first()?: run{
                    _readingState.value= ReadingState(error("Книга не найдена"))
                    return@launch
                }
                println("Original URI: ${book.book.bookUri}")
                println("Parsed URI scheme: ${Uri.parse(book.book.bookUri).scheme}")
                println("Parsed URI authority: ${Uri.parse(book.book.bookUri).authority}")


                println("BVM book URI: ${book.book.bookUri}")
                val fileExtension = getFileExtension(context, book.book.bookUri)
                println("BVM extension:"+fileExtension)
                val testUri = "content://com.example.provider/books/test.epub"
                println("Test extension: ${getFileExtension(context, testUri)}")
                when (fileExtension) {
                    "epub" -> {
                        /*val content = withContext(Dispatchers.IO) {
                            EpubReader().readEpub(context, book.book.bookUri)
                        }

                        if (content != null && content.chapters.isNotEmpty()) {
                            val currentChapter = maxOf(0, minOf(book.book.lastReadPosition, content.totalChapters - 1))
                            _readingState.value = ReadingState(
                                content = content,
                                currentChapter = currentChapter
                            )
                        } else {
                            _readingState.value = ReadingState(error = "Не удалось загрузить содержимое EPUB книги")
                        }*/
                        try {
                            val uri = Uri.parse(book.book.bookUri)
                            val file = File(uri.path ?: "")
                            println("BVM bookuri: "+uri.toString())
                            if (!file.exists()) {
                                throw IOException("Файл не найден")
                            }
                            val content = withContext(Dispatchers.IO) {
                                //EpubReader().readEpub(context, uri)
                            }
                        } catch (e: SecurityException) {
                            _readingState.value = ReadingState(error = "Нет доступа к файлу")
                        } catch (e: IOException) {
                            _readingState.value = ReadingState(error = "Ошибка чтения файла")
                        }
                    }
                    "fb2", "zip" -> {
                        val simpleContent = EpubContent(
                            chapters = listOf(
                                EpubChapter(
                                    id = "fb2_content",
                                    title = "FB2 книга",
                                    href = "",
                                    content = "Это книга в формате FB2.\n\nПолная поддержка чтения FB2 книг будет добавлена в следующих версиях приложения.\n\n" +
                                            "Пока вы можете просматривать метаданные книги и отмечать её как прочитанную.\n\n"
                                )
                            ),
                            totalChapters = 1
                        )
                        _readingState.value = ReadingState(
                            content = simpleContent,
                            currentChapter = 0
                        )
                    }
                    else -> {
                        _readingState.value = ReadingState(error = "Формат '$fileExtension' пока не поддерживается для чтения")
                    }
                }
            } catch (e: Exception) {
                _readingState.value = ReadingState(error = "Ошибка при загрузке книги: ${e.message}")
            }
        }
    }
    //Загрузка следующей главы книги для чтения
    fun nextChapter(bookId: Long) {
        val currentState = _readingState.value
        val content = currentState.content ?: return

        if (currentState.currentChapter < content.totalChapters - 1) {
            val newChapter = currentState.currentChapter + 1
            _readingState.value = currentState.copy(currentChapter = newChapter)
            updateReadingProgressInternal(bookId, newChapter, content.getCurrentProgress(newChapter))
        }
    }
    //Загрузка предыдущей главы книги для чтения
    fun previousChapter(bookId: Long) {
        val currentState = _readingState.value
        val content = currentState.content ?: return

        if (currentState.currentChapter > 0) {
            val newChapter = currentState.currentChapter - 1
            _readingState.value = currentState.copy(currentChapter = newChapter)
            updateReadingProgressInternal(bookId, newChapter, content.getCurrentProgress(newChapter))
        }
    }
    private fun updateReadingProgressInternal(bookId: Long, chapter: Int, progress: Float) {
        viewModelScope.launch {
            try {
                repository.updateReadingProgress(bookId, chapter, progress)

                if (progress < 1f) {
                    repository.markAsCurrentlyReading(bookId)
                } else {
                    repository.markAsRead(bookId)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Ошибка при обновлении прогресса: ${e.message}"
                )
            }
        }
    }
    fun clearReadingState() {
        _readingState.value = ReadingState()
    }
    private fun getFileExtension(context: Context, uriString: String): String {
        /*return try {
            val fileName = uriString.substringAfterLast('/')  // Извлекаем имя файла из пути
            when (val dotIndex = fileName.lastIndexOf('.')) {
                -1 -> ""  // Нет точки в имени
                else -> fileName.substring(dotIndex + 1).lowercase()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }. also { ext ->
            println("File extension: $ext")
        }*/
        return try {
            val uri = Uri.parse(uriString)

            // 1. Попробуем получить расширение из MIME типа
            context.contentResolver.getType(uri)?.substringAfterLast('/')?.takeIf { it.isNotBlank() }?.let {
                return it
            }

            // 2. Для DocumentsProvider URI
            if (uri.authority == "com.android.providers.downloads.documents") {
                return handleDownloadsDocumentUri(context, uri)
            }

            // 3. Общий случай для других URI
            val fileName = when (uri.scheme) {
                //"content" -> getDisplayNameFromContentUri(context, uri)
                "file" -> uri.lastPathSegment ?: ""
                else -> uriString.substringAfterLast('/')
            }

            fileName.substringAfterLast('.', "").lowercase()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }.also { ext ->
            println("Final extension: '$ext'")
        }
    }
    private fun handleDownloadsDocumentUri(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.query(
                uri,
                arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayName = cursor.getString(0)
                    displayName.substringAfterLast('.', "").lowercase()
                } else ""
            } ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use { c ->
                val displayNameIndex = c.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns.DISPLAY_NAME)
                c.moveToFirst()
                c.getString(displayNameIndex)
            } ?: uri.lastPathSegment ?: "unknown"
        } catch (e: Exception) {
            uri.lastPathSegment ?: "unknown"
        }
    }
    fun addBooksFromUris(uris: List<Uri>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val addedBooks = repository.parseAndSaveBooks(uris)

                val count = repository.bookDao.getBooksCount()
                Log.d("ViewModel", "Total books in DB after insert: $count")
                _currentBooks.value = repository.getAllBooks().first()
                println("BVM currently size:  "+_currentBooks.value.size)

                when (_screenTitle.value) {
                    "Все книги" -> loadAllBooks()
                    "Прочитанное" -> loadReadBooks()
                    "Читаю сейчас" -> loadCurrentlyReadingBooks()
                    else -> loadAllBooks()
                }

                println("BVM how much add books: "+addedBooks.size.toString())
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Добавлено книг: ${addedBooks[0].book.bookUri}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Ошибка при добавлении книг: ${e.message}"
                )
            }
        }
    }
    fun updateReadingProgress(bookId: Long, position: Int, progress: Float) {
        viewModelScope.launch {
            try {
                repository.updateReadingProgress(bookId, position, progress)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Ошибка при обновлении прогресса: ${e.message}"
                )
            }
        }
    }
    fun markAsRead(bookId: Long) {
        viewModelScope.launch {
            try {
                repository.markAsRead(bookId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Ошибка при отметке как прочитанное: ${e.message}"
                )
            }
        }
    }
    fun markAsCurrentlyReading(bookId: Long) {
        viewModelScope.launch {
            try {
                repository.markAsCurrentlyReading(bookId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Ошибка при отметке текущего чтения: ${e.message}"
                )
            }
        }
    }
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    //Оценки книги
    fun getBookRatings(bookId: Long): Flow<List<BookRating>>{
        return repository.getBookRatings(bookId)
    }

    suspend fun addBookRating(bookId: Long, rating: Int, review: String) {
        return repository.addBookRating(bookId, rating, review)
    }

    //статистика прочитанного
    fun getMonthlyBooks(year: Int, month: Int): Flow<List<MonthlyBook>> {
        //println("Executing query for year=$year, month=${month.toString().padStart(2, '0')}")
        return repository.getMonthlyBooks(year, month)
    }

    fun getYearlyStatistics(year: Int): Flow<List<YearlyStatistic>> {
        return repository.getYearlyStatistics(year)
    }

    fun getRatingStatistics(year: Int): Flow<List<RatingStatistic>> {
        return repository.getRatingStatistics(year)
    }
}

data class BookUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class BookViewModelFactory(private val repository: BookRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
