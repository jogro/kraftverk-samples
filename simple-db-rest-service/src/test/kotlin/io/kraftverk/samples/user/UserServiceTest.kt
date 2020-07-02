package io.kraftverk.samples.user

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kraftverk.core.Kraftverk
import io.kraftverk.samples.AppModule
import io.kraftverk.samples.junit5.get
import io.kraftverk.samples.junit5.mockk
import io.kraftverk.samples.junit5.test
import io.mockk.every
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.RegisterExtension

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UserServiceTest {

    @JvmField
    @RegisterExtension
    val app = Kraftverk.test { AppModule() }

    private val repository by app.mockk { user.repository }
    private val tx by app.mockk { orm.tx }
    private val sut by app.get { user.service }

    @Test
    fun create() {

        every { repository.emailExists(any()) } returns false
        every { repository.nameExists(any()) } returns false

        sut.create(TestObjects.user)

        verifyOrder {
            tx.begin()
            repository.create(TestObjects.user)
            tx.commit()
        }
    }

    @Test
    fun `create throws exception when email already exists`() {

        every { repository.emailExists(any()) } returns true
        every { repository.nameExists(any()) } returns false

        val exception = shouldThrow<IllegalArgumentException> {
            sut.create(TestObjects.user)
        }
        exception.message shouldBe "Email already exists"

        verifyOrder {
            tx.begin()
            tx.rollback()
        }
    }

    @Test
    fun `update throws exception when name already exists`() {

        every { repository.emailExists(any()) } returns false
        every { repository.nameExists(any()) } returns true

        val exception = shouldThrow<IllegalArgumentException> {
            sut.update(TestObjects.user)
        }
        exception.message shouldBe "Name already exists"

        verifyOrder {
            tx.begin()
            tx.rollback()
        }
    }

    @Test
    fun read() {
        every { repository.findById(any()) } returns TestObjects.user
        sut.findById(1L) shouldBe TestObjects.user
        verifyOrder {
            tx.begin()
            repository.findById(1L)
            tx.commit()
        }
    }

}
