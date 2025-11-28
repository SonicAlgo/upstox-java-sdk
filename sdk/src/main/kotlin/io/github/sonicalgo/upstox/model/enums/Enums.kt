package io.github.sonicalgo.upstox.model.enums

import com.google.gson.annotations.SerializedName

/**
 * Exchange identifiers supported by Upstox.
 *
 * Example usage:
 * ```kotlin
 * val exchange = Exchange.NSE
 * ```
 */
enum class Exchange {
    /** National Stock Exchange */
    NSE,
    /** Bombay Stock Exchange */
    BSE,
    /** Multi Commodity Exchange */
    MCX
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
    SEC,
    /** Commodity segment */
    COM
}

/**
 * Product types for orders.
 *
 * Specifies the type of order product to use.
 *
 * Example usage:
 * ```kotlin
 * val product = Product.D // For delivery orders
 * ```
 */
enum class Product {
    /** Intraday - Position squared off on the same day */
    I,
    /** Delivery - Position held for multiple days */
    D,
    /** Cover Order - Order with stop loss */
    CO,
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
    @SerializedName("SL-M")
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
 */
enum class OrderStatus {
    /** Order has been validated */
    @SerializedName("validation pending")
    VALIDATION_PENDING,
    /** Order is open/pending */
    @SerializedName("open pending")
    OPEN_PENDING,
    /** Order is open */
    @SerializedName("open")
    OPEN,
    /** Order is being modified */
    @SerializedName("modify pending")
    MODIFY_PENDING,
    /** Order modification validated */
    @SerializedName("modify validation pending")
    MODIFY_VALIDATION_PENDING,
    /** Order is being cancelled */
    @SerializedName("cancel pending")
    CANCEL_PENDING,
    /** Order has been triggered */
    @SerializedName("trigger pending")
    TRIGGER_PENDING,
    /** Order completed successfully */
    @SerializedName("complete")
    COMPLETE,
    /** Order was rejected */
    @SerializedName("rejected")
    REJECTED,
    /** Order was cancelled */
    @SerializedName("cancelled")
    CANCELLED,
    /** After Market Order - received */
    @SerializedName("after market order req received")
    AMO_REQ_RECEIVED,
    /** Not cancelled */
    @SerializedName("not cancelled")
    NOT_CANCELLED,
    /** Not modified */
    @SerializedName("not modified")
    NOT_MODIFIED,
    /** Modified */
    @SerializedName("modified")
    MODIFIED,
    /** Put Order Request Received */
    @SerializedName("put order req received")
    PUT_ORDER_REQ_RECEIVED
}

/**
 * Order variety classification.
 */
enum class OrderVariety {
    /** Simple order */
    SIMPLE,
    /** After market order */
    AMO,
    /** Cover order */
    CO,
    /** One Cancels Other order */
    OCO
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
    STOPLOSS
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
    EXPIRED
}

/**
 * Historical candle interval options for V2 API.
 *
 * Used with the deprecated V2 historical candle APIs.
 */
enum class CandleInterval {
    /** 1 minute candles */
    @SerializedName("1minute")
    ONE_MINUTE,
    /** 30 minute candles */
    @SerializedName("30minute")
    THIRTY_MINUTE,
    /** Daily candles */
    @SerializedName("day")
    DAY,
    /** Weekly candles */
    @SerializedName("week")
    WEEK,
    /** Monthly candles */
    @SerializedName("month")
    MONTH;

    override fun toString(): String = when (this) {
        ONE_MINUTE -> "1minute"
        THIRTY_MINUTE -> "30minute"
        DAY -> "day"
        WEEK -> "week"
        MONTH -> "month"
    }
}

/**
 * Time unit for V3 historical candle API.
 */
enum class CandleUnit {
    /** Minutes timeframe */
    @SerializedName("minutes")
    MINUTES,
    /** Hours timeframe */
    @SerializedName("hours")
    HOURS,
    /** Days timeframe */
    @SerializedName("days")
    DAYS,
    /** Weeks timeframe */
    @SerializedName("weeks")
    WEEKS,
    /** Months timeframe */
    @SerializedName("months")
    MONTHS;

    override fun toString(): String = name.lowercase()
}

/**
 * OHLC quote interval options.
 */
enum class OhlcInterval {
    /** 1 day interval */
    @SerializedName("1d")
    ONE_DAY,
    /** 1 minute interval */
    @SerializedName("I1")
    ONE_MINUTE,
    /** 30 minute interval */
    @SerializedName("I30")
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
    CE,
    /** Put Option */
    PE
}

/**
 * Underlying asset type for derivatives.
 */
enum class UnderlyingType {
    /** Commodity */
    COM,
    /** Index */
    INDEX,
    /** Equity */
    EQUITY,
    /** Currency */
    CUR,
    /** Interest Rate Derivative */
    IRD
}

/**
 * Instrument type for futures.
 */
enum class InstrumentType {
    /** Futures contract */
    FUT,
    /** Call Option */
    CE,
    /** Put Option */
    PE
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
    EQ,
    /** Futures & Options segment */
    FO,
    /** Commodity segment */
    COM,
    /** Currency Derivatives segment */
    CD,
    /** Mutual Funds segment */
    MF
}

/**
 * Trade type in P&L reports.
 */
enum class TradeType {
    /** Futures trade */
    FUT,
    /** Options trade */
    OPT,
    /** Equity trade */
    EQ
}

/**
 * Grant type for OAuth token requests.
 */
enum class GrantType {
    /** Authorization code grant */
    @SerializedName("authorization_code")
    AUTHORIZATION_CODE;

    override fun toString(): String = "authorization_code"
}
