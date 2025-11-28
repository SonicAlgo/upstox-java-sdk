package io.github.sonicalgo.upstox.model.response

/**
 * Historical candle data response.
 *
 * Contains OHLC (Open, High, Low, Close) candle data.
 *
 * @property candles Array of candle data. Each candle is an array with elements:
 *   [0]: Timestamp (ISO 8601 format), [1]: Open price, [2]: High price,
 *   [3]: Low price, [4]: Close price, [5]: Volume traded, [6]: Open interest (for derivatives)
 * @see <a href="https://upstox.com/developer/api-documentation/get-historical-candle-data">Historical Candle API</a>
 */
data class CandleData(
    val candles: List<List<Any>>
) {
    /**
     * Parses candle data into strongly-typed Candle objects.
     *
     * @return List of Candle objects
     */
    fun toCandles(): List<Candle> {
        return candles.map { candleArray ->
            Candle(
                timestamp = candleArray[0] as String,
                open = (candleArray[1] as Number).toDouble(),
                high = (candleArray[2] as Number).toDouble(),
                low = (candleArray[3] as Number).toDouble(),
                close = (candleArray[4] as Number).toDouble(),
                volume = (candleArray[5] as Number).toLong(),
                openInterest = (candleArray.getOrNull(6) as? Number)?.toLong()
            )
        }
    }
}

/**
 * Parsed candle data.
 *
 * Strongly-typed representation of a single OHLC candle.
 *
 * @property timestamp Candle start timestamp in ISO 8601 format
 * @property open Opening price of the candle
 * @property high Highest price during the candle period
 * @property low Lowest price during the candle period
 * @property close Closing price of the candle
 * @property volume Total volume traded during the candle period
 * @property openInterest Open interest (outstanding derivative contracts), only available for derivatives instruments
 */
data class Candle(
    val timestamp: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long,
    val openInterest: Long? = null
)
