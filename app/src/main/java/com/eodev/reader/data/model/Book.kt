package com.eodev.reader.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "books",
    foreignKeys = [
        ForeignKey(
            entity = Author::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Series::class,
            parentColumns = ["id"],
            childColumns = ["seriesId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val authorId: Long,
    val seriesId: Long? = null,
    val description: String? = null,
    val coverImagePath: String? = null,
    val bookUri: String,
    val lastReadPosition: Int = 0,
    val readProgressPercent: Float = 0f,
    val addedDate: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isCurrentlyReading: Boolean = false
)