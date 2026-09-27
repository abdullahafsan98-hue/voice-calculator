package com.example.engine

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.*

/**
 * Universal Equation Solver capable of solving:
 * 1. Single Variable Linear Equations (e.g. 3x + 5 = 20, 2(x - 3) = 4x + 10)
 * 2. Quadratic Equations (ax² + bx + c = 0) with real & complex roots
 * 3. Cubic Equations (ax³ + bx² + cx + d = 0) using Cardano's formula & factoring
 * 4. Polynomial Equations (e.g. x⁴ - 16 = 0, x⁴ - 5x² + 4 = 0)
 * 5. System of 2 Linear Equations (e.g. 2x + y = 7, x - y = 2) via Cramer's Rule
 * 6. System of 3 Linear Equations in x, y, z
 * 7. Non-linear & Transcendental Equations (e.g. cos(x) = x, e^x = 10, 2^x = 16, sqrt(x+5) = 4)
 *    using Newton-Raphson & Bracketed Bisection root finding.
 */
object UniversalEquationSolver {

    enum class EquationCategory {
        LINEAR_1VAR,
        QUADRATIC,
        CUBIC,
        SYSTEM_2VAR,
        SYSTEM_3VAR,
        POLYNOMIAL,
        TRANSCENDENTAL_NUMERICAL,
        IDENTITY,
        CONTRADICTION
    }

    data class EquationSolution(
        val category: EquationCategory,
        val originalEquation: String,
        val rootsSummary: String,
        val variables: Map<String, String>, // e.g. "x" -> "3", "y" -> "1"
        val steps: List<String>,
        val spokenSummary: String,
        val spokenSummaryHinglish: String = "",
        val isExact: Boolean = true
    )

    /**
     * Master solve function. Analyzes the input string, auto-detects equation category,
     * and applies the appropriate solver.
     */
    fun solveAny(rawInput: String): EquationSolution? {
        val clean = rawInput.trim()
        if (clean.isEmpty()) return null

        // Check if it's a system of equations separated by ';' or 'and' or ','
        val systemSeparators = Regex("\\s+(?:and|&)\\s+|;|,(?=\\s*[-+]?\\d*\\s*[xyz])", RegexOption.IGNORE_CASE)
        val systemParts = clean.split(systemSeparators).map { it.trim() }.filter { it.isNotEmpty() }

        if (systemParts.size == 2 && systemParts.all { it.contains("=") || it.contains("x") || it.contains("y") }) {
            val sys2 = solveSystem2Var(systemParts[0], systemParts[1])
            if (sys2 != null) return sys2
        }

        if (systemParts.size == 3 && systemParts.all { it.contains("=") }) {
            val sys3 = solveSystem3Var(systemParts[0], systemParts[1], systemParts[2])
            if (sys3 != null) return sys3
        }

        // Single equation processing
        val hasOriginalEquals = clean.contains("=")
        val eq = if (!hasOriginalEquals) "$clean = 0" else clean
        val sides = eq.split("=")
        if (sides.size != 2) return null

        val lhsStr = sides[0].trim()
        val rhsStr = sides[1].trim()

        // 1. Try Polynomial Solver (Linear, Quadratic, Cubic, Quartic)
        val polySol = trySolvePolynomial(lhsStr, rhsStr, eq, hasOriginalEquals)
        if (polySol != null) return polySol

        // 2. Try Transcendental / Numerical Solver (e.g. cos(x) = x, e^x = 10, 2^x = 16, sqrt(x+5) = 4)
        val numSol = solveNumericalTranscendental(lhsStr, rhsStr, eq)
        if (numSol != null) return numSol

        return null
    }

    /**
     * Converts a double to its exact or approximate rational fraction string (e.g. 6.8181818 -> "75/11", -1.666667 -> "-5/3").
     * Returns null if already an integer or denominator > maxDenominator.
     */
    fun toFractionString(value: Double, maxDenominator: Long = 10000): String? {
        if (value.isNaN() || value.isInfinite()) return null
        val sign = if (value < 0) "-" else ""
        val v = abs(value)
        val rounded = round(v)
        if (abs(v - rounded) < 1e-9) return null // Already a whole integer

        var h1 = 1L; var h0 = 0L
        var k1 = 0L; var k0 = 1L
        var b = v
        for (i in 0..25) {
            val a = b.toLong()
            val h2 = a * h1 + h0
            val k2 = a * k1 + k0
            if (k2 > maxDenominator || k2 < 0) break
            h0 = h1; h1 = h2
            k0 = k1; k1 = k2
            val diff = b - a
            if (abs(diff) < 1e-9) break
            b = 1.0 / diff
            if (k1 > 1 && abs(v - h1.toDouble() / k1.toDouble()) < 1e-6) break
        }

        if (k1 in 2..maxDenominator && abs(v - h1.toDouble() / k1.toDouble()) < 1e-5) {
            return "$sign$h1/$k1"
        }
        return null
    }

