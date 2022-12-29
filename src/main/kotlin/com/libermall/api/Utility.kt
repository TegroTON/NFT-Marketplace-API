package com.libermall.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.take
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.ton.bigint.BigInt
import org.ton.block.*
import org.ton.boc.BagOfCells
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.crypto.base64
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb

fun MsgAddressInt.toBlob() =
    ExposedBlob(BagOfCells(CellBuilder.createCell { storeTlb(MsgAddressInt, this@toBlob) }).toByteArray())

fun ExposedBlob.toMsgAddressInt() =
    BagOfCells(this.bytes).roots.first().parse { loadTlb(MsgAddressInt) }

fun BigInt.toBigInteger() =
    com.ionspin.kotlin.bignum.integer.BigInteger.parseString(this.toString()) // TODO

fun <T> Flow<T>.dropTake(drop: Int?, take: Int?): Flow<T> =
    this.drop(maxOf(drop ?: 0, 0))
        .take(minOf(maxOf(take ?: 16, 0), FLOW_DROPTAKE_TAKE_MAX))

const val FLOW_DROPTAKE_TAKE_MAX = 128

fun MsgAddressInt.toRaw() = when (this) {
    is AddrStd -> "$workchain_id:$address"
    is AddrVar -> "$workchain_id:$address"
}.lowercase()

fun MsgAddress.toRaw() = when (this) {
    is MsgAddressInt -> this.toRaw()
    else -> null
}

fun MsgAddress.toShortFriendly() = when (this) {
    is AddrStd -> this.toString(userFriendly = true, urlSafe = true, bounceable = true).let {
        it.take(4) + "..." + it.takeLast(5)
    }

    else -> null
}

fun Cell.toBase64() = base64(BagOfCells(this).toByteArray())

fun Block.accountBlockAddresses() =
    this.extra.account_blocks.value.toMap()
        .keys
        .map { AddrStd(this.info.shard.workchain_id, it.account_addr) }
