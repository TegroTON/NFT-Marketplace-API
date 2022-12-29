package com.libermall.api.model

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class OrdinaryItemModel(
    override val address: String,
    override val index: ULong,
    override val collection: CollectionModel?,
    override val owner: String?,
    override val name: String,
    override val description: String,
    override val image: ImageModel,
    override val attributes: Map<String, String>,

    @Contextual
    val royaltyValue: BigDecimal,
    @Contextual
    val marketplaceFeeValue: BigDecimal,
    @Contextual
    val saleInitializationFee: BigInteger,
    @Contextual
    val transferFee: BigInteger,

    @Contextual
    override val networkFee: BigInteger,
) : ItemModel
