package io.github.sonicalgo.upstox.validation

/**
 * Validation utilities for Upstox API request parameters.
 *
 * Contains official regex patterns from Upstox documentation for validating
 * instrument keys, order IDs, dates, and other API parameters.
 *
 * @see <a href="http://upstox.com/developer/api-documentation/appendix/field-pattern">Field Patterns</a>
 */
internal object Validators {

    // ============================================
    // Official Upstox Regex Patterns
    // Source: http://upstox.com/developer/api-documentation/appendix/field-pattern
    // ============================================

    /**
     * Pattern for instrument_key field.
     * Format: EXCHANGE|TOKEN (e.g., NSE_EQ|INE002A01018)
     * Multiple keys can be comma-separated.
     */
    private val INSTRUMENT_KEY_PATTERN = Regex(
        "^(?:NSE_EQ|NSE_FO|NCD_FO|BSE_EQ|BSE_FO|BCD_FO|MCX_FO|NSE_COM|NSE_INDEX|BSE_INDEX|MCX_INDEX)\\|[\\w ]+" +
        "(,(?:NSE_EQ|NSE_FO|NCD_FO|BSE_EQ|BSE_FO|BCD_FO|MCX_FO|NSE_COM|NSE_INDEX|BSE_INDEX|MCX_INDEX)\\|[\\w ]+)*$"
    )

    /**
     * Pattern for order_id field.
     * Alphanumeric with hyphens allowed.
     */
    private val ORDER_ID_PATTERN = Regex("^[-a-zA-Z0-9]+$")

    /**
     * Pattern for exchange field.
     * Valid exchanges: NSE, NFO, CDS, BSE, BFO, BCD, MCX, NSCOM
     */
    private val EXCHANGE_PATTERN = Regex("^(\\s*|(?:NSE|NFO|CDS|BSE|BFO|BCD|MCX|NSCOM)+)$")

    /**
     * Pattern for expired instrument keys.
     * Format: EXCHANGE|TOKEN|DD-MM-YYYY
     */
    private val EXPIRED_INSTRUMENT_KEY_PATTERN = Regex(
        "^(?:NSE_EQ|NSE_FO|NCD_FO|BSE_EQ|BSE_FO|BCD_FO|MCX_FO|NSE_INDEX|BSE_INDEX|MCX_INDEX|NSE_COM)\\|[\\w\\d\\-]+\\|(0[1-9]|[12]\\d|3[01])-(0[1-9]|1[012])-(\\d{4})$"
    )

    /**
     * Pattern for dates in YYYY-MM-DD format.
     */
    private val DATE_YYYY_MM_DD = Regex("^\\d{4}-\\d{2}-\\d{2}$")

    /**
     * Pattern for dates in DD-MM-YYYY format.
     */
    private val DATE_DD_MM_YYYY = Regex("^\\d{2}-\\d{2}-\\d{4}$")

    /**
     * Pattern for financial year in YYNN format.
     * Example: "2324" for FY 2023-24
     */
    private val FINANCIAL_YEAR_PATTERN = Regex("^\\d{4}$")

    /**
     * Pattern for GTT order ID.
     * Format: GTT-xxxxx
     */
    private val GTT_ORDER_ID_PATTERN = Regex("^GTT-[a-zA-Z0-9]+$")

    // ============================================
    // Validation Functions
    // ============================================

    /**
     * Validates an instrument key format.
     *
     * @param key The instrument key to validate (e.g., "NSE_EQ|INE002A01018")
     * @param fieldName The field name for error messages
     * @throws IllegalArgumentException if the key format is invalid
     */
    fun validateInstrumentKey(key: String, fieldName: String = "instrumentKey") {
        require(INSTRUMENT_KEY_PATTERN.matches(key)) {
            "Invalid $fieldName format. Expected: EXCHANGE|TOKEN (e.g., NSE_EQ|INE002A01018)"
        }
    }

    /**
     * Validates an order ID format.
     *
     * @param orderId The order ID to validate
     * @param fieldName The field name for error messages
     * @throws IllegalArgumentException if the order ID format is invalid
     */
    fun validateOrderId(orderId: String, fieldName: String = "orderId") {
        require(orderId.isNotBlank()) { "$fieldName cannot be empty" }
        require(ORDER_ID_PATTERN.matches(orderId)) {
            "Invalid $fieldName format. Expected alphanumeric with hyphens"
        }
    }

