package io.github.sonicalgo.upstox.api

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants
import io.github.sonicalgo.upstox.exception.UpstoxApiException
import io.github.sonicalgo.upstox.model.common.UpstoxResponse
import io.github.sonicalgo.upstox.model.request.BrokerageParams
import io.github.sonicalgo.upstox.model.response.BrokerageResponse
import io.github.sonicalgo.upstox.util.toQueryParams

/**
 * API module for brokerage charges calculations.
 *
 * Provides methods for calculating brokerage charges before placing orders.
 *
 * Example usage:
 * ```kotlin
 * val chargesApi = upstox.getChargesApi()
 *
 * // Calculate brokerage for a trade
 * val brokerage = chargesApi.getBrokerage(BrokerageParams(
 *     instrumentToken = "NSE_EQ|INE669E01016",
 *     quantity = 10,
 *     product = Product.DELIVERY,
 *     transactionType = TransactionType.BUY,
 *     price = 100.0
 * ))
 * println("Total Charges: ${brokerage.charges.total}")
 * ```
 *
 * @see <a href="https://upstox.com/developer/api-documentation/get-brokerage">Get Brokerage API</a>
 * @see MarginsApi for margin calculations
 */
class ChargesApi internal constructor(private val apiClient: ApiClient) {

    /**
     * Calculates the brokerage and other charges for a potential trade.
     *
     * Returns [BrokerageResponse] a detailed breakdown of all charges including brokerage,
     * taxes (GST, STT, stamp duty), and other charges (transaction,
     * clearing, SEBI turnover).
     *
     * Example:
     * ```kotlin
     * val chargesApi = upstox.getChargesApi()
     *
     * val brokerage = chargesApi.getBrokerage(BrokerageParams(
     *     instrumentToken = "NSE_EQ|INE669E01016",
     *     quantity = 10,
     *     product = Product.DELIVERY,
     *     transactionType = TransactionType.BUY,
     *     price = 100.0
     * ))
     * println("Brokerage: ${brokerage.charges.brokerage}")
     * println("GST: ${brokerage.charges.taxes.gst}")
     * println("STT: ${brokerage.charges.taxes.stt}")
     * println("Total: ${brokerage.charges.total}")
     * ```
     *
     * @param params Brokerage calculation parameters
     * @return [BrokerageResponse] Brokerage charges breakdown
     * @throws UpstoxApiException if the calculation fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-brokerage">Get Brokerage API</a>
     */
    fun getBrokerage(params: BrokerageParams): BrokerageResponse {
        val response: UpstoxResponse<BrokerageResponse> = apiClient.get(
            endpoint = Endpoints.GET_BROKERAGE,
            queryParams = toQueryParams(params),
            overrideBaseUrl = UpstoxConstants.BASE_URL_V2
        )
        return response.dataOrThrow()
    }

    internal object Endpoints {
        const val GET_BROKERAGE = "/charges/brokerage"
    }
}
