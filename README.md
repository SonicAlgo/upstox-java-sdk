# Upstox Java SDK

[![Maven Central](https://img.shields.io/maven-central/v/io.github.sonicalgo/upstox-java-sdk)](https://central.sonatype.com/artifact/io.github.sonicalgo/upstox-java-sdk)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-11%2B-blue)](https://www.oracle.com/java/)

Unofficial Kotlin/Java SDK for the [Upstox](https://upstox.com) trading platform. Supports REST APIs and real-time WebSocket streaming.

## Installation

### Gradle (Kotlin DSL)

```kotlin
implementation("io.github.sonicalgo:upstox-java-sdk:2.0.0")
```

### Gradle (Groovy)

```groovy
implementation 'io.github.sonicalgo:upstox-java-sdk:2.0.0'
```

### Maven

```xml
<dependency>
    <groupId>io.github.sonicalgo</groupId>
    <artifactId>upstox-java-sdk</artifactId>
    <version>2.0.0</version>
</dependency>
```

## Quick Start

<details open>
<summary>Kotlin</summary>

```kotlin
import io.github.sonicalgo.upstox.Upstox
import io.github.sonicalgo.upstox.usecase.*
import io.github.sonicalgo.upstox.common.*

// Create SDK instance with access token
val upstox = Upstox.builder()
    .accessToken("your-access-token")
    .build()

// Get user profile
val profile = upstox.getProfile()
println("Welcome, ${profile.userName}")

// Get market quote
val quotes = upstox.getLtp(listOf("NSE_EQ|INE669E01016"))
println("LTP: ${quotes["NSE_EQ|INE669E01016"]?.ltp}")

// Place an order
val response = upstox.placeOrder(PlaceOrderParams {
    instrumentToken = "NSE_EQ|INE669E01016"
    quantity = 1
    product = Product.DELIVERY
    validity = Validity.DAY
    price = 100.0
    orderType = OrderType.LIMIT
    transactionType = TransactionType.BUY
})
println("Order ID: ${response.orderIds?.firstOrNull()}")
```

</details>

<details>
<summary>Java</summary>

```java
import io.github.sonicalgo.upstox.Upstox;
import io.github.sonicalgo.upstox.usecase.*;
import io.github.sonicalgo.upstox.common.*;
import java.util.Arrays;

// Create SDK instance with access token
Upstox upstox = Upstox.builder()
    .accessToken("your-access-token")
    .build();

// Get user profile
var profile = upstox.getProfile();
System.out.println("Welcome, " + profile.getUserName());

// Get market quote
var quotes = upstox.getLtp(Arrays.asList("NSE_EQ|INE669E01016"));
System.out.println("LTP: " + quotes.get("NSE_EQ|INE669E01016").getLtp());

// Place an order using builder pattern
var response = upstox.placeOrder(PlaceOrderParams.builder()
    .instrumentToken("NSE_EQ|INE669E01016")
    .quantity(1)
    .product(Product.DELIVERY)
    .validity(Validity.DAY)
    .price(100.0)
    .orderType(OrderType.LIMIT)
    .transactionType(TransactionType.BUY)
    .build());
System.out.println("Order ID: " + response.getOrderIds().get(0));
```

</details>

> **Note:** All code examples below assume you have initialized the SDK as shown below:
> ```kotlin
> val upstox = Upstox.builder()
>     .accessToken("your-access-token")
>     .build()
> ```

## Type-Safe Enums

The SDK uses type-safe enums throughout the API responses for better code safety and IDE support:

```kotlin
// Order response uses enums
val order = upstox.getOrderDetails("order-id")
when (order.status) {
    OrderStatus.COMPLETE -> println("Order filled")
    OrderStatus.REJECTED -> println("Order rejected: ${order.statusMessage}")
    OrderStatus.OPEN -> println("Order pending")
    else -> println("Status: ${order.status}")
}

// User profile returns enum lists
val profile = upstox.getProfile()
profile.exchanges.forEach { exchange: Exchange ->
    println("Enabled exchange: $exchange")
}
profile.orderTypes.forEach { orderType: OrderType ->
    println("Enabled order type: $orderType")
}

// Position and holdings use enums
val positions = upstox.getPositions()
positions.filter { it.exchange == Exchange.NSE && it.product == Product.INTRADAY }
    .forEach { println("Intraday position: ${it.tradingSymbol}") }
```

**Available enums:**

| Category | Enums |
|----------|-------|
| Trading | `Exchange`, `Segment`, `Product`, `OrderType`, `TransactionType`, `Validity` |
| Order Status | `OrderStatus`, `OrderVariety` |
| GTT Orders | `GttType`, `GttStrategy`, `GttTriggerType`, `GttRuleStatus` |
| Market Data | `OhlcInterval`, `CandleUnit`, `MarketStatus`, `HolidayType` |
| Options | `OptionType`, `InstrumentType`, `UnderlyingType` |
| Reports | `TradeSegment`, `TradeType`, `FundSegment` |

## Why This SDK?

- **Modern & Secure** - Built with latest libraries (OkHttp 5.x, Protobuf 4.x) with no known vulnerabilities
- **WebSocket Ready** - Full protobuf parsing built-in with typed callbacks; no manual binary handling needed
- **HFT Optimized** - Uses dedicated HFT endpoints (`api-hft.upstox.com`) for lowest latency order execution
- **Auto-Reconnection** - WebSocket clients automatically reconnect with exponential backoff
- **Flat API** - Clean direct methods: `upstox.placeOrder()`, `upstox.getProfile()` with no nested accessors
- **Type-Safe** - Kotlin data classes with proper types; no raw Maps or Object casting
- **Rich Error Handling** - Exceptions with helpers like `isRateLimitError`, `isAuthenticationError`
- **Latest API Support** - V3 endpoints supported out of the box
- **Sandbox Built-In** - Test orders safely with `Upstox.builder().sandboxMode(true, "token").build()`
- **Thread-Safe** - Designed for concurrent usage in trading applications

## Features

- **45+ direct API methods** - Orders, Portfolio, Market Quotes, Historical Data, Option Chain, and more
- **Real-time market data** - WebSocket streaming with protobuf (low latency)
- **Real-time portfolio updates** - Order, position, holding, and GTT updates via WebSocket
- **Sandbox mode** - Test order operations without live execution
- **Automatic reconnection** - WebSocket clients reconnect with exponential backoff
- **Configurable rate limiting** - Automatic retry with exponential backoff for HTTP 429
- **Debug logging** - Optional HTTP request/response logging for troubleshooting
- **Full Kotlin & Java compatibility** - Use from either language

---

## API Reference

| Method | Description |
|--------|-------------|
| **Authentication** | |
| `getAuthorizationUrl()` | Get OAuth authorization URL |
| `getToken()` | Exchange auth code for access token |
| `requestAccessToken()` | Request new access token |
| `logout()` | End session and invalidate token |
| **User** | |
| `getProfile()` | Get user profile |
| `getFundsAndMargin()` | Get funds and margin details |
| **Orders** | |
| `placeOrder()` | Place a single order |
| `placeMultiOrder()` | Place up to 25 orders |
| `modifyOrder()` | Modify an existing order |
| `cancelOrder()` | Cancel an order |
| `cancelMultiOrder()` | Cancel multiple orders |
| `exitAllPositions()` | Exit all open positions |
| `getOrders()` | Get all orders for the day |
| `getOrderDetails()` | Get specific order details |
| `getOrderHistory()` | Get order history/audit trail |
| `getTrades()` | Get all trades for the day |
| `getTradesByOrder()` | Get trades for specific order |
| `getHistoricalTrades()` | Get historical trades |
| **GTT Orders** | |
| `placeGttOrder()` | Place GTT order |
| `modifyGttOrder()` | Modify GTT order |
| `cancelGttOrder()` | Cancel GTT order |
| `getGttOrders()` | Get all/specific GTT orders |
| **Portfolio** | |
| `getPositions()` | Get current positions |
| `getMtfPositions()` | Get MTF positions |
| `convertPosition()` | Convert position product type |
| `getHoldings()` | Get holdings |
| **Market Quotes** | |
| `getLtp()` | Get last traded price |
| `getOhlc()` | Get OHLC data |
| `getFullQuote()` | Get full market quote with depth |
| `getOptionGreeks()` | Get option greeks |
| **Historical Data** | |
| `getHistoricalCandles()` | Get historical candle data |
| `getIntradayCandles()` | Get intraday candle data |
| **Option Chain** | |
| `getOptionContracts()` | Get option contracts |
| `getOptionChain()` | Get option chain with greeks |
| **Market Info** | |
| `getMarketHolidays()` | Get market holidays |
| `getMarketHoliday()` | Get specific holiday |
| `getMarketTimings()` | Get market timings |
| `getMarketStatus()` | Get current market status |
| **Charges & Margins** | |
| `getBrokerage()` | Calculate brokerage charges |
| `getMargin()` | Calculate margin requirements |
| **Trade P&L** | |
| `getTradePnlMetadata()` | Get P&L report metadata |
| `getTradePnlReport()` | Get P&L report |
| `getTradeCharges()` | Get trade charges breakdown |
| **Instruments** | |
| `getInstruments()` | Download instrument master |
| `getAllInstruments()` | Download all instruments |
| `getNseInstruments()` | Download NSE instruments |
| `getBseInstruments()` | Download BSE instruments |
| **Expired Instruments** | |
| `getExpiries()` | Get available expiries |
| `getExpiredOptionContracts()` | Get expired option contracts |
| `getExpiredFutureContracts()` | Get expired future contracts |
| `getExpiredHistoricalCandles()` | Get expired instrument candles |
| **WebSocket** | |
| `createMarketDataFeedClient()` | Create market data WebSocket |
| `createPortfolioStreamClient()` | Create portfolio WebSocket |

---

## Configuration

### SDK Configuration

```kotlin
// Configure during initialization using builder pattern
val upstox = Upstox.builder()
    .accessToken("your-access-token")
    .loggingEnabled(true)       // Enable HTTP request/response logging
    .rateLimitRetries(3)        // Configure rate limit retry (0-5 attempts)
    .build()
```

| Setting | Builder Method | Default | Range | Description |
|---------|----------------|---------|-------|-------------|
| HTTP Logging | `loggingEnabled(Boolean)` | `false` | - | Log HTTP requests/responses for debugging |
| Rate Limit Retry | `rateLimitRetries(Int)` | `0` | 0-5 | Auto-retry on HTTP 429 with exponential backoff |

> **Note:** When `rateLimitRetries > 0`, the SDK automatically retries rate-limited requests (HTTP 429) with exponential backoff (1s, 2s, 4s, ...) before throwing an exception.

### WebSocket Configuration

WebSocket reconnection settings are configured per-client during creation:

```kotlin
// Market Data Feed Client
val feedClient = upstox.createMarketDataFeedClient(
    maxReconnectAttempts = 10,      // Default: 5, Max reconnection attempts
    autoReconnectEnabled = true,    // Default: true, Auto-reconnect on disconnect
    autoResubscribeEnabled = true   // Default: true, Auto-resubscribe after reconnect
)

// Portfolio Stream Client
val portfolioClient = upstox.createPortfolioStreamClient(
    maxReconnectAttempts = 10,   // Default: 5
    autoReconnectEnabled = true  // Default: true
)
```

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

## Table of Contents

- [API Reference](#api-reference)
- [Configuration](#configuration)
- [WebSocket Streaming](#websocket-streaming)
  - [Market Data Feed](#market-data-feed)
  - [Portfolio Stream](#portfolio-stream)
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
- [Sandbox Mode](#sandbox-mode)
- [Error Handling](#error-handling)
- [Instrument Key Format](#instrument-key-format)
- [Requirements](#requirements)
- [License](#license)

---

## Authentication

### Getting API Credentials

1. Log in to [Upstox Developer Portal](https://account.upstox.com/developer/apps)
2. Create a new app to get your **API Key** and **API Secret**
3. Set a redirect URI for OAuth callback

### OAuth Flow

**Kotlin:**
```kotlin
// Step 1: Create SDK instance (no token required for auth flow)
val upstox = Upstox.builder().build()

// Step 2: Get authorization URL
val authUrl = upstox.getAuthorizationUrl(
    clientId = "your-api-key",
    redirectUri = "https://yourapp.com/callback",
    state = "optional-state"  // For CSRF protection
)
// Redirect user to authUrl

// Step 3: Exchange authorization code for access token (in callback handler)
val tokenResponse = upstox.getToken(
    code = "authorization-code-from-callback",
    clientId = "your-api-key",
    clientSecret = "your-api-secret",
    redirectUri = "https://yourapp.com/callback"
)

// Step 4: Set the access token on the existing instance
upstox.setAccessToken(tokenResponse.accessToken)

// Token is valid for the trading day (until ~3:30 AM next day)
```

**Java:**
```java
// Step 1: Create SDK instance (no token required for auth flow)
Upstox upstox = Upstox.builder().build();

// Step 2: Get authorization URL
String authUrl = upstox.getAuthorizationUrl(
    "your-api-key",
    "https://yourapp.com/callback",
    "optional-state"  // For CSRF protection
);
// Redirect user to authUrl

// Step 3: Exchange authorization code for access token (in callback handler)
TokenResponse tokenResponse = upstox.getToken(
    "authorization-code-from-callback",
    "your-api-key",
    "your-api-secret",
    "https://yourapp.com/callback"
);

// Step 4: Set the access token on the existing instance
upstox.setAccessToken(tokenResponse.getAccessToken());
```

### Logout

```kotlin
upstox.logout()
// Access token is automatically cleared after successful logout
```

---

## REST API Reference

### User & Funds

```kotlin
// Get user profile
val profile = upstox.getProfile()
// Returns: userId, userName, email, exchanges, products, orderTypes, isActive

// Get funds and margin (all segments)
val funds = upstox.getFundsAndMargin()
```

### Orders

#### Place Order

**Kotlin:**
```kotlin
val response = upstox.placeOrder(PlaceOrderParams {
    instrumentToken = "NSE_EQ|INE669E01016"
    quantity = 1
    product = Product.DELIVERY      // DELIVERY, INTRADAY, MTF
    validity = Validity.DAY         // DAY or IOC
    price = 100.0
    orderType = OrderType.LIMIT     // LIMIT or MARKET
    transactionType = TransactionType.BUY
    disclosedQuantity = 0           // Optional
    triggerPrice = 0.0              // For SL orders
    isAmo = false                   // After Market Order
    tag = "my-tag"                  // Optional identifier
})
println("Order ID: ${response.orderIds?.firstOrNull()}")
```

**Java:**
```java
PlaceOrderParams params = new PlaceOrderParamsBuilder()
    .instrumentToken("NSE_EQ|INE669E01016")
    .quantity(1)
    .product(Product.DELIVERY)
    .validity(Validity.DAY)
    .price(100.0)
    .orderType(OrderType.LIMIT)
    .transactionType(TransactionType.BUY)
    .disclosedQuantity(0)
    .triggerPrice(0.0)
    .isAmo(false)
    .tag("my-tag")
    .build();

PlaceOrderResponse response = upstox.placeOrder(params);
System.out.println("Order ID: " + response.getOrderIds().get(0));
```

#### Place Multiple Orders (up to 25)

**Kotlin:**
```kotlin
val responses = upstox.placeMultiOrder(listOf(
    MultiOrderParams {
        instrumentToken = "NSE_EQ|INE669E01016"
        quantity = 1
        product = Product.DELIVERY
        validity = Validity.DAY
        price = 100.0
        orderType = OrderType.LIMIT
        transactionType = TransactionType.BUY
        correlationId = "order-1"
    },
    MultiOrderParams {
        instrumentToken = "NSE_EQ|INE002A01018"
        quantity = 1
        product = Product.INTRADAY
        validity = Validity.DAY
        price = 2500.0
        orderType = OrderType.LIMIT
        transactionType = TransactionType.SELL
        correlationId = "order-2"
    }
))
```

**Java:**
```java
List<MultiOrderParams> orders = Arrays.asList(
    new MultiOrderParamsBuilder()
        .instrumentToken("NSE_EQ|INE669E01016")
        .quantity(1)
        .product(Product.DELIVERY)
        .validity(Validity.DAY)
        .price(100.0)
        .orderType(OrderType.LIMIT)
        .transactionType(TransactionType.BUY)
        .correlationId("order-1")
        .build(),
    new MultiOrderParamsBuilder()
        .instrumentToken("NSE_EQ|INE002A01018")
        .quantity(1)
        .product(Product.INTRADAY)
        .validity(Validity.DAY)
        .price(2500.0)
        .orderType(OrderType.LIMIT)
        .transactionType(TransactionType.SELL)
        .correlationId("order-2")
        .build()
);

List<MultiOrderResponse> responses = upstox.placeMultiOrder(orders);
```

#### Modify Order

**Kotlin:**
```kotlin
val modified = upstox.modifyOrder(ModifyOrderParams {
    orderId = "240108010918222"
    quantity = 2
    validity = Validity.DAY
    price = 105.0
    orderType = OrderType.LIMIT
    disclosedQuantity = 0
    triggerPrice = 0.0
})
```

**Java:**
```java
ModifyOrderParams params = new ModifyOrderParamsBuilder()
    .orderId("240108010918222")
    .quantity(2)
    .validity(Validity.DAY)
    .price(105.0)
    .orderType(OrderType.LIMIT)
    .disclosedQuantity(0)
    .triggerPrice(0.0)
    .build();

ModifyOrderResponse modified = upstox.modifyOrder(params);
```

#### Cancel Order

```kotlin
val cancelled = upstox.cancelOrder("240108010445130")
```

#### Cancel Multiple Orders

**Kotlin:**
```kotlin
// Cancel by segment
val cancelled = upstox.cancelMultiOrder(segment = Segment.NSE_EQ)

// Cancel by tag
val cancelledByTag = upstox.cancelMultiOrder(tag = "my-tag")

// Cancel by both
val cancelledBoth = upstox.cancelMultiOrder(
    segment = Segment.NSE_EQ,
    tag = "my-tag"
)
```

**Java:**
```java
// Cancel by segment
MultiOrderResponse cancelled = upstox.cancelMultiOrder(Segment.NSE_EQ, null);

// Cancel by tag
MultiOrderResponse cancelledByTag = upstox.cancelMultiOrder(null, "my-tag");

// Cancel by both
MultiOrderResponse cancelledBoth = upstox.cancelMultiOrder(Segment.NSE_EQ, "my-tag");
```

#### Exit All Positions

**Kotlin:**
```kotlin
// Exit all positions
val exited = upstox.exitAllPositions()

// Exit positions with filters
val exited = upstox.exitAllPositions(
    segment = Segment.NSE_FO,  // Optional: exit by segment
    tag = "my-tag"             // Optional: exit by tag (intraday only)
)
```

**Java:**
```java
// Exit all positions
MultiOrderResponse exited = upstox.exitAllPositions();

// Exit positions with filters
MultiOrderResponse exited = upstox.exitAllPositions(Segment.NSE_FO, "my-tag");
```

#### Query Orders

```kotlin
// Get all orders for the day
val orderBook = upstox.getOrders()

// Get specific order details
val order = upstox.getOrderDetails("240108010445130")

// Get order history/audit trail
val history = upstox.getOrderHistory(orderId = "240108010445130")
// OR by tag
val historyByTag = upstox.getOrderHistory(tag = "my-tag")

// Get all trades for the day
val trades = upstox.getTrades()

// Get trades for specific order
val orderTrades = upstox.getTradesByOrder("240108010445100")

// Get historical trades (last 3 financial years)
val response = upstox.getHistoricalTrades(HistoricalTradesParams {
    startDate = "2023-04-01"
    endDate = "2024-03-31"
    pageNumber = 1
    pageSize = 100
    segment = TradeSegment.EQUITY
})
val trades = response.data  // List of HistoricalTrade
val totalPages = response.metaData?.page?.totalPages  // Pagination info
```

**Java (Historical Trades):**
```java
HistoricalTradesParams params = new HistoricalTradesParamsBuilder()
    .startDate("2023-04-01")
    .endDate("2024-03-31")
    .pageNumber(1)
    .pageSize(100)
    .segment(TradeSegment.EQUITY)
    .build();

HistoricalTradesResponse response = upstox.getHistoricalTrades(params);
List<HistoricalTrade> trades = response.getData();
```

### GTT Orders

Good Till Triggered orders execute automatically when price conditions are met.

#### Place GTT Order

**Kotlin:**
```kotlin
// Single trigger
val gtt = upstox.placeGttOrder(PlaceGttOrderParams {
    type = GttType.SINGLE
    quantity = 1
    product = Product.DELIVERY
    instrumentToken = "NSE_EQ|INE669E01016"
    transactionType = TransactionType.BUY
    rules = listOf(
        GttRuleParams {
            strategy = GttStrategy.ENTRY
            triggerType = GttTriggerType.BELOW
            triggerPrice = 95.0
        }
    )
})

// Multiple triggers with target and stop-loss
val gttOco = upstox.placeGttOrder(PlaceGttOrderParams {
    type = GttType.MULTIPLE
    quantity = 1
    product = Product.DELIVERY
    instrumentToken = "NSE_EQ|INE669E01016"
    transactionType = TransactionType.SELL
    rules = listOf(
        GttRuleParams { strategy = GttStrategy.ENTRY; triggerType = GttTriggerType.ABOVE; triggerPrice = 100.0 },
        GttRuleParams { strategy = GttStrategy.TARGET; triggerType = GttTriggerType.IMMEDIATE; triggerPrice = 110.0 },
        GttRuleParams { strategy = GttStrategy.STOP_LOSS; triggerType = GttTriggerType.IMMEDIATE; triggerPrice = 95.0; trailingGap = 2.0 }
    )
})
```

**Java:**
```java
// Single trigger
PlaceGttOrderParams params = new PlaceGttOrderParamsBuilder()
    .type(GttType.SINGLE)
    .quantity(1)
    .product(Product.DELIVERY)
    .instrumentToken("NSE_EQ|INE669E01016")
    .transactionType(TransactionType.BUY)
    .rules(Arrays.asList(
        new GttRuleParamsBuilder()
            .strategy(GttStrategy.ENTRY)
            .triggerType(GttTriggerType.BELOW)
            .triggerPrice(95.0)
            .build()
    ))
    .build();

GttOrderResponse gtt = upstox.placeGttOrder(params);
```

#### Modify GTT Order

**Kotlin:**
```kotlin
val modified = upstox.modifyGttOrder(ModifyGttOrderParams {
    gttOrderId = "GTT-C25270200137952"
    type = GttType.SINGLE
    quantity = 2
    rules = listOf(
        GttRuleParams { strategy = GttStrategy.ENTRY; triggerType = GttTriggerType.BELOW; triggerPrice = 90.0 }
    )
})
```

**Java:**
```java
ModifyGttOrderParams params = new ModifyGttOrderParamsBuilder()
    .gttOrderId("GTT-C25270200137952")
    .type(GttType.SINGLE)
    .quantity(2)
    .rules(Arrays.asList(
        new GttRuleParamsBuilder()
            .strategy(GttStrategy.ENTRY)
            .triggerType(GttTriggerType.BELOW)
            .triggerPrice(90.0)
            .build()
    ))
    .build();

GttOrderResponse modified = upstox.modifyGttOrder(params);
```

#### Cancel GTT Order

```kotlin
val cancelled = upstox.cancelGttOrder("GTT-C25280200137522")
```

#### Get GTT Orders

```kotlin
// Get all GTT orders
val allGtt = upstox.getGttOrders()

// Get specific GTT order
val gtt = upstox.getGttOrders("GTT-C25280200071351")
```

### Portfolio

#### Positions

```kotlin
// Get current positions
val positions = upstox.getPositions()
for (position in positions) {
    println("${position.tradingSymbol}: Qty=${position.quantity}, P&L=${position.pnl}")
}

// Get MTF positions (NSE only)
val mtfPositions = upstox.getMtfPositions()

// Convert position (e.g., intraday to delivery)
val converted = upstox.convertPosition(ConvertPositionParams {
    instrumentToken = "NSE_EQ|INE528G01035"
    newProduct = Product.DELIVERY
    oldProduct = Product.INTRADAY
    transactionType = TransactionType.BUY
    quantity = 1
})
```

**Java (Convert Position):**
```java
ConvertPositionParams params = new ConvertPositionParamsBuilder()
    .instrumentToken("NSE_EQ|INE528G01035")
    .newProduct(Product.DELIVERY)
    .oldProduct(Product.INTRADAY)
    .transactionType(TransactionType.BUY)
    .quantity(1)
    .build();

ConvertPositionResponse converted = upstox.convertPosition(params);
```

#### Holdings

```kotlin
val holdings = upstox.getHoldings()
for (holding in holdings) {
    println("${holding.companyName}: Qty=${holding.quantity}, P&L=${holding.pnl}")
}
```

### Market Quotes

#### LTP (Last Traded Price)

```kotlin
val ltp = upstox.getLtp(listOf(
    "NSE_EQ|INE669E01016",
    "NSE_EQ|INE002A01018"
))
// Returns Map<instrumentKey, LtpQuote>
println("LTP: ${ltp["NSE_EQ|INE669E01016"]?.ltp}")
```

#### OHLC Quotes

```kotlin
val ohlc = upstox.getOhlc(
    instrumentKeys = listOf("NSE_EQ|INE669E01016"),
    interval = OhlcInterval.ONE_DAY  // ONE_DAY, ONE_MINUTE, THIRTY_MINUTE
)
```

#### Full Market Quotes

```kotlin
// Get full quotes with depth (max 500 instruments)
val quotes = upstox.getFullQuote(listOf(
    "NSE_EQ|INE669E01016",
    "NSE_EQ|INE002A01018"
))
// Contains: ltp, ohlc, volume, oi, circuit limits, 5-level depth
```

#### Option Greeks

```kotlin
// Get Greeks for option instruments (max 50)
val greeks = upstox.getOptionGreeks(listOf("NSE_FO|43885"))
// Contains: ltp, iv, delta, gamma, theta, vega
```

### Historical Data

#### Historical Candles

**Kotlin:**
```kotlin
val candles = upstox.getHistoricalCandles(
    instrumentKey = "NSE_EQ|INE848E01016",
    unit = CandleUnit.MINUTES,    // MINUTES, HOURS, DAYS, WEEKS, MONTHS
    interval = 15,                // 1-300 for minutes
    toDate = "2025-01-15",
    fromDate = "2025-01-01"       // Optional
)
for (candle in candles.toCandles()) {
    println("${candle.timestamp}: O=${candle.open} H=${candle.high} L=${candle.low} C=${candle.close}")
}
```

**Java:**
```java
CandleData candles = upstox.getHistoricalCandles(
    "NSE_EQ|INE848E01016",
    CandleUnit.MINUTES,
    15,
    "2025-01-15",
    "2025-01-01"  // Optional, can be null
);

for (Candle candle : candles.toCandles()) {
    System.out.println(candle.getTimestamp() + ": O=" + candle.getOpen());
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
val intraday = upstox.getIntradayCandles(
    instrumentKey = "NSE_EQ|INE848E01016",
    unit = CandleUnit.MINUTES,
    interval = 1
)
```

### Option Chain

#### Get Option Contracts

```kotlin
val contracts = upstox.getOptionContracts(
    instrumentKey = "NSE_INDEX|Nifty 50",
    expiryDate = "2024-03-28"  // Optional filter
)
// Each: tradingSymbol, strikePrice, instrumentType, expiry, lotSize
```

#### Get Option Chain

```kotlin
val chain = upstox.getOptionChain(
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
// Get all market holidays
val holidays = upstox.getMarketHolidays()

// Get specific holiday
val holiday = upstox.getMarketHoliday("2024-01-26")

// Get market timings for a date
val timings = upstox.getMarketTimings("2024-01-22")

// Get current market status
val status = upstox.getMarketStatus("NSE")
// Status: NORMAL_OPEN, NORMAL_CLOSE, PRE_OPEN, CLOSING, etc.
```

### Charges & Margins

#### Brokerage Calculation

**Kotlin:**
```kotlin
val charges = upstox.getBrokerage(BrokerageParams {
    instrumentToken = "NSE_EQ|INE669E01016"
    quantity = 10
    product = Product.DELIVERY
    transactionType = TransactionType.BUY
    price = 100.0
})
// Returns: brokerage, GST, STT, stamp duty, total
```

**Java:**
```java
BrokerageParams params = new BrokerageParamsBuilder()
    .instrumentToken("NSE_EQ|INE669E01016")
    .quantity(10)
    .product(Product.DELIVERY)
    .transactionType(TransactionType.BUY)
    .price(100.0)
    .build();

BrokerageResponse charges = upstox.getBrokerage(params);
```

#### Margin Calculation

**Kotlin:**
```kotlin
// Calculate margin for orders (max 20 instruments)
val margin = upstox.getMargin(listOf(
    MarginInstrumentParams {
        instrumentKey = "NSE_FO|NIFTY24JANFUT"
        quantity = 50
        transactionType = TransactionType.BUY
        product = Product.INTRADAY
    }
))
// Returns: span, exposure, equity margin, and margin benefit for hedged positions
```

**Java:**
```java
List<MarginInstrumentParams> instruments = Arrays.asList(
    new MarginInstrumentParamsBuilder()
        .instrumentKey("NSE_FO|NIFTY24JANFUT")
        .quantity(50)
        .transactionType(TransactionType.BUY)
        .product(Product.INTRADAY)
        .build()
);

MarginResponse margin = upstox.getMargin(instruments);
```

### Trade P&L

**Kotlin:**
```kotlin
// Get report metadata
val metadata = upstox.getTradePnlMetadata(TradePnlMetadataParams {
    segment = TradeSegment.EQUITY
    financialYear = "2324"
    fromDate = "01-04-2023"
    toDate = "31-03-2024"
})

// Get P&L report (paginated)
val report = upstox.getTradePnlReport(TradePnlReportParams {
    segment = TradeSegment.EQUITY
    financialYear = "2324"
    pageNumber = 1
    pageSize = 100
    fromDate = "01-04-2023"
    toDate = "31-03-2024"
})

// Get trade charges breakdown
val charges = upstox.getTradeCharges(TradeChargesParams {
    segment = TradeSegment.EQUITY
    financialYear = "2324"
    fromDate = "01-04-2023"
    toDate = "31-03-2024"
})
```

**Java:**
```java
// Get report metadata
TradePnlMetadataParams metadataParams = new TradePnlMetadataParamsBuilder()
    .segment(TradeSegment.EQUITY)
    .financialYear("2324")
    .fromDate("01-04-2023")
    .toDate("31-03-2024")
    .build();

TradePnlMetadataResponse metadata = upstox.getTradePnlMetadata(metadataParams);

// Get P&L report (paginated)
TradePnlReportParams reportParams = new TradePnlReportParamsBuilder()
    .segment(TradeSegment.EQUITY)
    .financialYear("2324")
    .pageNumber(1)
    .pageSize(100)
    .fromDate("01-04-2023")
    .toDate("31-03-2024")
    .build();

TradePnlReportResponse report = upstox.getTradePnlReport(reportParams);

// Get trade charges breakdown
TradeChargesParams chargesParams = new TradeChargesParamsBuilder()
    .segment(TradeSegment.EQUITY)
    .financialYear("2324")
    .fromDate("01-04-2023")
    .toDate("31-03-2024")
    .build();

TradeChargesResponse charges = upstox.getTradeCharges(chargesParams);
```

### Instruments

Download instrument master data. **No authentication required.**

```kotlin
// Download instruments by type
val nseInstruments = upstox.getInstruments(InstrumentDownloadType.NSE)
val bseInstruments = upstox.getInstruments(InstrumentDownloadType.BSE)
val mcxInstruments = upstox.getInstruments(InstrumentDownloadType.MCX)
val allInstruments = upstox.getInstruments(InstrumentDownloadType.COMPLETE)

// Convenience methods
val nse = upstox.getNseInstruments()
val bse = upstox.getBseInstruments()
val all = upstox.getAllInstruments()

// Other types: SUSPENDED, MTF, NSE_MIS, BSE_MIS

// Get URL only (for custom download)
val url = upstox.getInstrumentsUrl(InstrumentDownloadType.NSE)
```

### Expired Instruments

> **Note:** Requires Upstox Plus subscription.

```kotlin
// Get available expiries for an underlying
val expiries = upstox.getExpiries("NSE_INDEX|Nifty 50")

// Get expired option contracts
val optionContracts = upstox.getExpiredOptionContracts(
    instrumentKey = "NSE_INDEX|Nifty 50",
    expiryDate = "2024-10-03"
)

// Get expired futures
val futureContracts = upstox.getExpiredFutureContracts(
    instrumentKey = "NSE_INDEX|Nifty 50",
    expiryDate = "2024-11-27"
)

// Get historical candles for expired instrument
val candles = upstox.getExpiredHistoricalCandles(
    expiredInstrumentKey = "NSE_FO|NIFTY22D0117800CE",
    interval = "day",  // 1minute, 3minute, 5minute, 15minute, 30minute, day
    toDate = "2022-11-30",
    fromDate = "2022-11-01"
)
```

**Java (Expired Historical Candles):**
```java
List<Candle> candles = upstox.getExpiredHistoricalCandles(
    "NSE_FO|NIFTY22D0117800CE",
    "day",         // 1minute, 3minute, 5minute, 15minute, 30minute, day
    "2022-11-30",
    "2022-11-01"
);
```

---

## WebSocket Streaming

### Market Data Feed

Real-time market data via WebSocket with protobuf encoding (low latency).

```kotlin
// Create client with custom reconnection settings (optional)
val feedClient = upstox.createMarketDataFeedClient(
    maxReconnectAttempts = 10,      // Default: 5
    autoReconnectEnabled = true,    // Default: true
    autoResubscribeEnabled = true   // Default: true, auto-resubscribe after reconnect
)

// Add listener
feedClient.addListener(object : MarketDataListener {
    override fun onConnected() {
        println("Connected!")
        // Subscribe with LTPC mode (minimal bandwidth)
        feedClient.subscribe(listOf("NSE_EQ|INE669E01016"))

        // Or subscribe with full market data
        feedClient.subscribe(listOf("NSE_INDEX|Nifty 50"), FeedMode.FULL)
    }

    override fun onReconnected() {
        println("Reconnected! Subscriptions restored automatically.")
    }

    override fun onDisconnected(code: Int, reason: String) {
        println("Disconnected: $reason")
    }

    override fun onError(error: Throwable) {
        println("Error: ${error.message}")
    }

    // Optional: override only the data callbacks you need
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
        println("Market status: ${status.segmentStatus}")
    }

    // Optional: handle reconnection attempts
    override fun onReconnecting(attempt: Int, delayMs: Long) {
        println("Reconnecting (attempt $attempt) in ${delayMs}ms...")
    }
})

// Connect to WebSocket
feedClient.connect()
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
// Create client with custom reconnection settings (optional)
val portfolioClient = upstox.createPortfolioStreamClient(
    maxReconnectAttempts = 10,   // Default: 5
    autoReconnectEnabled = true  // Default: true
)

// Add listener
portfolioClient.addListener(object : PortfolioStreamListener {
    override fun onConnected() {
        println("Connected to portfolio stream")
    }

    override fun onReconnected() {
        println("Reconnected to portfolio stream")
    }

    override fun onDisconnected(code: Int, reason: String) {
        println("Disconnected: $reason")
    }

    override fun onError(error: Throwable) {
        println("Error: ${error.message}")
    }

    // Optional: override only the data callbacks you need
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
        println("GTT ${gttOrder.gttOrderId}: ${gttOrder.rules?.firstOrNull()?.status}")
    }

    // Optional: handle reconnection attempts
    override fun onReconnecting(attempt: Int, delayMs: Long) {
        println("Reconnecting (attempt $attempt) in ${delayMs}ms...")
    }
})

// Connect with update types
portfolioClient.connect(
    updateTypes = setOf(
        PortfolioUpdateType.ORDER,
        PortfolioUpdateType.POSITION,
        PortfolioUpdateType.HOLDING,
        PortfolioUpdateType.GTT_ORDER
    )
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
// Create SDK instance with sandbox mode enabled
val upstox = Upstox.builder()
    .accessToken("your-access-token")
    .sandboxMode(enabled = true, token = "your-sandbox-token")
    .build()

// Orders will be simulated
val response = upstox.placeOrder(PlaceOrderParams(
    instrumentToken = "NSE_EQ|INE669E01016",
    quantity = 1,
    product = Product.DELIVERY,
    validity = Validity.DAY,
    price = 100.0,
    orderType = OrderType.LIMIT,
    transactionType = TransactionType.BUY
))
// Order is simulated via sandbox endpoint, not sent to exchange
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
    val order = upstox.placeOrder(...)
} catch (e: UpstoxApiException) {
    println("HTTP Status: ${e.httpStatusCode}")
    println("Message: ${e.message}")

    // Helper methods
    when {
        e.isRateLimitError -> println("Rate limited (429)")
        e.isAuthenticationError -> println("Auth failed (401/403)")
        e.isValidationError -> println("Bad request (400)")
        e.isServerError -> println("Server error (5xx)")
    }
}
```

### Exception Types

| Exception | Description |
|-----------|-------------|
| `UpstoxApiException` | Exception for all HTTP API errors |

---

## Instrument Key Format

Upstox uses instrument keys in the format `<SEGMENT>|<IDENTIFIER>`.

| Segment | Description | Identifier | Example |
|---------|-------------|------------|---------|
| `NSE_EQ` | NSE Equities | ISIN | `NSE_EQ\|INE669E01016` |
| `BSE_EQ` | BSE Equities | ISIN | `BSE_EQ\|INE669E01016` |
| `NSE_FO` | NSE F&O | Token | `NSE_FO\|43885` |
| `BSE_FO` | BSE F&O | Token | `BSE_FO\|12345` |
| `NSE_INDEX` | NSE Indices | Name | `NSE_INDEX\|Nifty 50` |
| `BSE_INDEX` | BSE Indices | Name | `BSE_INDEX\|SENSEX` |
| `MCX_FO` | MCX Commodities | Token | `MCX_FO\|54321` |
| `NCD_FO` | NSE Currency | Token | `NCD_FO\|67890` |
| `BCD_FO` | BSE Currency | Token | `BCD_FO\|98765` |

**How to get instrument keys:**
1. Download instrument master: `upstox.getNseInstruments()` or `upstox.getAllInstruments()`
2. Search for your symbol in the downloaded list
3. Use the `instrumentKey` field from the matching instrument

---

## Requirements

- **Java 11** or higher
- **Kotlin 2.0** or higher (if using Kotlin)
- Upstox trading account with API access

### Dependencies

- OkHttp 5.3.0
- Jackson 2.20.1
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
