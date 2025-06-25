package com.eodev.reader.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "series",
    foreignKeys = [ForeignKey(
        entity = Author::class,
        parentColumns = ["id"],
        childColumns = ["authorId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Series(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "authorId", index = true)
    val authorId: Long,
    val name: String
)