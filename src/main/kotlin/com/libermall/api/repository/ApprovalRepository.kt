package com.libermall.api.repository

import com.libermall.api.entity.ApprovalEntity
import com.libermall.api.table.ApprovalTable
import com.libermall.api.toBlob
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.ton.block.MsgAddressInt

class ApprovalRepository(override val di: DI) : DIAware {
    private val db: Database by instance()

    fun allApproved() = transaction(db) {
        ApprovalEntity.find { ApprovalTable.approved eq true }.toList()
    }

    fun isApproved(address: MsgAddressInt) = transaction(db) {
        ApprovalEntity.find { ApprovalTable.address eq address.toBlob() }.firstOrNull()?.approved ?: true
    }

    fun isDisapproved(address: MsgAddressInt) = transaction(db) {
        ApprovalEntity.find { (ApprovalTable.address eq address.toBlob()) and (ApprovalTable.approved eq false) }
            .empty().not()
    }
}
