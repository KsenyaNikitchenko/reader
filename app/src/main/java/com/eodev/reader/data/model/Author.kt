package com.eodev.reader.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "authors")
data class Author(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)