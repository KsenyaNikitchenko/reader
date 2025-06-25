package com.eodev.reader

import android.app.Application
import com.eodev.reader.data.database.AppDatabase
import com.eodev.reader.repository.BookRepository

class ReaderApplication : Application() {

    private val database by lazy { AppDatabase.getInstance(this) }

    val repository by lazy {
        BookRepository(
            bookDao = database.bookDao(),
            authorDao = database.authorDao(),
            seriesDao = database.seriesDao(),
            bookRatingDao = database.bookRatingDao(),
            this
        )
    }
}