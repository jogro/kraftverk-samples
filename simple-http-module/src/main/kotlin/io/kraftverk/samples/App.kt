package io.kraftverk.samples

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import io.kraftverk.core.Kraftverk
import io.kraftverk.core.module.Module
import io.kraftverk.core.module.bean
import io.kraftverk.core.module.configure
import io.kraftverk.core.module.module
import io.kraftverk.samples.sunhttp.SunHttpModule
import io.kraftverk.samples.sunhttp.expose
import io.kraftverk.samples.sunhttp.respond

class EchoHandler : HttpHandler {
    override fun handle(exchange: HttpExchange) = with(exchange) {
        respond(requestURI.path.substringAfterLast("/"))
    }
}

class AppModule : Module() {

    val http by module { SunHttpModule() }
    val echo by bean { EchoHandler() }

    init {
        configure(http.server) { b ->
            b.expose("/echo", echo())
        }
    }
}

fun main() {
    Kraftverk.start { AppModule() }
}