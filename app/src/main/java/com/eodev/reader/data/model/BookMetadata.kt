package com.eodev.reader.data.model

import java.io.InputStream

data class BookMetadata(
    val title: String,
    val author: String,
    val series: String? = null,
    val description: String? = null,
    val coverPath: String? = null,
    val coverStream: (() -> InputStream)? = null
)
