package com.libermall.api.metadata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemMetadataAttribute(
    @SerialName("trait_type")
    val trait: String,
    val value: String
)
