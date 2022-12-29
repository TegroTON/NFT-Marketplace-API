package com.libermall.api

import com.ionspin.kotlin.bignum.serialization.kotlinx.humanReadableSerializerModule
import com.libermall.api.contract.nft.CollectionContract
import com.libermall.api.contract.nft.ItemContract
import com.libermall.api.contract.nft.RoyaltyContract
import com.libermall.api.contract.nft.SaleContract
import com.libermall.api.controller.CollectionController
import com.libermall.api.controller.ItemController
import com.libermall.api.controller.StaticController
import com.libermall.api.logging.TonLogger
import com.libermall.api.metadata.CollectionMetadata
import com.libermall.api.metadata.ItemMetadata
import com.libermall.api.properties.MarketplaceProperties
import com.libermall.api.repository.ApprovalRepository
import com.libermall.api.repository.CollectionRepository
import com.libermall.api.repository.ItemRepository
import com.libermall.api.repository.RoyaltyRepository
import com.libermall.api.service.EvictionService
import com.libermall.api.service.LiveBlockService
import com.libermall.api.service.ReferenceBlockService
import com.libermall.api.table.ApprovalTable
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.bindEagerSingleton
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.kodein.di.ktor.controller.controller
import org.kodein.di.ktor.di
import org.slf4j.event.Level
import org.ton.api.liteclient.config.LiteClientConfigGlobal
import org.ton.block.MsgAddress
import org.ton.block.MsgAddressInt
import org.ton.lite.client.LiteClient
import java.io.File
import java.util.*
import kotlin.time.Duration.Companion.hours

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    install(ContentNegotiation) {
        json(
            Json {
                serializersModule = humanReadableSerializerModule
            }
        )
    }
    install(Resources)
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
    install(CallId) {
        header(HttpHeaders.XRequestId)
        verify { callId: String ->
            callId.isNotEmpty()
        }
    }

    install(StatusPages) {
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respondRedirect("/#404")
        }
    }

    di {
        bindSingleton { MarketplaceProperties.fromEnvironment(environment) }

        bindSingleton { TonLogger() }
        bindSingleton {
            val config =
                environment.config.propertyOrNull("liteapi.config")?.getString()

            LiteClient(
                Dispatchers.IO + CoroutineName("liteClient"),
                Json {
                    ignoreUnknownKeys = true
                }
                    .decodeFromString<LiteClientConfigGlobal>(requireNotNull(config?.let { File(it).readText() }
                        ?: ClassLoader.getSystemResourceAsStream("testnet-global.config.json")?.readAllBytes()
                            ?.decodeToString()) { "Could not load Lite Api config." })
            )
        }
        bindSingleton {
            HttpClient {
                install(HttpRequestRetry) {
                    maxRetries = 10
                    retryOnServerErrors()
                    retryOnException(retryOnTimeout = true)
                    exponentialDelay()
                }
                install(HttpTimeout) {
                    requestTimeoutMillis = 30_000L
                }
            }
        }

        bindEagerSingleton { ReferenceBlockService(di) }
        bindEagerSingleton { LiveBlockService(di) }
        bindEagerSingleton { EvictionService(di) }

        bindSingleton {
            Database.connect(
                url = environment.config.propertyOrNull("database.url")?.getString()
                    ?: "jdbc:postgresql://localhost:5432/market",
                driver = environment.config.propertyOrNull("database.driver")?.getString()
                    ?: "org.postgresql.Driver",
                user = environment.config.propertyOrNull("database.user")?.getString() ?: "postgres",
                password = environment.config.propertyOrNull("database.password")?.getString() ?: "postgrespw",
            ).also { db ->
                transaction(db) {
                    SchemaUtils.create(ApprovalTable)
                }
            }
        }

        bindSingleton { Cache.Builder().build<MsgAddressInt, Optional<CollectionContract>>() }
        bindSingleton { Cache.Builder().build<MsgAddressInt, Optional<CollectionMetadata>>() }
        bindSingleton {
            Cache.Builder().apply {
                expireAfterWrite(72.hours) // TODO: Evict cache smarter?
            }.build<Pair<MsgAddressInt, ULong>, Optional<MsgAddress>>()
        }
        bindSingleton { Cache.Builder().build<MsgAddressInt, Optional<ItemContract>>() }
        bindSingleton { Cache.Builder().build<MsgAddressInt, Optional<ItemMetadata>>() }
        bindSingleton { Cache.Builder().build<MsgAddressInt, Optional<SaleContract>>() }
        bindSingleton { Cache.Builder().build<MsgAddressInt, Optional<RoyaltyContract>>() }

        bindEagerSingleton { ApprovalRepository(di) }
        bindEagerSingleton {
            CollectionRepository(di).apply {
                CoroutineScope(Dispatchers.IO + CoroutineName("collectionRepositoryStartup")).launch {
                    this@apply.all().collect {}
                }
            }
        }
        bindEagerSingleton {
            ItemRepository(di).apply {
                CoroutineScope(Dispatchers.IO + CoroutineName("itemRepositoryStartup")).launch {
                    this@apply.all().collect {}
                }
            }
        }
        bindEagerSingleton { RoyaltyRepository(di) }
    }

    routing {
        controller("/") { StaticController(instance()) }
        controller("/api/v1") { CollectionController(instance()) }
        controller("/api/v1") { ItemController(instance()) }
    }
}

