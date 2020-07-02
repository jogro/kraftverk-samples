package io.kraftverk.samples.sunhttp

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import io.kraftverk.core.module.*
import java.net.InetSocketAddress

class SunHttpModule : Module() {

    val host by string(default = "localhost")
    val port by port(default = 8001)

    val address by bean { InetSocketAddress(host(), port()) }
    val server by bean { HttpServer.create(address(), 0) }

    init {
        configure(server) { b ->
            lifecycle {
                onCreate { b.start() }
                onDestroy { b.stop(0) }
            }
        }
    }
}

fun HttpServer.expose(path: String, handler: HttpHandler) {
    createContext(path, handler)
}

fun HttpExchange.respond(text: String) {
    sendResponseHeaders(200, text.toByteArray().size.toLong())
    responseBody.use { it.write(text.toByteArray()) }
}