    /**
     * Validates an exchange code.
     *
     * @param exchange The exchange code to validate (e.g., "NSE", "BSE")
     * @param fieldName The field name for error messages
     * @throws IllegalArgumentException if the exchange code is invalid
     */
    fun validateExchange(exchange: String, fieldName: String = "exchange") {
        require(EXCHANGE_PATTERN.matches(exchange)) {
            "Invalid $fieldName. Valid values: NSE, NFO, CDS, BSE, BFO, BCD, MCX, NSCOM"
        }
    }

    /**
     * Validates an expired instrument key format.
     *
     * @param key The expired instrument key to validate
     * @param fieldName The field name for error messages
     * @throws IllegalArgumentException if the key format is invalid
     */
    fun validateExpiredInstrumentKey(key: String, fieldName: String = "instrumentKey") {
        require(EXPIRED_INSTRUMENT_KEY_PATTERN.matches(key)) {
            "Invalid $fieldName format. Expected: EXCHANGE|TOKEN|DD-MM-YYYY"
        }
    }

    /**
     * Validates a date in YYYY-MM-DD format.
     *
     * @param date The date string to validate
     * @param fieldName The field name for error messages
     * @throws IllegalArgumentException if the date format is invalid
     */
    fun validateDateYYYYMMDD(date: String, fieldName: String = "date") {
        require(DATE_YYYY_MM_DD.matches(date)) {
            "Invalid $fieldName format. Expected: YYYY-MM-DD"
        }
    }

    /**
     * Validates a date in DD-MM-YYYY format.
     *
     * @param date The date string to validate
     * @param fieldName The field name for error messages
     * @throws IllegalArgumentException if the date format is invalid
     */
    fun validateDateDDMMYYYY(date: String, fieldName: String = "date") {
        require(DATE_DD_MM_YYYY.matches(date)) {
            "Invalid $fieldName format. Expected: DD-MM-YYYY"
        }
    }

    /**
     * Validates a financial year in YYNN format.
     *
     * @param fy The financial year to validate (e.g., "2324" for FY 2023-24)
     * @throws IllegalArgumentException if the format is invalid
     */
    fun validateFinancialYear(fy: String) {
        require(FINANCIAL_YEAR_PATTERN.matches(fy)) {
            "Invalid financial year format. Expected: YYNN (e.g., '2324' for FY 2023-24)"
        }
    }

    /**
     * Validates a GTT order ID format.
     *
     * @param id The GTT order ID to validate
     * @throws IllegalArgumentException if the format is invalid
     */
    fun validateGttOrderId(id: String) {
        require(GTT_ORDER_ID_PATTERN.matches(id)) {
            "Invalid GTT order ID format. Expected: GTT-xxxxx"
        }
    }

    /**
     * Validates that a quantity is positive.
     *
     * @param qty The quantity to validate
     * @param fieldName The field name for error messages
     * @throws IllegalArgumentException if quantity is not positive
     */
    fun validateQuantity(qty: Int, fieldName: String = "quantity") {
        require(qty > 0) { "$fieldName must be greater than 0" }
    }

    /**
     * Validates that a price is non-negative (or positive if required).
     *
     * @param price The price to validate
     * @param allowZero Whether zero is allowed (default: true)
     * @param fieldName The field name for error messages
     * @throws IllegalArgumentException if price validation fails
     */
    fun validatePrice(price: Double, allowZero: Boolean = true, fieldName: String = "price") {
        if (allowZero) {
            require(price >= 0) { "$fieldName must be non-negative" }
        } else {
            require(price > 0) { "$fieldName must be greater than 0" }
        }
    }

    /**
     * Validates that a list size is within allowed bounds.
     *
     * @param list The list to validate
     * @param maxSize Maximum allowed size
     * @param operation Name of the operation for error messages
     * @throws IllegalArgumentException if list is empty or exceeds maxSize
     */
    fun validateListSize(list: List<*>, maxSize: Int, operation: String) {
        require(list.isNotEmpty()) { "$operation requires at least one item" }
        require(list.size <= maxSize) {
            "$operation supports maximum $maxSize items, got ${list.size}"
        }
    }

    /**
     * Validates page size for paginated APIs.
     *
     * @param pageSize The page size to validate
     * @param minSize Minimum allowed size (default: 1)
     * @param maxSize Maximum allowed size (default: 5000)
     * @throws IllegalArgumentException if page size is out of bounds
     */
    fun validatePageSize(pageSize: Int, minSize: Int = 1, maxSize: Int = 5000) {
        require(pageSize in minSize..maxSize) {
            "pageSize must be between $minSize and $maxSize, got $pageSize"
        }
    }
}
