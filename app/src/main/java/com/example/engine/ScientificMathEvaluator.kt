package com.example.engine

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.*

/**
 * High-precision scientific math expression evaluator.
 * Safely parses and evaluates mathematical expressions supporting functions,
 * powers, roots, trigonometry, combinatorics, and chained evaluations.
 */
object ScientificMathEvaluator {

    data class EvaluationResult(
        val values: List<Double>,
        val formattedOutputs: List<String>,
        val isSuccess: Boolean,
        val errorMessage: String? = null
    )

    fun evaluate(rawExpression: String): EvaluationResult {
        var expr = rawExpression.trim()
        if (expr.isEmpty()) {
            return EvaluationResult(emptyList(), emptyList(), false, "Expression is empty")
        }

        // Handle chained expressions separated by ";" (e.g. "5 + 5 and 10 - 3")
        val parts = expr.split(";").map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.isEmpty()) {
            return EvaluationResult(emptyList(), emptyList(), false, "Expression is empty")
        }

        val results = mutableListOf<Double>()
        val formatted = mutableListOf<String>()

        for (part in parts) {
            try {
                val singleVal = evaluateSingle(part)
                if (singleVal.isNaN()) {
                    return EvaluationResult(emptyList(), emptyList(), false, "Math error: Undefined result (NaN)")
                }
                if (singleVal.isInfinite()) {
                    return EvaluationResult(emptyList(), emptyList(), false, "Math error: Division by zero or overflow")
                }
                results.add(singleVal)
                formatted.add(formatResult(singleVal))
            } catch (e: Exception) {
                return EvaluationResult(emptyList(), emptyList(), false, e.message ?: "Invalid expression")
            }
        }

