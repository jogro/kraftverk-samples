package io.kraftverk.samples.user

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.Context

class UserController(private val userService: UserService) {

    fun create(ctx: Context) {
        val user = ctx.body<User>()
        userService.create(user)
        ctx.json(user)
    }

    fun read(ctx: Context) {
        val id = ctx.pathParam("userId").toLong()
        val user = userService.findById(id)
        if (user != null) ctx.json(user) else ctx.status(404)
    }

    fun update(ctx: Context) {
        val user = ctx.body<User>()
        userService.update(user)
        ctx.status(200)
    }

}

internal fun Javalin.expose(userController: UserController) = routes {
    path("users") {
        post(userController::create)
        path(":userId") {
            get(userController::read)
        }
        put(userController::update)
    }
}