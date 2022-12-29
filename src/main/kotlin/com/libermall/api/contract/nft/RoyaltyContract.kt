package com.libermall.api.contract.nft

import com.libermall.api.toRaw
import mu.KLogging
import org.ton.api.exception.TvmException
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.block.AddrStd
import org.ton.block.MsgAddress
import org.ton.lite.api.liteserver.LiteServerAccountId
import org.ton.lite.client.LiteClient
import org.ton.tlb.loadTlb

data class RoyaltyContract(
    val numerator: Int,
    val denominator: Int,
    val destination: MsgAddress,
) {
    fun value() = numerator.toDouble() / denominator

    companion object : KLogging() {
        @JvmStatic
        suspend fun of(
            address: AddrStd,
            liteClient: LiteClient,
            referenceBlock: TonNodeBlockIdExt?,
        ): RoyaltyContract? =
            try {
                liteClient.runSmcMethod(
                    LiteServerAccountId(address),
                    referenceBlock ?: liteClient.getLastBlockId(),
                    "royalty_params"
                )
            } catch (e: TvmException) {
                logger.warn(e) { "could not get royalty information for address=${address.toRaw()}" }
                null
            }
                ?.toMutableVmStack()
                ?.let {
                    try {
                        RoyaltyContract(
                            numerator = it.popNumber().toInt(),
                            denominator = it.popNumber().toInt(),
                            destination = it.popSlice().loadTlb(MsgAddress),
                        )
                    } catch (e: ClassCastException) {
                        logger.warn(e) { "could not get royalty information for address=${address.toRaw()}" }
                        null
                    }
                }
    }
}
