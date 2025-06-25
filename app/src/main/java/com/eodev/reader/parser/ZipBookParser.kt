package com.eodev.reader.parser

import com.eodev.reader.data.model.BookMetadata
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ZipBookParser : BookParser {

    private val fb2Parser = Fb2BookParser()

    override fun parse(inputStream: InputStream): BookMetadata? {
        return try {
            val zis = ZipInputStream(inputStream)
            var entry: ZipEntry? = zis.nextEntry

            while (entry != null) {
                val fileName = entry.name
                when {
                    fileName.endsWith(".fb2", ignoreCase = true) -> {
                        return fb2Parser.parse(zis)
                    }
                }
                entry = zis.nextEntry
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun getSupportedExtensions(): List<String> {
        return listOf("zip")
    }
}