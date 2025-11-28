package io.github.sonicalgo.upstox.model.response

import com.google.gson.annotations.SerializedName

/**
 * User profile information.
 *
 * Contains details about the authenticated user.
 *
 * @property email User's email address
 * @property exchanges List of enabled exchanges (NSE, NFO, BSE, CDS, BFO, BCD)
 * @property products Product types enabled (I, D, CO, MTF)
 * @property broker Broker identifier
 * @property userId Unique user identifier (UCC)
 * @property userName User's registered name
 * @property orderTypes Supported order types (MARKET, LIMIT, SL, SL-M)
 * @property userType User registration role (typically "individual")
 * @property poa Power of attorney authorization status
 * @property ddpi DDPI (Demat Debit and Pledge Instruction) authorization status
 * @property isActive Account activity status
 * @see <a href="https://upstox.com/developer/api-documentation/get-profile">Get Profile API</a>
 */
data class UserProfile(
    val email: String,
    val exchanges: List<String>,
    val products: List<String>,
    val broker: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_name")
    val userName: String,
    @SerializedName("order_types")
    val orderTypes: List<String>,
    @SerializedName("user_type")
    val userType: String,
    val poa: Boolean,
    val ddpi: Boolean? = null,
    @SerializedName("is_active")
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
    val equity: SegmentMargin? = null,
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
    @SerializedName("used_margin")
    val usedMargin: Double,
    @SerializedName("payin_amount")
    val payinAmount: Double,
    @SerializedName("span_margin")
    val spanMargin: Double,
    @SerializedName("adhoc_margin")
    val adhocMargin: Double,
    @SerializedName("notional_cash")
    val notionalCash: Double,
    @SerializedName("available_margin")
    val availableMargin: Double,
    @SerializedName("exposure_margin")
    val exposureMargin: Double
)
