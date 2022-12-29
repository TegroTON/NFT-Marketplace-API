package com.libermall.api.entity

import com.libermall.api.table.ApprovalTable
import com.libermall.api.toBlob
import com.libermall.api.toMsgAddressInt
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ApprovalEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ApprovalEntity>(ApprovalTable)

    var address by ApprovalTable.address.transform({ it.toBlob() }, { it.toMsgAddressInt() })
    var approved by ApprovalTable.approved
    var timestamp by ApprovalTable.timestamp
}
