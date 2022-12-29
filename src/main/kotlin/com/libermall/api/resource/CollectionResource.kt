package com.libermall.api.resource

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
sealed interface CollectionResource {
    @Serializable
    @Resource("/collection/all")
    data class All(
        val sort: Sort? = null,
        val drop: Int? = null,
        val take: Int? = null,
    ) {
        @Serializable
        enum class Sort {
            ALL,
            TOP,
        }
    }

    @Serializable
    @Resource("/collection/address/{address}")
    data class ByAddress(
        val address: String,
    )
}
