package com.example

import com.example.engine.PhoneticMathNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Comprehensive Unit Tests for the PhoneticMathNormalizer layer.
 */
class PhoneticMathNormalizerTest {

    @Test
    fun testHomophoneDisambiguation() {
        // Trigonometric & mathematical homophones
        assertEquals("sin 30", PhoneticMathNormalizer.normalizeHomophones("sign 30"))
        assertEquals("sin 45", PhoneticMathNormalizer.normalizeHomophones("sigh 45"))
        assertEquals("cos 60", PhoneticMathNormalizer.normalizeHomophones("cause 60"))
        assertEquals("cos 0", PhoneticMathNormalizer.normalizeHomophones("cost 0"))
        assertEquals("cos 90", PhoneticMathNormalizer.normalizeHomophones("because 90"))
        assertEquals("2 * pi", PhoneticMathNormalizer.normalizeHomophones("2 * pie"))
        assertEquals("log 100", PhoneticMathNormalizer.normalizeHomophones("log in 100"))
        assertEquals("5 factorial", PhoneticMathNormalizer.normalizeHomophones("5 factoral"))
        assertEquals("5 factorial", PhoneticMathNormalizer.normalizeHomophones("5 factory all"))
        assertEquals("5 squared", PhoneticMathNormalizer.normalizeHomophones("5 scared"))
        assertEquals("4 squared", PhoneticMathNormalizer.normalizeHomophones("4 squad"))
        assertEquals("3 cube", PhoneticMathNormalizer.normalizeHomophones("3 cub"))
    }

    @Test
    fun testHinglishPhraseNormalization() {
        // Operation verbs
        assertEquals("5 plus 3", PhoneticMathNormalizer.normalizeHinglish("5 jodo 3"))
        assertEquals("10 minus 4", PhoneticMathNormalizer.normalizeHinglish("10 ghatao 4"))
        assertEquals("6 times 2", PhoneticMathNormalizer.normalizeHinglish("6 guna 2"))
        assertEquals("8 divided by 2", PhoneticMathNormalizer.normalizeHinglish("8 bhag 2"))
        assertEquals("1 by 3", PhoneticMathNormalizer.normalizeHinglish("1 bata 3"))
        assertEquals("2 by 5", PhoneticMathNormalizer.normalizeHinglish("2 batta 5"))
        assertEquals("half", PhoneticMathNormalizer.normalizeHinglish("aadha"))
        assertEquals("quarter", PhoneticMathNormalizer.normalizeHinglish("chauthai"))
        assertEquals("equals", PhoneticMathNormalizer.normalizeHinglish("barabar"))
        assertEquals("equals", PhoneticMathNormalizer.normalizeHinglish("barabar hai"))
        assertEquals("square", PhoneticMathNormalizer.normalizeHinglish("varg"))
        assertEquals("cube", PhoneticMathNormalizer.normalizeHinglish("ghan"))
    }

    @Test
    fun testNumberWordNormalizationEnglish() {
        // English words to digits
        assertEquals("25", PhoneticMathNormalizer.normalizeNumberWords("twenty five"))
        assertEquals("150", PhoneticMathNormalizer.normalizeNumberWords("one hundred fifty"))
        assertEquals("1000", PhoneticMathNormalizer.normalizeNumberWords("one thousand"))
        assertEquals("3.14", PhoneticMathNormalizer.normalizeNumberWords("three point one four"))
    }

    @Test
    fun testNumberWordNormalizationHindi() {
        // Hindi number words to digits
        assertEquals("150", PhoneticMathNormalizer.normalizeNumberWords("ek sau pachas"))
        assertEquals("2000", PhoneticMathNormalizer.normalizeNumberWords("do hazar"))
        assertEquals("2", PhoneticMathNormalizer.normalizeNumberWords("do"))
        assertEquals("3", PhoneticMathNormalizer.normalizeNumberWords("teen"))
        assertEquals("5", PhoneticMathNormalizer.normalizeNumberWords("paanch"))
        assertEquals("10", PhoneticMathNormalizer.normalizeNumberWords("das"))
        assertEquals("0", PhoneticMathNormalizer.normalizeNumberWords("shunya"))
    }

