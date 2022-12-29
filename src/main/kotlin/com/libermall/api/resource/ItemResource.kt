package com.libermall.api.resource

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
sealed interface ItemResource {
    @Serializable
    @Resource("/item/all")
    class All(
        val sort: Sort? = null,
        val relatedTo: String? = null,
        val relation: Relation? = null,
        val filter: Filter? = null,
        val drop: Int? = null,
        val take: Int? = null,
    ) {
        @Serializable
        enum class Sort {
            INDEX_UP,
            INDEX_DOWN,
            PRICE_UP,
            PRICE_DOWN,
        }

        @Serializable
        enum class Relation {
            COLLECTION,
            OWNERSHIP,
        }

        @Serializable
        enum class Filter {
            ON_SALE,
            NOT_FOR_SALE,
        }
    }

    @Serializable
    @Resource("/item/address/{address}")
    data class ByAddress(
        val address: String,
    ) {
        @Serializable
        @Resource("/transfer")
        data class Transfer(
            val parent: ByAddress,
            val newOwner: String,
            val response: String? = null,
        )

        @Serializable
        @Resource("/sell")
        data class Sell(
            val parent: ByAddress,
            val seller: String,
            val price: String,
        )
    }
}
