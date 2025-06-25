package com.eodev.reader.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.eodev.reader.data.model.Book
import com.eodev.reader.data.model.BookRating
import com.eodev.reader.data.model.MonthlyBook
import com.eodev.reader.data.model.RatingStatistic
import com.eodev.reader.data.model.YearlyStatistic
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface BookRatingDao {
    @Insert
    suspend fun insert(rating: BookRating)

    @Query("SELECT * FROM book_ratings WHERE bookId = :bookId ORDER BY date DESC")
    fun getRatingsForBook(bookId: Long): Flow<List<BookRating>>

    @Query("DELETE FROM book_ratings WHERE bookId = :bookId")
    suspend fun deleteRatingsForBook(bookId: Long)

    // Для месячной статистики
    @Query("""
        SELECT b.title, a.name as author, r.rating, r.date
        FROM book_ratings r
        JOIN books b ON r.bookId = b.id
        JOIN authors a ON b.authorId = a.id
        WHERE strftime('%Y', r.date/1000, 'unixepoch') = :year 
        AND strftime('%m', r.date/1000, 'unixepoch') = :month
        ORDER BY r.date DESC
    """)
    fun getMonthlyBooks(year: String, month: String): Flow<List<MonthlyBook>>

    // Для годовой статистики (по месяцам)
    @Query("""
        SELECT 
            CAST(strftime('%m', r.date/1000, 'unixepoch') AS INTEGER) as month,
            COUNT(*) as count
        FROM book_ratings r
        WHERE strftime('%Y', r.date/1000, 'unixepoch') = :year
        GROUP BY strftime('%m', r.date/1000, 'unixepoch')
    """)
    fun getYearlyStatistics(year: String): Flow<List<YearlyStatistic>>

    // Для статистики по рейтингам
    @Query("""
        SELECT 
            rating,
            COUNT(*) as count
        FROM book_ratings
        WHERE strftime('%Y', date/1000, 'unixepoch') = :year
        GROUP BY rating
    """)
    fun getRatingStatistics(year: String): Flow<List<RatingStatistic>>
}