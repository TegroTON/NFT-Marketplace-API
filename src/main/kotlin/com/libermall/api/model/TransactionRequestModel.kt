package com.libermall.api.model

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class TransactionRequestModel(
    val dest: String,
    @Contextual
    val value: BigInteger,
    val stateInit: String?,
    val text: String?,
    val payload: String?,
)
