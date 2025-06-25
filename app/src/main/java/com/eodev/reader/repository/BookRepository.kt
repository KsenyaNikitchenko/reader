package com.eodev.reader.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.eodev.reader.data.dao.AuthorDao
import com.eodev.reader.data.dao.BookDao
import com.eodev.reader.data.dao.BookRatingDao
import com.eodev.reader.data.dao.SeriesDao
import com.eodev.reader.data.model.Author
import com.eodev.reader.data.model.Book
import com.eodev.reader.data.model.AuthorWithCount
import com.eodev.reader.data.model.BookRating
import com.eodev.reader.data.model.BookWithDetails
import com.eodev.reader.data.model.MonthlyBook
import com.eodev.reader.data.model.RatingStatistic
import com.eodev.reader.data.model.Series
import com.eodev.reader.data.model.SeriesWithCount
import com.eodev.reader.data.model.YearlyStatistic
import com.eodev.reader.parser.BookParserManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class BookRepository(
    val bookDao: BookDao,
    private val authorDao: AuthorDao,
    private val seriesDao: SeriesDao,
    private val bookRatingDao: BookRatingDao,
    private val context: Context
) {

    fun getAllBooks(): Flow<List<BookWithDetails>> = bookDao.getAllBooks()

    fun getReadBooks(): Flow<List<BookWithDetails>> = bookDao.getReadBooks()

    fun getCurrentlyReadingBooks(): Flow<List<BookWithDetails>> = bookDao.getCurrentlyReadingBooks()

    fun getBooksByAuthor(author: String): Flow<List<BookWithDetails>> {
        return bookDao.getBooksByAuthor(author)
    }

    fun getBooksBySeries(series: String): Flow<List<BookWithDetails>> = bookDao.getBooksBySeries(series)

    fun getBookById(bookId: Long): Flow<BookWithDetails?> = bookDao.getBookByIdFlow(bookId)

    fun getAuthorsWithCount(): Flow<List<AuthorWithCount>> = bookDao.getAuthorsWithCount()

    fun getSeriesWithCount(): Flow<List<SeriesWithCount>> = bookDao.getSeriesWithCount()

    suspend fun insertBook(book: Book): Long {
        return withContext(Dispatchers.IO) {
            bookDao.insert(book)
        }
    }

    suspend fun updateBook(book: Book) {
        withContext(Dispatchers.IO) {
            bookDao.update(book)
        }
    }

    suspend fun deleteBook(bookId: Long) {
        withContext(Dispatchers.IO) {
            bookRatingDao.deleteRatingsForBook(bookId)
            bookDao.deleteBook(bookId)
        }
    }

    suspend fun updateReadingProgress(bookId: Long, position: Int, progress: Float) {
        withContext(Dispatchers.IO) {
            bookDao.updateReadingProgress(bookId, position, progress)
        }
    }
    suspend fun getCount(): Int{
        return bookDao.getBooksCount()
    }
    suspend fun markAsRead(bookId: Long) {
        withContext(Dispatchers.IO) {
            bookDao.updateReadStatus(bookId, true)
            bookDao.updateCurrentlyReadingStatus(bookId, false)
        }
    }

    suspend fun markAsCurrentlyReading(bookId: Long) {
        withContext(Dispatchers.IO) {
            bookDao.updateCurrentlyReadingStatus(bookId, true)
            bookDao.updateReadStatus(bookId, false)
        }
    }

    suspend fun parseAndSaveBooks(uris: List<Uri>): List<BookWithDetails>{
        return withContext(Dispatchers.IO) {
            val savedBooks = mutableListOf<BookWithDetails>()
            for (uri in uris) {
                try {
                    val metadata = BookParserManager.parseBook(context, uri)
                    if(metadata!=null){
                        //автор книги
                        var author = authorDao.getAuthorByName(metadata.author)
                        if (author == null) {
                            val authorId = authorDao.insert(Author(name = metadata.author))
                            author = Author(authorId, metadata.author)
                        }
                        //серия, если она есть
                        var seriesId: Long? = null
                        var seriesName: String? = null
                        if (!metadata.series.isNullOrEmpty()) {
                            var series = seriesDao.getSeriesByNameAndAuthor(metadata.series, author.id)
                            if (series == null) {
                                val newSeriesId = seriesDao.insert(
                                    Series(name = metadata.series, authorId = author.id)
                                )
                                series = Series(newSeriesId,  author.id, metadata.series,)
                            }
                            seriesId = series.id
                            seriesName = series.name
                        }
                        val book = Book(
                            title = metadata.title,
                            authorId = author.id,
                            seriesId = seriesId,
                            description = metadata.description,
                            coverImagePath = metadata.coverPath,
                            bookUri = uri.toString()
                        )
                        bookDao.insert(book)
                        savedBooks.add(
                            BookWithDetails(
                                book= book,
                                authorName = author.name,
                                seriesName = seriesName
                            )
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            savedBooks
        }
    }

    fun getBookRatings(bookId: Long) : Flow<List<BookRating>>{
        return bookRatingDao.getRatingsForBook(bookId)
    }

    suspend fun addBookRating(bookId: Long, rating: Int, review: String){
        val timestamp=System.currentTimeMillis()
        bookRatingDao.insert(
            BookRating(
                bookId =bookId,
                date = timestamp,
                rating = rating,
                review = review
            )
        )
    }

    fun getMonthlyBooks(year: Int, month: Int): Flow<List<MonthlyBook>> {
        val monthStr = month.toString().padStart(2, '0')
        return bookRatingDao.getMonthlyBooks(year.toString(), monthStr)
    }

    fun getYearlyStatistics(year: Int): Flow<List<YearlyStatistic>> {
        return bookRatingDao.getYearlyStatistics(year.toString())
    }

    fun getRatingStatistics(year: Int): Flow<List<RatingStatistic>> {
        return bookRatingDao.getRatingStatistics(year.toString())
    }
}

