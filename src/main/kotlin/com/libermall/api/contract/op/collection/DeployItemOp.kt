package com.libermall.api.contract.op.collection

import org.ton.block.Coins
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

data class DeployItemOp(
    val query_id: ULong,
    val index: ULong,
    val amount: Coins,
    val content: Cell,
) : CollectionOp {
    companion object : TlbCodec<DeployItemOp> by DeployItemOpConstructor {
        @JvmStatic
        fun tlbCodec(): TlbConstructor<DeployItemOp> = DeployItemOpConstructor
    }
}

private object DeployItemOpConstructor : TlbConstructor<DeployItemOp>(
    schema = "deploy#00000001 query_id:uint64 index:uint64 amount:Coins content:^Cell = InternalMsgBody;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: DeployItemOp) {
        cellBuilder.apply {
            storeUInt64(value.query_id)
            storeUInt64(value.index)
            storeTlb(Coins, value.amount)
            storeRef(value.content)
        }
    }

    override fun loadTlb(cellSlice: CellSlice): DeployItemOp = cellSlice.run {
        DeployItemOp(
            query_id = loadUInt64(),
            index = loadUInt64(),
            amount = loadTlb(Coins),
            content = loadRef(),
        )
    }
}
