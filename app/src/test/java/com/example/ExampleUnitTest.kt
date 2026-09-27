package com.example

import com.example.engine.ScientificMathEvaluator
import com.example.engine.SpokenMathParser
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testBasicSpokenArithmetic() {
        val normalized = SpokenMathParser.normalize("twelve plus fifteen")
        val result = ScientificMathEvaluator.evaluate(normalized)
        assertTrue(result.isSuccess)
        assertEquals(27.0, result.values.first(), 1e-6)

        val subNorm = SpokenMathParser.normalize("50 minus 20")
        val subRes = ScientificMathEvaluator.evaluate(subNorm)
        assertTrue(subRes.isSuccess)
        assertEquals(30.0, subRes.values.first(), 1e-6)

        val mulNorm = SpokenMathParser.normalize("6 times 7")
        val mulRes = ScientificMathEvaluator.evaluate(mulNorm)
        assertTrue(mulRes.isSuccess)
        assertEquals(42.0, mulRes.values.first(), 1e-6)

        val divNorm = SpokenMathParser.normalize("20 divided by 4")
        val divRes = ScientificMathEvaluator.evaluate(divNorm)
        assertTrue(divRes.isSuccess)
        assertEquals(5.0, divRes.values.first(), 1e-6)
    }

    @Test
    fun testPercentagesAndFractions() {
        // "20 percent of 80" -> 16
        val pctNorm = SpokenMathParser.normalize("20 percent of 80")
        val pctRes = ScientificMathEvaluator.evaluate(pctNorm)
        assertTrue(pctRes.isSuccess)
        assertEquals(16.0, pctRes.values.first(), 1e-6)

        // "a third of 90" -> 30
        val fracNorm = SpokenMathParser.normalize("a third of 90")
        val fracRes = ScientificMathEvaluator.evaluate(fracNorm)
        assertTrue(fracRes.isSuccess)
        assertEquals(30.0, fracRes.values.first(), 1e-6)

        // "two thirds of 60" -> 40
        val fracNorm2 = SpokenMathParser.normalize("two thirds of 60")
        val fracRes2 = ScientificMathEvaluator.evaluate(fracNorm2)
        assertTrue(fracRes2.isSuccess)
        assertEquals(40.0, fracRes2.values.first(), 1e-6)
    }

    @Test
    fun testPowersAndRoots() {
        val sqNorm = SpokenMathParser.normalize("five squared")
        val sqRes = ScientificMathEvaluator.evaluate(sqNorm)
        assertTrue(sqRes.isSuccess)
        assertEquals(25.0, sqRes.values.first(), 1e-6)

        val powNorm = SpokenMathParser.normalize("two to the power of ten")
        val powRes = ScientificMathEvaluator.evaluate(powNorm)
        assertTrue(powRes.isSuccess)
        assertEquals(1024.0, powRes.values.first(), 1e-6)

        val sqrtNorm = SpokenMathParser.normalize("square root of 81")
        val sqrtRes = ScientificMathEvaluator.evaluate(sqrtNorm)
        assertTrue(sqrtRes.isSuccess)
        assertEquals(9.0, sqrtRes.values.first(), 1e-6)

        val cbrtNorm = SpokenMathParser.normalize("cube root of 27")
        val cbrtRes = ScientificMathEvaluator.evaluate(cbrtNorm)
        assertTrue(cbrtRes.isSuccess)
        assertEquals(3.0, cbrtRes.values.first(), 1e-6)
    }

    @Test
    fun testTrigAndLogs() {
        // "sine of 30 degrees" -> 0.5
        val sinNorm = SpokenMathParser.normalize("sine of 30 degrees")
        val sinRes = ScientificMathEvaluator.evaluate(sinNorm)
        assertTrue(sinRes.isSuccess)
        assertEquals(0.5, sinRes.values.first(), 1e-5)

        // "arcsine of 0.5" -> 30 degrees
        val asinNorm = SpokenMathParser.normalize("arcsine of 0.5")
        val asinRes = ScientificMathEvaluator.evaluate(asinNorm)
        assertTrue(asinRes.isSuccess)
        assertEquals(30.0, asinRes.values.first(), 1e-5)

        // "log of 100" -> 2
        val logNorm = SpokenMathParser.normalize("log of 100")
        val logRes = ScientificMathEvaluator.evaluate(logNorm)
        assertTrue(logRes.isSuccess)
        assertEquals(2.0, logRes.values.first(), 1e-6)

        // "log base 2 of 8" -> 3
        val log2Norm = SpokenMathParser.normalize("log base 2 of 8")
        val log2Res = ScientificMathEvaluator.evaluate(log2Norm)
        assertTrue(log2Res.isSuccess)
        assertEquals(3.0, log2Res.values.first(), 1e-6)
    }

    @Test
    fun testCombinatoricsAndFactorial() {
        val factNorm = SpokenMathParser.normalize("5 factorial")
        val factRes = ScientificMathEvaluator.evaluate(factNorm)
        assertTrue(factRes.isSuccess)
        assertEquals(120.0, factRes.values.first(), 1e-6)

        val combNorm = SpokenMathParser.normalize("5 choose 2")
        val combRes = ScientificMathEvaluator.evaluate(combNorm)
        assertTrue(combRes.isSuccess)
        assertEquals(10.0, combRes.values.first(), 1e-6)

        val permNorm = SpokenMathParser.normalize("permutations of 5 and 2")
        val permRes = ScientificMathEvaluator.evaluate(permNorm)
        assertTrue(permRes.isSuccess)
        assertEquals(20.0, permRes.values.first(), 1e-6)

        val modNorm = SpokenMathParser.normalize("17 mod 5")
        val modRes = ScientificMathEvaluator.evaluate(modNorm)
        assertTrue(modRes.isSuccess)
        assertEquals(2.0, modRes.values.first(), 1e-6)
    }

    @Test
    fun testHindiHinglish() {
        val hindiNorm = SpokenMathParser.normalize("ek sau jodo pachas")
        val hindiRes = ScientificMathEvaluator.evaluate(hindiNorm)
        assertTrue(hindiRes.isSuccess)
        assertEquals(150.0, hindiRes.values.first(), 1e-6)

        val squareNorm = SpokenMathParser.normalize("paanch ka square")
        val squareRes = ScientificMathEvaluator.evaluate(squareNorm)
        assertTrue(squareRes.isSuccess)
        assertEquals(25.0, squareRes.values.first(), 1e-6)
    }

    @Test
    fun testCorrectionsAndChained() {
        // "times not plus"
        val corrected = SpokenMathParser.handleCorrection("no wait, times not plus", "5 plus 5")
        assertEquals("5 times 5", corrected)

        val chainedNorm = SpokenMathParser.normalize("5 plus 5 and 10 minus 3")
        val chainedRes = ScientificMathEvaluator.evaluate(chainedNorm)
        assertTrue(chainedRes.isSuccess)
        assertEquals(2, chainedRes.values.size)
        assertEquals(10.0, chainedRes.values[0], 1e-6)
        assertEquals(7.0, chainedRes.values[1], 1e-6)
    }

    @Test
    fun testQuadraticEquationSolver() {
        // Two real roots: x^2 - 5x + 6 = 0 -> x = 3, 2
        val sol1 = com.example.engine.QuadraticEquationSolver.solve(1.0, -5.0, 6.0)
        assertEquals(com.example.engine.QuadraticEquationSolver.RootType.TWO_REAL, sol1.rootType)
        assertEquals("3", sol1.root1Text)
        assertEquals("2", sol1.root2Text)
        assertEquals(1.0, sol1.discriminant, 1e-6)

        // Single repeated root: x^2 - 6x + 9 = 0 -> x = 3
        val sol2 = com.example.engine.QuadraticEquationSolver.solve(1.0, -6.0, 9.0)
        assertEquals(com.example.engine.QuadraticEquationSolver.RootType.ONE_REAL_REPEATED, sol2.rootType)
        assertEquals("3", sol2.root1Text)
        assertEquals(0.0, sol2.discriminant, 1e-6)

        // Complex conjugate roots: x^2 + 4 = 0 -> x = 2i, -2i
        val sol3 = com.example.engine.QuadraticEquationSolver.solve(1.0, 0.0, 4.0)
        assertEquals(com.example.engine.QuadraticEquationSolver.RootType.COMPLEX_CONJUGATE, sol3.rootType)
        assertTrue(sol3.root1Text.contains("2i"))
        assertTrue(sol3.root2Text.contains("-2i"))
        assertEquals(-16.0, sol3.discriminant, 1e-6)

        // Parse from string: "2x^2 + 4x - 6 = 0" -> x = 1, -3
        val parsedSol = com.example.engine.QuadraticEquationSolver.parseAndSolve("2x^2 + 4x - 6 = 0")
        assertNotNull(parsedSol)
        assertEquals("1", parsedSol!!.root1Text)
        assertEquals("-3", parsedSol.root2Text)

        // Spoken query: "solve x squared minus 5x plus 6 equals 0"
        val spokenSol = SpokenMathParser.tryParseQuadraticQuery("solve x squared minus 5x plus 6 equals 0")
        assertNotNull(spokenSol)
        assertEquals(com.example.engine.QuadraticEquationSolver.RootType.TWO_REAL, spokenSol!!.rootType)
        assertEquals("3", spokenSol.root1Text)
        assertEquals("2", spokenSol.root2Text)

        // Spoken query: "x square minus 5x plus 6 is equal to 0"
        val spokenSol2 = SpokenMathParser.tryParseQuadraticQuery("x square minus 5x plus 6 is equal to 0")
        assertNotNull(spokenSol2)
        assertEquals(com.example.engine.QuadraticEquationSolver.RootType.TWO_REAL, spokenSol2!!.rootType)
        assertEquals("3", spokenSol2.root1Text)
        assertEquals("2", spokenSol2.root2Text)
    }

    @Test
    fun testUniversalEquationSolver() {
        // 1. Single variable linear equation: 3x + 5 = 20 -> x = 5
        val linSol = com.example.engine.UniversalEquationSolver.solveAny("3x + 5 = 20")
        assertNotNull(linSol)
        assertEquals(com.example.engine.UniversalEquationSolver.EquationCategory.LINEAR_1VAR, linSol!!.category)
        assertEquals("5", linSol.variables["x"])

        // 2. Linear equation: 5x - 7 = 3x + 11 -> 2x = 18 -> x = 9
        val linSol2 = com.example.engine.UniversalEquationSolver.solveAny("5x - 7 = 3x + 11")
        assertNotNull(linSol2)
        assertEquals("9", linSol2!!.variables["x"])

        // 3. System of 2 Linear Equations: 2x + y = 7 and x - y = 2 -> x = 3, y = 1
        val sysSol = com.example.engine.UniversalEquationSolver.solveSystem2Var("2x + y = 7", "x - y = 2")
        assertNotNull(sysSol)
        assertEquals(com.example.engine.UniversalEquationSolver.EquationCategory.SYSTEM_2VAR, sysSol!!.category)
        assertEquals("3", sysSol.variables["x"])
        assertEquals("1", sysSol.variables["y"])

        // 4. Cubic equation: x^3 - 6x^2 + 11x - 6 = 0 -> roots are 1, 2, 3
        val cubicSol = com.example.engine.UniversalEquationSolver.solveAny("x^3 - 6x^2 + 11x - 6 = 0")
        assertNotNull(cubicSol)
        assertEquals(com.example.engine.UniversalEquationSolver.EquationCategory.CUBIC, cubicSol!!.category)
        assertTrue(cubicSol.rootsSummary.contains("3"))
        assertTrue(cubicSol.rootsSummary.contains("2"))
        assertTrue(cubicSol.rootsSummary.contains("1"))

        // 5. Spoken Linear: "solve 3x plus 5 equals 20"
        val spokenLin = SpokenMathParser.tryParseAnyEquationQuery("solve 3x plus 5 equals 20")
        assertNotNull(spokenLin)
        assertEquals("5", spokenLin!!.variables["x"])

        // 6. Spoken System: "solve system 2x plus y equals 7 and x minus y equals 2"
        val spokenSys = SpokenMathParser.tryParseAnyEquationQuery("solve system 2x plus y equals 7 and x minus y equals 2")
        assertNotNull(spokenSys)
        assertEquals("3", spokenSys!!.variables["x"])
        assertEquals("1", spokenSys!!.variables["y"])

        // 7. User prompt: "SOLVE 3x+5 = 0"
        val userPrompt1 = SpokenMathParser.tryParseAnyEquationQuery("SOLVE 3x+5 = 0")
        assertNotNull(userPrompt1)
        assertEquals(com.example.engine.UniversalEquationSolver.EquationCategory.LINEAR_1VAR, userPrompt1!!.category)
        assertTrue(userPrompt1.rootsSummary.contains("-1.6667"))

        // 8. User prompt: "x cube + x square + x +2=0"
        val userPrompt2 = SpokenMathParser.tryParseAnyEquationQuery("x cube + x square + x +2=0")
        assertNotNull(userPrompt2)
        assertEquals(com.example.engine.UniversalEquationSolver.EquationCategory.CUBIC, userPrompt2!!.category)
        assertTrue(userPrompt2.rootsSummary.contains("-1.3532"))

        // 9. Hinglish Linear: "3x plus 5 barabar 0"
        val hinglishLin = SpokenMathParser.tryParseAnyEquationQuery("3x plus 5 barabar 0")
        assertNotNull(hinglishLin)
        assertTrue(hinglishLin!!.rootsSummary.contains("-1.6667"))
        assertTrue(hinglishLin.spokenSummary.contains("Samikaran"))

        // 10. Hindi numbers linear: "teen x plus paanch barabar shunya"
        val hindiNumLin = SpokenMathParser.tryParseAnyEquationQuery("teen x plus paanch barabar shunya")
        assertNotNull(hindiNumLin)
        assertTrue(hindiNumLin!!.rootsSummary.contains("-1.6667"))

        // 11. Hinglish Cubic: "x cube plus x square plus x plus 2 barabar 0"
        val hinglishCubic = SpokenMathParser.tryParseAnyEquationQuery("x cube plus x square plus x plus 2 barabar 0")
        assertNotNull(hinglishCubic)
        assertEquals(com.example.engine.UniversalEquationSolver.EquationCategory.CUBIC, hinglishCubic!!.category)
        assertTrue(hinglishCubic.spokenSummary.contains("Trighat samikaran"))

        // 12. Hinglish 2x2 System: "2x plus y barabar 7 aur x minus y barabar 2"
        val hinglishSys = SpokenMathParser.tryParseAnyEquationQuery("2x plus y barabar 7 aur x minus y barabar 2")
        assertNotNull(hinglishSys)
        assertEquals("3", hinglishSys!!.variables["x"])
        assertEquals("1", hinglishSys.variables["y"])
        assertTrue(hinglishSys.spokenSummary.contains("Samikaran"))

        // 13. Fraction Equation: "(1/3)x + (2/5)x = 5"
        val fracEq1 = UniversalEquationSolver.solveAny("(1/3)x + (2/5)x = 5")
        assertNotNull(fracEq1)
        assertEquals(com.example.engine.UniversalEquationSolver.EquationCategory.LINEAR_1VAR, fracEq1!!.category)
        assertTrue(fracEq1.rootsSummary.contains("75/11"))
        assertTrue(fracEq1.rootsSummary.contains("6.8182"))

        // 14. Spoken English: "1 by 3 x plus 2 by 5 x = 5"
        val fracSpokenEn1 = SpokenMathParser.tryParseAnyEquationQuery("1 by 3 x plus 2 by 5 x = 5")
        assertNotNull(fracSpokenEn1)
        assertTrue(fracSpokenEn1!!.rootsSummary.contains("75/11"))
        assertTrue(fracSpokenEn1.spokenSummary.contains("75 over 11"))

        // 15. Spoken English in words: "one by three x plus two by five x equals five"
        val fracSpokenEn2 = SpokenMathParser.tryParseAnyEquationQuery("one by three x plus two by five x equals five")
        assertNotNull(fracSpokenEn2)
        assertTrue(fracSpokenEn2!!.rootsSummary.contains("75/11"))

        // 16. Spoken Hinglish: "1 bata 3 x plus 2 bata 5 x barabar 5"
        val fracSpokenHi1 = SpokenMathParser.tryParseAnyEquationQuery("1 bata 3 x plus 2 bata 5 x barabar 5")
        assertNotNull(fracSpokenHi1)
        assertTrue(fracSpokenHi1!!.rootsSummary.contains("75/11"))
        assertTrue(fracSpokenHi1.spokenSummary.contains("75 bata 11"))

        // 17. Spoken Hindi number words: "ek bata teen x jodo do bata paanch x barabar paanch"
        val fracSpokenHi2 = SpokenMathParser.tryParseAnyEquationQuery("ek bata teen x jodo do bata paanch x barabar paanch")
        assertNotNull(fracSpokenHi2)
        assertTrue(fracSpokenHi2!!.rootsSummary.contains("75/11"))
        assertTrue(fracSpokenHi2.spokenSummary.contains("75 bata 11"))

        // 18. Quadratic with fractions: "1 by 2 x square minus 3 by 4 x plus 1 by 8 equals 0"
        val fracQuad = SpokenMathParser.tryParseAnyEquationQuery("1 by 2 x square minus 3 by 4 x plus 1 by 8 equals 0")
        assertNotNull(fracQuad)
        assertEquals(com.example.engine.UniversalEquationSolver.EquationCategory.QUADRATIC, fracQuad!!.category)

        // 19. System 2x2 with fractions: "1 by 2 x plus 1 by 3 y equals 5 and x minus y equals 2"
        val fracSys = SpokenMathParser.tryParseAnyEquationQuery("1 by 2 x plus 1 by 3 y equals 5 and x minus y equals 2")
        assertNotNull(fracSys)
        assertEquals(com.example.engine.UniversalEquationSolver.EquationCategory.SYSTEM_2VAR, fracSys!!.category)
    }
}
