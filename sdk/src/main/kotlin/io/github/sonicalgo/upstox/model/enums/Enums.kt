package io.github.sonicalgo.upstox.model.enums

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Exchange identifiers supported by Upstox.
 *
 * Example usage:
 * ```kotlin
 * val exchange = Exchange.NSE
 * ```
 *
 * @see <a href="https://upstox.com/developer/api-documentation/appendix/exchange">Exchange Appendix</a>
 */
enum class Exchange {
    /** National Stock Exchange - Equities */
    NSE,

    /** NSE Futures & Options segment */
    NFO,

    /** NSE Currency Derivatives segment */
    CDS,

    /** Bombay Stock Exchange - Equities */
    BSE,

    /** BSE Futures & Options segment */
    BFO,

    /** BSE Currency Derivatives segment */
    BCD,

    /** Multi Commodity Exchange */
    MCX,

    /** NSE Commodity Derivatives segment */
    NSCOM
}

/**
 * Market segment identifiers.
 *
 * Used to specify the trading segment for various API operations.
 *
 * Example usage:
 * ```kotlin
 * val segment = Segment.NSE_FO // For NSE Futures & Options
 * ```
 */
enum class Segment {
    /** NSE Equity segment */
    NSE_EQ,

    /** NSE Index segment */
    NSE_INDEX,

    /** NSE Futures & Options segment */
    NSE_FO,

    /** NSE Currency Derivatives segment */
    NCD_FO,

    /** NSE Commodity segment */
    NSE_COM,

    /** BSE Equity segment */
    BSE_EQ,

    /** BSE Index segment */
    BSE_INDEX,

    /** BSE Futures & Options segment */
    BSE_FO,

    /** BSE Currency Derivatives segment */
    BCD_FO,

    /** MCX Futures & Options segment */
    MCX_FO
}

/**
 * Fund margin segment filter.
 *
 * Used when querying user funds and margin.
 */
enum class FundSegment {
    /** Equity segment (Securities) */
    @JsonProperty("SEC")
    SECURITIES,

    /** Commodity segment */
    @JsonProperty("COM")
    COMMODITY
}

/**
 * Product types for orders.
 *
 * Specifies the type of order product to use.
 *
 * Example usage:
 * ```kotlin
 * val product = Product.DELIVERY // For delivery orders
 * ```
 */
enum class Product {
    /** Intraday - Position squared off on the same day */
    @JsonProperty("I")
    INTRADAY,

    /** Delivery - Position held for multiple days */
    @JsonProperty("D")
    DELIVERY,

    /** Cover Order - Order with stop loss */
    @JsonProperty("CO")
    COVER_ORDER,

    /** Margin Trading Facility - Leveraged delivery */
    MTF
}

/**
 * Order types supported by Upstox.
 *
 * Example usage:
 * ```kotlin
 * val orderType = OrderType.LIMIT
 * ```
 */
enum class OrderType {
    /** Market order - executes at best available price */
    MARKET,

    /** Limit order - executes at specified price or better */
    LIMIT,

    /** Stop Loss Limit order */
    SL,

    /** Stop Loss Market order */
    @JsonProperty("SL-M")
    SL_M
}

/**
 * Transaction type for orders.
 */
enum class TransactionType {
    /** Buy transaction */
    BUY,

    /** Sell transaction */
    SELL
}

/**
 * Order validity types.
 *
 * Specifies how long the order remains active.
 */
enum class Validity {
    /** Day order - valid for the trading day */
    DAY,

    /** Immediate or Cancel - must execute immediately or cancel */
    IOC
}

/**
 * Order status values.
 *
 * Represents the current state of an order.
 *
 * @see <a href="https://upstox.com/developer/api-documentation/appendix/order-status">Order Status Appendix</a>
 */
enum class OrderStatus {
    /** Order has been validated */
    @JsonProperty("validation pending")
    VALIDATION_PENDING,

    /** Order is open/pending */
    @JsonProperty("open pending")
    OPEN_PENDING,

    /** Order is open */
    @JsonProperty("open")
    OPEN,

    /** Order is being modified */
    @JsonProperty("modify pending")
    MODIFY_PENDING,

    /** Order modification validated */
    @JsonProperty("modify validation pending")
    MODIFY_VALIDATION_PENDING,

    /** Order is being cancelled */
    @JsonProperty("cancel pending")
    CANCEL_PENDING,

    /** Order has been triggered */
    @JsonProperty("trigger pending")
    TRIGGER_PENDING,

    /** Order completed successfully */
    @JsonProperty("complete")
    COMPLETE,

    /** Order was rejected */
    @JsonProperty("rejected")
    REJECTED,

    /** Order was cancelled */
    @JsonProperty("cancelled")
    CANCELLED,

    /** After Market Order - received */
    @JsonProperty("after market order req received")
    AMO_REQ_RECEIVED,

    /** After Market Order modification request received */
    @JsonProperty("modify after market order req received")
    MODIFY_AMO_REQ_RECEIVED,

    /** After Market Order was cancelled */
    @JsonProperty("cancelled after market order")
    CANCELLED_AMO,

    /** Not cancelled */
    @JsonProperty("not cancelled")
    NOT_CANCELLED,

    /** Not modified */
    @JsonProperty("not modified")
    NOT_MODIFIED,

    /** Modified */
    @JsonProperty("modified")
    MODIFIED,

    /** Put Order Request Received */
    @JsonProperty("put order req received")
    PUT_ORDER_REQ_RECEIVED
}

/**
 * Order variety classification.
 */
enum class OrderVariety {
    /** Simple order */
    SIMPLE,

    /** After market order */
    @JsonProperty("AMO")
    AFTER_MARKET_ORDER,

    /** Cover order */
    @JsonProperty("CO")
    COVER_ORDER,

