package com.libermall.api.contract.nft

import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.bigint.BigInt
import org.ton.block.AddrStd
import org.ton.block.MsgAddress
import org.ton.block.VmStackValue
import org.ton.cell.Cell
import org.ton.lite.api.liteserver.LiteServerAccountId
import org.ton.lite.client.LiteClient
import org.ton.tlb.loadTlb

data class CollectionContract(
    val next_item_index: ULong,
    val content: Cell,
    val owner: MsgAddress,
) {
    companion object {
        @JvmStatic
        suspend fun of(
            address: AddrStd,
            liteClient: LiteClient,
            referenceBlock: TonNodeBlockIdExt?,
        ): CollectionContract =
            liteClient.runSmcMethod(
                LiteServerAccountId(address),
                referenceBlock ?: liteClient.getLastBlockId(),
                "get_collection_data"
            )
                .toMutableVmStack()
                .let {
                    CollectionContract(
                        next_item_index = it.popNumber().toBigInt().toString().toULong(), // TODO
                        content = it.popCell(),
                        owner = it.popSlice().loadTlb(MsgAddress),
                    )
                }

        @JvmStatic
        suspend fun itemAddressOf(
            collection: AddrStd,
            index: ULong,
            liteClient: LiteClient,
            referenceBlock: TonNodeBlockIdExt?,
        ): MsgAddress =
            liteClient.runSmcMethod(
                LiteServerAccountId(collection),
                referenceBlock ?: liteClient.getLastBlockId(),
                "get_nft_address_by_index",
                VmStackValue(BigInt(index.toString())) // TODO
            )
                .toMutableVmStack()
                .popSlice()
                .loadTlb(MsgAddress)

        @JvmStatic
        suspend fun itemContent(
            collection: AddrStd,
            index: ULong,
            individualContent: Cell,
            liteClient: LiteClient,
            referenceBlock: TonNodeBlockIdExt?,
        ): Cell =
            liteClient.runSmcMethod(
                LiteServerAccountId(collection),
                referenceBlock ?: liteClient.getLastBlockId(),
                "get_nft_content",
                VmStackValue(BigInt(index.toString())), // TODO
                VmStackValue(individualContent)
            ).toMutableVmStack().popCell()
    }
}
