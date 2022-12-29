package com.libermall.api.metadata

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import mu.KLogging
import org.ton.bitstring.BitString
import org.ton.cell.Cell
import org.ton.contract.*
import org.ton.crypto.base64
import org.ton.crypto.sha256
import org.ton.tlb.loadTlb

@Serializable
sealed interface Metadata {
    val uri: String?
    val name: String?
    val description: String?
    val image: String?
    val image_data: ByteArray?

    companion object : KLogging() {
        val ONCHAIN_URI_KEY = BitString(sha256("uri".toByteArray()))
        val ONCHAIN_NAME_KEY = BitString(sha256("name".toByteArray()))
        val ONCHAIN_DESCRIPTION_KEY = BitString(sha256("description".toByteArray()))
        val ONCHAIN_IMAGE_KEY = BitString(sha256("image".toByteArray()))
        val ONCHAIN_IMAGE_DATA_KEY = BitString(sha256("image_data".toByteArray()))

        @JvmStatic
        fun onchainDataOf(content: FullContent.OnChain): String =
            content.data.toMap().let { entries ->
                JsonObject(
                    mapOf(
                        "uri" to JsonPrimitive(entries.get(ONCHAIN_URI_KEY)?.flatten()?.decodeToString()),
                        "name" to JsonPrimitive(entries.get(ONCHAIN_NAME_KEY)?.flatten()?.decodeToString()),
                        "description" to JsonPrimitive(
                            entries.get(ONCHAIN_DESCRIPTION_KEY)?.flatten()?.decodeToString()
                        ),
                        "image" to JsonPrimitive(entries.get(ONCHAIN_IMAGE_KEY)?.flatten()?.decodeToString()),
                        "image_data" to JsonPrimitive(entries.get(ONCHAIN_IMAGE_DATA_KEY)?.flatten()?.let(::base64))
                    )
                ).toString()
            }

        @JvmStatic
        suspend fun of(content: Cell, httpClient: HttpClient = HttpClient {}): String =
            when (val full = content.parse { loadTlb(FullContent) }) {
                is FullContent.OnChain ->
                    onchainDataOf(full)

                is FullContent.OffChain ->
                    httpClient.get(String(full.uri.data.flatten())) {
                        accept(ContentType.Application.Json)
                    }
                        .bodyAsText()
            }
    }
}

fun SnakeData.flatten(): ByteArray = when (this) {
    is SnakeDataTail -> bits.toByteArray()
    is SnakeDataCons -> bits.toByteArray() + next.flatten()
}

fun ContentData.flatten(): ByteArray = when (this) {
    is ContentData.Snake -> this.data.flatten()
    is ContentData.Chunks -> TODO("chunky content data")
}
