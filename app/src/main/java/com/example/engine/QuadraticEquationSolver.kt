package com.example.engine

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Advanced solver for all types of quadratic equations:
 * a*x^2 + b*x + c = 0
 *
 * Supports:
 * - Two distinct real roots (Δ > 0)
 * - Single repeated real root (Δ = 0)
 * - Complex conjugate roots with imaginary unit i (Δ < 0)
 * - Degenerate linear equations (a = 0, bx + c = 0)
 * - Step-by-step solution derivation
 * - Parabola vertex & axis of symmetry
 * - Parsing from strings like "2x^2 - 5x + 3 = 0", "x² + 4 = 0", "3x^2 = 12x - 5"
 */
object QuadraticEquationSolver {

    data class QuadraticSolution(
        val a: Double,
        val b: Double,
        val c: Double,
        val discriminant: Double,
        val rootType: RootType,
        val root1Text: String,
        val root2Text: String,
        val vertexX: Double,
        val vertexY: Double,
        val spokenSummary: String,
        val steps: List<String>
    )

    enum class RootType {
        TWO_REAL,
        ONE_REAL_REPEATED,
        COMPLEX_CONJUGATE,
        LINEAR,
        NO_SOLUTION,
        INFINITE_SOLUTIONS
    }

    /**
     * Solves quadratic equation given coefficients a, b, c.
     */
    fun solve(a: Double, b: Double, c: Double): QuadraticSolution {
        val steps = mutableListOf<String>()

        steps.add("Standard form: ax² + bx + c = 0")
        val aFmt = formatNum(a)
        val bFmt = (if (b >= 0) "+ " else "- ") + formatNum(abs(b))
        val cFmt = (if (c >= 0) "+ " else "- ") + formatNum(abs(c))
        steps.add("Equation: ${if (a == 1.0) "" else if (a == -1.0) "-" else aFmt}x² $bFmt x $cFmt = 0")
        steps.add("Coefficients: a = $aFmt, b = ${formatNum(b)}, c = ${formatNum(c)}")

        if (abs(a) < 1e-12) {
            // Degenerate linear equation: bx + c = 0
            if (abs(b) < 1e-12) {
                if (abs(c) < 1e-12) {
                    return QuadraticSolution(
                        a = a, b = b, c = c,
                        discriminant = 0.0,
                        rootType = RootType.INFINITE_SOLUTIONS,
                        root1Text = "All real numbers",
                        root2Text = "All real numbers",
                        vertexX = 0.0, vertexY = 0.0,
                        spokenSummary = "Identity equation: 0 = 0. Infinite solutions exist.",
                        steps = listOf("Equation reduces to 0 = 0. Infinite solutions.")
                    )
                } else {
                    return QuadraticSolution(
                        a = a, b = b, c = c,
                        discriminant = 0.0,
                        rootType = RootType.NO_SOLUTION,
                        root1Text = "No solution",
                        root2Text = "No solution",
                        vertexX = 0.0, vertexY = 0.0,
                        spokenSummary = "Contradiction: no solutions exist.",
                        steps = listOf("Equation reduces to ${formatNum(c)} = 0. Contradiction.")
                    )
                }
            } else {
                val linRoot = -c / b
                val linRootFmt = formatNum(linRoot)
                steps.add("Linear equation bx + c = 0: x = -c / b = -$cFmt / ${formatNum(b)} = $linRootFmt")
                return QuadraticSolution(
                    a = a, b = b, c = c,
                    discriminant = 0.0,
                    rootType = RootType.LINEAR,
                    root1Text = linRootFmt,
                    root2Text = linRootFmt,
                    vertexX = linRoot, vertexY = 0.0,
                    spokenSummary = "Linear equation with one root: x equals $linRootFmt",
                    steps = steps
                )
            }
        }

        // Quadratic formula: x = (-b ± sqrt(b² - 4ac)) / (2a)
        val d = (b * b) - (4 * a * c)
        val dFmt = formatNum(d)
        steps.add("Discriminant Δ = b² - 4ac = (${formatNum(b)})² - 4($aFmt)(${formatNum(c)}) = $dFmt")

        val vertexX = -b / (2 * a)
        val vertexY = c - ((b * b) / (4 * a))
        val vertexXFmt = formatNum(vertexX)
        val vertexYFmt = formatNum(vertexY)
        steps.add("Parabola vertex: ($vertexXFmt, $vertexYFmt), axis of symmetry: x = $vertexXFmt")

        if (d > 1e-9) {
            // Two distinct real roots
            val sqrtD = sqrt(d)
            val r1 = (-b + sqrtD) / (2 * a)
            val r2 = (-b - sqrtD) / (2 * a)
            val r1Fmt = formatNum(r1)
            val r2Fmt = formatNum(r2)

            steps.add("Δ > 0: Two distinct real roots.")
            steps.add("x = (-b ± √Δ) / 2a")
            steps.add("x₁ = (-(${formatNum(b)}) + √$dFmt) / (2 * $aFmt) = $r1Fmt")
            steps.add("x₂ = (-(${formatNum(b)}) - √$dFmt) / (2 * $aFmt) = $r2Fmt")

            val spoken = "Two real roots: x equals $r1Fmt and x equals $r2Fmt"
            return QuadraticSolution(
                a = a, b = b, c = c,
                discriminant = d,
                rootType = RootType.TWO_REAL,
                root1Text = r1Fmt,
                root2Text = r2Fmt,
                vertexX = vertexX,
                vertexY = vertexY,
                spokenSummary = spoken,
                steps = steps
            )
        } else if (abs(d) <= 1e-9) {
            // Single repeated real root
            val r = -b / (2 * a)
            val rFmt = formatNum(r)

            steps.add("Δ = 0: Exactly one repeated real root (double root).")
            steps.add("x = -b / 2a = -(${formatNum(b)}) / (2 * $aFmt) = $rFmt")

            val spoken = "One repeated real root: x equals $rFmt"
            return QuadraticSolution(
                a = a, b = b, c = c,
                discriminant = 0.0,
                rootType = RootType.ONE_REAL_REPEATED,
                root1Text = rFmt,
                root2Text = rFmt,
                vertexX = vertexX,
                vertexY = vertexY,
                spokenSummary = spoken,
                steps = steps
            )
        } else {
            // Complex conjugate roots: x = -b/2a ± i * sqrt(-d)/2a
            val realPart = -b / (2 * a)
            val imagPart = abs(sqrt(-d) / (2 * a))

            val realFmt = formatNum(realPart)
            val imagFmt = formatNum(imagPart)

            val r1Text = if (abs(realPart) < 1e-9) "${if (imagPart == 1.0) "" else imagFmt}i"
                         else "$realFmt + ${if (imagPart == 1.0) "" else imagFmt}i"
            val r2Text = if (abs(realPart) < 1e-9) "-${if (imagPart == 1.0) "" else imagFmt}i"
                         else "$realFmt - ${if (imagPart == 1.0) "" else imagFmt}i"

            steps.add("Δ < 0: Two complex conjugate roots involving imaginary unit i.")
            steps.add("√Δ = √($dFmt) = ${if (imagPart * (2 * a) == 1.0) "" else formatNum(sqrt(-d))}i")
            steps.add("x₁ = $r1Text")
            steps.add("x₂ = $r2Text")

            val spoken = "Two complex roots: $r1Text and $r2Text"
            return QuadraticSolution(
                a = a, b = b, c = c,
                discriminant = d,
                rootType = RootType.COMPLEX_CONJUGATE,
                root1Text = r1Text,
                root2Text = r2Text,
                vertexX = vertexX,
                vertexY = vertexY,
                spokenSummary = spoken,
                steps = steps
            )
        }
    }

