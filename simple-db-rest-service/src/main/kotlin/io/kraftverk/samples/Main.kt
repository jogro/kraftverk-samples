package io.kraftverk.samples

import io.kraftverk.core.Kraftverk
import io.kraftverk.core.module.ChildModule
import io.kraftverk.core.module.Module
import io.kraftverk.core.module.import
import io.kraftverk.core.module.module
import io.kraftverk.samples.hibernate.HibernateModule
import io.kraftverk.samples.hikari.HikariModule
import io.kraftverk.samples.javalin.JavalinModule
import io.kraftverk.samples.openapi.openApiPlugin
import io.kraftverk.samples.user.User
import io.kraftverk.samples.user.UserController
import io.kraftverk.samples.user.UserRepository
import io.kraftverk.samples.user.UserService
import io.kraftverk.samples.user.expose
import org.hibernate.cfg.Environment.DATASOURCE

fun main() {
    Kraftverk.start { AppModule() }
}

class AppModule : Module() {

    val jdbc by module { HikariModule() }
    val orm by module { HibernateModule() }
    val user by module { UserModule() }
    val http by module { JavalinModule() }

    init {
        configure(orm.serviceRegistryBuilder) {
            it.applySetting(DATASOURCE, jdbc.dataSource())
        }
        configure(orm.metadataSources) {
            it.addAnnotatedClass(User::class.java)
        }
        configure(http.config) {
            it.registerPlugin(openApiPlugin)
        }
        configure(http.javalin) {
            it.expose(user.controller())
        }
    }
}

class UserModule : ChildModule<AppModule>() {

    private val sessionFactory by import { orm.sessionFactory }
    private val tx by import { orm.tx }

    val repository by bean { UserRepository(sessionFactory()) }
    val service by bean { UserService(tx(), repository()) }
    val controller by bean { UserController(service()) }
}
