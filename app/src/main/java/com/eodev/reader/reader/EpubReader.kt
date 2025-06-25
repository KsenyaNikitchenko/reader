package com.eodev.reader.reader

import android.content.Context
import android.net.Uri
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class EpubChapter(
    val id: String,
    val title: String,
    val href: String,
    val content: String
)

data class EpubContent(
    val chapters: List<EpubChapter>,
    val totalChapters: Int
) {
    fun getChapter(position: Int): EpubChapter? {
        return chapters.getOrNull(position)
    }

    fun getCurrentProgress(chapterIndex: Int): Float {
        return if (totalChapters > 0) {
            (chapterIndex + 1).toFloat() / totalChapters.toFloat()
        } else 0f
    }
}

class EpubReader {

    fun readEpub(context: Context, bookUri: String): EpubContent? {
        return try {
            val uri = Uri.parse(bookUri)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val zipInputStream = ZipInputStream(inputStream)

                val opfPath = findOpfPath(zipInputStream) ?: return null

                context.contentResolver.openInputStream(uri)?.use { inputStream2 ->
                    val zipInputStream2 = ZipInputStream(inputStream2)

                    val spineItems = parseSpine(zipInputStream2, opfPath)

                    context.contentResolver.openInputStream(uri)?.use { inputStream3 ->
                        val zipInputStream3 = ZipInputStream(inputStream3)

                        readChaptersContent(zipInputStream3, spineItems, opfPath)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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
                    return parser.getAttributeValue(null, "full-path")
                }
                eventType = parser.next()
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun parseSpine(zipInputStream: ZipInputStream, opfPath: String): List<SpineItem> {
        var entry: ZipEntry? = zipInputStream.nextEntry

        while (entry != null) {
            if (entry.name == opfPath) {
                val opfContent = zipInputStream.readBytes().toString(Charsets.UTF_8)
                return parseOpfSpine(opfContent)
            }
            entry = zipInputStream.nextEntry
        }

        return emptyList()
    }

    private data class SpineItem(val idref: String, val href: String, val title: String)

    private fun parseOpfSpine(opfContent: String): List<SpineItem> {
        return try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(ByteArrayInputStream(opfContent.toByteArray()), "UTF-8")

            val manifest = mutableMapOf<String, String>() // id -> href
            val spine = mutableListOf<String>() // list of idrefs

            var eventType = parser.eventType
            var inSpine = false
            var inManifest = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "manifest" -> inManifest = true
                            "spine" -> inSpine = true
                            "item" -> {
                                if (inManifest) {
                                    val id = parser.getAttributeValue(null, "id")
                                    val href = parser.getAttributeValue(null, "href")
                                    if (id != null && href != null) {
                                        manifest[id] = href
                                    }
                                }
                            }
                            "itemref" -> {
                                if (inSpine) {
                                    val idref = parser.getAttributeValue(null, "idref")
                                    if (idref != null) {
                                        spine.add(idref)
                                    }
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (parser.name) {
                            "manifest" -> inManifest = false
                            "spine" -> inSpine = false
                        }
                    }
                }
                eventType = parser.next()
            }

            spine.mapIndexed { index, idref ->
                val href = manifest[idref] ?: "unknown"
                val title = "Глава ${index + 1}"
                SpineItem(idref, href, title)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun readChaptersContent(
        zipInputStream: ZipInputStream,
        spineItems: List<SpineItem>,
        opfPath: String
    ): EpubContent {
        val chapters = mutableListOf<EpubChapter>()
        val contentMap = mutableMapOf<String, String>()

        val baseDir = opfPath.substringBeforeLast('/')

        var entry: ZipEntry? = zipInputStream.nextEntry
        while (entry != null) {
            if (!entry.isDirectory && (entry.name.endsWith(".html") || entry.name.endsWith(".xhtml"))) {
                val content = zipInputStream.readBytes().toString(Charsets.UTF_8)
                contentMap[entry.name] = content
            }
            entry = zipInputStream.nextEntry
        }

        spineItems.forEachIndexed { index, spineItem ->
            val fullPath = if (baseDir.isNotEmpty()) "$baseDir/${spineItem.href}" else spineItem.href
            val content = contentMap[fullPath] ?: contentMap[spineItem.href] ?: ""

            if (content.isNotEmpty()) {
                val cleanContent = cleanHtmlContent(content)
                chapters.add(
                    EpubChapter(
                        id = spineItem.idref,
                        title = spineItem.title,
                        href = spineItem.href,
                        content = cleanContent
                    )
                )
            }
        }

        return EpubContent(chapters, chapters.size)
    }

    private fun cleanHtmlContent(htmlContent: String): String {
        return try {
            htmlContent
                .replace("<head>.*?</head>".toRegex(RegexOption.DOT_MATCHES_ALL), "")
                .replace("<?xml.*?>".toRegex(), "")
                .replace("<!DOCTYPE.*?>".toRegex(), "")
                .replace("<\\?.*?\\?>".toRegex(), "")
                .trim()
        } catch (e: Exception) {
            htmlContent
        }
    }
}