    @Test
    fun testExponentialsAndPowersHinglishAndEnglish() {
        // "number ki power number"
        val p1 = PhoneticMathNormalizer.normalizePowersAndExponents("2 ki power 2")
        assertEquals("2**2", p1)

        val p2 = PhoneticMathNormalizer.normalizePowersAndExponents("2 ka power 3")
        assertEquals("2**3", p2)

        val p3 = PhoneticMathNormalizer.normalizePowersAndExponents("2 ke power 4")
        assertEquals("2**4", p3)

        val p4 = PhoneticMathNormalizer.normalizePowersAndExponents("2 power 2")
        assertEquals("2**2", p4)

        val p5 = PhoneticMathNormalizer.normalizePowersAndExponents("2 to the power 3")
        assertEquals("2**3", p5)

        val p6 = PhoneticMathNormalizer.normalizePowersAndExponents("2 raised to 4")
        assertEquals("2**4", p6)

        val p7 = PhoneticMathNormalizer.normalizePowersAndExponents("2 ki ghat 3")
        assertEquals("2**3", p7)

        // Caret option for equation parser
        val pEq = PhoneticMathNormalizer.normalizePowersAndExponents("2 ki power x", useCaretForPowers = true)
        assertTrue(pEq.contains("2 ^ x"))
    }

    @Test
    fun testWholePowersBinomialExpansion() {
        // "x plus 1 whole square"
        val w1 = PhoneticMathNormalizer.normalizeWholePowers("x plus 1 whole square")
        assertEquals("(x plus 1)^2", w1)

        // "x plus 1 ka whole square"
        val w2 = PhoneticMathNormalizer.normalizeWholePowers("x plus 1 ka whole square")
        assertEquals("(x plus 1)^2", w2)

        // Separate binomial whole squares connected by minus:
        // "x plus 1 whole square minus x minus 1 whole square"
        val w3 = PhoneticMathNormalizer.normalizeWholePowers("x plus 1 whole square minus x minus 1 whole square")
        assertEquals("(x plus 1)^2 minus (x minus 1)^2", w3)

        // Whole cube
        val w4 = PhoneticMathNormalizer.normalizeWholePowers("x plus 1 whole cube")
        assertEquals("(x plus 1)^3", w4)

        // Bracketed whole square: "(x + 1) whole square"
        val w5 = PhoneticMathNormalizer.normalizeWholePowers("(x + 1) whole square")
        assertEquals("(x + 1)^2", w5)
    }

    @Test
    fun testSpokenBrackets() {
        // Explicit open/close
        val b1 = PhoneticMathNormalizer.normalizeSpokenBrackets("open bracket 3 plus 4 close bracket")
        assertTrue(b1.contains("(") && b1.contains(")"))

        // "bracket me 2x plus 1"
        val b2 = PhoneticMathNormalizer.normalizeSpokenBrackets("bracket me 2x plus 1")
        assertEquals("(2x plus 1)", b2)

        // "into whole 3 plus 4"
        val b3 = PhoneticMathNormalizer.normalizeSpokenBrackets("into whole 3 plus 4")
        assertEquals("into (3 plus 4)", b3)
    }

    @Test
    fun testCorrectionTriggers() {
        val c1 = PhoneticMathNormalizer.extractCorrection("no wait, 5 plus 5")
        assertEquals("5 plus 5", c1)

        val c2 = PhoneticMathNormalizer.extractCorrection("actually 10 times 2")
        assertEquals("10 times 2", c2)

        val c3 = PhoneticMathNormalizer.extractCorrection("i meant 4 divided by 2")
        assertEquals("4 divided by 2", c3)

        val c4 = PhoneticMathNormalizer.extractCorrection("sorry 3 squared")
        assertEquals("3 squared", c4)

        val c5 = PhoneticMathNormalizer.extractCorrection("just 5 plus 5")
        assertNull(c5)
    }

    @Test
    fun testFullNormalizationPipeline() {
        // Complete normalization of a spoken Hinglish query: "do ki power do"
        val res1 = PhoneticMathNormalizer.normalize("do ki power do")
        assertEquals("2**2", res1)

        // "paanch ki power teen"
        val res2 = PhoneticMathNormalizer.normalize("paanch ki power teen")
        assertEquals("5**3", res2)

        // "sign 30 jodo cause 60"
        val res3 = PhoneticMathNormalizer.normalize("sign 30 jodo cause 60")
        assertEquals("sin 30 plus cos 60", res3)
    }
}
