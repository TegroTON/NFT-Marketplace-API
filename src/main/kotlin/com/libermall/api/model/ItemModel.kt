package com.libermall.api.model

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable


@Serializable
sealed interface ItemModel {
    val address: String
    val index: ULong
    val collection: CollectionModel?
    val owner: String?
    val name: String
    val description: String
    val image: ImageModel
    val attributes: Map<String, String>

    @Contextual
    val networkFee: BigInteger
}
