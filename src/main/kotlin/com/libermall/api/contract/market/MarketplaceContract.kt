package com.libermall.api.contract.market

import com.libermall.api.contract.nft.SaleContract
import mu.KLogging
import org.ton.bigint.BigInt
import org.ton.block.*
import org.ton.boc.BagOfCells
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.storeRef
import org.ton.crypto.hex
import org.ton.tlb.storeTlb

data class MarketplaceContract(
    val owner: MsgAddress,
    val authorizationPublicKey: ByteArray,
    val saleCode: Cell = SaleContract.CODE,
    val saleDeployAmount: BigInt,
    val transferAmount: BigInt,
    val code: Cell = CODE,
    val workchain: Int = 0,
) {
    fun createStateInit() = StateInit(
        code = code,
        data = CellBuilder.createCell {
            storeUInt(0, 1) // Uninitialized
            storeTlb(MsgAddress, owner)
            storeBytes(authorizationPublicKey)
            storeRef(saleCode)
            storeRef {
                storeTlb(Coins, Coins.ofNano(saleDeployAmount))
                storeTlb(Coins, Coins.ofNano(transferAmount))
            }
        }
    )

    fun address() = AddrStd(workchain, CellBuilder.createCell { storeTlb(StateInit, createStateInit()) }.hash())

    fun externalInitMessage() = Message(
        info = ExtInMsgInfo(dest = address()),
        init = createStateInit(),
        body = Cell.of()
    )

    companion object : KLogging() {
        val CODE =
            BagOfCells(hex("B5EE9C724102060100013C000114FF00F4A413F4BCF2C80B01020120020301DCD23221C700915BE0D0D3030171B0915BE0FA4030ED44D0D300FA40D3FFD4D4303405D31FD33F5364C7058E30313234238101A4BA8E12D3FF71C8CB005003CF16CBFF14CCCCC9ED5492345BE2810539BA98D4D43001FB04ED549130E2E034821005138D9112BAE3025F06840FF2F0040056F230ED44D0D300FA40D3FFD4D43004C0008E11F80071C8CB005003CF16CBFFCCCCC9ED54E05F04840FF2F001FC02FA4031D4D430D08308D718308200DEAD22F9004004F91012F2F47020C8CB0116F400F40014CB00C920F9007074C8CB02CA07CBFFC9D002D0FA00FA0030768018C8CB0525CF165003FA0212CB6B12CCC971FB00702082105FCC3D14C8CB1F16CB3F23CF165003CF1614CA0021FA02CA00C9718018C8CB055003CF165003050012FA02CB6ACCC971FB00531CEB30")).roots.first()
    }
}
