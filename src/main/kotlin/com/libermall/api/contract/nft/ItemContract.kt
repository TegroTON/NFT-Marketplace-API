package com.libermall.api.contract.nft

import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.block.AddrStd
import org.ton.block.MsgAddress
import org.ton.cell.Cell
import org.ton.lite.api.liteserver.LiteServerAccountId
import org.ton.lite.client.LiteClient
import org.ton.tlb.loadTlb

data class ItemContract(
    val initialized: Boolean,
    val index: ULong,
    val collection: MsgAddress,
    val owner: MsgAddress,
    val individual_content: Cell,
) {
    companion object {
        @JvmStatic
        suspend fun of(
            address: AddrStd,
            liteClient: LiteClient,
            referenceBlock: TonNodeBlockIdExt?,
        ): ItemContract =
            liteClient.runSmcMethod(
                LiteServerAccountId(address),
                referenceBlock ?: liteClient.getLastBlockId(),
                "get_nft_data"
            ).toMutableVmStack().let {
                ItemContract(
                    initialized = it.popTinyInt() == -1L,
                    index = it.popTinyInt().toULong(), // TODO
                    collection = it.popSlice().loadTlb(MsgAddress),
                    owner = it.popSlice().loadTlb(MsgAddress),
                    individual_content = it.popCell(),
                )
            }
    }
}
