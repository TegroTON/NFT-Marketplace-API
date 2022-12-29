package com.libermall.api.repository

import com.libermall.api.contract.nft.CollectionContract
import com.libermall.api.metadata.CollectionMetadata
import com.libermall.api.model.CollectionModel
import com.libermall.api.model.ImageModel
import com.libermall.api.service.ReferenceBlockService
import com.libermall.api.toRaw
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import mu.KLogging
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.ton.api.exception.TvmException
import org.ton.block.AddrStd
import org.ton.block.MsgAddress
import org.ton.block.MsgAddressInt
import org.ton.lite.client.LiteClient
import java.util.*

class CollectionRepository(
    override val di: DI,
) : DIAware {
    private val liteClient: LiteClient by instance()
    private val httpClient: HttpClient by instance()

    private val approvalRepository: ApprovalRepository by instance()

    private val contractCache: Cache<MsgAddressInt, Optional<CollectionContract>> by instance()
    private val metadataCache: Cache<MsgAddressInt, Optional<CollectionMetadata>> by instance()
    private val itemAddressCache: Cache<Pair<MsgAddressInt, ULong>, Optional<MsgAddress>> by instance()

    private val referenceBlockService: ReferenceBlockService by instance()

    fun all() =
        approvalRepository.allApproved()
            .asFlow()
            .mapNotNull { get(it.address) }

    suspend fun get(collection: MsgAddressInt): CollectionModel? =
        getContract(collection)?.let { contract ->
            getMetadata(collection)?.let { metadata ->
                CollectionModel(
                    address = collection.toRaw(),
                    numberOfItems = contract.next_item_index,
                    owner = contract.owner.toRaw(),
                    name = metadata.name ?: "Untitled Collection",
                    description = metadata.description.orEmpty(),
                    image = ImageModel(
                        original = metadata.image
                    ),
                    coverImage = ImageModel(
                        original = metadata.cover_image ?: metadata.image
                    ),
                )
            }
        }

    private suspend fun getContract(collection: MsgAddressInt): CollectionContract? =
        contractCache.get(collection) {
            if (approvalRepository.isApproved(collection)) { // Has been explicitly approved
                try {
                    logger.debug { "fetching collection address=${collection.toRaw()}" }
                    CollectionContract.of(collection as AddrStd, liteClient, referenceBlockService.last())
                        .let { Optional.of(it) }
                } catch (e: TvmException) {
                    logger.warn(e) { "could not get collection information for address=${collection.toRaw()}" }
                    Optional.empty()
                }
            } else {
                logger.warn { "address=${collection.toRaw()} was not approved" }
                Optional.empty()
            }
        }
            .orElse(null)

    private suspend fun getMetadata(collection: MsgAddressInt): CollectionMetadata? =
        metadataCache.get(collection) {
            if (approvalRepository.isApproved(collection)) { // Has been explicitly approved
                getContract(collection)
                    ?.let {
                        logger.debug { "loading metadata for ${collection.toRaw()}" }
                        CollectionMetadata.of(it.content, httpClient)
                    }
                    .let { Optional.ofNullable(it) }
            } else {
                logger.warn { "address=${collection.toRaw()} was not approved" }
                Optional.empty()
            }
        }
            .orElse(null)

    suspend fun getItemAddress(
        collection: MsgAddressInt,
        index: ULong,
    ): MsgAddress? =
        itemAddressCache.get(collection to index) {
            if (approvalRepository.isApproved(collection)) { // Has been explicitly approved
                try {
                    CollectionContract.itemAddressOf(
                        collection as AddrStd,
                        index,
                        liteClient,
                        referenceBlockService.last()
                    )
                        .let { Optional.of(it) }
                } catch (e: TvmException) {
                    logger.warn { "could not get item index=$index address of collection=${collection.toRaw()}" }
                    Optional.empty()
                }
            } else {
                logger.warn { "address=${collection.toRaw()} was not approved" }
                Optional.empty()
            }
        }
            .orElse(null)

    fun itemsOf(collection: MsgAddressInt) =
        flow {
            for (index in 0uL until (getContract(collection)?.next_item_index ?: 0uL)) {
                emit(index to getItemAddress(collection, index))
            }
        }

    companion object : KLogging()
}
