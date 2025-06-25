package com.eodev.reader.data.dao

import androidx.room.*
import com.eodev.reader.data.model.Book
import com.eodev.reader.data.model.AuthorWithCount
import com.eodev.reader.data.model.BookWithDetails
import com.eodev.reader.data.model.SeriesWithCount
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Insert
    suspend fun insert(book: Book): Long

    @Update
    suspend fun update(book: Book)

    @Delete
    suspend fun delete(book: Book)

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBook(bookId: Long)

    @Transaction
    @Query("SELECT * FROM  books")
    fun getAllBooksSimple(): Flow<List<Book>>

    @Transaction
    @Query("""
        SELECT books.*, authors.name as authorName, series.name as seriesName
        FROM books 
        JOIN authors ON books.authorId = authors.id
        LEFT JOIN series ON books.seriesId = series.id
        ORDER BY addedDate DESC
    """)
    fun getAllBooks(): Flow<List<BookWithDetails>>

    @Query("""
        SELECT books.*, authors.name as authorName, series.name as seriesName
        FROM books 
        JOIN authors ON books.authorId = authors.id
        LEFT JOIN series ON books.seriesId = series.id
        ORDER BY addedDate DESC
    """)
    suspend fun getAllBooksSync(): List<BookWithDetails>

    @Query("""
        SELECT books.*, authors.name as authorName, series.name as seriesName
        FROM books 
        JOIN authors ON books.authorId = authors.id
        LEFT JOIN series ON books.seriesId = series.id
        WHERE books.id= :bookId
    """)
    suspend fun getBookById(bookId: Long): BookWithDetails?

    @Query("""
        SELECT books.*, authors.name as authorName, series.name as seriesName
        FROM books 
        JOIN authors ON books.authorId = authors.id
        LEFT JOIN series ON books.seriesId = series.id
        WHERE books.id= :bookId
    """)
    fun getBookByIdFlow(bookId: Long): Flow<BookWithDetails?>

    @Query("""
        SELECT books.*, authors.name as authorName, series.name as seriesName
        FROM books 
        JOIN authors ON books.authorId = authors.id
        LEFT JOIN series ON books.seriesId = series.id
        WHERE isRead = 1 ORDER BY addedDate DESC
    """)
    fun getReadBooks(): Flow<List<BookWithDetails>>

    @Query("""
        SELECT books.*, authors.name as authorName, series.name as seriesName
        FROM books 
        JOIN authors ON books.authorId = authors.id
        LEFT JOIN series ON books.seriesId = series.id
        WHERE isCurrentlyReading = 1 ORDER BY addedDate DESC
    """)
    fun getCurrentlyReadingBooks(): Flow<List<BookWithDetails>>

    @Query("""
        SELECT books.*, authors.name as authorName, series.name as seriesName
        FROM books 
        JOIN authors ON books.authorId = authors.id
        LEFT JOIN series ON books.seriesId = series.id
        WHERE authors.name = :author ORDER BY addedDate DESC
    """)
    fun getBooksByAuthor(author: String): Flow<List<BookWithDetails>>

    @Query("""
        SELECT books.*, authors.name as authorName, series.name as seriesName
        FROM books 
        JOIN authors ON books.authorId = authors.id
        LEFT JOIN series ON books.seriesId = series.id
        WHERE series.name = :series ORDER BY addedDate DESC
    """)
    fun getBooksBySeries(series: String): Flow<List<BookWithDetails>>

    @Query("""
        SELECT authors.id as authorId, authors.name as author, COUNT(*) AS count
        FROM books JOIN authors ON books.authorId = authors.id
        GROUP BY authors.id, authors.name ORDER BY count DESC
    """)
    fun getAuthorsWithCount(): Flow<List<AuthorWithCount>>

    @Query("""
        SELECT series.id as seriesId, series.name as series, COUNT(*) AS count
        FROM books LEFT JOIN series ON books.seriesId = series.id
        WHERE seriesId IS NOT NULL
        GROUP BY series.id, series.name ORDER BY count DESC
    """)
    fun getSeriesWithCount(): Flow<List<SeriesWithCount>>

    @Query("UPDATE books SET isRead = :isRead WHERE id = :bookId")
    suspend fun updateReadStatus(bookId: Long, isRead: Boolean)

    @Query("UPDATE books SET isCurrentlyReading = :isCurrentlyReading WHERE id = :bookId")
    suspend fun updateCurrentlyReadingStatus(bookId: Long, isCurrentlyReading: Boolean)

    @Query("UPDATE books SET lastReadPosition = :position, readProgressPercent = :progress WHERE id = :bookId")
    suspend fun updateReadingProgress(bookId: Long, position: Int, progress: Float)

    @Query("SELECT COUNT(*) FROM books")
    suspend fun getBooksCount(): Int
}