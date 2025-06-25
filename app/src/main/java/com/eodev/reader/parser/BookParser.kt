package com.eodev.reader.parser

import com.eodev.reader.data.model.BookMetadata
import java.io.InputStream

interface BookParser {
    fun parse(inputStream: InputStream): BookMetadata?
    fun getSupportedExtensions(): List<String>
}