        return EvaluationResult(results, formatted, true)
    }

    private fun evaluateSingle(raw: String): Double {
        var t = preprocessExpression(raw)

        // Handle percentage: "%OF%"
        if (t.contains("%OF%")) {
            val pieces = t.split("%OF%")
            if (pieces.size == 2) {
                t = "((${pieces[0].trim()})/100)*(${pieces[1].trim()})"
            }
        }

        // Handle trailing unbalanced parentheses
        val openCount = t.count { it == '(' }
        val closeCount = t.count { it == ')' }
        if (openCount > closeCount) {
            t += ")".repeat(openCount - closeCount)
        }

        val parser = ExpressionParser(t)
        return parser.parse()
    }

    /**
     * Preprocesses math expressions to support:
     * - Unicode multiplication (×, ·, ✕, ✖) and division (÷, ∕)
     * - Square/curly brackets: [ ] { } -> ( )
     * - Implicit multiplication: 2(3+4), (2+3)(4-1), 2sqrt(9), (2)5
     */
    fun preprocessExpression(raw: String): String {
        var s = raw.trim()
        if (s.isEmpty()) return s

        // 1. Bracket normalization
        s = s.replace('[', '(').replace('{', '(')
        s = s.replace(']', ')').replace('}', ')')

        // 2. Unicode multiplication & division operators
        s = s.replace('×', '*').replace('·', '*').replace('✕', '*').replace('✖', '*')
        s = s.replace('÷', '/').replace('∕', '/')
        s = s.replace("²", "^2").replace("³", "^3").replace("⁴", "^4")

        // 2b. Inverse trigonometric notations & symbols:
        s = s.replace("sin⁻¹", "asin").replace("cos⁻¹", "acos").replace("tan⁻¹", "atan")
        s = s.replace("sec⁻¹", "asec").replace("csc⁻¹", "acsc").replace("cosec⁻¹", "acsc").replace("cot⁻¹", "acot")
        s = s.replace("sin^-1", "asin").replace("cos^-1", "acos").replace("tan^-1", "atan")
        s = s.replace("sec^-1", "asec").replace("csc^-1", "acsc").replace("cosec^-1", "acsc").replace("cot^-1", "acot")
        s = s.replace(Regex("\\barcsin\\b", RegexOption.IGNORE_CASE), "asin")
        s = s.replace(Regex("\\barccos\\b", RegexOption.IGNORE_CASE), "acos")
        s = s.replace(Regex("\\barctan\\b", RegexOption.IGNORE_CASE), "atan")
        s = s.replace(Regex("\\barcsec\\b", RegexOption.IGNORE_CASE), "asec")
        s = s.replace(Regex("\\barccsc\\b", RegexOption.IGNORE_CASE), "acsc")
        s = s.replace(Regex("\\barccosec\\b", RegexOption.IGNORE_CASE), "acsc")
        s = s.replace(Regex("\\barccot\\b", RegexOption.IGNORE_CASE), "acot")
        s = s.replace(Regex("\\binvsin\\b", RegexOption.IGNORE_CASE), "asin")
        s = s.replace(Regex("\\binvcos\\b", RegexOption.IGNORE_CASE), "acos")
        s = s.replace(Regex("\\binvtan\\b", RegexOption.IGNORE_CASE), "atan")

        // 3. Trig function powers: e.g. "sin^2(30)" -> "(sin(30))^2", "cos^2 30" -> "(cos(30))^2", "sin^2x" -> "(sin(x))^2"
        s = s.replace(Regex("\\b(sin|cos|tan|asin|acos|atan|asec|acsc|acot|sinh|cosh|tanh|sec|csc|cosec|cot)\\s*\\^\\s*(\\d+(?:\\.\\d+)?)\\s*\\(([^()]+)\\)", RegexOption.IGNORE_CASE)) { mr ->
            "(${mr.groupValues[1]}(${mr.groupValues[3]}))^${mr.groupValues[2]}"
        }
        s = s.replace(Regex("\\b(sin|cos|tan|asin|acos|atan|asec|acsc|acot|sinh|cosh|tanh|sec|csc|cosec|cot)\\s*\\^\\s*(\\d+(?:\\.\\d+)?)\\s*([a-zA-Z0-9_.]+(?:\\([^()]+\\))?)", RegexOption.IGNORE_CASE)) { mr ->
            val arg = mr.groupValues[3].removePrefix("(").removeSuffix(")")
            "(${mr.groupValues[1]}($arg))^${mr.groupValues[2]}"
        }

        // 3b. Trig and transcendental functions with variable: "sinx" -> "sin(x)", "sin x" -> "sin(x)", "lnx" -> "ln(x)"
        s = s.replace(Regex("\\b(sin|cos|tan|asin|acos|atan|asec|acsc|acot|sinh|cosh|tanh|sec|csc|cosec|cot|ln|log|exp)\\s*([xyz]|theta)\\b", RegexOption.IGNORE_CASE)) { mr ->
            val v = if (mr.groupValues[2].equals("theta", ignoreCase = true)) "x" else mr.groupValues[2]
            "${mr.groupValues[1]}($v)"
        }
        s = s.replace(Regex("\\b(sin|cos|tan|asin|acos|atan|asec|acsc|acot|sinh|cosh|tanh|sec|csc|cosec|cot|exp)\\s+([a-zA-Z0-9_.]+(?:\\^[0-9]+)?)(?!\\s*\\()", RegexOption.IGNORE_CASE)) { mr ->
            "${mr.groupValues[1]}(${mr.groupValues[2]})"
        }

        // 3. Spaced 'x' or 'X' between numbers as multiplication (e.g. "3 x 4", "2 x (3+4)", "(2+3) x 5")
        s = s.replace(Regex("(\\d+(?:\\.\\d+)?|\\))\\s+[xX]\\s+(\\d+(?:\\.\\d+)?|\\()")) { mr ->
            "${mr.groupValues[1]}*${mr.groupValues[2]}"
        }

        // 4. Implicit multiplication
        // Variable followed by function or constant: "x sin(x)" -> "x*sin(x)", "x e^x" -> "x*e^x", "x ln(x)" -> "x*ln(x)"
        s = s.replace(Regex("\\b([xyz])\\s*(sqrt|cbrt|sin|cos|tan|asin|acos|atan|sinh|cosh|tanh|sec|csc|cosec|cot|log|ln|exp|pi|phi|E(?![a-zA-Z0-9]))", RegexOption.IGNORE_CASE)) { mr ->
            "${mr.groupValues[1]}*${mr.groupValues[2]}"
        }
        // Variable followed by '(': "x(x+1)" -> "x*(x+1)"
        s = s.replace(Regex("\\b([xyz])\\s*\\(")) { mr ->
            "${mr.groupValues[1]}*("
        }
        // Variable followed by variable: "x y" -> "x*y", "x x" -> "x*x"
        s = s.replace(Regex("\\b([xyz])\\s+([xyz])\\b")) { mr ->
            "${mr.groupValues[1]}*${mr.groupValues[2]}"
        }
        // Number followed by variable: "2 x" -> "2*x"
        s = s.replace(Regex("(\\d+(?:\\.\\d+)?)\\s*([xyz])\\b")) { mr ->
            "${mr.groupValues[1]}*${mr.groupValues[2]}"
        }

        // Number followed by '(': "2(3+4)" -> "2*(3+4)"
        s = s.replace(Regex("(\\d+(?:\\.\\d+)?)\\s*\\(")) { mr ->
            "${mr.groupValues[1]}*("
        }
        // ')' followed by '(': "(2+3)(4-1)" -> "(2+3)*(4-1)"
        s = s.replace(Regex("\\)\\s*\\(")) {
            ")*("
        }
        // ')' followed by number: "(2+3)5" -> "(2+3)*5"
        s = s.replace(Regex("\\)\\s*(\\d+(?:\\.\\d+)?)")) { mr ->
            ")*${mr.groupValues[1]}"
        }
        // ')' followed by letter/variable/function: "(2)sqrt(9)" -> "(2)*sqrt(9)", "(sin(x))cos(x)" -> "(sin(x))*cos(x)"
        s = s.replace(Regex("\\)\\s*([a-zA-Z])")) { mr ->
            ")*${mr.groupValues[1]}"
        }
        // Number followed by known function or constant (e.g. 2pi, 3sqrt, 2sin, 3log):
        s = s.replace(Regex("(\\d+(?:\\.\\d+)?)\\s*(sqrt|cbrt|sin|cos|tan|asin|acos|atan|log|ln|exp|pi|phi|E(?!\\d))", RegexOption.IGNORE_CASE)) { mr ->
            "${mr.groupValues[1]}*${mr.groupValues[2]}"
        }

        return s
    }

    fun formatDecimalOnly(value: Double): String {
        if (value.isNaN()) return "NaN"
        if (value == Double.POSITIVE_INFINITY) return "Infinity"
        if (value == Double.NEGATIVE_INFINITY) return "-Infinity"

        val rounded = (value * 100_000_000.0).roundToLong() / 100_000_000.0
        val isInt = abs(rounded - rounded.toLong()) < 1e-9

        return if (isInt) {
            rounded.toLong().toString()
        } else {
            try {
                BigDecimal.valueOf(rounded)
                    .setScale(8, RoundingMode.HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString()
            } catch (_: Exception) {
                String.format(java.util.Locale.US, "%.8f", rounded).trimEnd('0').trimEnd('.')
            }
        }
    }

    fun formatShort(value: Double): String {
        val rounded = (value * 10000.0).roundToLong() / 10000.0
        val isInt = abs(rounded - rounded.toLong()) < 1e-9
        return if (isInt) {
            rounded.toLong().toString()
        } else {
            String.format(java.util.Locale.US, "%.4f", rounded).trimEnd('0').trimEnd('.')
        }
    }

    fun formatResult(value: Double): String {
        val decStr = formatDecimalOnly(value)
        if (value.isNaN() || value.isInfinite()) return decStr

        val rounded = (value * 100_000_000.0).roundToLong() / 100_000_000.0
        val isInt = abs(rounded - rounded.toLong()) < 1e-9
        if (isInt) return decStr

        val frac = UniversalEquationSolver.toFractionString(value)
        return if (frac != null) {
            "$frac ≈ $decStr"
        } else {
            decStr
        }
    }

    private class ExpressionParser(val text: String) {
        private var pos = 0
        private val length = text.length

        fun parse(): Double {
            val result = parseExpression()
            skipWhitespace()
            if (pos < length) {
                throw IllegalArgumentException("Unexpected character '${text[pos]}' at position $pos")
            }
            return result
        }

        // Expression -> Term ( ('+' | '-') Term )*
        private fun parseExpression(): Double {
            var value = parseTerm()
            while (true) {
                skipWhitespace()
                if (pos >= length) break
                val c = text[pos]
                if (c == '+' || c == '-') {
                    pos++
                    val nextTerm = parseTerm()
                    value = if (c == '+') value + nextTerm else value - nextTerm
                } else {
                    break
                }
            }
            return value
        }

        // Term -> Power ( ('*' | '/' | '%') Power )*
        private fun parseTerm(): Double {
            var value = parsePower()
            while (true) {
                skipWhitespace()
                if (pos >= length) break
                val c = text[pos]
                if (c == '*' && (pos + 1 < length && text[pos + 1] == '*')) {
                    // Power operator handled in parsePower, break here
                    break
                }
                if (c == '*' || c == '/' || c == '%') {
                    pos++
                    val nextPower = parsePower()
                    value = when (c) {
                        '*' -> value * nextPower
                        '/' -> {
                            if (abs(nextPower) < 1e-15) throw ArithmeticException("Division by zero")
                            value / nextPower
                        }
                        '%' -> value % nextPower
                        else -> value
                    }
                } else if (c == '(' || c.isLetter()) {
                    // Implicit multiplication fallback: e.g. 2(3+4), (2+3)(4-1), 2pi
                    val nextPower = parsePower()
                    value *= nextPower
                } else {
                    break
                }
            }
            return value
        }

        // Power -> Unary ( ('**' | '^') Power )? (Right-associative)
        private fun parsePower(): Double {
            val base = parseUnary()
            skipWhitespace()
            if (pos < length) {
                if (pos + 1 < length && text[pos] == '*' && text[pos + 1] == '*') {
                    pos += 2
                    val exponent = parsePower()
                    return base.pow(exponent)
                } else if (text[pos] == '^') {
                    pos++
                    val exponent = parsePower()
                    return base.pow(exponent)
                }
            }
            return base
        }

        // Unary -> ('+' | '-')? Primary
        private fun parseUnary(): Double {
            skipWhitespace()
            if (pos >= length) throw IllegalArgumentException("Unexpected end of expression")
            val c = text[pos]
            if (c == '+') {
                pos++
                return parseUnary()
            }
            if (c == '-') {
                pos++
                return -parseUnary()
            }
            return parsePrimary()
        }

        // Primary -> Number | Identifier / Function | '(' Expression ')'
        private fun parsePrimary(): Double {
            skipWhitespace()
            if (pos >= length) throw IllegalArgumentException("Unexpected end of expression")

            val c = text[pos]
            if (c == '(') {
                pos++
                val value = parseExpression()
                skipWhitespace()
                if (pos >= length || text[pos] != ')') {
                    throw IllegalArgumentException("Missing closing parenthesis ')'")
                }
                pos++
                return value
            }

            if (c.isDigit() || c == '.') {
                return parseNumber()
            }

            if (c.isLetter()) {
                return parseIdentifierOrFunction()
            }

            throw IllegalArgumentException("Unexpected token '$c' at position $pos")
        }

        private fun parseNumber(): Double {
            val start = pos
            var hasDot = false
            while (pos < length) {
                val ch = text[pos]
                if (ch.isDigit()) {
                    pos++
                } else if (ch == '.' && !hasDot) {
                    hasDot = true
                    pos++
                } else {
                    break
                }
            }
            val numStr = text.substring(start, pos)
            return numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $numStr")
        }

        private fun parseIdentifierOrFunction(): Double {
            val start = pos
            while (pos < length && (text[pos].isLetterOrDigit() || text[pos] == '_')) {
                pos++
            }
            val name = text.substring(start, pos)
            skipWhitespace()

            // Check if it's a function call with '('
            if (pos < length && text[pos] == '(') {
                pos++ // consume '('
                val args = mutableListOf<Double>()
                skipWhitespace()
                if (pos < length && text[pos] != ')') {
                    while (true) {
                        args.add(parseExpression())
                        skipWhitespace()
                        if (pos < length && text[pos] == ',') {
                            pos++ // consume ','
                        } else {
                            break
                        }
                    }
                }
                skipWhitespace()
                if (pos >= length || text[pos] != ')') {
                    throw IllegalArgumentException("Missing closing ')' for function '$name'")
                }
                pos++ // consume ')'
                return executeFunction(name, args)
            }

            // It's a named constant or variable
            return when (name.lowercase()) {
                "pi" -> Math.PI
                "e" -> Math.E
                else -> throw IllegalArgumentException("Unknown symbol: '$name'")
            }
        }

        private fun executeFunction(name: String, args: List<Double>): Double {
            val fname = name.lowercase()
            return when (fname) {
                "sin" -> {
                    checkArgCount(fname, args, 1)
                    sin(args[0])
                }
                "cos" -> {
                    checkArgCount(fname, args, 1)
                    cos(args[0])
                }
                "tan" -> {
                    checkArgCount(fname, args, 1)
                    tan(args[0])
                }
                "asin" -> {
                    checkArgCount(fname, args, 1)
                    val x = args[0]
                    if (x < -1.0 || x > 1.0) throw IllegalArgumentException("asin domain error: input must be in [-1, 1]")
                    asin(x)
                }
                "acos" -> {
                    checkArgCount(fname, args, 1)
                    val x = args[0]
                    if (x < -1.0 || x > 1.0) throw IllegalArgumentException("acos domain error: input must be in [-1, 1]")
                    acos(x)
                }
                "atan" -> {
                    checkArgCount(fname, args, 1)
                    atan(args[0])
                }
                "asec" -> {
                    checkArgCount(fname, args, 1)
                    val x = args[0]
                    if (abs(x) < 1.0) throw IllegalArgumentException("asec domain error: |x| must be >= 1")
                    acos(1.0 / x)
                }
                "acsc" -> {
                    checkArgCount(fname, args, 1)
                    val x = args[0]
                    if (abs(x) < 1.0) throw IllegalArgumentException("acsc domain error: |x| must be >= 1")
                    asin(1.0 / x)
                }
                "acot" -> {
                    checkArgCount(fname, args, 1)
                    Math.PI / 2.0 - atan(args[0])
                }
                "asinh" -> {
                    checkArgCount(fname, args, 1)
                    val x = args[0]
                    ln(x + sqrt(x * x + 1.0))
                }
                "acosh" -> {
                    checkArgCount(fname, args, 1)
                    val x = args[0]
                    if (x < 1.0) throw IllegalArgumentException("acosh domain error: x must be >= 1")
                    ln(x + sqrt(x * x - 1.0))
                }
                "atanh" -> {
                    checkArgCount(fname, args, 1)
                    val x = args[0]
                    if (abs(x) >= 1.0) throw IllegalArgumentException("atanh domain error: |x| must be < 1")
                    0.5 * ln((1.0 + x) / (1.0 - x))
                }
                "sinh" -> {
                    checkArgCount(fname, args, 1)
                    sinh(args[0])
                }
                "cosh" -> {
                    checkArgCount(fname, args, 1)
                    cosh(args[0])
                }
                "tanh" -> {
                    checkArgCount(fname, args, 1)
                    tanh(args[0])
                }
                "sec" -> {
                    checkArgCount(fname, args, 1)
                    val cosVal = cos(args[0])
                    if (abs(cosVal) < 1e-15) throw ArithmeticException("Division by zero (sec undefined)")
                    1.0 / cosVal
                }
                "csc", "cosec" -> {
                    checkArgCount(fname, args, 1)
                    val sinVal = sin(args[0])
                    if (abs(sinVal) < 1e-15) throw ArithmeticException("Division by zero (csc undefined)")
                    1.0 / sinVal
                }
                "cot" -> {
                    checkArgCount(fname, args, 1)
                    val tanVal = tan(args[0])
                    if (abs(tanVal) < 1e-15) throw ArithmeticException("Division by zero (cot undefined)")
                    1.0 / tanVal
                }
                "sqrt" -> {
                    checkArgCount(fname, args, 1)
                    if (args[0] < 0) throw IllegalArgumentException("Cannot take square root of negative number")
                    sqrt(args[0])
                }
                "abs" -> {
                    checkArgCount(fname, args, 1)
                    abs(args[0])
                }
                "floor" -> {
                    checkArgCount(fname, args, 1)
                    floor(args[0])
                }
                "ceiling", "ceil" -> {
                    checkArgCount(fname, args, 1)
                    ceil(args[0])
                }
                "factorial", "fact" -> {
                    checkArgCount(fname, args, 1)
                    computeFactorial(args[0])
                }
                "log" -> {
                    when (args.size) {
                        1 -> ln(args[0])
                        2 -> {
                            // log(value, base)
                            val value = args[0]
                            val base = args[1]
                            if (value <= 0 || base <= 0 || base == 1.0) {
                                throw IllegalArgumentException("Invalid arguments for log($value, base=$base)")
                            }
                            ln(value) / ln(base)
                        }
                        else -> throw IllegalArgumentException("Function 'log' requires 1 or 2 arguments")
                    }
                }
                "ln" -> {
                    checkArgCount(fname, args, 1)
                    ln(args[0])
                }
                "exp" -> {
                    checkArgCount(fname, args, 1)
                    exp(args[0])
                }
                "log10" -> {
                    checkArgCount(fname, args, 1)
                    log10(args[0])
                }
                "mod" -> {
                    checkArgCount(fname, args, 2)
                    args[0] % args[1]
                }
                "binomial", "ncr", "combinations" -> {
                    checkArgCount(fname, args, 2)
                    computeBinomial(args[0].toLong(), args[1].toLong())
                }
                "permutations", "npr" -> {
                    checkArgCount(fname, args, 2)
                    computePermutations(args[0].toLong(), args[1].toLong())
                }
                else -> throw IllegalArgumentException("Unknown function '$name'")
            }
        }

        private fun checkArgCount(fnName: String, args: List<Double>, expected: Int) {
            if (args.size != expected) {
                throw IllegalArgumentException("Function '$fnName' expects $expected argument(s), got ${args.size}")
            }
        }

        private fun computeFactorial(n: Double): Double {
            if (n < 0 || n != floor(n)) {
                throw IllegalArgumentException("Factorial is only defined for non-negative integers")
            }
            val intVal = n.toLong()
            if (intVal > 170) throw ArithmeticException("Factorial overflow for n > 170")
            var res = 1.0
            for (i in 2..intVal) {
                res *= i
            }
            return res
        }

        private fun computeBinomial(n: Long, k: Long): Double {
            if (k < 0 || k > n || n < 0) return 0.0
            var res = 1.0
            val effectiveK = min(k, n - k)
            for (i in 1..effectiveK) {
                res = res * (n - effectiveK + i) / i
            }
            return res
        }

        private fun computePermutations(n: Long, r: Long): Double {
            if (r < 0 || r > n || n < 0) return 0.0
            var res = 1.0
            for (i in 0 until r) {
                res *= (n - i)
            }
            return res
        }

        private fun skipWhitespace() {
            while (pos < length && text[pos].isWhitespace()) {
                pos++
            }
        }
    }
}
