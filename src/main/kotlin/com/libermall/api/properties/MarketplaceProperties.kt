package com.libermall.api.properties

import io.ktor.server.application.*
import mu.KLogging
import org.ton.bigint.BigInt
import org.ton.block.Coins
import org.ton.block.MsgAddressInt
import org.ton.crypto.base64

data class MarketplaceProperties(
    val testnet: Boolean,

    val address: MsgAddressInt,

    val authorizationPrivateKey: ByteArray = base64("+Oag5l7EVtrqAPlV3CMGUxWNr4uMn5p6h5eWGwK9xcc="),

    val serviceFeeNumerator: Int = 5,

    val serviceFeeDenominator: Int = 100,

    val saleFee: Coins = Coins.ofNano(200_000_000),

    val transferFee: Coins = Coins.ofNano(100_000_000),

    val networkFee: Coins = Coins.ofNano(100_000_000),

    val gasFee: Coins = Coins.ofNano(1_000_000_000),
) {
    companion object : KLogging() {
        @JvmStatic
        fun fromEnvironment(environment: ApplicationEnvironment): MarketplaceProperties {
            val testnet =
                requireNotNull(environment.config.propertyOrNull("marketplace.testnet")?.getString()?.toBoolean())
            val marketplaceAddress =
                MsgAddressInt(requireNotNull(environment.config.propertyOrNull("marketplace.address")?.getString()))

            return MarketplaceProperties(
                testnet = testnet,
                address = marketplaceAddress,
                authorizationPrivateKey = environment.config.propertyOrNull("marketplace.authorization")
                    ?.getString()
                    ?.let { base64(it) }
                    ?: MarketplaceProperties(testnet, marketplaceAddress).authorizationPrivateKey,
                serviceFeeNumerator = environment.config.propertyOrNull("marketplace.fee.service.numerator")
                    ?.getString()
                    ?.toInt()
                    ?: MarketplaceProperties(testnet, marketplaceAddress).serviceFeeNumerator,
                serviceFeeDenominator = environment.config.propertyOrNull("marketplace.fee.service.denominator")
                    ?.getString()
                    ?.toInt()
                    ?: MarketplaceProperties(testnet, marketplaceAddress).serviceFeeDenominator,
                saleFee = environment.config.propertyOrNull("marketplace.fee.sale")?.getString()
                    ?.let { Coins.ofNano(BigInt(it)) }
                    ?: MarketplaceProperties(testnet, marketplaceAddress).saleFee,
                transferFee = environment.config.propertyOrNull("marketplace.fee.transfer")?.getString()
                    ?.let { Coins.ofNano(BigInt(it)) }
                    ?: MarketplaceProperties(testnet, marketplaceAddress).transferFee,
                networkFee = environment.config.propertyOrNull("marketplace.fee.network")?.getString()
                    ?.let { Coins.ofNano(BigInt(it)) }
                    ?: MarketplaceProperties(testnet, marketplaceAddress).networkFee,
                gasFee = environment.config.propertyOrNull("marketplace.fee.gas")?.getString()
                    ?.let { Coins.ofNano(BigInt(it)) }
                    ?: MarketplaceProperties(testnet, marketplaceAddress).gasFee,
            ).also {
                logger.info { "Marketplace Properties: $it" }
            }
        }
    }
}
