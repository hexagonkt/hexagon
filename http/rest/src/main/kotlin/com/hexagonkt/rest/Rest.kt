package com.hexagonkt.rest

import com.hexagonkt.core.media.ANY_MEDIA
import com.hexagonkt.core.media.MediaType
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.HttpBase
import com.hexagonkt.serialization.*

val anyContentType = ContentType(ANY_MEDIA)
val emptyBodies = setOf("", ByteArray(0))

fun HttpBase.bodyList(): List<*> =
    bodyString().parseList(mediaType())

fun HttpBase.bodyMap(): Map<String, *> =
    bodyString().parseMap(mediaType())

fun HttpBase.bodyMaps(): List<Map<String, *>> =
    bodyString().parseMaps(mediaType())

fun <T> HttpBase.bodyObjects(converter: (Map<String, *>) -> T): List<T> =
    bodyMaps().map(converter)

fun <T> HttpBase.bodyObject(converter: (Map<String, *>) -> T): T =
    bodyMap().let(converter)

fun HttpBase.mediaType(): MediaType =
    contentType?.mediaType ?: error("Missing content type")