    /**
     * Preprocesses fraction coefficients and expressions for any variable:
     * - (1/3)x, 1/3x, 1/3 x, (2/5)x^2, 3/4 x^3
     * - x/3, 2x/5, -x/4, 3x^2/5
     * - Constant fractions like + 1/8, - (2/5), 3/4
     */
    fun preprocessFractions(raw: String, vars: List<Char> = listOf('x', 'y', 'z')): String {
        var s = raw.lowercase().replace("²", "^2").replace("³", "^3").replace("⁴", "^4")
        for (v in vars) {
            // 1. Variable divided by number: e.g. "x/3", "2x/5", "-x/4", "3x^2/5"
            s = s.replace(Regex("([+-]?)\\s*(\\d+(?:\\.\\d+)?)?\\s*$v(?:\\^(\\d+))?\\s*/\\s*(\\d+(?:\\.\\d+)?)")) { mr ->
                val sign = mr.groupValues[1]
                val numStr = mr.groupValues[2]
                val powStr = mr.groupValues[3]
                val denStr = mr.groupValues[4]
                val num = if (numStr.isEmpty()) 1.0 else numStr.toDoubleOrNull() ?: 1.0
                val den = denStr.toDoubleOrNull() ?: 1.0
                val coef = (if (sign == "-") -num else num) / den
                val powPart = if (powStr.isNotEmpty()) "$v^$powStr" else "$v"
                val signPrefix = if (coef >= 0) "+$coef" else "$coef"
                "$signPrefix$powPart"
            }

            // 2. Parenthesized fraction before variable: e.g. "(1/3)x", "(1/3)*x", "(-2/5)x^2", "+(2/5)x"
            s = s.replace(Regex("([+-]?)\\s*\\(\\s*([+-]?\\d+(?:\\.\\d+)?)\\s*/\\s*(\\d+(?:\\.\\d+)?)\\s*\\)\\s*\\*?\\s*$v(?:\\^(\\d+))?")) { mr ->
                val outerSign = mr.groupValues[1]
                val numStr = mr.groupValues[2]
                val denStr = mr.groupValues[3]
                val powStr = mr.groupValues[4]
                val num = numStr.toDoubleOrNull() ?: 1.0
                val den = denStr.toDoubleOrNull() ?: 1.0
                var coef = num / den
                if (outerSign == "-") coef = -coef
                val powPart = if (powStr.isNotEmpty()) "$v^$powStr" else "$v"
                val signPrefix = if (coef >= 0) "+$coef" else "$coef"
                "$signPrefix$powPart"
            }

            // 3. Simple fraction before variable: e.g. "1/3x", "+ 2/5 x", "- 3/4x^2"
            s = s.replace(Regex("([+-]?)\\s*(\\d+(?:\\.\\d+)?)\\s*/\\s*(\\d+(?:\\.\\d+)?)\\s*\\*?\\s*$v(?:\\^(\\d+))?")) { mr ->
                val sign = mr.groupValues[1]
                val numStr = mr.groupValues[2]
                val denStr = mr.groupValues[3]
                val powStr = mr.groupValues[4]
                val num = numStr.toDoubleOrNull() ?: 1.0
                val den = denStr.toDoubleOrNull() ?: 1.0
                val coef = (if (sign == "-") -num else num) / den
                val powPart = if (powStr.isNotEmpty()) "$v^$powStr" else "$v"
                val signPrefix = if (coef >= 0) "+$coef" else "$coef"
                "$signPrefix$powPart"
            }
        }

        // 4. Parenthesized constant fractions: e.g. "(1/8)", "-(2/5)"
        s = s.replace(Regex("([+-]?)\\s*\\(\\s*([+-]?\\d+(?:\\.\\d+)?)\\s*/\\s*(\\d+(?:\\.\\d+)?)\\s*\\)(?!\\s*[a-zA-Z])")) { mr ->
            val outerSign = mr.groupValues[1]
            val num = mr.groupValues[2].toDoubleOrNull() ?: 0.0
            val den = mr.groupValues[3].toDoubleOrNull() ?: 1.0
            var coef = num / den
            if (outerSign == "-") coef = -coef
            if (coef >= 0) "+$coef" else "$coef"
        }

        // 5. Unparenthesized constant fractions: e.g. "+ 1/8", "- 2/5"
        s = s.replace(Regex("([+-])\\s*(\\d+(?:\\.\\d+)?)\\s*/\\s*(\\d+(?:\\.\\d+)?)(?!\\s*[a-zA-Z])")) { mr ->
            val sign = mr.groupValues[1]
            val num = mr.groupValues[2].toDoubleOrNull() ?: 0.0
            val den = mr.groupValues[3].toDoubleOrNull() ?: 1.0
            val coef = (if (sign == "-") -num else num) / den
            if (coef >= 0) "+$coef" else "$coef"
        }

        // 6. Starting fraction without sign: e.g. "1/8 + x = 2"
        s = s.replace(Regex("^(\\d+(?:\\.\\d+)?)\\s*/\\s*(\\d+(?:\\.\\d+)?)(?!\\s*[a-zA-Z])")) { mr ->
            val num = mr.groupValues[1].toDoubleOrNull() ?: 0.0
            val den = mr.groupValues[2].toDoubleOrNull() ?: 1.0
            "${num / den}"
        }

        return s
    }

    // ==========================================
    // 1. SYSTEM OF 2 LINEAR EQUATIONS (2x2)
    // ==========================================

