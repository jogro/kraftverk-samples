package io.kraftverk.samples.junit5

import io.kraftverk.core.Kraftverk
import io.kraftverk.core.binding.Bean
import io.kraftverk.core.binding.Binding
import io.kraftverk.core.managed.*
import io.kraftverk.core.module.Module
import io.kraftverk.core.module.bind
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.spyk
import org.apache.commons.lang3.reflect.MethodUtils.getMethodsListWithAnnotation
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@Target(allowedTargets = [AnnotationTarget.FUNCTION])
annotation class BeforeStart

@Target(allowedTargets = [AnnotationTarget.FUNCTION])
annotation class AfterStart

@Target(allowedTargets = [AnnotationTarget.FUNCTION])
annotation class BeforeStop

@Target(allowedTargets = [AnnotationTarget.FUNCTION])
annotation class AfterStop

class KraftverkJunit5Extension<M : Module>(@PublishedApi internal val managed: Managed<M>) : BeforeAllCallback,
    AfterAllCallback, BeforeEachCallback {

    val mocks = mutableListOf<Any>()

    override fun beforeAll(ctx: ExtensionContext) {
        runAnnotatedMethods(ctx, BeforeStart::class)
        managed.start(lazy = true)
        runAnnotatedMethods(ctx, AfterStart::class)
    }

    override fun afterAll(ctx: ExtensionContext) {
        runAnnotatedMethods(ctx, BeforeStop::class)
        managed.stop()
        runAnnotatedMethods(ctx, AfterStop::class)
    }

    override fun beforeEach(context: ExtensionContext?) {
        mocks.forEach { clearMocks(it) }
    }

    private fun runAnnotatedMethods(
        ctx: ExtensionContext,
        annotationClass: KClass<out Annotation>
    ) {
        ctx.testClass
            .map { clazz -> getMethodsListWithAnnotation(clazz, annotationClass.java) }
            .orElse(emptyList())
            .forEach { method ->
                ctx.testInstance.ifPresent { instance ->
                    method.invoke(instance)
                }
            }
    }

}

fun <M : Module> KraftverkJunit5Extension<M>.configure(block: M.() -> Unit): KraftverkJunit5Extension<M> {
    managed.configure(block)
    return this
}

fun <M : Module> Kraftverk.test(module: () -> M): KraftverkJunit5Extension<M> {
    return KraftverkJunit5Extension(manage { module() })
}

inline fun <M : Module, reified T : Any> KraftverkJunit5Extension<M>.get(noinline binding: M.() -> Binding<T>):
        ReadOnlyProperty<Any?, T> {
    return managed.get(binding)
}

inline fun <M : Module, reified T : Any> KraftverkJunit5Extension<M>.mockk(
    relaxed: Boolean = true,
    noinline binding: M.() -> Bean<T>
): ReadOnlyProperty<Any?, T> {
    val mock = mockk<T>(relaxed = relaxed)
    managed.configure {
        bind(binding()) to { mock }
    }
    mocks += mock
    return object : ReadOnlyProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>) = mock
    }
}

inline fun <M : Module, reified T : Any> KraftverkJunit5Extension<M>.spyk(noinline bean: M.() -> Bean<T>):
        ReadOnlyProperty<Any?, T> {
    managed.configure {
        bind(bean()) to { spyk(proceed()) }
    }
    return managed.get(bean)
}
