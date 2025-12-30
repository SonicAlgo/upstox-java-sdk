package io.github.sonicalgo.upstox.usecase

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.builder.GenerateBuilder
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.UpstoxConstants.BASE_URL_V2
import io.github.sonicalgo.upstox.common.UpstoxResponse
import io.github.sonicalgo.upstox.common.Product
import io.github.sonicalgo.upstox.common.TransactionType
import io.github.sonicalgo.upstox.validation.Validators

private const val MAX_MARGIN_INSTRUMENTS = 20

/** Gets margin requirements for placing orders. */
@JvmSynthetic
internal fun executeGetMargin(apiClient: ApiClient, instruments: List<MarginInstrumentParams>): MarginResponse {
    Validators.validateListSize(instruments, MAX_MARGIN_INSTRUMENTS, "getMargin")

    val response: UpstoxResponse<MarginResponse> = apiClient.post(
        endpoint = "/charges/margin",
        body = mapOf("instruments" to instruments),
        overrideBaseUrl = BASE_URL_V2
    )
    return response.dataOrThrow()
}

/**
 * Instrument details for margin calculation.
 *
 * @property instrumentKey Key of the instrument
 * @property quantity Quantity (must be a multiple of the lot size)
 * @property transactionType Transaction type: BUY or SELL
 * @property product Product type: INTRADAY, DELIVERY, COVER_ORDER, or MTF
 * @property price Price at which the order is to be placed (optional)
 */
@GenerateBuilder
data class MarginInstrumentParams(
    @JsonProperty("instrument_key")
    val instrumentKey: String,

    @JsonProperty("quantity")
    val quantity: Int,

    @JsonProperty("transaction_type")
    val transactionType: TransactionType,

    @JsonProperty("product")
    val product: Product,

    @JsonProperty("price")
    val price: Double? = null
)

/**
 * Margin calculation response.
 *
 * @property margins Individual margin details for each instrument
 * @property requiredMargin Total margin required to execute all orders
 * @property finalMargin Total margin after applying hedging benefit
 * @see <a href="https://upstox.com/developer/api-documentation/margin">Margin API</a>
 */
data class MarginResponse(
    @JsonProperty("margins")
    val margins: List<InstrumentMargin>?,

    @JsonProperty("required_margin")
    val requiredMargin: Double?,

    @JsonProperty("final_margin")
    val finalMargin: Double?
)

/**
 * Margin details for a single instrument.
 *
 * @property spanMargin Upfront SPAN margin mandatory by exchange
 * @property exposureMargin Exposure margin based on ELM percentage
 * @property equityMargin Margin applicable for equity trades
 * @property netBuyPremium Option premium required for option buying
 * @property additionalMargin Additional margin for MCX FNO trades
 * @property totalMargin Total margin required for this instrument
 * @property tenderMargin Tender margin as futures approach expiration
 */
data class InstrumentMargin(
    @JsonProperty("span_margin")
    val spanMargin: Double?,

    @JsonProperty("exposure_margin")
    val exposureMargin: Double?,

    @JsonProperty("equity_margin")
    val equityMargin: Double?,

    @JsonProperty("net_buy_premium")
    val netBuyPremium: Double?,

    @JsonProperty("additional_margin")
    val additionalMargin: Double?,

    @JsonProperty("total_margin")
    val totalMargin: Double?,

    @JsonProperty("tender_margin")
    val tenderMargin: Double?
)