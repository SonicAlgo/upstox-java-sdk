package io.github.sonicalgo.upstox.api

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants
import io.github.sonicalgo.upstox.exception.UpstoxApiException
import io.github.sonicalgo.upstox.model.common.UpstoxResponse
import io.github.sonicalgo.upstox.model.response.OptionChainEntry
import io.github.sonicalgo.upstox.model.response.OptionContract

/**
 * API module for option chain data.
 *
 * Provides methods for retrieving option contracts and put/call option chains.
 *
 * Example usage:
 * ```kotlin
 * val optionChainApi = upstox.getOptionChainApi()
 *
 * // Get option contracts
 * val contracts = optionChainApi.getOptionContracts("NSE_INDEX|Nifty 50")
 * contracts.forEach { contract ->
 *     println("${contract.tradingSymbol}: Strike=${contract.strikePrice} Type=${contract.instrumentType}")
 * }
 *
 * // Get option chain
 * val chain = optionChainApi.getOptionChain("NSE_INDEX|Nifty 50", "2024-03-28")
 * chain.forEach { entry ->
 *     println("Strike: ${entry.strikePrice}")
 *     println("  Call: LTP=${entry.callOptions.marketData.ltp} IV=${entry.callOptions.optionGreeks.iv}")
 *     println("  Put: LTP=${entry.putOptions.marketData.ltp} IV=${entry.putOptions.optionGreeks.iv}")
 * }
 * ```
 *
 * @see <a href="https://upstox.com/developer/api-documentation/get-option-contracts">Option Contracts API</a>
 */
class OptionChainApi internal constructor(private val apiClient: ApiClient) {

    /**
     * Gets option contracts for an underlying instrument.
     *
     * Returns all available option contracts for the specified underlying.
     * Optionally filter by expiry date.
     *
     * Example - Get all contracts:
     * ```kotlin
     * val optionChainApi = upstox.getOptionChainApi()
     *
     * val contracts = optionChainApi.getOptionContracts("NSE_INDEX|Nifty 50")
     * contracts.forEach { contract ->
     *     println("${contract.tradingSymbol}")
     *     println("  Strike: ${contract.strikePrice}")
     *     println("  Type: ${contract.instrumentType}")
     *     println("  Expiry: ${contract.expiry}")
     *     println("  Lot Size: ${contract.lotSize}")
     * }
     * ```
     *
     * Example - Filter by expiry:
     * ```kotlin
     * val filteredContracts = optionChainApi.getOptionContracts(
     *     instrumentKey = "NSE_INDEX|Nifty 50",
     *     expiryDate = "2024-03-28"
     * )
     * ```
     *
     * @param instrumentKey Key of the underlying instrument
     * @param expiryDate Optional expiry date filter in YYYY-MM-DD format
     * @return List of [OptionContract] entries
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-option-contracts">Option Contracts API</a>
     */
    @JvmOverloads
    fun getOptionContracts(instrumentKey: String, expiryDate: String? = null): List<OptionContract> {
        val queryParams = mutableMapOf<String, String?>("instrument_key" to instrumentKey)
        expiryDate?.let { queryParams["expiry_date"] = it }

        val response: UpstoxResponse<List<OptionContract>> = apiClient.get(
            endpoint = Endpoints.GET_OPTION_CONTRACTS,
            queryParams = queryParams,
            overrideBaseUrl = UpstoxConstants.BASE_URL_V2
        )
        return response.dataOrThrow()
    }

    /**
     * Gets the put/call option chain for an underlying.
     *
     * Returns comprehensive option chain data including market data
     * and Greeks for both call and put options at each strike price.
     *
     * Note: Not available for MCX Exchange.
     *
     * Example:
     * ```kotlin
     * val optionChainApi = upstox.getOptionChainApi()
     *
     * val chain = optionChainApi.getOptionChain(
     *     instrumentKey = "NSE_INDEX|Nifty 50",
     *     expiryDate = "2024-03-28"
     * )
     * chain.forEach { entry ->
     *     println("Strike: ${entry.strikePrice}")
     *     println("Underlying: ${entry.underlyingSpotPrice}")
     *     println("PCR: ${entry.pcr}")
     *
     *     // Call option data
     *     val call = entry.callOptions
     *     println("  CALL:")
     *     println("    LTP: ${call.marketData.ltp}")
     *     println("    OI: ${call.marketData.oi}")
     *     println("    IV: ${call.optionGreeks.iv}")
     *     println("    Delta: ${call.optionGreeks.delta}")
     *
     *     // Put option data
     *     val put = entry.putOptions
     *     println("  PUT:")
     *     println("    LTP: ${put.marketData.ltp}")
     *     println("    OI: ${put.marketData.oi}")
     *     println("    IV: ${put.optionGreeks.iv}")
     *     println("    Delta: ${put.optionGreeks.delta}")
     * }
     * ```
     *
     * @param instrumentKey Key of the underlying instrument
     * @param expiryDate Expiry date in YYYY-MM-DD format
     * @return List of [OptionChainEntry] for each strike price
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-pc-option-chain">Option Chain API</a>
     */
    fun getOptionChain(instrumentKey: String, expiryDate: String): List<OptionChainEntry> {
        val response: UpstoxResponse<List<OptionChainEntry>> = apiClient.get(
            endpoint = Endpoints.GET_OPTION_CHAIN,
            queryParams = mapOf("instrument_key" to instrumentKey, "expiry_date" to expiryDate),
            overrideBaseUrl = UpstoxConstants.BASE_URL_V2
        )
        return response.dataOrThrow()
    }

    internal object Endpoints {
        const val GET_OPTION_CONTRACTS = "/option/contract"
        const val GET_OPTION_CHAIN = "/option/chain"
    }
}
