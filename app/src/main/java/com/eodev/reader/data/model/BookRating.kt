package com.eodev.reader.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

@Entity (tableName ="book_ratings")
@TypeConverters(Converters::class)
data class BookRating(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "bookId") val bookId: Long,
    val date: Long,
    val rating: Int, // от 1 до 5
    val review: String
)
