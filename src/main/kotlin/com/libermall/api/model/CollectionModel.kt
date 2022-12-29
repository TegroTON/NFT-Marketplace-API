package com.libermall.api.model

import kotlinx.serialization.Serializable

@Serializable
data class CollectionModel(
    val address: String,
    val numberOfItems: ULong,
    val owner: String?,
    val name: String,
    val description: String,
    val image: ImageModel,
    val coverImage: ImageModel,
)
