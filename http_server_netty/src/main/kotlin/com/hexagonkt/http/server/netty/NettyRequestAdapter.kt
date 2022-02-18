package com.hexagonkt.http.server.netty

import com.hexagonkt.core.MultiMap
import com.hexagonkt.http.model.*
import com.hexagonkt.http.parseContentType
import com.hexagonkt.http.server.model.HttpServerRequestPort
import io.netty.buffer.ByteBufUtil
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames.*
import io.netty.handler.codec.http.QueryStringDecoder
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.codec.http.cookie.ServerCookieDecoder
import io.netty.handler.codec.http.multipart.*
import java.net.InetSocketAddress
import java.net.URI
import java.security.cert.X509Certificate
import io.netty.handler.codec.http.HttpMethod as NettyHttpMethod

class NettyRequestAdapter(
    methodName: NettyHttpMethod,
    req: FullHttpRequest,
    override val certificateChain: List<X509Certificate>,
    address: InetSocketAddress,
) : HttpServerRequestPort {

    override val accept: List<ContentType> by lazy {
        req.headers().getAll(ACCEPT).map { parseContentType(it) }
    }

    override val contentLength: Long by lazy {
        req.headers()[CONTENT_LENGTH]?.toLong() ?: 0L
    }

    override val queryParameters: MultiMap<String, String> by lazy {
        val queryStringDecoder = QueryStringDecoder(req.uri())
        MultiMap(queryStringDecoder.parameters())
    }

    override val parts: List<HttpPartPort> by lazy {
        HttpPostRequestDecoder(req).bodyHttpDatas.map {
            when (it) {
                is FileUpload -> HttpPart(
                    name = it.name,
                    body = ByteBufUtil.getBytes(it.content()),
                    submittedFileName = it.filename,
                    contentType = it.contentType?.let { ct -> parseContentType(ct) },
                )
                is Attribute -> HttpPart(it.name, it.value)
                else -> error("Unknown part type: ${it.javaClass}")
            }
        }
    }

    override val formParameters: MultiMap<String, String> by lazy {
        MultiMap(parts.filter { it.submittedFileName == null }.map { it.name to it.bodyString() })
    }

    override val method: HttpMethod by lazy {
        HttpMethod.valueOf(methodName.name())
    }

    override val protocol: HttpProtocol by lazy {
        HttpProtocol.valueOf(req.protocolVersion().protocolName())
    }

    override val host: String by lazy {
        address.hostName
    }

    override val port: Int by lazy {
        address.port
    }

    override val path: String by lazy { URI(req.uri()).path }

    override val cookies: List<HttpCookie> by lazy {
        val cookieHeader: String = req.headers().get(COOKIE)
            ?: return@lazy emptyList<HttpCookie>()

        val cookies: Set<Cookie> = ServerCookieDecoder.STRICT.decode(cookieHeader)

        cookies.map {
            HttpCookie(
                name = it.name(),
                value = it.value(),
                maxAge = if (it.maxAge() == Long.MIN_VALUE) -1 else it.maxAge(),
                secure = it.isSecure,
            )
        }
    }

    override val body: Any by lazy {
        val content = req.content()

        if (content.isReadable) ByteBufUtil.getBytes(content)
        else byteArrayOf()
    }

    override val headers: MultiMap<String, String> by lazy {
        MultiMap(
            req.headers().names()
                .toList()
                .map { it.lowercase() }
                .associateWith { req.headers().getAll(it) }
        )
    }

    override val contentType: ContentType? by lazy {
        req.headers()[CONTENT_TYPE]?.let { parseContentType(it) }
    }
}