    /** One Cancels Other order */
    @JsonProperty("OCO")
    ONE_CANCELS_OTHER
}

/**
 * GTT (Good Till Triggered) order types.
 */
enum class GttType {
    /** Single rule GTT order */
    SINGLE,

    /** Multiple rules GTT order */
    MULTIPLE
}

/**
 * GTT order strategy types.
 */
enum class GttStrategy {
    /** Entry strategy - triggers when price is reached */
    ENTRY,

    /** Target strategy - for profit booking */
    TARGET,

    /** Stop loss strategy - for loss prevention */
    @JsonProperty("STOPLOSS")
    STOP_LOSS
}

/**
 * GTT trigger types.
 */
enum class GttTriggerType {
    /** Trigger when price goes above the trigger price */
    ABOVE,

    /** Trigger when price goes below the trigger price */
    BELOW,

    /** Trigger immediately */
    IMMEDIATE
}

/**
 * GTT rule status values.
 *
 * @see <a href="https://upstox.com/developer/api-documentation/get-gtt-order-details">GTT Order Details API</a>
 */
enum class GttRuleStatus {
    /** Rule is scheduled and waiting for trigger */
    SCHEDULED,

    /** Rule has been triggered */
    TRIGGERED,

    /** Rule execution failed */
    FAILED,

    /** Rule was cancelled */
    CANCELLED,

    /** Rule has expired */
    EXPIRED,

    /** Rule is open */
    OPEN,

    /** Rule has been completed */
    COMPLETED,

    /** Rule is pending */
    PENDING,

    /** Rule is inactive */
    INACTIVE
}

/**
 * Time unit for historical candle API.
 */
enum class CandleUnit {
    /** Minutes timeframe */
    @JsonProperty("minutes")
    MINUTES,

    /** Hours timeframe */
    @JsonProperty("hours")
    HOURS,

    /** Days timeframe */
    @JsonProperty("days")
    DAYS,

    /** Weeks timeframe */
    @JsonProperty("weeks")
    WEEKS,

    /** Months timeframe */
    @JsonProperty("months")
    MONTHS;

    override fun toString(): String = name.lowercase()
}

/**
 * OHLC quote interval options.
 */
enum class OhlcInterval {
    /** 1 day interval */
    @JsonProperty("1d")
    ONE_DAY,

    /** 1 minute interval */
    @JsonProperty("I1")
    ONE_MINUTE,

    /** 30 minute interval */
    @JsonProperty("I30")
    THIRTY_MINUTE;

    override fun toString(): String = when (this) {
        ONE_DAY -> "1d"
        ONE_MINUTE -> "I1"
        THIRTY_MINUTE -> "I30"
    }
}

/**
 * Option type for derivatives.
 */
enum class OptionType {
    /** Call Option */
    @JsonProperty("CE")
    CALL_OPTION,

    /** Put Option */
    @JsonProperty("PE")
    PUT_OPTION
}

/**
 * Underlying asset type for derivatives.
 */
enum class UnderlyingType {
    /** Commodity */
    @JsonProperty("COM")
    COMMODITY,

    /** Index */
    INDEX,

    /** Equity */
    EQUITY,

    /** Currency */
    @JsonProperty("CUR")
    CURRENCY,

    /** Interest Rate Derivative */
    @JsonProperty("IRD")
    INTEREST_RATE_DERIVATIVE
}

/**
 * Instrument type for futures and options.
 */
enum class InstrumentType {
    /** Futures contract */
    @JsonProperty("FUT")
    FUTURES,

    /** Call Option */
    @JsonProperty("CE")
    CALL_OPTION,

    /** Put Option */
    @JsonProperty("PE")
    PUT_OPTION
}

/**
 * Holiday types.
 */
enum class HolidayType {
    /** Settlement holiday - no settlement */
    SETTLEMENT_HOLIDAY,

    /** Trading holiday - markets closed */
    TRADING_HOLIDAY,

    /** Special timing - modified hours */
    SPECIAL_TIMING
}

/**
 * Market status values.
 */
enum class MarketStatus {
    /** Normal trading session open */
    NORMAL_OPEN,

    /** Normal trading session closed */
    NORMAL_CLOSE,

    /** Pre-open session */
    PRE_OPEN_START,

    /** Pre-open session ended */
    PRE_OPEN_END,

    /** Closing session started */
    CLOSING_START,

    /** Closing session ended */
    CLOSING_END
}

/**
 * Trade segment for historical trades and P&L reports.
 */
enum class TradeSegment {
    /** Equity segment */
    @JsonProperty("EQ")
    EQUITY,

    /** Futures & Options segment */
    @JsonProperty("FO")
    FUTURES_OPTIONS,

    /** Commodity segment */
    @JsonProperty("COM")
    COMMODITY,

    /** Currency Derivatives segment */
    @JsonProperty("CD")
    CURRENCY_DERIVATIVES,

    /** Mutual Funds segment */
    @JsonProperty("MF")
    MUTUAL_FUNDS
}

/**
 * Trade type in P&L reports.
 */
enum class TradeType {
    /** Futures trade */
    @JsonProperty("FUT")
    FUTURES,

    /** Options trade */
    @JsonProperty("OPT")
    OPTIONS,

    /** Equity trade */
    @JsonProperty("EQ")
    EQUITY
}

/**
 * Grant type for OAuth token requests.
 */
enum class GrantType {
    /** Authorization code grant */
    @JsonProperty("authorization_code")
    AUTHORIZATION_CODE;

    override fun toString(): String = "authorization_code"
}


/**
 * Types of updates available in Portfolio Stream.
 */
enum class PortfolioUpdateType(val value: String) {
    ORDER("order"),

    GTT_ORDER("gtt_order"),

    POSITION("position"),

    HOLDING("holding")
}