    /**
     * Parses an equation string like "x^2 - 5x + 6 = 0", "2x² + 4x - 6", "x^2 + 4 = 0", "3x^2 = 12x - 5".
     * Extracts a, b, c and solves the quadratic equation.
     */
    fun parseAndSolve(equationStr: String): QuadraticSolution? {
        val coeffs = extractCoefficients(equationStr) ?: return null
        return solve(coeffs.first, coeffs.second, coeffs.third)
    }

    /**
     * Extracts coefficients (a, b, c) from a natural quadratic equation string.
     */
    fun extractCoefficients(raw: String): Triple<Double, Double, Double>? {
        var str = raw.lowercase().trim()
        str = str.replace("²", "^2")
        str = str.replace(" ", "")

        // Split by '=' if present
        val sides = str.split("=")
        val lhs = sides[0]
        val rhs = if (sides.size > 1) sides[1] else "0"

        val lhsTerms = parseSide(lhs) ?: return null
        val rhsTerms = parseSide(rhs) ?: return null

        val a = lhsTerms.first - rhsTerms.first
        val b = lhsTerms.second - rhsTerms.second
        val c = lhsTerms.third - rhsTerms.third

        if (abs(a) < 1e-12 && abs(b) < 1e-12 && abs(c) < 1e-12) {
            return null
        }

        return Triple(a, b, c)
    }

    private fun parseSide(side: String): Triple<Double, Double, Double>? {
        if (side.isEmpty() || side == "0") return Triple(0.0, 0.0, 0.0)

        var s = side
        // Insert '+' before '-' if not at start
        s = s.replace("-", "+-")
        if (s.startsWith("+-")) s = s.substring(1)

        val tokens = s.split("+").filter { it.isNotEmpty() }
        var a = 0.0
        var b = 0.0
        var c = 0.0

        for (token in tokens) {
            when {
                token.contains("x^2") -> {
                    val coefStr = token.replace("x^2", "")
                    val coef = when (coefStr) {
                        "", "+" -> 1.0
                        "-" -> -1.0
                        else -> coefStr.toDoubleOrNull() ?: return null
                    }
                    a += coef
                }
                token.contains("x") -> {
                    val coefStr = token.replace("x", "")
                    val coef = when (coefStr) {
                        "", "+" -> 1.0
                        "-" -> -1.0
                        else -> coefStr.toDoubleOrNull() ?: return null
                    }
                    b += coef
                }
                else -> {
                    val coef = token.toDoubleOrNull() ?: return null
                    c += coef
                }
            }
        }
        return Triple(a, b, c)
    }

    private fun formatNum(n: Double): String {
        val rounded = (n * 100_000.0).let { Math.round(it) } / 100_000.0
        return if (abs(rounded - rounded.toLong()) < 1e-7) {
            rounded.toLong().toString()
        } else {
            try {
                BigDecimal.valueOf(rounded)
                    .setScale(4, RoundingMode.HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString()
            } catch (_: Exception) {
                String.format(java.util.Locale.US, "%.4f", rounded).trimEnd('0').trimEnd('.')
            }
        }
    }
}
