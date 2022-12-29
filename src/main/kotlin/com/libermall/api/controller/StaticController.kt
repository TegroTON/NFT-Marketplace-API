package com.libermall.api.controller

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.ktor.controller.AbstractDIController

class StaticController(application: Application) : AbstractDIController(application) {
    override fun Route.getRoutes() {
        get("/") {
            call.respondText(
                this::class.java.classLoader.getResource("index.html")!!.readText(),
                ContentType.Text.Html
            )
        }

        static("/") {
            resources("")
        }
    }
}