    fun solveSystem2Var(eq1Raw: String, eq2Raw: String): EquationSolution? {
        val c1 = extractLinear2VarCoeffs(eq1Raw) ?: return null
        val c2 = extractLinear2VarCoeffs(eq2Raw) ?: return null

        // eq1: a1*x + b1*y = d1
        // eq2: a2*x + b2*y = d2
        val (a1, b1, d1) = c1
        val (a2, b2, d2) = c2

        val det = (a1 * b2) - (a2 * b1)
        val detX = (d1 * b2) - (d2 * b1)
        val detY = (a1 * d2) - (a2 * d1)

        val steps = mutableListOf<String>()
        steps.add("System of 2 linear equations in standard form:")
        steps.add("  Eq 1: ${formatLinearEq2(a1, b1, d1)}")
        steps.add("  Eq 2: ${formatLinearEq2(a2, b2, d2)}")
        steps.add("Using Cramer's Rule:")
        steps.add("  Determinant D = (${formatNum(a1)})(${formatNum(b2)}) - (${formatNum(a2)})(${formatNum(b1)}) = ${formatNum(det)}")

        if (abs(det) < 1e-11) {
            if (abs(detX) < 1e-11 && abs(detY) < 1e-11) {
                return EquationSolution(
                    category = EquationCategory.IDENTITY,
                    originalEquation = "$eq1Raw ; $eq2Raw",
                    rootsSummary = "Infinitely many solutions (Dependent system)",
                    variables = emptyMap(),
                    steps = steps + listOf("D = 0 and Dx = Dy = 0: The equations are coincident."),
                    spokenSummary = "The system has infinitely many solutions."
                )
            } else {
                return EquationSolution(
                    category = EquationCategory.CONTRADICTION,
                    originalEquation = "$eq1Raw ; $eq2Raw",
                    rootsSummary = "No solution (Inconsistent parallel lines)",
                    variables = emptyMap(),
                    steps = steps + listOf("D = 0 and Dx ≠ 0: The equations represent parallel lines with no intersection."),
                    spokenSummary = "The system has no solution. The lines are parallel."
                )
            }
        }

        val x = detX / det
        val y = detY / det
        val xFmt = formatNum(x)
        val yFmt = formatNum(y)
        val xFrac = toFractionString(x)
        val yFrac = toFractionString(y)
        val xDisplay = if (xFrac != null) "$xFrac ($xFmt)" else xFmt
        val yDisplay = if (yFrac != null) "$yFrac ($yFmt)" else yFmt

        steps.add("  Dx = (${formatNum(d1)})(${formatNum(b2)}) - (${formatNum(d2)})(${formatNum(b1)}) = ${formatNum(detX)}")
        steps.add("  Dy = (${formatNum(a1)})(${formatNum(d2)}) - (${formatNum(a2)})(${formatNum(d1)}) = ${formatNum(detY)}")
        steps.add("  x = Dx / D = ${formatNum(detX)} / ${formatNum(det)} = $xDisplay")
        steps.add("  y = Dy / D = ${formatNum(detY)} / ${formatNum(det)} = $yDisplay")

        val spokenEn = if (xFrac != null || yFrac != null) {
            "Solution: x is ${xFrac?.replace("/", " over ") ?: xFmt}, and y is ${yFrac?.replace("/", " over ") ?: yFmt}."
        } else {
            "Solution: x equals $xFmt, and y equals $yFmt."
        }
        val spokenHi = if (xFrac != null || yFrac != null) {
            "Samikaran ka hal hai: x barabar ${xFrac?.replace("/", " bata ") ?: xFmt}, aur y barabar ${yFrac?.replace("/", " bata ") ?: yFmt}."
        } else {
            "Samikaran ka hal hai: x barabar $xFmt, aur y barabar $yFmt."
        }

        return EquationSolution(
            category = EquationCategory.SYSTEM_2VAR,
            originalEquation = "$eq1Raw ; $eq2Raw",
            rootsSummary = "x = $xDisplay,  y = $yDisplay",
            variables = mapOf("x" to (xFrac ?: xFmt), "y" to (yFrac ?: yFmt)),
            steps = steps,
            spokenSummary = spokenEn,
            spokenSummaryHinglish = spokenHi
        )
    }

    private fun extractLinear2VarCoeffs(eqStr: String): Triple<Double, Double, Double>? {
        var s = eqStr.lowercase().replace(" ", "")
        val sides = s.split("=")
        val lhs = preprocessFractions(sides[0], listOf('x', 'y'))
        val rhs = if (sides.size > 1) preprocessFractions(sides[1], listOf('x', 'y')) else "0"

        val (aL, bL, cL) = parseLinear2VarSide(lhs) ?: return null
        val (aR, bR, cR) = parseLinear2VarSide(rhs) ?: return null

        val a = aL - aR
        val b = bL - bR
        // constant moved to RHS: a*x + b*y = -(cL - cR)
        val d = -(cL - cR)

        if (abs(a) < 1e-12 && abs(b) < 1e-12) return null
        return Triple(a, b, d)
    }

    private fun parseLinear2VarSide(side: String): Triple<Double, Double, Double>? {
        val cleanSide = preprocessFractions(side, listOf('x', 'y')).replace(" ", "")
        if (cleanSide.isEmpty() || cleanSide == "0") return Triple(0.0, 0.0, 0.0)
        var s = side.replace("-", "+-")
        if (s.startsWith("+-")) s = s.substring(1)
        val tokens = s.split("+").filter { it.isNotEmpty() }

        var a = 0.0
        var b = 0.0
        var c = 0.0

        for (token in tokens) {
            when {
                token.endsWith("x") -> {
                    val coef = when (val cStr = token.dropLast(1)) {
                        "", "+" -> 1.0
                        "-" -> -1.0
                        else -> cStr.toDoubleOrNull() ?: return null
                    }
                    a += coef
                }
                token.endsWith("y") -> {
                    val coef = when (val cStr = token.dropLast(1)) {
                        "", "+" -> 1.0
                        "-" -> -1.0
                        else -> cStr.toDoubleOrNull() ?: return null
                    }
                    b += coef
                }
                else -> {
                    val num = token.toDoubleOrNull() ?: return null
                    c += num
                }
            }
        }
        return Triple(a, b, c)
    }

    private fun formatLinearEq2(a: Double, b: Double, d: Double): String {
        val aPart = if (a == 1.0) "x" else if (a == -1.0) "-x" else "${formatNum(a)}x"
        val bSign = if (b >= 0) "+ " else "- "
        val bVal = abs(b)
        val bPart = if (bVal == 1.0) "y" else "${formatNum(bVal)}y"
        return "$aPart $bSign$bPart = ${formatNum(d)}"
    }

    // ==========================================
    // 2. SYSTEM OF 3 LINEAR EQUATIONS (3x3)
    // ==========================================

