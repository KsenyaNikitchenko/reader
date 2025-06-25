package com.eodev.reader.parser

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.eodev.reader.data.model.BookMetadata
import java.io.InputStream

object BookParserManager {

    private val parsers: Map<String, BookParser> = mapOf(
        "fb2" to Fb2BookParser(),
        "zip" to ZipBookParser(),
        "epub" to EpubBookParser()
    )

    fun parseBook(context: Context, uri: Uri): BookMetadata? {
        /*val extension = getFileExtension(context, uri)
        val parser = parsers[extension] ?: return null

        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                if (extension == "epub") {
                    val bytes = stream.readBytes()
                    parser.parse(bytes.inputStream())
                } else {
                    parser.parse(stream)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }*/
        return try {
            val extension = getFileExtension(context, uri).takeIf { it.isNotEmpty() }
                ?: run {
                    Log.e("ParserManager", "Unknown file extension for $uri")
                    return null
                }

            val parser = parsers[extension] ?: run {
                Log.e("ParserManager", "No parser for extension: $extension")
                return null
            }

            context.contentResolver.openInputStream(uri)?.use { stream ->
                // Для всех форматов используем копию потока
                val bytes = stream.readBytes()
                parser.parse(bytes.inputStream()).also { metadata ->
                    if (metadata == null) {
                        Log.e("ParserManager", "Parser returned null for $uri")
                    } else {
                        Log.d("ParserManager", "Successfully parsed: ${metadata.title}")
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("ParserManager", "Security exception for $uri", e)
            null
        } catch (e: Exception) {
            Log.e("ParserManager", "Error parsing $uri", e)
            null
        }
    }

    fun getSupportedExtensions(): List<String> {
        return parsers.keys.toList()
    }

    private fun getFileExtension(context: Context, uri: Uri): String {
        val fileName = getFileNameFromUri(context, uri)
        val lastDotIndex = fileName.lastIndexOf('.')
        return if (lastDotIndex >= 0) {
            fileName.substring(lastDotIndex + 1).lowercase()
        } else {
            ""
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use { c ->
                val displayNameIndex = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                c.moveToFirst()
                c.getString(displayNameIndex)
            } ?: uri.lastPathSegment ?: "unknown"
        } catch (e: Exception) {
            uri.lastPathSegment ?: "unknown"
        }
    }
}