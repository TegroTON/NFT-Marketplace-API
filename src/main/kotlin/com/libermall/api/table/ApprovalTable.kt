package com.libermall.api.table

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ApprovalTable : LongIdTable() {
    val address = blob("address").uniqueIndex()
    val approved = bool("approved").default(false)
    val timestamp = timestamp("timestamp").defaultExpression(CurrentTimestamp())
}