    fun solveSystem3Var(eq1: String, eq2: String, eq3: String): EquationSolution? {
        val c1 = extractLinear3VarCoeffs(eq1) ?: return null
        val c2 = extractLinear3VarCoeffs(eq2) ?: return null
        val c3 = extractLinear3VarCoeffs(eq3) ?: return null

        val (a1, b1, c1v, d1) = c1
        val (a2, b2, c2v, d2) = c2
        val (a3, b3, c3v, d3) = c3

        fun det3(
            m11: Double, m12: Double, m13: Double,
            m21: Double, m22: Double, m23: Double,
            m31: Double, m32: Double, m33: Double
        ): Double {
            return m11 * (m22 * m33 - m23 * m32) -
                   m12 * (m21 * m33 - m23 * m31) +
                   m13 * (m21 * m32 - m22 * m31)
        }

        val det = det3(a1, b1, c1v, a2, b2, c2v, a3, b3, c3v)
        if (abs(det) < 1e-11) {
            return EquationSolution(
                category = EquationCategory.SYSTEM_3VAR,
                originalEquation = "$eq1 ; $eq2 ; $eq3",
                rootsSummary = "System is dependent or inconsistent (Determinant D = 0)",
                variables = emptyMap(),
                steps = listOf("Determinant of 3x3 coefficient matrix is 0. No unique solution exists."),
                spokenSummary = "This 3-variable system has no unique solution."
            )
        }

        val detX = det3(d1, b1, c1v, d2, b2, c2v, d3, b3, c3v)
        val detY = det3(a1, d1, c1v, a2, d2, c2v, a3, d3, c3v)
        val detZ = det3(a1, b1, d1, a2, b2, d2, a3, b3, d3)

        val x = detX / det
        val y = detY / det
        val z = detZ / det

        val xFmt = formatNum(x)
        val yFmt = formatNum(y)
        val zFmt = formatNum(z)

        val steps = listOf(
            "System of 3 linear equations solved via Cramer's Rule (3x3 matrix):",
            "  Determinant D = ${formatNum(det)}",
            "  Dx = ${formatNum(detX)}, Dy = ${formatNum(detY)}, Dz = ${formatNum(detZ)}",
            "  x = Dx / D = $xFmt",
            "  y = Dy / D = $yFmt",
            "  z = Dz / D = $zFmt"
        )

        return EquationSolution(
            category = EquationCategory.SYSTEM_3VAR,
            originalEquation = "$eq1 ; $eq2 ; $eq3",
            rootsSummary = "x = $xFmt,  y = $yFmt,  z = $zFmt",
            variables = mapOf("x" to xFmt, "y" to yFmt, "z" to zFmt),
            steps = steps,
            spokenSummary = "Solution: x is $xFmt, y is $yFmt, and z is $zFmt.",
            spokenSummaryHinglish = "Teen charon ka hal hai: x barabar $xFmt, y barabar $yFmt, aur z barabar $zFmt."
        )
    }

    private data class Linear3VarCoeffs(val a: Double, val b: Double, val c: Double, val d: Double)

    private fun extractLinear3VarCoeffs(eqStr: String): Linear3VarCoeffs? {
        val s = eqStr.lowercase().replace(" ", "")
        val sides = s.split("=")
        val lhs = preprocessFractions(sides[0], listOf('x', 'y', 'z'))
        val rhs = if (sides.size > 1) preprocessFractions(sides[1], listOf('x', 'y', 'z')) else "0"

        fun parseSide(side: String): DoubleArray? {
            val cleanSide = preprocessFractions(side, listOf('x', 'y', 'z')).replace(" ", "")
            if (cleanSide.isEmpty() || cleanSide == "0") return doubleArrayOf(0.0, 0.0, 0.0, 0.0)
            var str = cleanSide.replace("-", "+-")
            if (str.startsWith("+-")) str = str.substring(1)
            val tokens = str.split("+").filter { it.isNotEmpty() }
            val res = DoubleArray(4) // x, y, z, const
            for (token in tokens) {
                when {
                    token.endsWith("x") -> {
                        val c = when (val p = token.dropLast(1)) { "", "+" -> 1.0; "-" -> -1.0; else -> p.toDoubleOrNull() ?: return null }
                        res[0] += c
                    }
                    token.endsWith("y") -> {
                        val c = when (val p = token.dropLast(1)) { "", "+" -> 1.0; "-" -> -1.0; else -> p.toDoubleOrNull() ?: return null }
                        res[1] += c
                    }
                    token.endsWith("z") -> {
                        val c = when (val p = token.dropLast(1)) { "", "+" -> 1.0; "-" -> -1.0; else -> p.toDoubleOrNull() ?: return null }
                        res[2] += c
                    }
                    else -> {
                        res[3] += token.toDoubleOrNull() ?: return null
                    }
                }
            }
            return res
        }

        val l = parseSide(lhs) ?: return null
        val r = parseSide(rhs) ?: return null
        return Linear3VarCoeffs(l[0] - r[0], l[1] - r[1], l[2] - r[2], -(l[3] - r[3]))
    }

    // ==========================================
    // 3. POLYNOMIAL EQUATIONS (Linear, Quad, Cubic, Quartic)
    // ==========================================

