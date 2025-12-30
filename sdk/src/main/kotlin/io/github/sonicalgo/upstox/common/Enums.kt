package io.github.sonicalgo.upstox.common

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
    NSCOM;

    companion object {
        /** Returns the Exchange for the given name, or null if not found. */
        @JvmStatic
        fun fromName(name: String): Exchange? = entries.find { it.name == name }
    }
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
    MCX_FO;

    companion object {
        /** Returns the Segment for the given name, or null if not found. */
        @JvmStatic
        fun fromName(name: String): Segment? = entries.find { it.name == name }
    }
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
    @JsonProperty("MTF")
    MTF;

    companion object {
        /** Returns the Product for the given name, or null if not found. */
        @JvmStatic
        fun fromName(name: String): Product? = entries.find { it.name == name }
    }
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
    SL_M;

    companion object {
        /** Returns the OrderType for the given name, or null if not found. */
        @JvmStatic
        fun fromName(name: String): OrderType? = entries.find { it.name == name }
    }
}

/**
 * Transaction type for orders.
 */
enum class TransactionType {
    /** Buy transaction */
    BUY,

    /** Sell transaction */
    SELL;

    companion object {
        /** Returns the TransactionType for the given name, or null if not found. */
        @JvmStatic
        fun fromName(name: String): TransactionType? = entries.find { it.name == name }
    }
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
    IOC;

    companion object {
        /** Returns the Validity for the given name, or null if not found. */
        @JvmStatic
        fun fromName(name: String): Validity? = entries.find { it.name == name }
    }
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
    PUT_ORDER_REQ_RECEIVED;

    companion object {
        /** Returns the OrderStatus for the given name, or null if not found. */
        @JvmStatic
        fun fromName(name: String): OrderStatus? = entries.find { it.name == name }
    }
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
    ONE_CANCELS_OTHER;

    companion object {
        /** Returns the OrderVariety for the given name, or null if not found. */
        @JvmStatic
        fun fromName(name: String): OrderVariety? = entries.find { it.name == name }
    }
}

/**
 * GTT (Good Till Triggered) order types.
 */
enum class GttType {
    /** Single rule GTT order */
    SINGLE,

    /** Multiple rules GTT order */
    MULTIPLE;

    companion object {
        /** Returns the GttType for the given name, or null if not found. */
        @JvmStatic
        fun fromName(name: String): GttType? = entries.find { it.name == name }
    }
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
    STOP_LOSS;

    companion object {
        /** Returns the GttStrategy for the given name, or null if not found. */
        @JvmStatic
        fun fromName(name: String): GttStrategy? = entries.find { it.name == name }
    }
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
    IMMEDIATE;

    companion object {
        /** Returns the GttTriggerType for the given name, or null if not found. */
        @JvmStatic
        fun fromName(name: String): GttTriggerType? = entries.find { it.name == name }
    }
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

    companion object {
        /** Returns the CandleUnit for the given name, or null if not found. */
        @JvmStatic
        fun fromName(name: String): CandleUnit? = entries.find { it.name == name }
    }
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
    MUTUAL_FUNDS;

    companion object {
        /** Returns the TradeSegment for the given name, or null if not found. */
        @JvmStatic
        fun fromName(name: String): TradeSegment? = entries.find { it.name == name }
    }
}
