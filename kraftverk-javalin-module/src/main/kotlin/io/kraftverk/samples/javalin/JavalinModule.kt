package io.kraftverk.samples.javalin

import io.javalin.Javalin
import io.javalin.core.JavalinConfig
import io.kraftverk.core.module.*

class JavalinModule : Module() {

    val contextPath by string(default = "/")
    val enforceSsl by boolean(default = false)
    val port by port(default = 7000)

    val config by sink<JavalinConfig>()

    val javalin by bean { Javalin.create { c -> config(c) } }

    init {
        configure(config) {
            it.contextPath = contextPath()
            it.enforceSsl = enforceSsl()
        }
        configure(javalin) {
            lifecycle {
                onCreate { it.start(port()) }
                onDestroy { it.stop() }
            }
        }
    }
}