    private fun trySolvePolynomial(lhsStr: String, rhsStr: String, originalEq: String, hasOriginalEquals: Boolean = true): EquationSolution? {
        val lTerms = parsePolynomialTerms(lhsStr) ?: return null
        val rTerms = parsePolynomialTerms(rhsStr) ?: return null

        val degree = max(lTerms.keys.maxOrNull() ?: 0, rTerms.keys.maxOrNull() ?: 0)
        if (degree == 0) return null

        // Collect combined coefficients: a_n * x^n + ... + a_0 = 0
        val coeffs = mutableMapOf<Int, Double>()
        for (d in 0..degree) {
            val c = (lTerms[d] ?: 0.0) - (rTerms[d] ?: 0.0)
            if (abs(c) > 1e-12) coeffs[d] = c
        }

        val actualDegree = coeffs.keys.maxOrNull() ?: return null
        if (actualDegree == 0) {
            val constTerm = coeffs[0] ?: 0.0
            return if (abs(constTerm) < 1e-12) {
                EquationSolution(
                    category = EquationCategory.IDENTITY,
                    originalEquation = originalEq,
                    rootsSummary = "Identity: all real numbers",
                    variables = emptyMap(),
                    steps = listOf("Equation simplifies to 0 = 0. All numbers are solutions."),
                    spokenSummary = "All numbers are solutions."
                )
            } else {
                EquationSolution(
                    category = EquationCategory.CONTRADICTION,
                    originalEquation = originalEq,
                    rootsSummary = "No solution (Contradiction)",
                    variables = emptyMap(),
                    steps = listOf("Equation simplifies to ${formatNum(constTerm)} = 0. No solution."),
                    spokenSummary = "No solution exists."
                )
            }
        }

        val lExpanded = formatPoly(lTerms)
        val rExpanded = formatPoly(rTerms)
        val stdPoly = formatPoly(coeffs)
        val expansionSteps = mutableListOf<String>()
        val hasBracketsOrPowers = lhsStr.contains("(") || lhsStr.contains("^") || rhsStr.contains("(") || rhsStr.contains("^")
        if (hasBracketsOrPowers || (rhsStr != "0" && rhsStr.isNotEmpty())) {
            expansionSteps.add("Algebraic Expansion:")
            expansionSteps.add("  LHS: $lhsStr ➔ $lExpanded")
            if (rhsStr != "0" && rhsStr.isNotEmpty()) {
                expansionSteps.add("  RHS: $rhsStr ➔ $rExpanded")
            }
            expansionSteps.add("  Standard Form: $stdPoly = 0")
        }

        // Degree 1: Linear Equation a*x + b = 0
        if (actualDegree == 1) {
            val a = coeffs[1] ?: 1.0
            val b = coeffs[0] ?: 0.0
            val x = -b / a
            val xFmt = formatNum(x)
            val frac = toFractionString(x)
            val summaryText = if (!hasOriginalEquals && hasBracketsOrPowers) {
                "x = ${frac ?: xFmt}  [Expands to: $lExpanded]"
            } else if (frac != null) {
                "x = $frac ≈ $xFmt"
            } else {
                "x = $xFmt"
            }

            val steps = mutableListOf<String>()
            steps.addAll(expansionSteps)
            steps.add("Standard Linear Form: ax + b = 0")
            steps.add("  (${formatNum(a)})x + (${formatNum(b)}) = 0")
            steps.add("  x = -(${formatNum(b)}) / (${formatNum(a)})")
            if (frac != null) {
                steps.add("  x = $frac ≈ $xFmt")
            } else {
                steps.add("  x = $xFmt")
            }

            val spokenEn = if (!hasOriginalEquals && hasBracketsOrPowers) {
                "Expression expands to $lExpanded, with solution x equals ${frac?.replace("/", " over ") ?: xFmt}."
            } else if (frac != null) {
                "The solution is x equals ${frac.replace("/", " over ")}, or approximately $xFmt."
            } else {
                "The solution is x equals $xFmt."
            }
            val spokenHi = if (!hasOriginalEquals && hasBracketsOrPowers) {
                "Vyanjak $lExpanded banta hai, jiska hal x barabar ${frac?.replace("/", " bata ") ?: xFmt} hai."
            } else if (frac != null) {
                "Samikaran ka hal hai: x barabar ${frac.replace("/", " bata ")}, ya lagbhag $xFmt."
            } else {
                "Samikaran ka hal hai: x barabar $xFmt."
            }

            return EquationSolution(
                category = EquationCategory.LINEAR_1VAR,
                originalEquation = originalEq,
                rootsSummary = summaryText,
                variables = mapOf("x" to (frac ?: xFmt)),
                steps = steps,
                spokenSummary = spokenEn,
                spokenSummaryHinglish = spokenHi
            )
        }

        // Degree 2: Quadratic Equation
        if (actualDegree == 2) {
            val a = coeffs[2] ?: 1.0
            val b = coeffs[1] ?: 0.0
            val c = coeffs[0] ?: 0.0
            val quadSol = QuadraticEquationSolver.solve(a, b, c)
            return EquationSolution(
                category = EquationCategory.QUADRATIC,
                originalEquation = originalEq,
                rootsSummary = "x₁ = ${quadSol.root1Text},  x₂ = ${quadSol.root2Text}",
                variables = mapOf("x₁" to quadSol.root1Text, "x₂" to quadSol.root2Text),
                steps = expansionSteps + quadSol.steps,
                spokenSummary = quadSol.spokenSummary,
                spokenSummaryHinglish = "Dwighat samikaran ke roots hain: x1 barabar ${quadSol.root1Text}, aur x2 barabar ${quadSol.root2Text}."
            )
        }

        // Degree 3: Cubic Equation ax³ + bx² + cx + d = 0
        if (actualDegree == 3) {
            val a = coeffs[3] ?: 1.0
            val b = coeffs[2] ?: 0.0
            val c = coeffs[1] ?: 0.0
            val d = coeffs[0] ?: 0.0
            val cubicSol = solveCubic(a, b, c, d, originalEq)
            return cubicSol.copy(steps = expansionSteps + cubicSol.steps)
        }

        // Degree 4 / Higher: Polynomial root finding (Numerical / companion matrix / Bairstow / Newton)
        return solveHigherPolynomial(coeffs, actualDegree, originalEq)
    }

    data class Poly(val map: Map<Int, Double> = emptyMap()) {
        val degree: Int get() = map.keys.maxOrNull() ?: 0

        operator fun plus(other: Poly): Poly {
            val res = HashMap<Int, Double>(this.map)
            for ((d, c) in other.map) {
                res[d] = (res[d] ?: 0.0) + c
            }
            return Poly(res.filterValues { abs(it) > 1e-12 })
        }

