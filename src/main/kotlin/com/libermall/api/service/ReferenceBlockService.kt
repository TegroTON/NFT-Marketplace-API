package com.libermall.api.service

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import mu.KLogging
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.lite.client.LiteClient

class ReferenceBlockService(override val di: DI) : DIAware {
    private val liteClient: LiteClient by instance()
    private lateinit var last: TonNodeBlockIdExt

    val data = flow {
        while (currentCoroutineContext().isActive) {
            emit(liteClient.getLastBlockId())
            delay(2_000L)
        }
    }
        .distinctUntilChanged()
        .onEach {
            last = it
        }
        .onEach { logger.debug { "latest masterchain block seqno=${it.seqno}" } }
        .shareIn(CoroutineScope(Dispatchers.IO + CoroutineName("referenceBlockService")), SharingStarted.Eagerly)

    fun last() = last

    companion object : KLogging()
}
