package io.kraftverk.samples

import io.kraftverk.core.Kraftverk
import io.kraftverk.core.module.*
import io.kraftverk.samples.hibernate.HibernateModule
import io.kraftverk.samples.hikari.HikariModule
import io.kraftverk.samples.javalin.JavalinModule
import io.kraftverk.samples.user.*
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
