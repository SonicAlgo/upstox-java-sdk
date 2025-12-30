package io.github.sonicalgo.builder

/**
 * Annotation to generate builder pattern for data classes.
 *
 * When applied to a data class, KSP processor generates:
 * - A Builder class with fluent setters for Java usage
 * - A DSL function for Kotlin usage
 * - A companion extension function `builder()` for Java static access
 *
 * Example usage:
 * ```kotlin
 * @GenerateBuilder
 * data class PlaceOrderParams(
 *     val symbol: String,      // Required (no default)
 *     val qty: Int,            // Required
 *     val tag: String? = null  // Optional (has default)
 * )
 * ```
 *
 * Generated Kotlin DSL:
 * ```kotlin
 * val params = PlaceOrderParams {
 *     symbol = "NSE:SBIN-EQ"
 *     qty = 10
 * }
 * ```
 *
 * Generated Java Builder:
 * ```java
 * PlaceOrderParams params = PlaceOrderParams.builder()
 *     .symbol("NSE:SBIN-EQ")
 *     .qty(10)
 *     .build();
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class GenerateBuilder