        operator fun minus(other: Poly): Poly {
            val res = HashMap<Int, Double>(this.map)
            for ((d, c) in other.map) {
                res[d] = (res[d] ?: 0.0) - c
            }
            return Poly(res.filterValues { abs(it) > 1e-12 })
        }

        operator fun times(other: Poly): Poly {
            val res = mutableMapOf<Int, Double>()
            for ((d1, c1) in this.map) {
                for ((d2, c2) in other.map) {
                    val deg = d1 + d2
                    res[deg] = (res[deg] ?: 0.0) + c1 * c2
                }
            }
            return Poly(res.filterValues { abs(it) > 1e-12 })
        }

        fun pow(n: Int): Poly {
            if (n < 0 || n > 10) throw IllegalArgumentException("Power $n not supported for polynomial")
            if (n == 0) return Poly(mapOf(0 to 1.0))
            var res = this
            for (i in 2..n) {
                res = res * this
            }
            return res
        }

        companion object {
            val ZERO = Poly(emptyMap())
            fun constant(c: Double) = if (abs(c) < 1e-12) ZERO else Poly(mapOf(0 to c))
            fun x(power: Int = 1, coef: Double = 1.0) = if (abs(coef) < 1e-12) ZERO else Poly(mapOf(power to coef))
        }
    }

    class PolyParser(raw: String) {
        private val input = preprocessInput(raw)
        private var pos = 0
        private val varName = detectVarName(input)

        private fun detectVarName(s: String): Char {
            for (ch in s) {
                if (ch in listOf('x', 'y', 'z', 'X', 'Y', 'Z')) return ch.lowercaseChar()
            }
            return 'x'
        }

        private fun preprocessInput(s: String): String {
            var t = s.trim().replace("²", "^2").replace("³", "^3").replace("⁴", "^4")
            t = t.replace('[', '(').replace(']', ')')
            t = t.replace('{', '(').replace('}', ')')
            t = t.replace('×', '*').replace('·', '*').replace('✕', '*').replace('✖', '*')
            t = t.replace('÷', '/').replace('∕', '/')
            return t
        }

        private fun peek(): Char = if (pos < input.length) input[pos] else '\u0000'
        private fun get(): Char = if (pos < input.length) input[pos++] else '\u0000'
        private fun skipWhitespace() {
            while (pos < input.length && input[pos].isWhitespace()) pos++
        }

        fun parse(): Poly {
            skipWhitespace()
            if (pos >= input.length) return Poly.ZERO
            val res = parseExpression()
            skipWhitespace()
            if (pos < input.length) {
                throw IllegalArgumentException("Unexpected trailing character '${input[pos]}' in '$input'")
            }
            return res
        }

        private fun parseExpression(): Poly {
            skipWhitespace()
            var left = parseTerm()
            while (true) {
                skipWhitespace()
                val c = peek()
                if (c == '+' || c == '-') {
                    get()
                    val right = parseTerm()
                    left = if (c == '+') left + right else left - right
                } else {
                    break
                }
            }
            return left
        }

        private fun parseTerm(): Poly {
            skipWhitespace()
            var isNeg = false
            if (peek() == '+') {
                get()
                skipWhitespace()
            } else if (peek() == '-') {
                get()
                skipWhitespace()
                isNeg = true
            }
            var left = parseFactor()
            if (isNeg) {
                left = Poly.ZERO - left
            }
            while (true) {
                skipWhitespace()
                val c = peek()
                if (c == '*') {
                    get()
                    if (peek() == '*') { // '**' power
                        pos--
                        break
                    }
                    val right = parseFactor()
                    left = left * right
                } else if (c == '/') {
                    get()
                    val right = parseFactor()
                    if (right.degree != 0 || abs(right.map[0] ?: 0.0) < 1e-12) {
                        throw IllegalArgumentException("Non-constant divisor")
                    }
                    val div = right.map[0]!!
                    left = Poly(left.map.mapValues { it.value / div }.filterValues { abs(it) > 1e-12 })
                } else if (c == '(' || c.lowercaseChar() == varName || c.isDigit()) {
                    // Implicit multiplication: 2(x+1), (x+1)(x-1), 2x, (x+1)2
                    val right = parseFactor()
                    left = left * right
                } else {
                    break
                }
            }
            return left
        }

        private fun parseFactor(): Poly {
            skipWhitespace()
            var base = parsePrimary()
            skipWhitespace()
            if (peek() == '^') {
                get()
                skipWhitespace()
                val powNum = parseInteger()
                base = base.pow(powNum)
            } else if (peek() == '*' && pos + 1 < input.length && input[pos + 1] == '*') {
                get(); get()
                skipWhitespace()
                val powNum = parseInteger()
                base = base.pow(powNum)
            }
            return base
        }

        private fun parsePrimary(): Poly {
            skipWhitespace()
            val c = peek()
            if (c == '(') {
                get()
                val inner = parseExpression()
                skipWhitespace()
                if (peek() == ')') get()
                return inner
            }
            if (c.lowercaseChar() == varName) {
                get()
                return Poly.x(1, 1.0)
            }
            if (c.isDigit() || c == '.') {
                val num = parseNumber()
                return Poly.constant(num)
            }
            throw IllegalArgumentException("Unexpected character '$c' at pos $pos in '$input'")
        }

        private fun parseInteger(): Int {
            skipWhitespace()
            val start = pos
            if (peek() == '-') get()
            while (pos < input.length && input[pos].isDigit()) pos++
            val str = input.substring(start, pos)
            return str.toIntOrNull() ?: throw IllegalArgumentException("Expected integer power, got '$str'")
        }

