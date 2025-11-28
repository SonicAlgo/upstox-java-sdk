package io.github.sonicalgo.upstox.api

import com.google.gson.reflect.TypeToken
import io.github.sonicalgo.upstox.config.ApiClient
import io.github.sonicalgo.upstox.config.OkHttpClientFactory
import io.github.sonicalgo.upstox.exception.UpstoxApiException
import io.github.sonicalgo.upstox.model.instrument.Instrument
import io.github.sonicalgo.upstox.model.instrument.InstrumentDownloadType
import okhttp3.Request
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream

/**
 * API module for downloading and parsing instrument master data.
 *
 * Provides access to the Upstox instruments master which contains details
 * of all tradeable instruments across NSE, BSE, and MCX exchanges.
 *
 * The instruments data is refreshed daily around 6 AM IST. Intraday updates
 * are rare but may occur for corporate actions or new listings.
 *
 * ## Important Notes
 * - Use `instrument_key` for unique identification (exchange tokens may be reused post-expiry)
 * - JSON format is recommended; CSV format is deprecated
 * - Files are gzip compressed for efficient download
 *
 * ## Example usage
 * ```kotlin
 * val upstox = Upstox.getInstance()
 *
 * // Get all NSE instruments
 * val nseInstruments = upstox.getInstrumentsApi().getInstruments(InstrumentDownloadType.NSE)
 *
 * // Get all instruments across all exchanges
 * val allInstruments = upstox.getInstrumentsApi().getInstruments(InstrumentDownloadType.COMPLETE)
 *
 * // Get MTF eligible instruments
 * val mtfInstruments = upstox.getInstrumentsApi().getInstruments(InstrumentDownloadType.MTF)
 *
 * // Filter specific instruments
 * val niftyOptions = nseInstruments.filter {
 *     it.underlyingSymbol == "NIFTY" && it.instrumentType == "OPTIDX"
 * }
 * ```
 *
 * @see <a href="https://upstox.com/developer/api-documentation/instruments">Instruments API</a>
 */
class InstrumentsApi private constructor() {

    /**
     * Downloads and parses instruments for the specified type.
     *
     * This method downloads the gzip-compressed JSON file from Upstox servers,
     * decompresses it, and parses it into a list of [Instrument] objects.
     *
     * The download may take a few seconds depending on the instrument type:
     * - COMPLETE: ~100MB uncompressed, contains all instruments
     * - Exchange-specific (NSE, BSE, MCX): Smaller, faster downloads
     * - MTF/MIS: Contains subset with margin trading eligibility info
     *
     * Example:
     * ```kotlin
     * val instrumentsApi = upstox.getInstrumentsApi()
     *
     * // Get NSE equity instruments
     * val nseInstruments = instrumentsApi.getInstruments(InstrumentDownloadType.NSE)
     * val equities = nseInstruments.filter { it.segment == "NSE_EQ" }
     *
     * // Get F&O instruments expiring this month
     * val futures = instrumentsApi.getInstruments(InstrumentDownloadType.NSE)
     *     .filter { it.instrumentType == "FUTIDX" || it.instrumentType == "FUTSTK" }
     * ```
     *
     * @param type The type of instrument list to download
     * @return List of instruments
     * @throws UpstoxApiException if download or parsing fails
     */
    fun getInstruments(type: InstrumentDownloadType): List<Instrument> {
        val url = INSTRUMENT_URLS[type]
            ?: throw UpstoxApiException(
                message = "Unknown instrument type: $type",
                httpStatusCode = 0
            )

        return downloadAndParse(url)
    }

    /**
     * Returns the download URL for the specified instrument type.
     *
     * Use this method if you want to download the file yourself or
     * need the URL for external processing.
     *
     * Example:
     * ```kotlin
     * val instrumentsApi = upstox.getInstrumentsApi()
     *
     * val url = instrumentsApi.getInstrumentsUrl(InstrumentDownloadType.NSE)
     * // url = "https://assets.upstox.com/market-quote/instruments/exchange/NSE.json.gz"
     * ```
     *
     * @param type The type of instrument list
     * @return The download URL for the gzip-compressed JSON file
     */
    fun getInstrumentsUrl(type: InstrumentDownloadType): String {
        return INSTRUMENT_URLS[type]
            ?: throw UpstoxApiException(
                message = "Unknown instrument type: $type",
                httpStatusCode = 0
            )
    }

    /**
     * Downloads and parses all instruments across all exchanges.
     *
     * This is a convenience method equivalent to `getInstruments(InstrumentDownloadType.COMPLETE)`.
     *
     * Note: The complete file is large (~100MB uncompressed). For better performance,
     * consider using exchange-specific methods if you only need a subset.
     *
     * @return List of all instruments
     * @throws UpstoxApiException if download or parsing fails
     */
    fun getAllInstruments(): List<Instrument> = getInstruments(InstrumentDownloadType.COMPLETE)

