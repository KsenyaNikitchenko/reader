package com.eodev.reader.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.eodev.reader.data.dao.AuthorDao
import com.eodev.reader.data.dao.BookDao
import com.eodev.reader.data.dao.BookRatingDao
import com.eodev.reader.data.dao.SeriesDao
import com.eodev.reader.data.model.Author
import com.eodev.reader.data.model.Book
import com.eodev.reader.data.model.BookRating
import com.eodev.reader.data.model.Converters
import com.eodev.reader.data.model.Series

@Database(
    entities = [Book::class, BookRating::class, Author::class, Series::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun authorDao(): AuthorDao
    abstract fun seriesDao(): SeriesDao
    abstract fun bookRatingDao(): BookRatingDao

    companion object {
        private var INSTANCE: AppDatabase? = null
        private val lock = Any()

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: kotlin.synchronized(lock) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reader_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}