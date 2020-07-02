package io.kraftverk.samples.hibernate

import io.kraftverk.core.module.Module
import io.kraftverk.core.module.bean
import io.kraftverk.core.module.configure
import org.hibernate.SessionFactory
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.AvailableSettings

class HibernateModule : Module() {

    val serviceRegistryBuilder by bean { StandardServiceRegistryBuilder() }
    val serviceRegistry by bean { serviceRegistryBuilder().build() }
    val metadataSources by bean { MetadataSources(serviceRegistry()) }
    val metadataBuilder by bean { metadataSources().metadataBuilder }
    val metadata by bean { metadataBuilder().build() }
    val sessionFactoryBuilder by bean { metadata().sessionFactoryBuilder }
    val sessionFactory by bean { sessionFactoryBuilder().build() }
    val tx by bean { Tx(sessionFactory()) }

    init {
        configure(serviceRegistryBuilder) { b ->
            b.applySetting(AvailableSettings.TRANSACTION_COORDINATOR_STRATEGY, "jdbc")
        }
    }
}

class Tx(private val sessionFactory: SessionFactory) {

    internal val session get() = sessionFactory.currentSession

    fun begin() {
        session.transaction.begin()
    }

    fun rollback() {
        session.transaction.rollback()
    }

    fun commit() {
        session.transaction.commit()
    }
}


fun <T> Tx.read(block: () -> T): T = with(session) {
    transacted {
        isDefaultReadOnly = true
        hibernateFlushMode = org.hibernate.FlushMode.MANUAL
        block()
    }
}

fun <T> Tx.write(block: () -> T) = transacted(block)

private inline fun <T> Tx.transacted(block: () -> T): T {
    begin()
    try {
        return block().also { commit() }
    } catch (ex: Exception) {
        rollback()
        throw ex
    }
}
