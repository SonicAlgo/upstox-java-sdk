package io.github.sonicalgo.upstox.api

import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants
import io.github.sonicalgo.upstox.exception.UpstoxApiException
import io.github.sonicalgo.upstox.model.request.ConvertPositionParams
import io.github.sonicalgo.upstox.model.response.ConvertPositionResponse
import io.github.sonicalgo.upstox.model.response.Holding
import io.github.sonicalgo.upstox.model.response.Position

/**
 * API module for portfolio operations.
 *
 * Provides methods for managing positions and holdings.
 *
 * Example usage:
 * ```kotlin
 * val upstox = Upstox.getInstance()
 *
 * // Get positions
 * val positions = upstox.getPortfolioApi().getPositions()
 * positions.forEach { position ->
 *     println("${position.tradingSymbol}: ${position.quantity} @ ${position.averagePrice}")
 *     println("P&L: ${position.pnl}")
 * }
 *
 * // Get holdings
 * val holdings = upstox.getPortfolioApi().getHoldings()
 * holdings.forEach { holding ->
 *     println("${holding.tradingSymbol}: ${holding.quantity} shares")
 * }
 * ```
 *
 * @see <a href="https://upstox.com/developer/api-documentation/get-positions">Get Positions API</a>
 * @see <a href="https://upstox.com/developer/api-documentation/get-holdings">Get Holdings API</a>
 */
class PortfolioApi private constructor() {

    /**
     * Gets all current positions.
     *
     * Retrieves short-term positions including intraday and delivery positions.
     * Positions remain until sold or reach their expiration date.
     *
     * Example:
     * ```kotlin
     * val portfolioApi = upstox.getPortfolioApi()
     *
     * val positions = portfolioApi.getPositions()
     * positions.forEach { position ->
     *     println("${position.tradingSymbol}")
     *     println("  Quantity: ${position.quantity}")
     *     println("  Avg Price: ${position.averagePrice}")
     *     println("  LTP: ${position.lastPrice}")
     *     println("  P&L: ${position.pnl}")
     *     println("  Unrealized: ${position.unrealised}")
     *     println("  Realized: ${position.realised}")
     * }
     * ```
     *
     * @return List<[Position]> entries
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-positions">Get Positions API</a>
     */
    fun getPositions(): List<Position> {
        return ApiClient.get(
            endpoint = Endpoints.GET_POSITIONS,
            baseUrl = UpstoxConstants.BASE_URL_V2
        )
    }

    /**
     * Gets MTF (Margin Trading Facility) positions.
     *
     * Retrieves positions held under the margin trading facility.
     * MTF positions are only available on NSE.
     *
     * Example:
     * ```kotlin
     * val portfolioApi = upstox.getPortfolioApi()
     *
     * val mtfPositions = portfolioApi.getMtfPositions()
     * mtfPositions.forEach { position ->
     *     println("${position.tradingSymbol}: ${position.quantity} @ ${position.averagePrice}")
     * }
     * ```
     *
     * @return List<[Position]> for MTF
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-mtf-positions">Get MTF Positions API</a>
     */
    fun getMtfPositions(): List<Position> {
        return ApiClient.get(
            endpoint = Endpoints.GET_MTF_POSITIONS,
            baseUrl = UpstoxConstants.BASE_URL_V3
        )
    }

    /**
     * Converts a position from one product type to another.
     *
     * Useful for converting intraday positions to delivery or vice versa.
     *
     * Example:
     * ```kotlin
     * val portfolioApi = upstox.getPortfolioApi()
     *
     * val response = portfolioApi.convertPosition(ConvertPositionParams(
     *     instrumentToken = "NSE_EQ|INE528G01035",
     *     newProduct = Product.D,
     *     oldProduct = Product.I,
     *     transactionType = TransactionType.BUY,
     *     quantity = 1
     * ))
     * println("Conversion status: ${response.status}")
     * ```
     *
     * @param params Position conversion parameters
     * @return [ConvertPositionResponse] Conversion response with status
     * @throws UpstoxApiException if conversion fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/convert-positions">Convert Positions API</a>
     */
    fun convertPosition(params: ConvertPositionParams): ConvertPositionResponse {
        return ApiClient.put(
            endpoint = Endpoints.CONVERT_POSITION,
            body = params,
            baseUrl = UpstoxConstants.BASE_URL_V2
        )
    }

    /**
     * Gets all holdings in the demat account.
     *
     * Retrieves long-term holdings including stocks delivered to demat.
     *
     * Example:
     * ```kotlin
     * val portfolioApi = upstox.getPortfolioApi()
     *
     * val holdings = portfolioApi.getHoldings()
     * holdings.forEach { holding ->
     *     println("${holding.companyName} (${holding.tradingSymbol})")
     *     println("  ISIN: ${holding.isin}")
     *     println("  Quantity: ${holding.quantity}")
     *     println("  Avg Price: ${holding.averagePrice}")
     *     println("  LTP: ${holding.lastPrice}")
     *     println("  P&L: ${holding.pnl}")
     *     println("  Day Change: ${holding.dayChangePercentage}%")
     * }
     * ```
     *
     * @return List<[Holding]> entries
     * @throws UpstoxApiException if retrieval fails
     *
     * @see <a href="https://upstox.com/developer/api-documentation/get-holdings">Get Holdings API</a>
     */
    fun getHoldings(): List<Holding> {
        return ApiClient.get(
            endpoint = Endpoints.GET_HOLDINGS,
            baseUrl = UpstoxConstants.BASE_URL_V2
        )
    }

    internal object Endpoints {
        const val GET_POSITIONS = "/portfolio/short-term-positions"
        const val GET_MTF_POSITIONS = "/portfolio/mtf-positions"
        const val CONVERT_POSITION = "/portfolio/convert-position"
        const val GET_HOLDINGS = "/portfolio/long-term-holdings"
    }

    companion object {
        internal val instance by lazy { PortfolioApi() }
    }
}
