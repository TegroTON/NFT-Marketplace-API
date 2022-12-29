package com.libermall.api.service

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import mu.KLogging
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.ton.api.tonnode.TonNodeBlockId
import org.ton.bigint.BigInt
import org.ton.block.Block
import org.ton.block.ShardDescr
import org.ton.lite.client.LiteClient
import java.util.concurrent.ConcurrentHashMap

class LiveBlockService(override val di: DI) : DIAware {
    private val liteClient: LiteClient by instance()
    private val referenceBlockService: ReferenceBlockService by instance()

    @OptIn(FlowPreview::class)
    val data = referenceBlockService.data
        .mapNotNull {
            try {
                liteClient.getBlock(it)
            } catch (e: Exception) {
                logger.warn(e) { "couldn't get masterchain block seqno=${it.seqno}" }
                null
            }
        }
        .flatMapConcat(::getShardchainBlocks)
        .onEach {
            logger.info { "block workchain=${it.info.shard.workchain_id} seqno=${it.info.seq_no}" }
        }
        .shareIn(CoroutineScope(Dispatchers.IO + CoroutineName("liveBlockService")), SharingStarted.Eagerly, 64)

    private val lastMasterchainShards = ConcurrentHashMap<Int, ShardDescr>()

    private suspend fun getShardchainBlocks(masterchainBlock: Block): Flow<Block> {
        val masterchainShards = masterchainBlock.extra.custom.value?.value?.shard_hashes
            ?.nodes()
            .orEmpty()
            .associate { BigInt(it.first.toByteArray()).toInt() to it.second.nodes().maxBy { it.seq_no } }

        val shardchainBlocks = masterchainShards
            .flatMap { (workchain, shard) ->
                (lastMasterchainShards.getOrDefault(workchain, shard).seq_no + 1u..shard.seq_no)
                    .map { seqno ->
                        TonNodeBlockId(
                            workchain,
                            shard.next_validator_shard.toLong(), /* Shard.ID_ALL */ // TODO
                            seqno.toInt(),
                        )
                    }
            }
            .asFlow()
            .mapNotNull {
                try {
                    liteClient.lookupBlock(it)?.let { liteClient.getBlock(it) }
                } catch (e: Exception) {
                    logger.warn(e) { "couldn't get block workchain=${it.workchain} seqno=${it.seqno}" }
                    null
                }
            }

        lastMasterchainShards.clear()
        lastMasterchainShards.putAll(masterchainShards)

        return flowOf(masterchainBlock).onStart { emitAll(shardchainBlocks) }
    }

    companion object : KLogging()
}
