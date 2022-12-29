package com.libermall.api.contract.op.collection

import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbCombinator

sealed interface CollectionOp {
    companion object : TlbCodec<CollectionOp> by CollectionOpCombinator
}

private object CollectionOpCombinator : TlbCombinator<CollectionOp>(
    CollectionOp::class,
    DeployItemOp::class to DeployItemOp,
    ChangeOwnerOp::class to ChangeOwnerOp,
)
