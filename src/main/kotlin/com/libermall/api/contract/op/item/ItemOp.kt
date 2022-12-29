package com.libermall.api.contract.op.item

import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbCombinator

sealed interface ItemOp {
    companion object : TlbCodec<ItemOp> by ItemOpCombinator
}

private object ItemOpCombinator : TlbCombinator<ItemOp>(
    ItemOp::class,
    TransferOp::class to TransferOp,
)
