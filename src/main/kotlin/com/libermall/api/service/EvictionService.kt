package com.libermall.api.service

import com.libermall.api.contract.nft.CollectionContract
import com.libermall.api.contract.nft.ItemContract
import com.libermall.api.contract.nft.RoyaltyContract
import com.libermall.api.contract.nft.SaleContract
import com.libermall.api.metadata.CollectionMetadata
import com.libermall.api.metadata.ItemMetadata
import com.libermall.api.toRaw
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import mu.KLogging
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.ton.block.AddrStd
import org.ton.block.MsgAddressInt
import java.util.*

class EvictionService(override val di: DI) : DIAware {
    private val liveBlockService: LiveBlockService by instance()

    private val collectionContractCache: Cache<MsgAddressInt, Optional<CollectionContract>> by instance()
    private val collectionMetadataCache: Cache<MsgAddressInt, Optional<CollectionMetadata>> by instance()

    private val itemContractCache: Cache<MsgAddressInt, Optional<ItemContract>> by instance()
    private val itemMetadataCache: Cache<MsgAddressInt, Optional<ItemMetadata>> by instance()

    private val saleCache: Cache<MsgAddressInt, Optional<SaleContract>> by instance()
    private val royaltyCache: Cache<MsgAddressInt, Optional<RoyaltyContract>> by instance()

    private val allCaches = listOf(
        collectionContractCache,
        collectionMetadataCache,
        itemContractCache,
        itemMetadataCache,
        saleCache,
        royaltyCache
    )

    @OptIn(FlowPreview::class)
    private val backgroundJob =
        CoroutineScope(Dispatchers.Default + CoroutineName("evictionService")).launch {
            liveBlockService.data
                .flatMapConcat { block ->
                    block.extra.account_blocks.value.toMap()
                        .keys
                        .map { AddrStd(block.info.shard.workchain_id, it.account_addr) }
                        .asFlow()
                }
                .collect { address ->
                    allCaches.filter { it.get(address) != null }
                        .onEach { logger.debug { "entity ${address.toRaw()} matched cache $it" } }
                        .forEach {
                            it.invalidate(address)
                        }
                }
        }

    companion object : KLogging()
}
