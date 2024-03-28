package com.hexagonkt.http.mutable.model

interface HttpMessage : HttpBase {
    var cookies: List<Cookie>           // hash of browser cookies

    fun cookiesMap(): Map<String, Cookie> =
        cookies.associateBy { it.name }
}