# Upstox Java SDK

[![Maven Central](https://img.shields.io/maven-central/v/io.github.sonicalgo/upstox-java-sdk)](https://central.sonatype.com/artifact/io.github.sonicalgo/upstox-java-sdk)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-11%2B-blue)](https://www.oracle.com/java/)

Unofficial Kotlin/Java SDK for the [Upstox](https://upstox.com) trading platform. Supports REST APIs and real-time WebSocket streaming.

## Installation

### Gradle (Kotlin DSL)

```kotlin
implementation("io.github.sonicalgo:upstox-java-sdk:1.0.0")
```

### Gradle (Groovy)

```groovy
implementation 'io.github.sonicalgo:upstox-java-sdk:1.0.0'
```

### Maven

```xml
<dependency>
    <groupId>io.github.sonicalgo</groupId>
    <artifactId>upstox-java-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

<details open>
<summary>Java</summary>

```java
import io.github.sonicalgo.upstox.Upstox;
import io.github.sonicalgo.upstox.model.request.*;
import io.github.sonicalgo.upstox.model.response.*;
import java.util.Arrays;

// Get SDK instance and set access token
Upstox upstox = Upstox.getInstance();
upstox.setAccessToken("your-access-token");

// Get user profile
UserProfile profile = upstox.getUserApi().getProfile();
System.out.println("Welcome, " + profile.getUserName());

// Get market quote
Map<String, LtpQuote> quotes = upstox.getMarketQuoteApi().getLtpV3(
    Arrays.asList("NSE_EQ|INE669E01016")
);
System.out.println("LTP: " + quotes.get("NSE_EQ|INE669E01016").getLtp());

// Place an order
var response = upstox.getOrdersApi().placeOrder(new PlaceOrderParams(
    "NSE_EQ|INE669E01016",  // instrumentToken
    1,                      // quantity
    Product.D,              // product (D=Delivery)
    Validity.DAY,           // validity
    100.0,                  // price
    OrderType.LIMIT,        // orderType
    TransactionType.BUY,    // transactionType
    0,                      // disclosedQuantity
    0.0,                    // triggerPrice
    false,                  // isAmo
    null                    // tag
));
System.out.println("Order ID: " + response.getData().getOrderIds().get(0));
```

</details>

<details>
<summary>Kotlin</summary>

```kotlin
import io.github.sonicalgo.upstox.Upstox
import io.github.sonicalgo.upstox.model.request.*
import io.github.sonicalgo.upstox.model.response.*

// Get SDK instance and set access token
val upstox = Upstox.getInstance()
upstox.setAccessToken("your-access-token")

// Get user profile
val profile = upstox.getUserApi().getProfile()
println("Welcome, ${profile.userName}")

// Get market quote
val quotes = upstox.getMarketQuoteApi().getLtpV3(listOf("NSE_EQ|INE669E01016"))
println("LTP: ${quotes["NSE_EQ|INE669E01016"]?.ltp}")

// Place an order
val response = upstox.getOrdersApi().placeOrder(PlaceOrderParams(
    instrumentToken = "NSE_EQ|INE669E01016",
    quantity = 1,
    product = Product.D,
    validity = Validity.DAY,
    price = 100.0,
    orderType = OrderType.LIMIT,
    transactionType = TransactionType.BUY
))
println("Order ID: ${response.data?.orderIds?.first()}")
```

</details>

> **Note:** All code examples below assume you have initialized the SDK as shown above:
> ```kotlin
> val upstox = Upstox.getInstance()
> upstox.setAccessToken("your-access-token")
> ```

## Why This SDK?

- **Modern & Secure** - Built with latest libraries (OkHttp 5.x, Protobuf 4.x) with no known vulnerabilities
- **WebSocket Ready** - Full protobuf parsing built-in with typed callbacks; no manual binary handling needed
- **HFT Optimized** - Uses dedicated HFT endpoints (`api-hft.upstox.com`) for lowest latency order execution
- **Auto-Reconnection** - WebSocket clients automatically reconnect with exponential backoff
- **Simple API** - Clean singleton design: `Upstox.getInstance().getOrdersApi().placeOrder()` instead of complex client setup
- **Type-Safe** - Kotlin data classes with proper types; no raw Maps or Object casting
- **Rich Error Handling** - Exceptions with helpers like `isRateLimitError`, `isAuthenticationError`
- **Latest API Support** - V3 endpoints supported out of the box
- **Sandbox Built-In** - Test orders safely with `upstox.setSandboxMode(true, token)`
- **Thread-Safe** - Designed for concurrent usage in trading applications

## Features

- **15 REST API modules** - Orders, Portfolio, Market Quotes, Historical Data, Option Chain, and more
- **Real-time market data** - WebSocket streaming with protobuf (low latency)
- **Real-time portfolio updates** - Order, position, holding, and GTT updates via WebSocket
- **Sandbox mode** - Test order operations without live execution
- **Automatic reconnection** - WebSocket clients reconnect with exponential backoff
- **Configurable rate limiting** - Automatic retry with exponential backoff for HTTP 429
- **Debug logging** - Optional HTTP request/response logging for troubleshooting
- **Full Kotlin & Java compatibility** - Use from either language

## Table of Contents

- [Authentication](#authentication)
- [REST API Reference](#rest-api-reference)
  - [User & Funds](#user--funds)
  - [Orders](#orders)
  - [GTT Orders](#gtt-orders)
  - [Portfolio](#portfolio)
  - [Market Quotes](#market-quotes)
  - [Historical Data](#historical-data)
  - [Option Chain](#option-chain)
  - [Market Info](#market-info)
  - [Charges & Margins](#charges--margins)
  - [Trade P&L](#trade-pnl)
  - [Instruments](#instruments)
  - [Expired Instruments](#expired-instruments)
- [WebSocket Streaming](#websocket-streaming)
  - [Market Data Feed](#market-data-feed)
  - [Portfolio Stream](#portfolio-stream)
- [Sandbox Mode](#sandbox-mode)
- [Error Handling](#error-handling)
- [Configuration](#configuration)
- [Requirements](#requirements)
- [License](#license)

---

## Authentication

### Getting API Credentials

1. Log in to [Upstox Developer Portal](https://account.upstox.com/developer/apps)
2. Create a new app to get your **API Key** and **API Secret**
3. Set a redirect URI for OAuth callback

### OAuth Flow

```kotlin
val loginApi = upstox.getLoginApi()

// Step 1: Get authorization URL
val authUrl = loginApi.getAuthorizationUrl(AuthorizeParams(
    clientId = "your-api-key",
    redirectUri = "https://yourapp.com/callback",
    state = "optional-state"  // For CSRF protection
))
// Redirect user to authUrl

// Step 2: Exchange authorization code for access token
val tokenResponse = loginApi.getToken(GetTokenParams(
    code = "authorization-code-from-callback",
    clientId = "your-api-key",
    clientSecret = "your-api-secret",
    redirectUri = "https://yourapp.com/callback"
))

// Step 3: Set the access token
upstox.setAccessToken(tokenResponse.accessToken)

// Token is valid for the trading day (until ~3:30 AM next day)
```

### Logout

```kotlin
upstox.getLoginApi().logout()
```

---

## REST API Reference

### User & Funds

```kotlin
val userApi = upstox.getUserApi()

// Get user profile
val profile = userApi.getProfile()
// Returns: userId, userName, email, exchanges, products, orderTypes, isActive

// Get funds and margin (all segments)
val funds = userApi.getFundsAndMargin()

// Get funds for specific segment
val equityFunds = userApi.getFundsAndMargin(FundSegment.SEC)  // Equity
val commodityFunds = userApi.getFundsAndMargin(FundSegment.COM)  // Commodity
```

### Orders

#### Place Order

```kotlin
val response = upstox.getOrdersApi().placeOrder(PlaceOrderParams(
    instrumentToken = "NSE_EQ|INE669E01016",
    quantity = 1,
    product = Product.D,           // D=Delivery, I=Intraday, M=MTF
    validity = Validity.DAY,       // DAY or IOC
    price = 100.0,
    orderType = OrderType.LIMIT,   // LIMIT or MARKET
    transactionType = TransactionType.BUY,
    disclosedQuantity = 0,         // Optional
    triggerPrice = 0.0,            // For SL orders
    isAmo = false,                 // After Market Order
    tag = "my-tag"                 // Optional identifier
))
println("Order IDs: ${response.data?.orderIds}")
println("Latency: ${response.metadata?.latency}ms")
```

#### Place Multiple Orders (up to 25)

```kotlin
val responses = upstox.getOrdersApi().placeMultiOrder(listOf(
    MultiOrderParams(
        instrumentToken = "NSE_EQ|INE669E01016",
        quantity = 1,
        product = Product.D,
        validity = Validity.DAY,
        price = 100.0,
        orderType = OrderType.LIMIT,
        transactionType = TransactionType.BUY,
        correlationId = "order-1"
    ),
    MultiOrderParams(
        instrumentToken = "NSE_EQ|INE002A01018",
        quantity = 1,
        product = Product.I,
        validity = Validity.DAY,
        price = 2500.0,
        orderType = OrderType.LIMIT,
        transactionType = TransactionType.SELL,
        correlationId = "order-2"
    )
))
```

#### Modify Order

```kotlin
val modified = upstox.getOrdersApi().modifyOrder(ModifyOrderParams(
    orderId = "240108010918222",
    quantity = 2,
    validity = Validity.DAY,
    price = 105.0,
    orderType = OrderType.LIMIT,
    disclosedQuantity = 0,
    triggerPrice = 0.0
))
```

#### Cancel Order

```kotlin
val cancelled = upstox.getOrdersApi().cancelOrder("240108010445130")
```

#### Cancel Multiple Orders

```kotlin
val cancelled = upstox.getOrdersApi().cancelMultiOrder(CancelMultiOrderParams(
    segment = Segment.NSE_EQ,
    tag = "my-tag"  // Optional: cancel by tag
))
```

#### Exit All Positions

```kotlin
val exited = upstox.getOrdersApi().exitAllPositions(ExitAllPositionsParams(
    segment = Segment.NSE_FO  // Optional: specific segment
))
```

#### Query Orders

```kotlin
val ordersApi = upstox.getOrdersApi()

// Get all orders for the day
val orderBook = ordersApi.getOrderBook()

// Get specific order details
val order = ordersApi.getOrderDetails("240108010445130")

// Get order history/audit trail
val history = ordersApi.getOrderHistory(orderId = "240108010445130")
// OR by tag
val historyByTag = ordersApi.getOrderHistory(tag = "my-tag")

// Get all trades for the day
val trades = ordersApi.getTrades()

// Get trades for specific order
val orderTrades = ordersApi.getTradesByOrder("240108010445100")

// Get historical trades (last 3 financial years)
val historicalTrades = ordersApi.getHistoricalTrades(HistoricalTradesParams(
    startDate = "2023-04-01",
    endDate = "2024-03-31",
    pageNumber = 1,
    pageSize = 100,
    segment = TradeSegment.EQ
))
```

### GTT Orders

Good Till Triggered orders execute automatically when price conditions are met.

#### Place GTT Order

```kotlin
val gttApi = upstox.getGttOrdersApi()

// Single trigger
val gtt = gttApi.placeGttOrder(PlaceGttOrderParams(
    type = GttType.SINGLE,
    quantity = 1,
    product = Product.D,
    instrumentToken = "NSE_EQ|INE669E01016",
    transactionType = TransactionType.BUY,
    rules = listOf(
        GttRule(
            strategy = GttStrategy.ENTRY,
            triggerType = GttTriggerType.BELOW,
            triggerPrice = 95.0
        )
    )
))

// Multiple triggers with target and stop-loss
val gttOco = gttApi.placeGttOrder(PlaceGttOrderParams(
    type = GttType.MULTIPLE,
    quantity = 1,
    product = Product.D,
    instrumentToken = "NSE_EQ|INE669E01016",
    transactionType = TransactionType.SELL,
    rules = listOf(
        GttRule(GttStrategy.ENTRY, GttTriggerType.ABOVE, 100.0),
        GttRule(GttStrategy.TARGET, GttTriggerType.IMMEDIATE, 110.0),
        GttRule(GttStrategy.STOPLOSS, GttTriggerType.IMMEDIATE, 95.0, trailingGap = 2.0)
    )
))
```

#### Modify GTT Order

```kotlin
val modified = upstox.getGttOrdersApi().modifyGttOrder(ModifyGttOrderParams(
    gttOrderId = "GTT-C25270200137952",
    type = GttType.SINGLE,
    quantity = 2,
    rules = listOf(
        GttRule(GttStrategy.ENTRY, GttTriggerType.BELOW, 90.0)
    )
))
```

#### Cancel GTT Order

```kotlin
val cancelled = upstox.getGttOrdersApi().cancelGttOrder(CancelGttOrderParams(
    gttOrderId = "GTT-C25280200137522"
))
```

#### Get GTT Orders

```kotlin
val gttApi = upstox.getGttOrdersApi()

// Get all GTT orders
val allGtt = gttApi.getGttOrderDetails()

// Get specific GTT order
val gtt = gttApi.getGttOrderDetails("GTT-C25280200071351")
```

### Portfolio

#### Positions

```kotlin
val portfolioApi = upstox.getPortfolioApi()

// Get current positions
val positions = portfolioApi.getPositions()
for (position in positions) {
    println("${position.tradingSymbol}: Qty=${position.quantity}, P&L=${position.pnl}")
}

// Get MTF positions (NSE only)
val mtfPositions = portfolioApi.getMtfPositions()

// Convert position (e.g., intraday to delivery)
val converted = portfolioApi.convertPosition(ConvertPositionParams(
    instrumentToken = "NSE_EQ|INE528G01035",
    newProduct = Product.D,
    oldProduct = Product.I,
    transactionType = TransactionType.BUY,
    quantity = 1
))
```

#### Holdings

```kotlin
val holdings = upstox.getPortfolioApi().getHoldings()
for (holding in holdings) {
    println("${holding.companyName}: Qty=${holding.quantity}, P&L=${holding.pnl}")
}
```

### Market Quotes

#### LTP (Last Traded Price)

```kotlin
// V3 API (recommended)
val ltp = upstox.getMarketQuoteApi().getLtpV3(listOf(
    "NSE_EQ|INE669E01016",
    "NSE_EQ|INE002A01018"
))
// Returns Map<instrumentKey, LtpQuote>
println("LTP: ${ltp["NSE_EQ|INE669E01016"]?.ltp}")
```

#### OHLC Quotes

```kotlin
// V3 API (recommended)
val ohlc = upstox.getMarketQuoteApi().getOhlcQuoteV3(
    instrumentKeys = listOf("NSE_EQ|INE669E01016"),
    interval = OhlcInterval.ONE_DAY  // ONE_DAY, INTRADAY_1MIN, INTRADAY_30MIN
)
```

#### Full Market Quotes

```kotlin
// Get full quotes with depth (max 500 instruments)
val quotes = upstox.getMarketQuoteApi().getFullQuote(listOf(
    "NSE_EQ|INE669E01016",
    "NSE_EQ|INE002A01018"
))
// Contains: ltp, ohlc, volume, oi, circuit limits, 5-level depth
```

#### Option Greeks

```kotlin
// Get Greeks for option instruments (max 50)
val greeks = upstox.getMarketQuoteApi().getOptionGreeks(listOf("NSE_FO|43885"))
// Contains: ltp, iv, delta, gamma, theta, vega
```

### Historical Data

#### Historical Candles

```kotlin
val histApi = upstox.getHistoricalDataApi()

// V3 API (recommended)
val candles = histApi.getHistoricalCandleV3(HistoricalCandleV3Params(
    instrumentKey = "NSE_EQ|INE848E01016",
    unit = CandleUnit.MINUTES,  // MINUTES, HOURS, DAYS, WEEKS, MONTHS
    interval = 15,               // 1-1440 for minutes
    toDate = "2025-01-15",
    fromDate = "2025-01-01"
))
for (candle in candles) {
    println("${candle.timestamp}: O=${candle.open} H=${candle.high} L=${candle.low} C=${candle.close}")
}
```

**Data availability:**
| Unit | Available From | Max Range |
|------|----------------|-----------|
| Minutes (1-15) | Jan 2022 | 1 month |
| Minutes (>15) | Jan 2022 | 1 quarter |
| Hours | Jan 2022 | 1 quarter |
| Days | Jan 2000 | 10 years |
| Weeks/Months | Jan 2000 | Unlimited |

#### Intraday Candles

```kotlin
// Current trading day data
val intraday = upstox.getHistoricalDataApi().getIntradayCandleV3(IntradayCandleV3Params(
    instrumentKey = "NSE_EQ|INE848E01016",
    unit = CandleUnit.MINUTES,
    interval = 1
))
```

### Option Chain

#### Get Option Contracts

```kotlin
val contracts = upstox.getOptionChainApi().getOptionContracts(
    instrumentKey = "NSE_INDEX|Nifty 50",
    expiryDate = "2024-03-28"  // Optional filter
)
// Each: tradingSymbol, strikePrice, instrumentType, expiry, lotSize
```

#### Get Option Chain

```kotlin
val optionApi = upstox.getOptionChainApi()
val chain = optionApi.getOptionChain(
    instrumentKey = "NSE_INDEX|Nifty 50",
    expiryDate = "2024-03-28"
)
for (entry in chain) {
    println("Strike: ${entry.strikePrice}")
    println("  Call: LTP=${entry.callOptions?.marketData?.ltp}, IV=${entry.callOptions?.optionGreeks?.iv}")
    println("  Put: LTP=${entry.putOptions?.marketData?.ltp}, IV=${entry.putOptions?.optionGreeks?.iv}")
}
```

### Market Info

```kotlin
val marketInfoApi = upstox.getMarketInfoApi()

// Get all market holidays
val holidays = marketInfoApi.getMarketHolidays()

// Get specific holiday
val holiday = marketInfoApi.getMarketHoliday("2024-01-26")

// Get market timings for a date
val timings = marketInfoApi.getMarketTimings("2024-01-22")

// Get current market status
val status = marketInfoApi.getMarketStatus("NSE")
// Status: NORMAL_OPEN, NORMAL_CLOSE, PRE_OPEN, CLOSING, etc.
```

### Charges & Margins

#### Brokerage Calculation

```kotlin
val charges = upstox.getChargesApi().getBrokerage(BrokerageParams(
    instrumentToken = "NSE_EQ|INE669E01016",
    quantity = 10,
    product = Product.D,
    transactionType = TransactionType.BUY,
    price = 100.0
))
// Returns: brokerage, GST, STT, stamp duty, total
```

#### Margin Calculation

```kotlin
// Calculate margin for orders (max 20 instruments)
val margin = upstox.getMarginsApi().getMargin(MarginParams(
    instruments = listOf(
        MarginInstrument(
            instrumentKey = "NSE_FO|NIFTY24JANFUT",
            quantity = 50,
            transactionType = TransactionType.BUY,
            product = Product.I
        )
    )
))
// Returns: span, exposure, equity margin, and margin benefit for hedged positions
```

### Trade P&L

```kotlin
val pnlApi = upstox.getTradePnlApi()

// Get report metadata
val metadata = pnlApi.getReportMetadata(TradePnlMetadataParams(
    segment = TradeSegment.EQ,
    financialYear = "2324",
    fromDate = "01-04-2023",
    toDate = "31-03-2024"
))

// Get P&L report (paginated)
val report = pnlApi.getProfitAndLossReport(TradePnlReportParams(
    segment = TradeSegment.EQ,
    financialYear = "2324",
    pageNumber = 1,
    pageSize = 100,
    fromDate = "01-04-2023",
    toDate = "31-03-2024"
))

// Get trade charges breakdown
val charges = pnlApi.getTradeCharges(TradeChargesParams(
    segment = TradeSegment.EQ,
    financialYear = "2324",
    fromDate = "01-04-2023",
    toDate = "31-03-2024"
))
```

### Instruments

Download instrument master data. **No authentication required.**

```kotlin
val instrumentsApi = upstox.getInstrumentsApi()

// Download instruments by type
val nseInstruments = instrumentsApi.getInstruments(InstrumentType.NSE)
val bseInstruments = instrumentsApi.getInstruments(InstrumentType.BSE)
val mcxInstruments = instrumentsApi.getInstruments(InstrumentType.MCX)
val allInstruments = instrumentsApi.getInstruments(InstrumentType.COMPLETE)

// Convenience methods
val nse = instrumentsApi.getNseInstruments()
val bse = instrumentsApi.getBseInstruments()
val all = instrumentsApi.getAllInstruments()

// Other types: SUSPENDED, MTF, NSE_MIS, BSE_MIS

// Get URL only (for custom download)
val url = instrumentsApi.getInstrumentsUrl(InstrumentType.NSE)
```

### Expired Instruments

> **Note:** Requires Upstox Plus subscription.

```kotlin
val expiredApi = upstox.getExpiredInstrumentsApi()

// Get available expiries for an underlying
val expiries = expiredApi.getExpiries("NSE_INDEX|Nifty 50")

// Get expired option contracts
val optionContracts = expiredApi.getExpiredOptionContracts(
    instrumentKey = "NSE_INDEX|Nifty 50",
    expiryDate = "2024-10-03"
)

// Get expired futures
val futureContracts = expiredApi.getExpiredFutureContracts(
    instrumentKey = "NSE_INDEX|Nifty 50",
    expiryDate = "2024-11-27"
)

// Get historical candles for expired instrument
val candles = expiredApi.getExpiredHistoricalCandle(
    ExpiredHistoricalCandleParams(
        expiredInstrumentKey = "NSE_FO|NIFTY22D0117800CE",
        interval = "day",  // 1minute, 3minute, 5minute, 15minute, 30minute, day
        toDate = "2022-11-30",
        fromDate = "2022-11-01"
    )
)
```

---

## WebSocket Streaming

### Market Data Feed

Real-time market data via WebSocket with protobuf encoding (low latency).

```kotlin
val feedClient = upstox.createMarketDataFeedClient()

feedClient.connect(object : MarketDataListener {
    override fun onConnected() {
        // Subscribe with LTPC mode (minimal bandwidth)
        feedClient.subscribe(listOf("NSE_EQ|INE669E01016"))

        // Or subscribe with full market data
        feedClient.subscribe(listOf("NSE_INDEX|Nifty 50"), FeedMode.FULL)
    }

    override fun onLtpcUpdate(instrumentKey: String, tick: LtpcTick) {
        println("$instrumentKey: LTP=${tick.ltp}, LTT=${tick.ltt}")
    }

    override fun onFullFeedUpdate(instrumentKey: String, feed: FullFeedTick) {
        println("$instrumentKey: LTP=${feed.ltp}, Volume=${feed.volume}")
        // Contains: ltp, ohlc, depth (5-30 levels), volume, oi
    }

    override fun onIndexFeedUpdate(instrumentKey: String, feed: IndexFeedTick) {
        println("$instrumentKey: Index=${feed.ltp}")
    }

    override fun onOptionGreeksUpdate(instrumentKey: String, feed: OptionGreeksTick) {
        println("$instrumentKey: IV=${feed.iv}, Delta=${feed.delta}")
    }

    override fun onMarketStatus(status: MarketStatusEvent) {
        println("Market status: ${status.status}")
    }

    override fun onReconnecting(attempt: Int, delayMs: Long) {
        println("Reconnecting (attempt $attempt) in ${delayMs}ms...")
    }

    override fun onReconnected() {
        println("Reconnected! Subscriptions restored.")
    }

    override fun onDisconnected(code: Int, reason: String) {
        println("Disconnected: $reason")
    }

    override fun onError(error: Throwable) {
        println("Error: ${error.message}")
    }
})
```

#### Feed Modes

| Mode | Description | Use Case |
|------|-------------|----------|
| `FeedMode.LTPC` | LTP + Close only | Minimal bandwidth |
| `FeedMode.FULL` | Full data + 5-level depth | Standard trading |
| `FeedMode.OPTION_GREEKS` | Greeks + 1-level depth | Options trading |
| `FeedMode.FULL_D30` | 30-level depth | Requires Upstox Plus |

#### Subscription Management

```kotlin
// Subscribe to instruments
feedClient.subscribe(listOf("NSE_EQ|INE002A01018"), FeedMode.LTPC)

// Unsubscribe
feedClient.unsubscribe(listOf("NSE_EQ|INE669E01016"))

// Change mode for subscribed instruments
feedClient.changeMode(listOf("NSE_EQ|INE002A01018"), FeedMode.FULL)

// Get current subscriptions
val subscriptions = feedClient.getSubscriptions()  // Map<instrumentKey, FeedMode>

// Clear all subscriptions
feedClient.clearSubscriptions()

// Close connection
feedClient.close()
```

### Portfolio Stream

Real-time updates for orders, positions, holdings, and GTT orders.

```kotlin
val portfolioClient = upstox.createPortfolioStreamClient()

portfolioClient.connect(
    listener = object : PortfolioStreamListener {
        override fun onConnected() {
            println("Connected to portfolio stream")
        }

        override fun onOrderUpdate(order: OrderUpdate) {
            println("Order ${order.orderId}: ${order.status}")
        }

        override fun onPositionUpdate(position: PositionUpdate) {
            println("Position ${position.tradingSymbol}: Qty=${position.quantity}")
        }

        override fun onHoldingUpdate(holding: HoldingUpdate) {
            println("Holding ${holding.tradingSymbol}: Qty=${holding.quantity}")
        }

        override fun onGttOrderUpdate(gttOrder: GttOrderUpdate) {
            println("GTT ${gttOrder.gttOrderId}: ${gttOrder.status}")
        }

        override fun onReconnecting(attempt: Int, delayMs: Long) {}
        override fun onReconnected() {}
        override fun onDisconnected(code: Int, reason: String) {}
        override fun onError(error: Throwable) {}
    },
    updateTypes = setOf(
        PortfolioUpdateType.ORDER,
        PortfolioUpdateType.POSITION,
        PortfolioUpdateType.HOLDING,
        PortfolioUpdateType.GTT_ORDER
    ),
    autoReconnect = true
)

// Close when done
portfolioClient.close()
```

---

## Sandbox Mode

Test order operations without executing live trades. When enabled, sandbox mode:
- Uses a **sandbox-specific token** for authentication
- Switches order endpoints to the **sandbox API** (`https://api-sandbox.upstox.com/v3`)

```kotlin
// Enable sandbox mode
upstox.setSandboxMode(enabled = true, token = "your-sandbox-token")

// Orders will be simulated (use ordersApi from earlier)
val ordersApi = upstox.getOrdersApi()
val response = ordersApi.placeOrder(PlaceOrderParams(
    instrumentToken = "NSE_EQ|INE669E01016",
    quantity = 1,
    product = Product.D,
    validity = Validity.DAY,
    price = 100.0,
    orderType = OrderType.LIMIT,
    transactionType = TransactionType.BUY
))
// Order is simulated via sandbox endpoint, not sent to exchange

// Disable sandbox mode for live trading
upstox.setSandboxMode(enabled = false)
```

**Sandbox-enabled endpoints:**
- Place order
- Modify order
- Cancel order
- Place multi-order

---

## Error Handling

```kotlin
import io.github.sonicalgo.upstox.exception.UpstoxApiException

try {
    val ordersApi = upstox.getOrdersApi()
    val order = ordersApi.placeOrder(...)
} catch (e: UpstoxApiException) {
    println("HTTP Status: ${e.httpStatusCode}")
    println("Error Code: ${e.errorCode}")
    println("Message: ${e.message}")
    println("Errors: ${e.errors}")  // For multi-order operations

    // Helper properties
    when {
        e.isRateLimitError -> println("Rate limited (429)")
        e.isAuthenticationError -> println("Auth failed (401)")
        e.isValidationError -> println("Bad request (400)")
        e.isServerError -> println("Server error (5xx)")
        e.isNotFoundError -> println("Not found (404)")
        e.isServiceUnavailableError -> println("Service unavailable (503)")
    }
}
```

### Exception Types

| Exception | Description |
|-----------|-------------|
| `UpstoxApiException` | Base exception for all API errors |
| `UpstoxPlusRequiredException` | Feature requires Upstox Plus subscription |
| `ServiceUnavailableException` | API available only during specific hours |

---

## Configuration

### SDK Configuration

```kotlin
val upstox = Upstox.getInstance()

// Enable HTTP request/response logging (for debugging)
upstox.setLoggingEnabled(true)  // Default: false

// Configure rate limit retry (0-5 attempts with exponential backoff)
upstox.setRateLimitRetries(3)  // Default: 0 (disabled)

// Configure WebSocket reconnection attempts
upstox.setMaxWebSocketReconnectAttempts(10)  // Default: 5

// Reset all configuration to defaults (clears tokens too)
upstox.resetConfiguration()
```

| Setting | Method | Default | Range | Description |
|---------|--------|---------|-------|-------------|
| HTTP Logging | `setLoggingEnabled(Boolean)` | `false` | - | Log HTTP requests/responses for debugging |
| Rate Limit Retry | `setRateLimitRetries(Int)` | `0` | 0-5 | Auto-retry on HTTP 429 with exponential backoff |
| WebSocket Reconnect | `setMaxWebSocketReconnectAttempts(Int)` | `5` | 1-20 | Max reconnection attempts before giving up |
| Reset Config | `resetConfiguration()` | - | - | Reset all settings to defaults (clears tokens) |

> **Note:** When `rateLimitRetries > 0`, the SDK automatically retries rate-limited requests (HTTP 429) with exponential backoff (1s, 2s, 4s, ...) before throwing an exception.

### Timeouts

| Setting | Default |
|---------|---------|
| Connect timeout | 10 seconds |
| Read timeout | 30 seconds |
| Write timeout | 30 seconds |

### WebSocket Settings

| Setting | Default |
|---------|---------|
| Ping interval | 10 seconds |
| Initial reconnect delay | 1 second |
| Max reconnect delay | 30 seconds |
| Max reconnect attempts | 5 (configurable) |

### Base URLs

| Endpoint | URL |
|----------|-----|
| REST API v2 | `https://api.upstox.com/v2` |
| REST API v3 | `https://api.upstox.com/v3` |
| HFT (fast orders) | `https://api-hft.upstox.com/v3` |
| Sandbox | `https://api-sandbox.upstox.com/v3` |
| Auth | `https://api.upstox.com` |

---

## Requirements

- **Java 11** or higher
- **Kotlin 1.8** or higher (if using Kotlin)
- Upstox trading account with API access

### Dependencies

- OkHttp 5.3.0
- Gson 2.13.2
- Protobuf 4.33.1

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## Links

- [Upstox API Documentation](https://upstox.com/developer/api-documentation/)
- [Upstox Developer Portal](https://account.upstox.com/developer/apps)
- [Report Issues](https://github.com/SonicAlgo/upstox-java-sdk/issues)

---

## Disclaimer

This is an **unofficial** SDK and is not affiliated with, endorsed by, or supported by Upstox. Use at your own risk. Always test thoroughly in sandbox mode before using in production.
