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
        var t = raw

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

    fun formatResult(value: Double): String {
        if (value.isNaN()) return "NaN"
        if (value == Double.POSITIVE_INFINITY) return "Infinity"
        if (value == Double.NEGATIVE_INFINITY) return "-Infinity"

        // Round to 8 decimal places as in the Python script
        val rounded = (value * 100_000_000.0).roundToLong() / 100_000_000.0
        val isInt = abs(rounded - rounded.toLong()) < 1e-9

        return if (isInt) {
            rounded.toLong().toString()
        } else {
            // Remove unnecessary trailing zeros
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
                    asin(args[0])
                }
                "acos" -> {
                    checkArgCount(fname, args, 1)
                    acos(args[0])
                }
                "atan" -> {
                    checkArgCount(fname, args, 1)
                    atan(args[0])
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
