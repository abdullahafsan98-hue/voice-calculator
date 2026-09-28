package com.example

import com.example.engine.ScientificMathEvaluator
import com.example.engine.SpokenMathParser
import com.example.engine.UniversalEquationSolver
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

    @Test
    fun testNestedBracketsAndFractions() {
        // A. (2/3)/(4/7) -> 7/6 ≈ 1.1667
        val resA = ScientificMathEvaluator.evaluate("(2/3)/(4/7)")
        assertTrue(resA.isSuccess)
        assertEquals(7.0 / 6.0, resA.values.first(), 1e-6)
        assertTrue(resA.formattedOutputs.first().contains("7/6"))

        // B. 1/(2+3/4) -> 4/11 ≈ 0.3636
        val resB = ScientificMathEvaluator.evaluate("1/(2+3/4)")
        assertTrue(resB.isSuccess)
        assertEquals(4.0 / 11.0, resB.values.first(), 1e-6)
        assertTrue(resB.formattedOutputs.first().contains("4/11"))

        // C. 3/7 × 14/9 -> 2/3 ≈ 0.6667 (with Unicode multiplication symbol '×')
        val resC = ScientificMathEvaluator.evaluate("3/7 × 14/9")
        assertTrue(resC.isSuccess)
        assertEquals(2.0 / 3.0, resC.values.first(), 1e-6)
        assertTrue(resC.formattedOutputs.first().contains("2/3"))

        // D. 2(3+4) -> 14 (implicit multiplication)
        val resD = ScientificMathEvaluator.evaluate("2(3+4)")
        assertTrue(resD.isSuccess)
        assertEquals(14.0, resD.values.first(), 1e-6)
        assertEquals("14", resD.formattedOutputs.first())

        // E. (2+(3×(4+5))) -> 29
        val resE = ScientificMathEvaluator.evaluate("(2+(3×(4+5)))")
        assertTrue(resE.isSuccess)
        assertEquals(29.0, resE.values.first(), 1e-6)
        assertEquals("29", resE.formattedOutputs.first())

        // F. ((2+3)×(4-1))/5 -> 3
        val resF = ScientificMathEvaluator.evaluate("((2+3)×(4-1))/5")
        assertTrue(resF.isSuccess)
        assertEquals(3.0, resF.values.first(), 1e-6)
        assertEquals("3", resF.formattedOutputs.first())

        // G. 1/(1+(1/(1+1/2))) -> 3/5 = 0.6
        val resG = ScientificMathEvaluator.evaluate("1/(1+(1/(1+1/2)))")
        assertTrue(resG.isSuccess)
        assertEquals(3.0 / 5.0, resG.values.first(), 1e-6)
        assertTrue(resG.formattedOutputs.first().contains("3/5"))

        // Spoken Case C variations:
        // "3 by 7 into 14 by 9"
        val normC1 = SpokenMathParser.normalize("3 by 7 into 14 by 9")
        val evalC1 = ScientificMathEvaluator.evaluate(normC1)
        assertTrue(evalC1.isSuccess)
        assertEquals(2.0 / 3.0, evalC1.values.first(), 1e-6)

        // "3 by 7 times 14 by 9"
        val normC2 = SpokenMathParser.normalize("3 by 7 times 14 by 9")
        val evalC2 = ScientificMathEvaluator.evaluate(normC2)
        assertTrue(evalC2.isSuccess)
        assertEquals(2.0 / 3.0, evalC2.values.first(), 1e-6)

        // "3 bata 7 guna 14 bata 9"
        val normC3 = SpokenMathParser.normalize("3 bata 7 guna 14 bata 9")
        val evalC3 = ScientificMathEvaluator.evaluate(normC3)
        assertTrue(evalC3.isSuccess)
        assertEquals(2.0 / 3.0, evalC3.values.first(), 1e-6)

        // "three by seven into fourteen by nine"
        val normC4 = SpokenMathParser.normalize("three by seven into fourteen by nine")
        val evalC4 = ScientificMathEvaluator.evaluate(normC4)
        assertTrue(evalC4.isSuccess)
        assertEquals(2.0 / 3.0, evalC4.values.first(), 1e-6)

        // "teen bata saat guna chaudah bata nau"
        val normC5 = SpokenMathParser.normalize("teen bata saat guna chaudah bata nau")
        val evalC5 = ScientificMathEvaluator.evaluate(normC5)
        assertTrue(evalC5.isSuccess)
        assertEquals(2.0 / 3.0, evalC5.values.first(), 1e-6)

        // Spoken Case D variations:
        // "2 into whole 3 plus 4"
        val normD1 = SpokenMathParser.normalize("2 into whole 3 plus 4")
        val evalD1 = ScientificMathEvaluator.evaluate(normD1)
        assertTrue(evalD1.isSuccess)
        assertEquals(14.0, evalD1.values.first(), 1e-6)

        // "2 into bracket me 3 plus 4"
        val normD2 = SpokenMathParser.normalize("2 into bracket me 3 plus 4")
        val evalD2 = ScientificMathEvaluator.evaluate(normD2)
        assertTrue(evalD2.isSuccess)
        assertEquals(14.0, evalD2.values.first(), 1e-6)

        // "2 into bracket mein 3 plus 4"
        val normD3 = SpokenMathParser.normalize("2 into bracket mein 3 plus 4")
        val evalD3 = ScientificMathEvaluator.evaluate(normD3)
        assertTrue(evalD3.isSuccess)
        assertEquals(14.0, evalD3.values.first(), 1e-6)

        // "2 into bracket 3 plus 4"
        val normD4 = SpokenMathParser.normalize("2 into bracket 3 plus 4")
        val evalD4 = ScientificMathEvaluator.evaluate(normD4)
        assertTrue(evalD4.isSuccess)
        assertEquals(14.0, evalD4.values.first(), 1e-6)

        // "2 bracket 3 plus 4"
        val normD5 = SpokenMathParser.normalize("2 bracket 3 plus 4")
        val evalD5 = ScientificMathEvaluator.evaluate(normD5)
        assertTrue(evalD5.isSuccess)
        assertEquals(14.0, evalD5.values.first(), 1e-6)

        // "2 guna bracket me teen plus char"
        val normD6 = SpokenMathParser.normalize("2 guna bracket me teen plus char")
        val evalD6 = ScientificMathEvaluator.evaluate(normD6)
        assertTrue(evalD6.isSuccess)
        assertEquals(14.0, evalD6.values.first(), 1e-6)

        // Spoken Case F:
        // "bracket me 2 plus 3 into bracket me 4 minus 1 divided by 5"
        val normF = SpokenMathParser.normalize("bracket me 2 plus 3 into bracket me 4 minus 1 divided by 5")
        val evalF = ScientificMathEvaluator.evaluate(normF)
        assertTrue(evalF.isSuccess)
        assertEquals(3.0, evalF.values.first(), 1e-6)
    }

    @Test
    fun testTrigonometryPowersAndOperations() {
        // 1. "sin square thirty plus cos square 30" -> 1.0
        val norm1 = SpokenMathParser.normalize("sin square thirty plus cos square 30")
        val eval1 = ScientificMathEvaluator.evaluate(norm1)
        assertTrue(eval1.isSuccess)
        assertEquals(1.0, eval1.values.first(), 1e-6)
        assertEquals("1", ScientificMathEvaluator.formatDecimalOnly(eval1.values.first()))

        // 2. "sin sqaure 30 plus cos square 30" -> 1.0 (with common speech typo "sqaure")
        val norm2 = SpokenMathParser.normalize("sin sqaure 30 plus cos square 30")
        val eval2 = ScientificMathEvaluator.evaluate(norm2)
        assertTrue(eval2.isSuccess)
        assertEquals(1.0, eval2.values.first(), 1e-6)

        // 3. Hinglish: "sin varg tees plus cos varg tees" -> 1.0
        val norm3 = SpokenMathParser.normalize("sin varg tees plus cos varg tees")
        val eval3 = ScientificMathEvaluator.evaluate(norm3)
        assertTrue(eval3.isSuccess)
        assertEquals(1.0, eval3.values.first(), 1e-6)

        // 4. Hindi operations: "sin varg 30 jodo cos varg 30" -> 1.0
        val norm4 = SpokenMathParser.normalize("sin varg 30 jodo cos varg 30")
        val eval4 = ScientificMathEvaluator.evaluate(norm4)
        assertTrue(eval4.isSuccess)
        assertEquals(1.0, eval4.values.first(), 1e-6)

        // 5. Tan square: "tan square 45" -> 1.0
        val norm5 = SpokenMathParser.normalize("tan square 45")
        val eval5 = ScientificMathEvaluator.evaluate(norm5)
        assertTrue(eval5.isSuccess)
        assertEquals(1.0, eval5.values.first(), 1e-6)

        // 6. Multiplication: "sin square 30 into cos square 60" -> 0.25 * 0.25 = 0.0625 = 1/16
        val norm6 = SpokenMathParser.normalize("sin square 30 into cos square 60")
        val eval6 = ScientificMathEvaluator.evaluate(norm6)
        assertTrue(eval6.isSuccess)
        assertEquals(0.0625, eval6.values.first(), 1e-6)
        assertTrue(eval6.formattedOutputs.first().contains("1/16"))

        // 7. Subtraction: "sin square 45 minus cos square 45" -> 0.5 - 0.5 = 0.0
        val norm7 = SpokenMathParser.normalize("sin square 45 minus cos square 45")
        val eval7 = ScientificMathEvaluator.evaluate(norm7)
        assertTrue(eval7.isSuccess)
        assertEquals(0.0, eval7.values.first(), 1e-6)

        // 8. Cubic power: "sin cube 30" -> 0.5^3 = 0.125 = 1/8
        val norm8 = SpokenMathParser.normalize("sin cube 30")
        val eval8 = ScientificMathEvaluator.evaluate(norm8)
        assertTrue(eval8.isSuccess)
        assertEquals(0.125, eval8.values.first(), 1e-6)
        assertTrue(eval8.formattedOutputs.first().contains("1/8"))

        // 9. Typed math notation: "sin^2(30*pi/180) + cos^2(30*pi/180)"
        val eval9 = ScientificMathEvaluator.evaluate("sin^2(30*pi/180) + cos^2(30*pi/180)")
        assertTrue(eval9.isSuccess)
        assertEquals(1.0, eval9.values.first(), 1e-6)

        // 10. Reciprocal trig functions: "sec 60" -> 2.0, "csc 30" -> 2.0, "cot 45" -> 1.0
        val norm10 = SpokenMathParser.normalize("sec 60")
        val eval10 = ScientificMathEvaluator.evaluate(norm10)
        assertTrue(eval10.isSuccess)
        assertEquals(2.0, eval10.values.first(), 1e-6)
    }

    @Test
    fun testWholeSquarePolynomialEquations() {
        // 1. Direct typed expression: ((x+1)^2 - (x-1)^2)
        val sol1 = UniversalEquationSolver.solveAny("((x+1)^2 - (x-1)^2)")
        assertNotNull(sol1)
        assertEquals(UniversalEquationSolver.EquationCategory.LINEAR_1VAR, sol1!!.category)
        assertTrue(sol1.rootsSummary.contains("x = 0"))
        assertTrue(sol1.rootsSummary.contains("4x"))

        // 2. Direct typed equation: ((x+1)^2 - (x-1)^2) = 8
        val sol2 = UniversalEquationSolver.solveAny("((x+1)^2 - (x-1)^2) = 8")
        assertNotNull(sol2)
        assertEquals("2", sol2!!.variables["x"])

        // 3. Spoken English: "x plus 1 whole square - x minus 1 whole square"
        val sol3 = SpokenMathParser.tryParseAnyEquationQuery("x plus 1 whole square - x minus 1 whole square")
        assertNotNull(sol3)
        assertEquals(UniversalEquationSolver.EquationCategory.LINEAR_1VAR, sol3!!.category)
        assertTrue(sol3.rootsSummary.contains("x = 0"))

        // 4. Spoken English: "x plus 1 whole square minus x minus 1 whole square"
        val sol4 = SpokenMathParser.tryParseAnyEquationQuery("x plus 1 whole square minus x minus 1 whole square")
        assertNotNull(sol4)
        assertEquals(UniversalEquationSolver.EquationCategory.LINEAR_1VAR, sol4!!.category)
        assertTrue(sol4.rootsSummary.contains("x = 0"))

        // 5. Spoken Hinglish: "x plus 1 ka whole square minus x minus 1 ka whole square"
        val sol5 = SpokenMathParser.tryParseAnyEquationQuery("x plus 1 ka whole square minus x minus 1 ka whole square")
        assertNotNull(sol5)
        assertEquals(UniversalEquationSolver.EquationCategory.LINEAR_1VAR, sol5!!.category)
        assertTrue(sol5.rootsSummary.contains("x = 0"))

        // 6. Spoken English with equals: "x plus 1 whole square minus x minus 1 whole square equals 8"
        val sol6 = SpokenMathParser.tryParseAnyEquationQuery("x plus 1 whole square minus x minus 1 whole square equals 8")
        assertNotNull(sol6)
        assertEquals("2", sol6!!.variables["x"])

        // 7. Spoken Hinglish with barabar: "x plus 1 ka whole square minus x minus 1 ka whole square barabar 8"
        val sol7 = SpokenMathParser.tryParseAnyEquationQuery("x plus 1 ka whole square minus x minus 1 ka whole square barabar 8")
        assertNotNull(sol7)
        assertEquals("2", sol7!!.variables["x"])

        // 8. Quadratic with whole squares: (x+1)^2 + (x-1)^2 = 20 -> 2x^2 + 2 = 20 -> x^2 = 9 -> roots 3, -3
        val sol8 = UniversalEquationSolver.solveAny("(x+1)^2 + (x-1)^2 = 20")
        assertNotNull(sol8)
        assertEquals(UniversalEquationSolver.EquationCategory.QUADRATIC, sol8!!.category)
        assertTrue(sol8.rootsSummary.contains("3") && sol8.rootsSummary.contains("-3"))

        // 9. Nested bracket product: 2(x+3) - 3(x-1) = 11 -> -x + 9 = 11 -> x = -2
        val sol9 = UniversalEquationSolver.solveAny("2(x+3) - 3(x-1) = 11")
        assertNotNull(sol9)
        assertEquals("-2", sol9!!.variables["x"])

        // 10. Whole cube expansion: "x plus 1 whole cube minus x cube equals 7" -> 3x^2 + 3x - 6 = 0 -> roots 1, -2
        val sol10 = SpokenMathParser.tryParseAnyEquationQuery("x plus 1 whole cube minus x cube equals 7")
        assertNotNull(sol10)
        assertEquals(UniversalEquationSolver.EquationCategory.QUADRATIC, sol10!!.category)
        assertTrue(sol10.rootsSummary.contains("1") && sol10.rootsSummary.contains("-2"))
    }

    @Test
    fun testHinglishNumberKiPowerNumber() {
        // 1. "2 ki power 2" -> 4.0
        val norm1 = SpokenMathParser.normalize("2 ki power 2")
        val eval1 = ScientificMathEvaluator.evaluate(norm1)
        assertTrue(eval1.isSuccess)
        assertEquals(4.0, eval1.values.first(), 1e-6)

        // 2. "2 ka power 3" -> 8.0
        val norm2 = SpokenMathParser.normalize("2 ka power 3")
        val eval2 = ScientificMathEvaluator.evaluate(norm2)
        assertTrue(eval2.isSuccess)
        assertEquals(8.0, eval2.values.first(), 1e-6)

        // 3. Hindi words: "do ki power do" -> 4.0
        val norm3 = SpokenMathParser.normalize("do ki power do")
        val eval3 = ScientificMathEvaluator.evaluate(norm3)
        assertTrue(eval3.isSuccess)
        assertEquals(4.0, eval3.values.first(), 1e-6)

        // 4. "paanch ki power teen" -> 125.0
        val norm4 = SpokenMathParser.normalize("paanch ki power teen")
        val eval4 = ScientificMathEvaluator.evaluate(norm4)
        assertTrue(eval4.isSuccess)
        assertEquals(125.0, eval4.values.first(), 1e-6)

        // 5. "10 ki power 4" -> 10000.0
        val norm5 = SpokenMathParser.normalize("10 ki power 4")
        val eval5 = ScientificMathEvaluator.evaluate(norm5)
        assertTrue(eval5.isSuccess)
        assertEquals(10000.0, eval5.values.first(), 1e-6)

        // 6. "2 ki ghat 3" -> 8.0
        val norm6 = SpokenMathParser.normalize("2 ki ghat 3")
        val eval6 = ScientificMathEvaluator.evaluate(norm6)
        assertTrue(eval6.isSuccess)
        assertEquals(8.0, eval6.values.first(), 1e-6)

        // 7. "2 raised to 4" -> 16.0
        val norm7 = SpokenMathParser.normalize("2 raised to 4")
        val eval7 = ScientificMathEvaluator.evaluate(norm7)
        assertTrue(eval7.isSuccess)
        assertEquals(16.0, eval7.values.first(), 1e-6)

        // 8. Negative power: "2 ki power minus 2" -> 0.25
        val norm8 = SpokenMathParser.normalize("2 ki power minus 2")
        val eval8 = ScientificMathEvaluator.evaluate(norm8)
        assertTrue(eval8.isSuccess)
        assertEquals(0.25, eval8.values.first(), 1e-6)

        // 9. In equation: "2 ki power x equals 16"
        val eqSol = SpokenMathParser.tryParseAnyEquationQuery("2 ki power x equals 16")
        assertNotNull(eqSol)
        assertTrue(eqSol!!.rootsSummary.contains("4"))
    }

    @Test
    fun testTrigonometricEquationsWithPowersAndIdentities() {
        // 1. User's exact prompt: sin^2x + cos^2x = 5 -> Contradiction / No real solution!
        val sol1 = UniversalEquationSolver.solveAny("sin^2x + cos^2x = 5")
        assertNotNull(sol1)
        assertEquals(UniversalEquationSolver.EquationCategory.CONTRADICTION, sol1!!.category)
        assertTrue(sol1.rootsSummary.contains("No real solution"))

        // 2. User's spoken form: "sin square x + cos square x = 5"
        val sol2 = SpokenMathParser.tryParseAnyEquationQuery("sin square x + cos square x = 5")
        assertNotNull(sol2)
        assertEquals(UniversalEquationSolver.EquationCategory.CONTRADICTION, sol2!!.category)
        assertTrue(sol2.rootsSummary.contains("No real solution"))

        // 3. Spoken Hinglish: "sin ka square x plus cos ka square x barabar 5"
        val sol3 = SpokenMathParser.tryParseAnyEquationQuery("sin ka square x plus cos ka square x barabar 5")
        assertNotNull(sol3)
        assertEquals(UniversalEquationSolver.EquationCategory.CONTRADICTION, sol3!!.category)

        // 4. Identity: sin^2x + cos^2x = 1 -> True for all real x!
        val sol4 = UniversalEquationSolver.solveAny("sin^2x + cos^2x = 1")
        assertNotNull(sol4)
        assertEquals(UniversalEquationSolver.EquationCategory.IDENTITY, sol4!!.category)
        assertTrue(sol4.rootsSummary.contains("Identity") || sol4.rootsSummary.contains("∀ x ∈ ℝ"))

        // 5. sin(x) = 0.5 -> 30°, 150°
        val sol5 = UniversalEquationSolver.solveAny("sin(x) = 0.5")
        assertNotNull(sol5)
        assertTrue(sol5!!.rootsSummary.contains("30°") && sol5.rootsSummary.contains("150°"))

        // 6. tan(x) = 1 -> 45°, 225°
        val sol6 = UniversalEquationSolver.solveAny("tan(x) = 1")
        assertNotNull(sol6)
        assertTrue(sol6!!.rootsSummary.contains("45°") && sol6.rootsSummary.contains("225°"))

        // 7. cos(x) = 0 -> 90°, 270°
        val sol7 = UniversalEquationSolver.solveAny("cos(x) = 0")
        assertNotNull(sol7)
        assertTrue(sol7!!.rootsSummary.contains("90°") && sol7.rootsSummary.contains("270°"))

        // 8. Higher power trig equation: sin^2x = 0.25 -> 30°, 150°, 210°, 330°
        val sol8 = UniversalEquationSolver.solveAny("sin^2x = 0.25")
        assertNotNull(sol8)
        assertTrue(sol8!!.rootsSummary.contains("30°") && sol8.rootsSummary.contains("150°"))

        // 9. sec^2x - tan^2x = 1 -> Identity!
        val sol9 = UniversalEquationSolver.solveAny("sec^2x - tan^2x = 1")
        assertNotNull(sol9)
        assertEquals(UniversalEquationSolver.EquationCategory.IDENTITY, sol9!!.category)

        // 10. sec^2x - tan^2x = 5 -> Contradiction!
        val sol10 = UniversalEquationSolver.solveAny("sec^2x - tan^2x = 5")
        assertNotNull(sol10)
        assertEquals(UniversalEquationSolver.EquationCategory.CONTRADICTION, sol10!!.category)

        // 11. csc^2x - cot^2x = 1 -> Identity!
        val sol11 = UniversalEquationSolver.solveAny("csc^2x - cot^2x = 1")
        assertNotNull(sol11)
        assertEquals(UniversalEquationSolver.EquationCategory.IDENTITY, sol11!!.category)

        // 12. cot(x) = 1 -> 45°, 225°
        val sol12 = UniversalEquationSolver.solveAny("cot(x) = 1")
        assertNotNull(sol12)
        assertTrue(sol12!!.rootsSummary.contains("45°") && sol12.rootsSummary.contains("225°"))

        // 13. sec(x) = 2 -> 60°, 300°
        val sol13 = UniversalEquationSolver.solveAny("sec(x) = 2")
        assertNotNull(sol13)
        assertTrue(sol13!!.rootsSummary.contains("60°") && sol13.rootsSummary.contains("300°"))

        // 14. csc(x) = 2 -> 30°, 150°
        val sol14 = UniversalEquationSolver.solveAny("csc(x) = 2")
        assertNotNull(sol14)
        assertTrue(sol14!!.rootsSummary.contains("30°") && sol14.rootsSummary.contains("150°"))

        // 15. tan^2x = 3 -> 60°, 120°, 240°, 300°
        val sol15 = UniversalEquationSolver.solveAny("tan^2x = 3")
        assertNotNull(sol15)
        assertTrue(sol15!!.rootsSummary.contains("60°") && sol15.rootsSummary.contains("120°"))
    }

    @Test
    fun testFunctionMultiplicationByItselfAndMixedFunctions() {
        // 1. Function multiplied by itself: sin(x) * sin(x) = 0.25 -> 30°, 150°
        val sol1 = UniversalEquationSolver.solveAny("sin(x) * sin(x) = 0.25")
        assertNotNull(sol1)
        assertTrue(sol1!!.rootsSummary.contains("30°") && sol1.rootsSummary.contains("150°"))

        // 2. sinx * sinx + cosx * cosx = 1 -> Identity
        val sol2 = UniversalEquationSolver.solveAny("sinx * sinx + cosx * cosx = 1")
        assertNotNull(sol2)
        assertEquals(UniversalEquationSolver.EquationCategory.IDENTITY, sol2!!.category)

        // 3. sinx * sinx + cosx * cosx = 5 -> Contradiction
        val sol3 = UniversalEquationSolver.solveAny("sinx * sinx + cosx * cosx = 5")
        assertNotNull(sol3)
        assertEquals(UniversalEquationSolver.EquationCategory.CONTRADICTION, sol3!!.category)

        // 4. Mixed trig functions: sin(x) * cos(x) = 0.5 -> 45°, 225°
        val sol4 = UniversalEquationSolver.solveAny("sin(x) * cos(x) = 0.5")
        assertNotNull(sol4)
        assertTrue(sol4!!.rootsSummary.contains("45°") && sol4.rootsSummary.contains("225°"))

        // 5. Implicit function multiplication: sinx cosx = 0.5 -> 45°, 225°
        val sol5 = UniversalEquationSolver.solveAny("sinx cosx = 0.5")
        assertNotNull(sol5)
        assertTrue(sol5!!.rootsSummary.contains("45°") && sol5.rootsSummary.contains("225°"))

        // 6. Reciprocal identity: tan(x) * cot(x) = 1 -> Identity
        val sol6 = UniversalEquationSolver.solveAny("tan(x) * cot(x) = 1")
        assertNotNull(sol6)
        assertEquals(UniversalEquationSolver.EquationCategory.IDENTITY, sol6!!.category)

        // 7. Reciprocal contradiction: tan(x) * cot(x) = 5 -> Contradiction
        val sol7 = UniversalEquationSolver.solveAny("tan(x) * cot(x) = 5")
        assertNotNull(sol7)
        assertEquals(UniversalEquationSolver.EquationCategory.CONTRADICTION, sol7!!.category)

        // 8. Algebraic * Trigonometric: x * sin(x) = 1
        val sol8 = UniversalEquationSolver.solveAny("x * sin(x) = 1")
        assertNotNull(sol8)
        assertTrue(sol8!!.variables.isNotEmpty())

        // 9. Algebraic * Exponential: x * exp(x) = 2
        val sol9 = UniversalEquationSolver.solveAny("x * exp(x) = 2")
        assertNotNull(sol9)
        assertTrue(sol9!!.rootsSummary.contains("0.85") || sol9.rootsSummary.contains("0.852"))

        // 10. Algebraic * Logarithmic: x * ln(x) = 1
        val sol10 = UniversalEquationSolver.solveAny("x * ln(x) = 1")
        assertNotNull(sol10)
        assertTrue(sol10!!.rootsSummary.contains("1.76") || sol10.rootsSummary.contains("1.763"))

        // 11. Spoken mixed: "sin x into cos x barabar point 5"
        val sol11 = SpokenMathParser.tryParseAnyEquationQuery("sin x into cos x barabar point 5")
        assertNotNull(sol11)
        assertTrue(sol11!!.rootsSummary.contains("45°"))
    }

    @Test
    fun testInverseTrigonometryFunctions() {
        // 1. Direct evaluations in radians
        val eval1 = ScientificMathEvaluator.evaluate("asin(0.5)")
        assertTrue(eval1.isSuccess)
        assertEquals(Math.PI / 6.0, eval1.values.first(), 1e-5)

        val eval2 = ScientificMathEvaluator.evaluate("acos(0.5)")
        assertTrue(eval2.isSuccess)
        assertEquals(Math.PI / 3.0, eval2.values.first(), 1e-5)

        val eval3 = ScientificMathEvaluator.evaluate("atan(1)")
        assertTrue(eval3.isSuccess)
        assertEquals(Math.PI / 4.0, eval3.values.first(), 1e-5)

        val eval4 = ScientificMathEvaluator.evaluate("asec(2)")
        assertTrue(eval4.isSuccess)
        assertEquals(Math.PI / 3.0, eval4.values.first(), 1e-5)

        val eval5 = ScientificMathEvaluator.evaluate("acsc(2)")
        assertTrue(eval5.isSuccess)
        assertEquals(Math.PI / 6.0, eval5.values.first(), 1e-5)

        val eval6 = ScientificMathEvaluator.evaluate("acot(1)")
        assertTrue(eval6.isSuccess)
        assertEquals(Math.PI / 4.0, eval6.values.first(), 1e-5)

        // 2. Notation variants: sin^-1, sin⁻¹, arcsin
        val eval7 = ScientificMathEvaluator.evaluate("sin^-1(0.5)")
        assertTrue(eval7.isSuccess)
        assertEquals(Math.PI / 6.0, eval7.values.first(), 1e-5)

        val eval8 = ScientificMathEvaluator.evaluate("sin⁻¹(0.5)")
        assertTrue(eval8.isSuccess)
        assertEquals(Math.PI / 6.0, eval8.values.first(), 1e-5)

        val eval9 = ScientificMathEvaluator.evaluate("arcsin(0.5)")
        assertTrue(eval9.isSuccess)
        assertEquals(Math.PI / 6.0, eval9.values.first(), 1e-5)

        // 3. Spoken commands in degrees
        val norm1 = SpokenMathParser.normalize("arcsine of 0.5")
        val spokenEval1 = ScientificMathEvaluator.evaluate(norm1)
        assertTrue(spokenEval1.isSuccess)
        assertEquals(30.0, spokenEval1.values.first(), 1e-4)

        val norm2 = SpokenMathParser.normalize("sin inverse of 0.5")
        val spokenEval2 = ScientificMathEvaluator.evaluate(norm2)
        assertTrue(spokenEval2.isSuccess)
        assertEquals(30.0, spokenEval2.values.first(), 1e-4)

        val norm3 = SpokenMathParser.normalize("tan inverse of 1")
        val spokenEval3 = ScientificMathEvaluator.evaluate(norm3)
        assertTrue(spokenEval3.isSuccess)
        assertEquals(45.0, spokenEval3.values.first(), 1e-4)

        val norm4 = SpokenMathParser.normalize("sec inverse of 2")
        val spokenEval4 = ScientificMathEvaluator.evaluate(norm4)
        assertTrue(spokenEval4.isSuccess)
        assertEquals(60.0, spokenEval4.values.first(), 1e-4)

        val norm5 = SpokenMathParser.normalize("cosec inverse of 2")
        val spokenEval5 = ScientificMathEvaluator.evaluate(norm5)
        assertTrue(spokenEval5.isSuccess)
        assertEquals(30.0, spokenEval5.values.first(), 1e-4)

        val norm6 = SpokenMathParser.normalize("cot inverse of 1")
        val spokenEval6 = ScientificMathEvaluator.evaluate(norm6)
        assertTrue(spokenEval6.isSuccess)
        assertEquals(45.0, spokenEval6.values.first(), 1e-4)
    }
}
