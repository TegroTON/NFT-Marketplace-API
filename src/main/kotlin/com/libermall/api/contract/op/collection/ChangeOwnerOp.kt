package com.libermall.api.contract.op.collection

import org.ton.block.MsgAddress
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

data class ChangeOwnerOp(
    val query_id: ULong,
    val new_owner: MsgAddress
) : CollectionOp {
    companion object : TlbCodec<ChangeOwnerOp> by ChangeOwnerOpConstructor {
        @JvmStatic
        fun tlbCodec(): TlbConstructor<ChangeOwnerOp> = ChangeOwnerOpConstructor
    }
}

private object ChangeOwnerOpConstructor : TlbConstructor<ChangeOwnerOp>(
    schema = "change_owner#00000003 query_id:uint64 owner:MsgAddress = InternalMsgBody;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: ChangeOwnerOp) {
        cellBuilder.apply {
            storeUInt64(value.query_id)
            storeTlb(MsgAddress, value.new_owner)
        }
    }

    override fun loadTlb(cellSlice: CellSlice): ChangeOwnerOp = cellSlice.run {
        ChangeOwnerOp(
            query_id = loadUInt64(),
            new_owner = loadTlb(MsgAddress),
        )
    }
}
