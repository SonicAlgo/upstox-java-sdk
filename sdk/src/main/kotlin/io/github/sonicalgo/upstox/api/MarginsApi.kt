package io.github.sonicalgo.upstox.api

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants
import io.github.sonicalgo.upstox.exception.UpstoxApiException
import io.github.sonicalgo.upstox.model.common.UpstoxResponse
import io.github.sonicalgo.upstox.model.request.MarginParams
import io.github.sonicalgo.upstox.model.response.MarginResponse
import io.github.sonicalgo.upstox.validation.Validators

/**
 * API module for margin calculations.
 *
 * Provides methods for calculating margin requirements before placing orders.
 *
 * Example usage:
 * ```kotlin
 * val marginsApi = upstox.getMarginsApi()
 *
 * // Calculate margin requirements
 * val margin = marginsApi.getMargin(MarginParams(
 *     instruments = listOf(
 *         MarginInstrument(
 *             instrumentKey = "NSE_EQ|INE669E01016",
 *             quantity = 1,
 *             transactionType = TransactionType.BUY,
 *             product = Product.DELIVERY
 *         )
 *     )
 * ))
 * println("Required Margin: ${margin.requiredMargin}")
 * ```
 *
 * @see <a href="https://upstox.com/developer/api-documentation/margin">Margin API</a>
 * @see ChargesApi for brokerage calculations
 */
class MarginsApi internal constructor(private val apiClient: ApiClient) {

    /**
     * Calculates the margin required for placing orders.
     *
     * Returns margin details for each instrument including SPAN margin,
     * exposure margin, equity margin, and the total required margin.
     * Also calculates margin benefit for hedged positions.
     *
     * Constraints:
     * - Maximum 20 instruments per request
     * - No duplicate instrument keys allowed
     * - Quantity must be greater than zero and multiple of lot size
     *
     * Example:
     * ```kotlin
     * val marginsApi = upstox.getMarginsApi()
     *
     * val margin = marginsApi.getMargin(MarginParams(
     *     instruments = listOf(
     *         MarginInstrument(
     *             instrumentKey = "NSE_FO|NIFTY24JANFUT",
     *             quantity = 50,
     *             transactionType = TransactionType.BUY,
     *             product = Product.INTRADAY
     *         ),
     *         MarginInstrument(
     *             instrumentKey = "NSE_FO|NIFTY24JAN22000CE",
     *             quantity = 50,
     *             transactionType = TransactionType.SELL,
     *             product = Product.INTRADAY
     *         )
     *     )
     * ))
     * println("Required Margin: ${margin.requiredMargin}")
     * println("Final Margin (with benefit): ${margin.finalMargin}")
     * ```
     *
     * @param params Margin calculation parameters with list of instruments
     * @return [MarginResponse] Margin details including per-instrument breakdown and totals
     * @throws UpstoxApiException if the calculation fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/margin">Margin API</a>
     */
    fun getMargin(params: MarginParams): MarginResponse {
        Validators.validateListSize(params.instruments, MAX_MARGIN_INSTRUMENTS, "getMargin")

        val response: UpstoxResponse<MarginResponse> = apiClient.post(
            endpoint = Endpoints.GET_MARGIN,
            body = params,
            overrideBaseUrl = UpstoxConstants.BASE_URL_V2
        )
        return response.dataOrThrow()
    }

    internal object Endpoints {
        const val GET_MARGIN = "/charges/margin"
    }

    companion object {
        /** Maximum number of instruments for margin calculation */
        private const val MAX_MARGIN_INSTRUMENTS = 20
    }
}
