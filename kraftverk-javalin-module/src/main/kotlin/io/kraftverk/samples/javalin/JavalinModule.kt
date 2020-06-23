package io.kraftverk.samples.javalin

import io.javalin.Javalin
import io.javalin.core.JavalinConfig
import io.javalin.core.security.Role
import io.javalin.http.Context
import io.javalin.http.Handler
import io.kraftverk.module.*

class JavalinModule : Module() {

    val config by customBean { Config() }
    val server by bean { config().createJavalin() }

    init {
        configure(server) { s ->
            lifecycle {
                onCreate { s.start() }
                onDestroy { s.stop() }
            }
        }
    }
}

class Config : CustomBeanSpi<JavalinConfig> {

    private val blocks = mutableListOf<(JavalinConfig) -> Unit>()

    override fun onConfigure(configure: (JavalinConfig) -> Unit) {
        blocks.add(configure)
    }

    fun createJavalin(): Javalin = Javalin.create { c ->
        blocks.forEach { it.invoke(c) }
    }
}
