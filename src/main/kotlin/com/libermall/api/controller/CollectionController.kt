package com.libermall.api.controller

import com.libermall.api.dropTake
import com.libermall.api.repository.CollectionRepository
import com.libermall.api.resource.CollectionResource
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.toList
import org.kodein.di.instance
import org.kodein.di.ktor.controller.AbstractDIController
import org.ton.block.MsgAddressInt

class CollectionController(application: Application) : AbstractDIController(application) {
    private val collectionRepository: CollectionRepository by instance()

    override fun Route.getRoutes() {
        get<CollectionResource.All> { request ->
            collectionRepository.all()
                .let {
                    when (request.sort) {
                        CollectionResource.All.Sort.ALL -> it

                        CollectionResource.All.Sort.TOP -> it // TODO: Actual collection top

                        else -> it
                    }
                }
                .dropTake(request.drop, request.take)
                .toList()
                .let { call.respond(it) }
        }

        get<CollectionResource.ByAddress> { request ->
            requireNotNull(collectionRepository.get(MsgAddressInt(request.address)))
                .let { call.respond(it) }
        }
    }
}