    /**
     * Downloads and parses NSE instruments.
     *
     * Convenience method equivalent to `getInstruments(InstrumentDownloadType.NSE)`.
     *
     * @return List of NSE instruments
     * @throws UpstoxApiException if download or parsing fails
     */
    fun getNseInstruments(): List<Instrument> = getInstruments(InstrumentDownloadType.NSE)

    /**
     * Downloads and parses BSE instruments.
     *
     * Convenience method equivalent to `getInstruments(InstrumentDownloadType.BSE)`.
     *
     * @return List of BSE instruments
     * @throws UpstoxApiException if download or parsing fails
     */
    fun getBseInstruments(): List<Instrument> = getInstruments(InstrumentDownloadType.BSE)

    /**
     * Downloads and parses MCX instruments.
     *
     * Convenience method equivalent to `getInstruments(InstrumentDownloadType.MCX)`.
     *
     * @return List of MCX instruments
     * @throws UpstoxApiException if download or parsing fails
     */
    fun getMcxInstruments(): List<Instrument> = getInstruments(InstrumentDownloadType.MCX)

    /**
     * Downloads and parses suspended instruments.
     *
     * Suspended instruments are those that are temporarily halted from trading.
     *
     * @return List of suspended instruments
     * @throws UpstoxApiException if download or parsing fails
     */
    fun getSuspendedInstruments(): List<Instrument> = getInstruments(InstrumentDownloadType.SUSPENDED)

    /**
     * Downloads and parses MTF (Margin Trading Facility) eligible instruments.
     *
     * MTF instruments include additional fields like `mtfEnabled` and `mtfBracket`.
     *
     * @return List of MTF eligible instruments
     * @throws UpstoxApiException if download or parsing fails
     */
    fun getMtfInstruments(): List<Instrument> = getInstruments(InstrumentDownloadType.MTF)

    /**
     * Downloads and parses NSE MIS (Margin Intraday Square-off) eligible instruments.
     *
     * MIS instruments include additional fields like `intradayMargin` and `intradayLeverage`.
     *
     * @return List of NSE MIS eligible instruments
     * @throws UpstoxApiException if download or parsing fails
     */
    fun getNseMisInstruments(): List<Instrument> = getInstruments(InstrumentDownloadType.NSE_MIS)

    /**
     * Downloads and parses BSE MIS (Margin Intraday Square-off) eligible instruments.
     *
     * MIS instruments include additional fields like `intradayMargin` and `intradayLeverage`.
     *
     * @return List of BSE MIS eligible instruments
     * @throws UpstoxApiException if download or parsing fails
     */
    fun getBseMisInstruments(): List<Instrument> = getInstruments(InstrumentDownloadType.BSE_MIS)

    private fun downloadAndParse(url: String): List<Instrument> {
        val request = Request.Builder()
            .url(url)
            .build()

        val response = try {
            OkHttpClientFactory.httpClient.newCall(request).execute()
        } catch (e: Exception) {
            throw UpstoxApiException(
                message = "Failed to download instruments from $url: ${e.message}",
                httpStatusCode = 0,
                cause = e
            )
        }

        if (!response.isSuccessful) {
            val statusCode = response.code
            response.close()
            throw UpstoxApiException(
                message = "Failed to download instruments: HTTP $statusCode - ${response.message}",
                httpStatusCode = statusCode
            )
        }

        return try {
            val inputStream = response.body.byteStream()
            GZIPInputStream(inputStream).use { gzipStream ->
                InputStreamReader(gzipStream, Charsets.UTF_8).use { reader ->
                    val listType = object : TypeToken<List<Instrument>>() {}.type
                    ApiClient.gson.fromJson(reader, listType)
                }
            }
        } catch (e: UpstoxApiException) {
            throw e
        } catch (e: Exception) {
            throw UpstoxApiException(
                message = "Failed to parse instruments from $url: ${e.message}",
                httpStatusCode = 0,
                cause = e
            )
        } finally {
            response.close()
        }
    }

    companion object {
        private const val BASE_URL = "https://assets.upstox.com/market-quote/instruments/exchange"

        private val INSTRUMENT_URLS = mapOf(
            InstrumentDownloadType.COMPLETE to "$BASE_URL/complete.json.gz",
            InstrumentDownloadType.NSE to "$BASE_URL/NSE.json.gz",
            InstrumentDownloadType.BSE to "$BASE_URL/BSE.json.gz",
            InstrumentDownloadType.MCX to "$BASE_URL/MCX.json.gz",
            InstrumentDownloadType.SUSPENDED to "$BASE_URL/suspended-instrument.json.gz",
            InstrumentDownloadType.MTF to "$BASE_URL/MTF.json.gz",
            InstrumentDownloadType.NSE_MIS to "$BASE_URL/NSE_MIS.json.gz",
            InstrumentDownloadType.BSE_MIS to "$BASE_URL/BSE_MIS.json.gz"
        )

        internal val instance by lazy { InstrumentsApi() }
    }
}
