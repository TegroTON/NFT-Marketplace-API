package com.libermall.api.repository

import com.libermall.api.contract.nft.RoyaltyContract
import com.libermall.api.service.ReferenceBlockService
import com.libermall.api.toRaw
import io.github.reactivecircus.cache4k.Cache
import mu.KLogging
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.ton.block.AddrStd
import org.ton.block.MsgAddressInt
import org.ton.lite.client.LiteClient
import java.util.*

class RoyaltyRepository(override val di: DI) : DIAware {
    private val liteClient: LiteClient by instance()

    private val approvalRepository: ApprovalRepository by instance()

    private val cache: Cache<MsgAddressInt, Optional<RoyaltyContract>> by instance()

    private val referenceBlockService: ReferenceBlockService by instance()

    suspend fun get(royalty: MsgAddressInt): RoyaltyContract? =
        cache.get(royalty) {
            if (approvalRepository.isDisapproved(royalty)) { // Explicitly forbidden
                logger.debug { "address=${royalty.toRaw()} was disapproved" }
                Optional.empty()
            } else {
                logger.debug { "fetching royalty address=${royalty.toRaw()}" }
                RoyaltyContract.of(royalty as AddrStd, liteClient, referenceBlockService.last())
                    .let { Optional.ofNullable(it) }
            }
        }
            .orElse(null)

    companion object : KLogging()
}
