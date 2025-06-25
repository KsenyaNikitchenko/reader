package com.eodev.reader.parser

import com.eodev.reader.data.model.BookMetadata
import com.eodev.reader.data.model.Fb2Cover
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import android.util.Base64

class Fb2BookParser : BookParser {

    override fun parse(inputStream: InputStream): BookMetadata? {
        return try {
            /*val parserFactory = XmlPullParserFactory.newInstance()
            parserFactory.isNamespaceAware = true
            val parser = parserFactory.newPullParser()
            parser.setInput(inputStream, "UTF-8")*/
            val parser = XmlPullParserFactory.newInstance().apply {
                isNamespaceAware = true
            }.newPullParser().apply {
                setInput(inputStream, "UTF-8")
            }

            var eventType = parser.eventType
            var title: String? = null
            val authors = LinkedHashSet<String>()
            var currentAuthorParts = mutableListOf<String>()
            var series: String? = null
            var description: String? = null
            var cover: Fb2Cover? = null

            /*while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "book-title" -> title = parser.nextText()
                            "first-name", "middle-name", "last-name" -> {
                                val name = parser.nextText()
                                if (author == null || author.isEmpty()) {
                                    author = name
                                } else {
                                    author += " $name"
                                }
                            }
                            "sequence" -> {
                                series = parser.getAttributeValue(null, "name")
                            }
                            "annotation" -> {
                                description = readTextContent(parser)
                            }
                            "binary" -> {
                                val id = parser.getAttributeValue(null, "id")
                                val contentType = parser.getAttributeValue(null, "content-type")
                                val data = parser.nextText()
                                if (id != null && contentType != null && data.isNotEmpty()) {
                                    try {
                                        cover = Fb2Cover(
                                            contentType,
                                            id,
                                            Base64.decode(data, Base64.DEFAULT)
                                        )
                                    } catch (e: Exception) {

                                    }
                                }
                            }
                        }
                    }
                }
                eventType = parser.next()
            }*/
            loop@ while (parser.next() != XmlPullParser.END_DOCUMENT) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> when (parser.name) {
                        "book-title" -> title = parser.nextText()
                        //"author" -> currentAuthorParts.clear()  // Новый автор - очищаем буфер
                        "first-name", "middle-name", "last-name" -> {
                            currentAuthorParts.add(parser.nextText().trim())
                        }
                        "sequence" -> series = parser.getAttributeValue(null, "name")
                        "annotation" -> description = readTextContent(parser)
                        "binary" -> {
                            val id = parser.getAttributeValue(null, "id")
                            val contentType = parser.getAttributeValue(null, "content-type")
                            val data = parser.nextText()
                            if (contentType?.startsWith("image/") == true) {
                                cover = Fb2Cover(contentType, id, Base64.decode(data, Base64.DEFAULT))
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> when (parser.name) {
                        "author" -> {
                            if (currentAuthorParts.isNotEmpty()) {
                                val fullName = currentAuthorParts.joinToString(" ")
                                authors.add(fullName)  // Set автоматически исключит дубликаты
                                currentAuthorParts.clear()
                            }
                        }
                    }
                }
            }
            BookMetadata(
                title = title ?: "Без названия",
                author = authors.joinToString(", ") ?: "Неизвестный автор",
                series = series,
                description = description,
                coverPath = cover?.href
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun getSupportedExtensions(): List<String> {
        return listOf("fb2")
    }

    private fun readTextContent(parser: XmlPullParser): String {
        val result = StringBuilder()
        var type = parser.next()
        while (type != XmlPullParser.END_TAG && type != XmlPullParser.END_DOCUMENT) {
            if (type == XmlPullParser.TEXT) {
                result.append(parser.text)
            }
            type = parser.next()
        }
        return result.toString()
    }
}