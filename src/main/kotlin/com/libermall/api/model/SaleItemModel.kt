package com.libermall.api.model

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class SaleItemModel(
    override val address: String,
    override val index: ULong,
    override val collection: CollectionModel?,
    override val owner: String?,
    override val name: String,
    override val description: String,
    override val image: ImageModel,
    override val attributes: Map<String, String>,

    val sale: String,
    val marketplace: String?,
    @Contextual
    val fullPrice: BigInteger,
    @Contextual
    val marketplaceFee: BigInteger,
    @Contextual
    val royalties: BigInteger,
    val royaltyDestination: String?,

    @Contextual
    override val networkFee: BigInteger,
) : ItemModel
