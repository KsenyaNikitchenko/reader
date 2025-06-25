package com.eodev.reader.parser

import android.util.Log
import com.eodev.reader.data.model.BookMetadata
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class EpubBookParser : BookParser {

    /*override fun parse(inputStream: InputStream): BookMetadata? {
        return try {
            // Создаем копию потока для повторного использования
            val bytes = inputStream.readBytes()
            val zipInputStream = ZipInputStream(bytes.inputStream())

            val opfPath = findOpfPath(zipInputStream) ?: run {
                Log.e("EpubParser", "OPF path not found")
                return tryParseDirectly(bytes.inputStream())
            }

            parseOpfFile(bytes.inputStream(), opfPath)
        } catch (e: Exception) {
            Log.e("EpubParser", "Error parsing EPUB", e)
            tryParseDirectly(inputStream)
        }
    }
    override fun getSupportedExtensions(): List<String> {
        return listOf("epub")
    }

    private fun parseContainerXml(containerXml: String): String? {
        return try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(ByteArrayInputStream(containerXml.toByteArray(Charsets.UTF_8)), "UTF-8")

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "rootfile") {
                    val fullPath = parser.getAttributeValue(null, "full-path")
                    if (fullPath != null) {
                        return fullPath
                    }
                }
                eventType = parser.next()
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun findOpfPath(zipInputStream: ZipInputStream): String? {
        return generateSequence { zipInputStream.nextEntry }
            .firstOrNull { it.name.equals("META-INF/container.xml", ignoreCase = true) }
            ?.let { entry ->
                try {
                    val containerXml = zipInputStream.readBytes().toString(Charsets.UTF_8)
                    parseContainerXml(containerXml)
                } catch (e: Exception) {
                    Log.e("EpubParser", "Error reading container.xml", e)
                    null
                }
            }
    }

    private fun parseOpfFile(inputStream: InputStream, opfPath: String) : BookMetadata?{
        val zipInputStream = ZipInputStream(inputStream)
        return try{
            generateSequence { zipInputStream.nextEntry }
                .firstOrNull { it.name.equals(opfPath, ignoreCase = true) }
                ?.let { entry ->
                    try {
                        val opfContent = zipInputStream.readBytes().toString(Charsets.UTF_8)
                        return parseOpfContent(opfContent, opfPath)
                    } catch (e: Exception) {
                        Log.e("EpubParser", "Error reading OPF file", e)
                        null
                    }
                }
        } catch (e: Exception) {
            Log.e("EpubParser", "Error reading OPF file", e)
            null
        }


    }

    private fun parseOpfContent(opfContent: String, opfPath: String): BookMetadata? {
        return try {
            val factory = XmlPullParserFactory.newInstance().apply {
                isNamespaceAware = true
            }
            val parser = factory.newPullParser().apply {
                setInput(ByteArrayInputStream(opfContent.toByteArray(Charsets.UTF_8)), "UTF-8")
            }

            var title: String? = null
            var author: String? = null
            var description: String? = null
            var coverHref: String? = null

            loop@ while (true) {
                when (parser.next()) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "title" -> title = parser.nextText().trim()
                            "creator" -> {
                                val role = parser.getAttributeValue(null, "role")
                                    ?: parser.getAttributeValue("http://www.idpf.org/2007/opf", "role")
                                if (role == null || role == "aut") {
                                    author = parser.nextText().trim()
                                }
                            }
                            "description" -> description = parser.nextText().trim()
                            "meta" -> {
                                if (parser.getAttributeValue(null, "name") == "cover") {
                                    coverHref = parser.getAttributeValue(null, "content")
                                }
                            }
                            "item" -> {
                                val id = parser.getAttributeValue(null, "id")
                                val href = parser.getAttributeValue(null, "href")
                                if (id?.contains("cover", ignoreCase = true) == true ||
                                    href?.contains("cover", ignoreCase = true) == true) {
                                    coverHref = href
                                }
                            }
                        }
                    }
                    XmlPullParser.END_DOCUMENT -> break@loop
                }
            }

            if (title.isNullOrEmpty() || author.isNullOrEmpty()) {
                Log.e("EpubParser", "Required fields (title/author) are missing")
                return null
            }

            val coverPath = coverHref?.let { href ->
                val opfDir = opfPath.substringBeforeLast('/')
                if (opfDir.isNotEmpty()) "$opfDir/$href" else href
            }

            BookMetadata(
                title = title!!,
                author = author!!,
                series = null,
                description = description,
                coverPath = coverPath,
                coverStream = null
            )
        } catch (e: Exception) {
            Log.e("EpubParser", "Error parsing OPF content", e)
            null
        }
    }

    private fun tryParseDirectly(inputStream: InputStream): BookMetadata? {
        return try {
            inputStream.reset()
            val zipInputStream = ZipInputStream(inputStream)
            var entry: ZipEntry? = zipInputStream.nextEntry

            while (entry != null) {
                val fileName = entry.name
                println(" EBP fileName: "+ fileName)
                if (fileName.endsWith(".opf", ignoreCase = true)) {
                    val opfContent = zipInputStream.readBytes().toString(Charsets.UTF_8)
                    return parseOpfContent(opfContent,  fileName)
                }
                entry = zipInputStream.nextEntry
            }

            BookMetadata(
                title = "EPUB книга",
                author = "Неизвестный автор",
                series = null,
                description = "EPUB книга без метаданных",
                coverPath = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }*/

    override fun parse(inputStream: InputStream): BookMetadata? {
        return try {
            val zipInputStream = ZipInputStream(inputStream)

            val opfPath = findOpfPath(zipInputStream) ?: return null

            inputStream.reset()
            val zipInputStream2 = ZipInputStream(inputStream)

            parseOpfFile(zipInputStream2, opfPath)
        } catch (e: Exception) {
            e.printStackTrace()
            tryParseDirectly(inputStream)
        }
    }

    override fun getSupportedExtensions(): List<String> {
        return listOf("epub")
    }

    private fun findOpfPath(zipInputStream: ZipInputStream): String? {
        var entry: ZipEntry? = zipInputStream.nextEntry

        while (entry != null) {
            if (entry.name == "META-INF/container.xml") {
                val containerXml = zipInputStream.readBytes().toString(Charsets.UTF_8)
                return parseContainerXml(containerXml)
            }
            entry = zipInputStream.nextEntry
        }

        return null
    }

    private fun parseContainerXml(containerXml: String): String? {
        return try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(ByteArrayInputStream(containerXml.toByteArray()), "UTF-8")

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "rootfile") {
                    val fullPath = parser.getAttributeValue(null, "full-path")
                    if (fullPath != null) {
                        return fullPath
                    }
                }
                eventType = parser.next()
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseOpfFile(zipInputStream: ZipInputStream, opfPath: String): BookMetadata? {
        var entry: ZipEntry? = zipInputStream.nextEntry

        while (entry != null) {
            if (entry.name == opfPath) {
                val opfContent = zipInputStream.readBytes().toString(Charsets.UTF_8)
                return parseOpfContent(opfContent, zipInputStream, opfPath)
            }
            entry = zipInputStream.nextEntry
        }

        return null
    }

    private fun parseOpfContent(opfContent: String, zipInputStream: ZipInputStream, opfPath: String): BookMetadata? {
        return try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(ByteArrayInputStream(opfContent.toByteArray()), "UTF-8")

            var title: String? = null
            var author: String? = null
            var description: String? = null
            var coverHref: String? = null

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "title" -> {
                                title = parser.nextText()
                            }
                            "creator" -> {
                                val role = parser.getAttributeValue(null, "role")
                                    ?: parser.getAttributeValue("http://www.idpf.org/2007/opf", "role")
                                if (role == "aut" || role == null) {
                                    author = parser.nextText()
                                }
                            }
                            "description" -> {
                                description = parser.nextText()
                            }
                            "meta" -> {
                                val name = parser.getAttributeValue(null, "name")
                                val content = parser.getAttributeValue(null, "content")
                                if (name == "cover" && content != null) {
                                    coverHref = content
                                }
                            }
                            "item" -> {
                                val id = parser.getAttributeValue(null, "id")
                                val href = parser.getAttributeValue(null, "href")
                                val mediaType = parser.getAttributeValue(null, "media-type")

                                if ((id == "cover" || id == "cover-image" ||
                                            href?.contains("cover", ignoreCase = true) == true ||
                                            id?.contains("cover", ignoreCase = true) == true) &&
                                    mediaType?.startsWith("image/") == true) {
                                    coverHref = href
                                }
                            }
                        }
                    }
                }
                eventType = parser.next()
            }

            val coverPath = if (coverHref != null) {
                val opfDir = opfPath.substringBeforeLast('/')
                if (opfDir.isNotEmpty()) "$opfDir/$coverHref" else coverHref
            } else null

            BookMetadata(
                title = title ?: "Без названия",
                author = author ?: "Неизвестный автор",
                series = null,
                description = description,
                coverPath = coverPath
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun tryParseDirectly(inputStream: InputStream): BookMetadata? {
        return try {
            inputStream.reset()
            val zipInputStream = ZipInputStream(inputStream)
            var entry: ZipEntry? = zipInputStream.nextEntry

            while (entry != null) {
                val fileName = entry.name
                if (fileName.endsWith(".opf", ignoreCase = true)) {
                    val opfContent = zipInputStream.readBytes().toString(Charsets.UTF_8)
                    return parseOpfContent(opfContent, zipInputStream, fileName)
                }
                entry = zipInputStream.nextEntry
            }

            BookMetadata(
                title = "EPUB книга",
                author = "Неизвестный автор",
                series = null,
                description = "EPUB книга без метаданных",
                coverPath = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}