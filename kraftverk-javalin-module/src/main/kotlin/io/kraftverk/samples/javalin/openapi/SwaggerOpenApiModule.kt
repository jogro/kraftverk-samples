package io.kraftverk.samples.javalin.openapi

import io.javalin.plugin.openapi.OpenApiOptions
import io.javalin.plugin.openapi.OpenApiPlugin
import io.javalin.plugin.openapi.ui.SwaggerOptions
import io.kraftverk.core.module.Module
import io.kraftverk.core.module.string
import io.swagger.v3.oas.models.info.Info

class SwaggerOpenApiModule : Module() {

    val version by string(default = "1.0")
    val title by string(default = "My API")
    val description by string(default = "My API")
    val docsPath by string(default = "/swagger-docs")
    val uiPath by string(default = "/swagger-ui")

    val uiOptions by bean { SwaggerOptions(uiPath()) }
    val info by bean { Info() }
    val options by bean { OpenApiOptions(info()) }
    val plugin by bean { OpenApiPlugin(options()) }

    init {
        configure(info) {
            it.title(title())
            it.version(version())
            it.description(description())
        }
        configure(options) {
            it.path(docsPath())
            it.swagger(uiOptions())
        }
    }
}
