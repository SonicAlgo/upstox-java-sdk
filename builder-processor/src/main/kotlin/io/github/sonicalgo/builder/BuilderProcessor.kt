package io.github.sonicalgo.builder

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * KSP processor that generates builder classes for data classes annotated with @GenerateBuilder.
 *
 * For each annotated class, it generates:
 * 1. A Builder class with mutable properties and fluent setters
 * 2. A DSL function for Kotlin usage
 *
 * Usage:
 * - Kotlin: `PlaceOrderParams { symbol = "..."; qty = 1 }` (DSL) or constructor with named params
 * - Java: `new PlaceOrderParamsBuilder().symbol("...").qty(1).build()`
 */
class BuilderProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(GenerateBuilder::class.qualifiedName!!)
        val unprocessed = mutableListOf<KSAnnotated>()

        symbols.forEach { symbol ->
            if (symbol is KSClassDeclaration && symbol.classKind == ClassKind.CLASS) {
                if (!symbol.validate()) {
                    unprocessed.add(symbol)
                } else {
                    processClass(symbol)
                }
            } else {
                logger.error("@GenerateBuilder can only be applied to classes", symbol)
            }
        }

        return unprocessed
    }

    /**
     * Data class to hold parameter info with default value literal if available.
     */
    private data class ParamInfo(
        val name: String,
        val type: TypeName,
        val hasDefault: Boolean,
        val isNullable: Boolean,
        val defaultLiteral: String?, // e.g., "0.0", "false", "null"
        val kdoc: String? = null // extracted from @property tag in class KDoc
    )

    private fun processClass(classDecl: KSClassDeclaration) {
        val className = classDecl.toClassName()
        val packageName = classDecl.packageName.asString()
        val builderClassName = ClassName(packageName, "${classDecl.simpleName.asString()}Builder")

        // Get primary constructor parameters
        val constructor = classDecl.primaryConstructor
        if (constructor == null) {
            logger.error("Class must have a primary constructor", classDecl)
            return
        }

        val parameters = constructor.parameters
        if (parameters.isEmpty()) {
            logger.error("Class must have at least one constructor parameter", classDecl)
            return
        }

        // Extract @property docs from class KDoc
        val propertyDocs = extractPropertyDocs(classDecl.docString)

        // Convert to ParamInfo with default value detection and KDoc
        val paramInfos = parameters.map { param ->
            val paramName = param.name!!.asString()
            val paramType = param.type.toTypeName()
            val defaultLiteral = getDefaultValueLiteral(paramType, param.hasDefault)
            ParamInfo(
                name = paramName,
                type = paramType,
                hasDefault = param.hasDefault,
                isNullable = paramType.isNullable,
                defaultLiteral = defaultLiteral,
                kdoc = propertyDocs[paramName]
            )
        }

        // Build the file with builder class and DSL functions
        FileSpec.builder(packageName, "${classDecl.simpleName.asString()}Builder")
            .addType(generateBuilderClass(className, builderClassName, paramInfos))
            .addFunction(generateDslFunction(className, builderClassName))
            .build()
            .writeTo(codeGenerator, Dependencies(true, classDecl.containingFile!!))
    }

    /**
     * Gets a sensible default value literal for common types when parameter has a default.
     */
    private fun getDefaultValueLiteral(type: TypeName, hasDefault: Boolean): String? {
        if (!hasDefault) return null

        // For parameters with defaults, we need to provide a fallback when builder value is null
        // We use sensible defaults based on type
        return when {
            type.isNullable -> "null"
            type == BOOLEAN -> "false"
            type == INT -> "0"
            type == LONG -> "0L"
            type == FLOAT -> "0.0f"
            type == DOUBLE -> "0.0"
            type == STRING -> "\"\""
            else -> null // For custom types, we can't infer a default
        }
    }

    /**
     * Extracts @property documentation from class-level KDoc.
     *
     * Parses KDoc like:
     * ```
     * /**
     *  * @property symbol Symbol in FYERS format (Mandatory)
     *  * @property qty The quantity - should be in multiples of lot size
     *  */
     * ```
     *
     * Returns a map of property name to description.
     */
    private fun extractPropertyDocs(docString: String?): Map<String, String> {
        if (docString.isNullOrBlank()) return emptyMap()

        // Pattern matches @property name description (until next @property or end)
        val propertyPattern = Regex(
            """@property\s+(\w+)\s+(.+?)(?=@property|@param|@return|@throws|@see|@since|\z)""",
            RegexOption.DOT_MATCHES_ALL
        )

        return propertyPattern.findAll(docString)
            .associate { match ->
                val name = match.groupValues[1]
                // Clean up the description - remove extra whitespace and asterisks from multi-line docs
                val description = match.groupValues[2]
                    .replace(Regex("""\s*\*\s*"""), " ")  // Remove KDoc asterisks
                    .replace(Regex("""\s+"""), " ")       // Normalize whitespace
                    .trim()
                name to description
            }
    }

    /**
     * Generates the Builder class with:
     * - Mutable properties for each constructor parameter
     * - Fluent setter methods for Java usage
     * - build() method that constructs the target class
     */
    private fun generateBuilderClass(
        targetClass: ClassName,
        builderClass: ClassName,
        parameters: List<ParamInfo>
    ): TypeSpec {
        val builder = TypeSpec.classBuilder(builderClass)
            .addKdoc("Builder for [%T].\n\nGenerated by @GenerateBuilder annotation processor.", targetClass)

        // Add mutable properties for each parameter
        parameters.forEach { param ->
            // All builder properties are nullable to track if they were set
            val propertyType = param.type.copy(nullable = true)

            // Use extracted KDoc if available for the property
            val propertyBuilder = PropertySpec.builder(param.name, propertyType)
                .mutable()
                .initializer("null")

            // Add KDoc to property for Kotlin DSL usage
            // Use %L to avoid interpreting % as format specifier
            param.kdoc?.let { propertyBuilder.addKdoc("%L", it) }

            builder.addProperty(propertyBuilder.build())

            // Add fluent setter for Java usage - accepts non-null value
            // Use extracted KDoc if available, otherwise fallback to generic description
            // Use %L to avoid interpreting % as format specifier
            val setterKdoc = param.kdoc ?: "Sets the ${param.name} property."
            builder.addFunction(
                FunSpec.builder(param.name)
                    .addKdoc("%L", setterKdoc)
                    .addParameter("value", param.type.copy(nullable = false))
                    .returns(builderClass)
                    .addStatement("return apply { this.%L = value }", param.name)
                    .build()
            )
        }

        // Add build() method
        builder.addFunction(generateBuildMethod(targetClass, parameters))

        return builder.build()
    }

    /**
     * Generates the build() method that:
     * - Validates required parameters (those without defaults)
     * - Constructs the target class with required parameters
     * - Uses copy() to apply optional parameters only if they were set
     *
     * Strategy:
     * - Required parameters (no default, non-null): requireNotNull check, pass to constructor
     * - Optional parameters with defaults: skip in constructor, apply via copy() if set
     * - This allows constructor defaults to be used when builder values are not set
     */
    private fun generateBuildMethod(
        targetClass: ClassName,
        parameters: List<ParamInfo>
    ): FunSpec {
        val buildFun = FunSpec.builder("build")
            .addKdoc("Builds the [%T] instance.\n\n@throws IllegalArgumentException if required parameters are not set", targetClass)
            .returns(targetClass)

        val codeBlock = CodeBlock.builder()

        // Separate required and optional parameters
        val requiredParams = parameters.filter { !it.hasDefault && !it.isNullable }
        val optionalParams = parameters.filter { it.hasDefault || it.isNullable }

        // Validate required parameters
        requiredParams.forEach { param ->
            codeBlock.addStatement(
                "val %LChecked = requireNotNull(%L) { %S }",
                param.name, param.name, "${param.name} is required"
            )
        }

        // Build constructor call with only required parameters
        codeBlock.add("return %T(\n", targetClass)
        requiredParams.forEachIndexed { index, param ->
            val isLast = index == requiredParams.size - 1
            val comma = if (isLast) "" else ","
            codeBlock.add("    %L = %LChecked%L\n", param.name, param.name, comma)
        }
        codeBlock.add(")")

        // Apply optional parameters via copy() if they were set
        if (optionalParams.isNotEmpty()) {
            codeBlock.add(".let { base ->\n")
            codeBlock.add("    var result = base\n")
            optionalParams.forEach { param ->
                codeBlock.add("    %L?.let { result = result.copy(%L = it) }\n", param.name, param.name)
            }
            codeBlock.add("    result\n")
            codeBlock.add("}\n")
        } else {
            codeBlock.add("\n")
        }

        buildFun.addCode(codeBlock.build())
        return buildFun.build()
    }

    /**
     * Generates DSL function for Kotlin usage:
     * ```kotlin
     * inline fun TargetClass(block: TargetClassBuilder.() -> Unit): TargetClass
     * ```
     */
    private fun generateDslFunction(
        targetClass: ClassName,
        builderClass: ClassName
    ): FunSpec {
        return FunSpec.builder(targetClass.simpleName)
            .addKdoc(
                "DSL function to build [%T] using builder syntax.\n\n" +
                "Example:\n```kotlin\nval params = %L {\n    symbol = \"NSE:SBIN-EQ\"\n    qty = 10\n}\n```",
                targetClass, targetClass.simpleName
            )
            .addModifiers(KModifier.INLINE)
            .addParameter(
                "block",
                LambdaTypeName.get(
                    receiver = builderClass,
                    returnType = UNIT
                )
            )
            .returns(targetClass)
            .addStatement("return %T().apply(block).build()", builderClass)
            .build()
    }

}
