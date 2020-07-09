package io.kraftverk.samples.rabbit

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import com.rabbitmq.client.impl.AMQBasicProperties
import io.kraftverk.core.module.Module
import io.kraftverk.core.module.port
import io.kraftverk.core.module.string
import org.apache.commons.pool2.PooledObject
import org.apache.commons.pool2.PooledObjectFactory
import org.apache.commons.pool2.impl.DefaultPooledObject
import org.apache.commons.pool2.impl.GenericObjectPool
import org.apache.commons.pool2.impl.GenericObjectPoolConfig

/**
 * Ongoing work ...
 */
open class RabbitModule() : Module() {

    val username by string()
    val password by string()
    val host by string()
    val port by port()

    val connectionFactory by bean { ConnectionFactory() }
    val connection by bean { connectionFactory().newConnection() }

    val txChannelFactory by bean { TxChannelFactory(connection()) }
    val txChannelPoolConfig by bean { GenericObjectPoolConfig<Channel>() }
    val txChannelPool by bean { GenericObjectPool(txChannelFactory()) }

    init {
        configure(connectionFactory) {
            it.username = username()
            it.password = password()
            it.host = host()
            it.port = port()
        }
        configure(txChannelPoolConfig) {
            it.maxTotal = 3
        }
        configure(connection) {
            lifecycle {
                onDestroy { if (it.isOpen) it.close() }
            }
        }
    }
}

class Delivery<T : Any>(
    val consumerTag: String,
    private val channel: Channel,
    private val envelope: Envelope,
    properties: AMQBasicProperties,
    val body: ByteArray
) {
    fun ack() = channel.basicAck(envelope.deliveryTag, false)
}

class RabbitConsumer<T : Any>(
    private val conn: Connection,
    private val queueName: String = "",
    private val exchangeName: String = "",
    private val routingKey: String = "",
    private val durable: Boolean = false,
    private val exclusive: Boolean = false,
    private val autoDelete: Boolean = false,
    private val block: (Delivery<T>) -> Unit
) {
    fun start() {
        val channel = conn.createChannel()
        channel.queueDeclare(
            queueName,
            durable,
            exclusive,
            autoDelete,
            null
        )
        if (exchangeName.isNotBlank()) {
            channel.queueBind(queueName, exchangeName, routingKey)
        }
        val consumer = object : DefaultConsumer(channel) {
            override fun handleDelivery(
                consumerTag: String,
                envelope: Envelope,
                properties: AMQP.BasicProperties,
                body: ByteArray
            ) {
                block(
                    Delivery(
                        consumerTag,
                        channel,
                        envelope,
                        properties,
                        body
                    )
                )
            }
        }
        channel.basicConsume(queueName, false, consumer)
    }
}

class TxChannelFactory(private val connection: Connection) : PooledObjectFactory<Channel> {

    override fun makeObject(): PooledObject<Channel> = connection.createChannel()
        .apply { txSelect() }
        .let(::DefaultPooledObject)

    override fun destroyObject(p: PooledObject<Channel>) {
        p.getObject().close()
    }

    override fun validateObject(p: PooledObject<Channel>): Boolean = p.getObject().isOpen

    override fun activateObject(p: PooledObject<Channel>) {}
    override fun passivateObject(p: PooledObject<Channel>) {}
}
