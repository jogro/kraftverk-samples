package io.kraftverk.samples.javalin

import io.javalin.Javalin
import io.javalin.core.JavalinConfig
import io.kraftverk.core.module.Module
import io.kraftverk.core.module.boolean
import io.kraftverk.core.module.port
import io.kraftverk.core.module.string

class JavalinModule : Module() {

    val contextPath by string(default = "/")
    val enforceSsl by boolean(default = false)
    val port by port(default = 7000)

    val config by pipe<JavalinConfig>()

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
