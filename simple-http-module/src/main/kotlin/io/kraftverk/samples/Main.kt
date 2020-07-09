package io.kraftverk.samples

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import io.kraftverk.core.Kraftverk
import io.kraftverk.core.module.Module
import io.kraftverk.core.module.module
import io.kraftverk.samples.sunhttp.SunHttpModule
import io.kraftverk.samples.sunhttp.expose
import io.kraftverk.samples.sunhttp.respond

fun main() {
    Kraftverk.start { AppModule() }
}

class AppModule : Module() {

    val http by module { SunHttpModule() }
    val echo by bean { EchoHandler() }

    init {
        configure(http.server) {
            it.expose("/echo", echo())
        }
    }
}

class EchoHandler : HttpHandler {
    override fun handle(exchange: HttpExchange) = with(exchange) {
        respond(requestURI.path.substringAfterLast("/"))
    }
}
