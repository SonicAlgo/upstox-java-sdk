package io.github.sonicalgo.upstox.model.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.sonicalgo.upstox.model.enums.Exchange
import io.github.sonicalgo.upstox.model.enums.OrderType
import io.github.sonicalgo.upstox.model.enums.Product

/**
 * User profile information.
 *
 * Contains details about the authenticated user.
 *
 * @property email User's email address
 * @property exchanges List of enabled exchanges (NSE, NFO, BSE, CDS, BFO, BCD)
 * @property products Product types enabled (INTRADAY, DELIVERY, COVER_ORDER, MTF)
 * @property broker Broker identifier
 * @property userId Unique user identifier (UCC)
 * @property userName User's registered name
 * @property orderTypes Supported order types (MARKET, LIMIT, SL, SL_M)
 * @property userType User registration role (typically "individual")
 * @property poa Power of attorney authorization status
 * @property ddpi DDPI (Demat Debit and Pledge Instruction) authorization status
 * @property isActive Account activity status
 * @see <a href="https://upstox.com/developer/api-documentation/get-profile">Get Profile API</a>
 */
data class UserProfile(
    @JsonProperty("email")
    val email: String,

    @JsonProperty("exchanges")
    val exchanges: List<Exchange>,

    @JsonProperty("products")
    val products: List<Product>,

    @JsonProperty("broker")
    val broker: String,

    @JsonProperty("user_id")
    val userId: String,

    @JsonProperty("user_name")
    val userName: String,

    @JsonProperty("order_types")
    val orderTypes: List<OrderType>,

    @JsonProperty("user_type")
    val userType: String,

    @JsonProperty("poa")
    val poa: Boolean,

    @JsonProperty("ddpi")
    val ddpi: Boolean? = null,

    @JsonProperty("is_active")
    val isActive: Boolean
)

/**
 * User funds and margin information.
 *
 * Contains details about available funds across segments.
 *
 * @property equity Equity segment funds and margin details
 * @property commodity Commodity segment funds and margin details
 * @see <a href="https://upstox.com/developer/api-documentation/get-user-fund-margin">Get Funds and Margin API</a>
 */
data class FundsAndMargin(
    @JsonProperty("equity")
    val equity: SegmentMargin? = null,

    @JsonProperty("commodity")
    val commodity: SegmentMargin? = null
)

/**
 * Margin details for a specific segment.
 *
 * @property usedMargin Amount blocked in open positions
 * @property payinAmount Instant deposit amount reflected
 * @property spanMargin Futures/options margin blocked for SPAN
 * @property adhocMargin Manually credited margin by broker
 * @property notionalCash Amount reserved for withdrawals
 * @property availableMargin Total tradable margin balance
 * @property exposureMargin Futures/options margin blocked for exposure
 */
data class SegmentMargin(
    @JsonProperty("used_margin")
    val usedMargin: Double,

    @JsonProperty("payin_amount")
    val payinAmount: Double,

    @JsonProperty("span_margin")
    val spanMargin: Double,

    @JsonProperty("adhoc_margin")
    val adhocMargin: Double,

    @JsonProperty("notional_cash")
    val notionalCash: Double,

    @JsonProperty("available_margin")
    val availableMargin: Double,

    @JsonProperty("exposure_margin")
    val exposureMargin: Double
)
