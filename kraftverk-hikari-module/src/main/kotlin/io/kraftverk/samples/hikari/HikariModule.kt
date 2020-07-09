package io.kraftverk.samples.hikari

import com.zaxxer.hikari.HikariDataSource
import io.kraftverk.core.module.Module
import io.kraftverk.core.module.string

class HikariModule : Module() {

    val url by string(default = "jdbc:h2:mem:testdb")
    val username by string(default = "sa")
    val password by string(default = "", secret = true)

    val dataSource by bean { HikariDataSource() }

    init {
        configure(dataSource) { ds ->
            ds.jdbcUrl = url()
            ds.username = username()
            ds.password = password()
        }
    }
}