        private fun parseNumber(): Double {
            skipWhitespace()
            val start = pos
            while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) pos++
            val str = input.substring(start, pos)
            return str.toDoubleOrNull() ?: throw IllegalArgumentException("Expected number, got '$str'")
        }
    }

    fun formatPoly(terms: Map<Int, Double>, varName: String = "x"): String {
        val nonZero = terms.filter { abs(it.value) > 1e-9 }
        if (nonZero.isEmpty()) return "0"
        val degs = nonZero.keys.sortedDescending()
        val sb = StringBuilder()
        for (d in degs) {
            val c = nonZero[d] ?: 0.0
            val sign = if (c >= 0) (if (sb.isEmpty()) "" else " + ") else (if (sb.isEmpty()) "-" else " - ")
            val absC = abs(c)
            val frac = toFractionString(absC)
            val cFmt = frac ?: formatNum(absC)
            when (d) {
                0 -> sb.append("$sign$cFmt")
                1 -> {
                    val coefStr = if (abs(absC - 1.0) < 1e-9) "" else (if (frac != null) "($frac)" else cFmt)
                    sb.append("$sign$coefStr$varName")
                }
                else -> {
                    val coefStr = if (abs(absC - 1.0) < 1e-9) "" else (if (frac != null) "($frac)" else cFmt)
                    sb.append("$sign$coefStr$varName^$d")
                }
            }
        }
        return sb.toString()
    }

    private fun parsePolynomialTerms(expr: String): Map<Int, Double>? {
        val s = expr.trim()
        if (s.isEmpty() || s == "0") return emptyMap()

        // Quick rejection of non-polynomial functions
        if (s.contains("sin", ignoreCase = true) ||
            s.contains("cos", ignoreCase = true) ||
            s.contains("tan", ignoreCase = true) ||
            s.contains("log", ignoreCase = true) ||
            s.contains("sqrt", ignoreCase = true)
        ) {
            return null
        }

        return try {
            val poly = PolyParser(s).parse()
            poly.map
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Solves cubic equation: ax³ + bx² + cx + d = 0 using Cardano's formula.
     */
    private fun solveCubic(a: Double, b: Double, c: Double, d: Double, originalEq: String): EquationSolution {
        val steps = mutableListOf<String>()
        steps.add("Standard Cubic Form: ax³ + bx² + cx + d = 0")
        steps.add("  a = ${formatNum(a)}, b = ${formatNum(b)}, c = ${formatNum(c)}, d = ${formatNum(d)}")

        // Depressed cubic t³ + pt + q = 0 by substituting x = t - b/(3a)
        val p = (3 * a * c - b * b) / (3 * a * a)
        val q = (2 * b * b * b - 9 * a * b * c + 27 * a * a * d) / (27 * a * a * a)
        val delta = (q * q / 4) + (p * p * p / 27)

        val shift = -b / (3 * a)
        steps.add("Depressed cubic t³ + pt + q = 0 (t = x - ${formatNum(shift)}):")
        steps.add("  p = ${formatNum(p)}, q = ${formatNum(q)}")
        steps.add("  Cubic Discriminant Δ = (q/2)² + (p/3)³ = ${formatNum(delta)}")

        val roots = mutableListOf<String>()
        if (delta > 1e-9) {
            // One real root, two complex conjugate roots
            val u = cbrt(-q / 2 + sqrt(delta))
            val v = cbrt(-q / 2 - sqrt(delta))
            val t1 = u + v
            val r1 = t1 + shift
            val r1Fmt = formatNum(r1)
            roots.add(r1Fmt)

            val realPart = -(u + v) / 2 + shift
            val imagPart = abs(sqrt(3.0) * (u - v) / 2)
            val r2 = "${formatNum(realPart)} + ${formatNum(imagPart)}i"
            val r3 = "${formatNum(realPart)} - ${formatNum(imagPart)}i"
            roots.add(r2)
            roots.add(r3)

            steps.add("Δ > 0: One real root and two complex conjugate roots:")
            steps.add("  x₁ = $r1Fmt")
            steps.add("  x₂ = $r2")
            steps.add("  x₃ = $r3")
        } else if (abs(delta) <= 1e-9) {
            // Multiple real roots
            val r1 = 2 * cbrt(-q / 2) + shift
            val r2 = -cbrt(-q / 2) + shift
            val r1Fmt = formatNum(r1)
            val r2Fmt = formatNum(r2)
            roots.add(r1Fmt)
            roots.add(r2Fmt)
            steps.add("Δ = 0: All real roots (at least two equal):")
            steps.add("  x₁ = $r1Fmt,  x₂ = x₃ = $r2Fmt")
        } else {
            // Three distinct real roots (casus irreducibilis)
            val r = sqrt(-p * p * p / 27)
            val phi = acos((-q / (2 * r)).coerceIn(-1.0, 1.0))
            val m = 2 * sqrt(-p / 3)

            val r1 = m * cos(phi / 3) + shift
            val r2 = m * cos((phi + 2 * Math.PI) / 3) + shift
            val r3 = m * cos((phi + 4 * Math.PI) / 3) + shift

            val r1Fmt = formatNum(r1)
            val r2Fmt = formatNum(r2)
            val r3Fmt = formatNum(r3)
            roots.add(r1Fmt)
            roots.add(r2Fmt)
            roots.add(r3Fmt)

            steps.add("Δ < 0: Three distinct real roots:")
            steps.add("  x₁ = $r1Fmt")
            steps.add("  x₂ = $r2Fmt")
            steps.add("  x₃ = $r3Fmt")
        }

        val summary = roots.mapIndexed { idx, r -> "x${idx + 1} = $r" }.joinToString(", ")
        val spoken = "Cubic roots found: ${roots.joinToString(" and ")}"

        return EquationSolution(
            category = EquationCategory.CUBIC,
            originalEquation = originalEq,
            rootsSummary = summary,
            variables = roots.mapIndexed { idx, r -> "x${idx + 1}" to r }.toMap(),
            steps = steps,
            spokenSummary = spoken,
            spokenSummaryHinglish = "Trighat samikaran ke mool hain: ${roots.joinToString(", ")}."
        )
    }

    private fun solveHigherPolynomial(coeffs: Map<Int, Double>, degree: Int, originalEq: String): EquationSolution {
        // Find roots numerically via Newton / Aberth / bisection scan
        val foundRoots = mutableListOf<Double>()
        fun evalPoly(x: Double): Double {
            var sum = 0.0
            for ((d, c) in coeffs) {
                sum += c * x.pow(d)
            }
            return sum
        }

        fun evalDeriv(x: Double): Double {
            var sum = 0.0
            for ((d, c) in coeffs) {
                if (d > 0) sum += c * d * x.pow(d - 1)
            }
            return sum
        }

        // Scan intervals between -50 and 50
        var prevX = -50.0
        var prevY = evalPoly(prevX)
        val step = 0.25
        var currX = prevX + step

        while (currX <= 50.0 && foundRoots.size < degree) {
            val currY = evalPoly(currX)
            if (prevY * currY <= 0.0) {
                // Bracketed: refine with Newton-Raphson
                var r = (prevX + currX) / 2.0
                for (iter in 0..20) {
                    val fVal = evalPoly(r)
                    val dfVal = evalDeriv(r)
                    if (abs(dfVal) < 1e-12) break
                    val deltaR = fVal / dfVal
                    r -= deltaR
                    if (abs(deltaR) < 1e-9) break
                }
                if (foundRoots.none { abs(it - r) < 0.05 }) {
                    foundRoots.add(r)
                }
            }
            prevX = currX
            prevY = currY
            currX += step
        }

        val steps = mutableListOf<String>()
        steps.add("Polynomial equation of degree $degree:")
        steps.add("  Identified real roots through numerical root decomposition:")

        val rootTexts = foundRoots.map { formatNum(it) }
        rootTexts.forEachIndexed { i, r -> steps.add("  x${i + 1} ≈ $r") }

        val rootsSummary = if (rootTexts.isEmpty()) "No real roots (all complex)"
                           else rootTexts.mapIndexed { i, r -> "x${i + 1} = $r" }.joinToString(", ")
        val spoken = if (rootTexts.isEmpty()) "No real roots found."
                     else "Real roots are: ${rootTexts.joinToString(" and ")}"

        return EquationSolution(
            category = EquationCategory.POLYNOMIAL,
            originalEquation = originalEq,
            rootsSummary = rootsSummary,
            variables = rootTexts.mapIndexed { i, r -> "x${i + 1}" to r }.toMap(),
            steps = steps,
            spokenSummary = spoken,
            spokenSummaryHinglish = "Samikaran ke mool hain: ${rootTexts.joinToString(", ")}.",
            isExact = false
        )
    }

    // ==========================================
    // 4. TRANSCENDENTAL & NUMERICAL ROOT FINDING
    // ==========================================

    private fun solveNumericalTranscendental(lhsStr: String, rhsStr: String, originalEq: String): EquationSolution? {
        val diffExpr = "($lhsStr) - ($rhsStr)"

        fun f(xVal: Double): Double? {
            val replaced = diffExpr
                .replace(Regex("(?<![a-zA-Z])x(?![a-zA-Z])"), "($xVal)")
            val eval = ScientificMathEvaluator.evaluate(replaced)
            return if (eval.isSuccess && eval.values.isNotEmpty()) eval.values.first() else null
        }

        // Bracket scan between -20 and 20
        val roots = mutableListOf<Double>()
        var prevX = -20.0
        var prevY = f(prevX)
        val step = 0.2
        var currX = prevX + step

        while (currX <= 20.0 && roots.size < 4) {
            val currY = f(currX)
            if (prevY != null && currY != null && !prevY.isNaN() && !currY.isNaN()) {
                if (prevY * currY <= 0.0) {
                    // Refine root via Bisection
                    var low = prevX
                    var high = currX
                    var mid = (low + high) / 2.0
                    for (iter in 0..35) {
                        mid = (low + high) / 2.0
                        val fMid = f(mid) ?: break
                        if (abs(fMid) < 1e-9 || (high - low) < 1e-9) break
                        val fLow = f(low) ?: break
                        if (fLow * fMid <= 0.0) high = mid else low = mid
                    }
                    if (roots.none { abs(it - mid) < 0.05 }) {
                        roots.add(mid)
                    }
                }
            }
            prevX = currX
            prevY = currY
            currX += step
        }

        if (roots.isEmpty()) return null

        val steps = mutableListOf<String>()
        steps.add("Non-linear / Transcendental Equation:")
        steps.add("  f(x) = ($lhsStr) - ($rhsStr) = 0")
        steps.add("Solved using iterative numerical bracket root convergence:")

        val formattedRoots = roots.map { formatNum(it) }
        formattedRoots.forEachIndexed { i, r -> steps.add("  x${if (roots.size > 1) "${i + 1}" else ""} ≈ $r") }

        val summary = if (roots.size == 1) "x ≈ ${formattedRoots.first()}"
                      else formattedRoots.mapIndexed { i, r -> "x${i + 1} ≈ $r" }.joinToString(", ")
        val spoken = if (roots.size == 1) "The solution is approximately x equals ${formattedRoots.first()}."
                     else "Solutions found: ${formattedRoots.joinToString(", then ")}."

        return EquationSolution(
            category = EquationCategory.TRANSCENDENTAL_NUMERICAL,
            originalEquation = originalEq,
            rootsSummary = summary,
            variables = formattedRoots.mapIndexed { i, r -> "x${i + 1}" to r }.toMap(),
            steps = steps,
            spokenSummary = spoken,
            spokenSummaryHinglish = "Samikaran ka anumanit hal hai: x lagbhag ${formattedRoots.first()} ke barabar hai.",
            isExact = false
        )
    }

    private fun cbrt(v: Double): Double {
        return if (v >= 0) v.pow(1.0 / 3.0) else -(-v).pow(1.0 / 3.0)
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
