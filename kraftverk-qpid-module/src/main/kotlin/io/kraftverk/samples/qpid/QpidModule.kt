package io.kraftverk.samples.qpid

import io.kraftverk.core.module.*
import org.apache.qpid.server.SystemLauncher

class QpidModule : Module() {

    val initialConfigLocation by string(default = "/qpid-config.json")
    val port by port(default = 0)

    val systemConfigAttributes by bean { mutableMapOf<String, Any>() }
    val systemLauncher by bean { SystemLauncher() }

    init {
        configure(systemConfigAttributes) { attrs ->
            attrs["type"] = "Memory"
            attrs["initialConfigurationLocation"] =
                QpidModule::class.java.getResource(initialConfigLocation()).toExternalForm()
            attrs["startupLoggedToSystemOut"] = true
            attrs["context"] = mapOf(
                "qpid.amqp_port" to port().toString()
            )
        }
        configure(systemLauncher) { s ->
            lifecycle {
                onCreate { s.startup(systemConfigAttributes()) }
                onDestroy { s.shutdown() }
            }
        }
    }

}
