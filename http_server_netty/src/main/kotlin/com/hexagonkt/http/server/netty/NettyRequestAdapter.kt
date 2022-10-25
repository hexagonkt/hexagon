package com.hexagonkt.http.server.netty

import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.Headers as HxHttpHeaders
import com.hexagonkt.http.parseContentType
import com.hexagonkt.http.server.model.HttpServerRequestPort
import io.netty.buffer.ByteBufUtil
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames.*
import io.netty.handler.codec.http.HttpHeaders
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
    nettyHeaders: HttpHeaders,
) : HttpServerRequestPort {

    override val accept: List<ContentType> by lazy {
        nettyHeaders.getAll(ACCEPT).flatMap { it.split(",") }.map { parseContentType(it) }
    }

    override val contentLength: Long by lazy {
        nettyHeaders[CONTENT_LENGTH]?.toLong() ?: 0L
    }

    override val queryParameters: QueryParameters by lazy {
        val queryStringDecoder = QueryStringDecoder(req.uri())
        QueryParameters(queryStringDecoder.parameters().map { (k, v) -> QueryParameter(k, v) })
    }

    override val parts: List<HttpPart> by lazy {
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

    override val formParameters: FormParameters by lazy {
        val fields = parts
            .filter { it.submittedFileName == null }
            .groupBy { it.name }
            .mapValues { it.value.map { v -> v.bodyString() } }
            .map { (k, v) -> FormParameter(k, v) }

        FormParameters(fields)
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
        val cookieHeader: String = nettyHeaders.get(COOKIE)
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

    override val headers: HxHttpHeaders by lazy {
        HxHttpHeaders(
            nettyHeaders.names()
                .toList()
                .map { it.lowercase() }
                .map { Header(it, nettyHeaders.getAll(it)) }
        )
    }

    override val contentType: ContentType? by lazy {
        nettyHeaders[CONTENT_TYPE]?.let { parseContentType(it) }
    }

    override val authorization: Authorization? by lazy { authorization() }
}
