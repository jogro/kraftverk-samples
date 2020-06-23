package io.kraftverk.samples

import io.kraftverk.Kraftverk
import io.kraftverk.module.*
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
        configure(orm.serviceRegistryBuilder) { b ->
            b.applySetting(DATASOURCE, jdbc.dataSource())
        }
        configure(orm.metadataSources) { m ->
            m.addAnnotatedClass(User::class.java)
        }
        configure(http.server) { s ->
            s.expose(user.controller())
        }
    }
}

class UserModule : ChildModule<AppModule>() {

    private val sessionFactory by ref { orm.sessionFactory }
    private val tx by ref { orm.tx }

    val repository by bean { UserRepository(sessionFactory()) }
    val service by bean { UserService(tx(), repository()) }
    val controller by bean { UserController(service()) }
}
