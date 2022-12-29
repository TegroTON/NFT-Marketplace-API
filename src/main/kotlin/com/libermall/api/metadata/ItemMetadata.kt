package com.libermall.api.metadata

import io.ktor.client.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KLogging
import org.ton.cell.Cell
import org.ton.crypto.Base64ByteArraySerializer

@Serializable
data class ItemMetadata(
    override val uri: String? = null,
    override val name: String? = null,
    override val description: String? = null,
    override val image: String? = null,
    @Serializable(with = Base64ByteArraySerializer::class)
    override val image_data: ByteArray? = null,
    val attributes: List<ItemMetadataAttribute>? = null,
) : Metadata {
    companion object : KLogging() {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        @JvmStatic
        suspend fun of(content: Cell, httpClient: HttpClient = HttpClient {}): ItemMetadata? =
            try {
                json.decodeFromString(Metadata.of(content, httpClient))
            } catch (e: SerializationException) {
                logger.warn(e) { "could not load item metadata from $content" }
                null
            }
    }
}

