package com.example.engine

import java.util.regex.Pattern

/**
 * PhoneticMathNormalizer: A dedicated, modular phonetic and colloquial speech normalization layer.
 *
 * Responsibilities:
 * 1. Speech-to-Text Homophone Disambiguation (e.g. "sign" -> "sin", "cause" -> "cos", "pie" -> "pi").
 * 2. Bilingual Hindi / Hinglish Phrase Normalization (e.g. "jodo" -> "plus", "barabar" -> "equals", "bata" -> "by").
 * 3. Exponential & Power Phrasing Normalization (e.g. "2 ki power 2" -> "2**2", "ka whole square" -> "(...)^2").
 * 4. Number Word Translation for both English and Hindi numerals (e.g. "ek sau pachas" -> "150", "three point five" -> "3.5").
 * 5. Speech Correction Triggers ("no wait", "actually", "i meant").
 */
object PhoneticMathNormalizer {

    // =========================================================================
    // 1. DICTIONARIES & MAPPINGS
    // =========================================================================

    private val SMALL_NUMBERS = mapOf(
        "zero" to 0L, "one" to 1L, "two" to 2L, "three" to 3L, "four" to 4L,
        "five" to 5L, "six" to 6L, "seven" to 7L, "eight" to 8L, "nine" to 9L,
        "ten" to 10L, "eleven" to 11L, "twelve" to 12L, "thirteen" to 13L,
        "fourteen" to 14L, "fifteen" to 15L, "sixteen" to 16L, "seventeen" to 17L,
        "eighteen" to 18L, "nineteen" to 19L, "twenty" to 20L, "thirty" to 30L,
        "forty" to 40L, "fifty" to 50L, "sixty" to 60L, "seventy" to 70L,
        "eighty" to 80L, "ninety" to 90L
    )

    private val DIGIT_WORDS = mapOf(
        "zero" to 0, "one" to 1, "two" to 2, "three" to 3, "four" to 4,
        "five" to 5, "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9
    )

    private val SCALE_WORDS = mapOf(
        "hundred" to 100L,
        "thousand" to 1000L,
        "million" to 1000000L
    )

    private val FRACTION_WORDS = mapOf(
        "half" to 2, "halves" to 2, "third" to 3, "thirds" to 3,
        "quarter" to 4, "quarters" to 4, "fourth" to 4, "fourths" to 4,
        "fifth" to 5, "fifths" to 5, "sixth" to 6, "sixths" to 6,
        "seventh" to 7, "sevenths" to 7, "eighth" to 8, "eighths" to 8,
        "ninth" to 9, "ninths" to 9, "tenth" to 10, "tenths" to 10
    )

    private val HINDI_NUMBERS = mapOf(
        "shunya" to 0L, "shoonya" to 0L, "sifar" to 0L,
        "ek" to 1L, "do" to 2L, "teen" to 3L, "char" to 4L, "chaar" to 4L,
        "paanch" to 5L, "panch" to 5L, "che" to 6L, "chhe" to 6L, "cheh" to 6L,
        "saat" to 7L, "aath" to 8L, "nau" to 9L, "das" to 10L,
        "gyarah" to 11L, "barah" to 12L, "terah" to 13L, "chaudah" to 14L, "pandrah" to 15L,
        "solah" to 16L, "satrah" to 17L, "atharah" to 18L, "unnees" to 19L, "bees" to 20L,
        "ikkis" to 21L, "baees" to 22L, "baais" to 22L, "teis" to 23L, "chaubis" to 24L, "pachis" to 25L,
        "chhabbis" to 26L, "sattaees" to 27L, "atthaees" to 28L, "unatis" to 29L, "unattis" to 29L, "tees" to 30L,
        "ikattis" to 31L, "battis" to 32L, "taintis" to 33L, "chauntis" to 34L, "paintis" to 35L,
        "chhattis" to 36L, "saintis" to 37L, "adhatis" to 38L, "untalis" to 39L, "chalis" to 40L,
        "iktalis" to 41L, "bayalis" to 42L, "tetalis" to 43L, "chavalis" to 44L, "chauvalis" to 44L, "paintalis" to 45L,
        "chhiyalis" to 46L, "saintalis" to 47L, "adtalis" to 48L, "unchas" to 49L, "pachas" to 50L,
        "ikyavan" to 51L, "bawan" to 52L, "tirpan" to 53L, "chauvan" to 54L, "pachpan" to 55L,
        "chhappan" to 56L, "sattavan" to 57L, "atthavan" to 58L, "unsath" to 59L, "saath" to 60L,
        "iksath" to 61L, "basath" to 62L, "tirsath" to 63L, "chausath" to 64L, "painsath" to 65L,
        "chhiyasath" to 66L, "sadsath" to 67L, "adsath" to 68L, "unhattar" to 69L, "sattar" to 70L,
        "ikhattar" to 71L, "bahattar" to 72L, "tihattar" to 73L, "chauhattar" to 74L, "pachattar" to 75L,
        "chhihattar" to 76L, "satahattar" to 77L, "atthahattar" to 78L, "unyasi" to 79L, "assi" to 80L,
        "ikyasi" to 81L, "bayasi" to 82L, "tirasi" to 83L, "chaurasi" to 84L, "pachasi" to 85L,
        "chhiyasi" to 86L, "sattasi" to 87L, "atthasi" to 88L, "nawasi" to 89L, "nabbe" to 90L,
        "ikyanave" to 91L, "banave" to 92L, "tiranave" to 93L, "chauranve" to 94L, "pachanave" to 95L,
        "chhiyanave" to 96L, "sattanave" to 97L, "atthanave" to 98L, "ninyanave" to 99L
    )

    private val HINDI_SCALE_WORDS = mapOf(
        "sau" to 100L,
        "hazar" to 1000L,
        "hazaar" to 1000L,
        "lakh" to 100000L,
        "crore" to 10000000L
    )

    private val HOMOPHONE_RULES = listOf(
        Regex("\\bbecause\\b|\\bcause\\b|\\bcost\\b") to "cos",
        Regex("\\bsign\\b|\\bsigh\\b|\\bsine\\b") to "sin",
        Regex("\\bcosine\\b") to "cos",
        Regex("\\btangent\\b") to "tan",
        Regex("\\bsecant\\b") to "sec",
        Regex("\\bcosecant\\b|\\bcosec\\b") to "csc",
        Regex("\\bcotangent\\b") to "cot",
        Regex("\\bsqaure\\b|\\bsquar\\b") to "square",
        Regex("\\bcub\\b") to "cube",
        Regex("\\blog in\\b|\\blogon\\b") to "log",
        Regex("\\bpie\\b") to "pi",
        Regex("\\bfactory all\\b|\\bfactoral\\b") to "factorial",
        Regex("\\bscared\\b|\\bspared\\b|\\bscarred\\b|\\bsquirt\\b|\\bsquad\\b|\\bsquat\\b|\\bsquire\\b") to "squared",
        Regex("\\bsquare it\\b") to "squared",
        Regex("\\bcubed?\\s+it\\b") to "cubed"
    )

    private val HINGLISH_RULES = listOf(
        Regex("\\bjodo\\b|\\bjoda\\b|\\bjod\\b|\\badd karo\\b|\\bdhan\\b") to "plus",
        Regex("\\bghatao\\b|\\bghata\\b|\\bghataiye\\b|\\bsubtract karo\\b|\\brin\\b") to "minus",
        Regex("\\bguna\\b|\\bgunaa\\b|\\bmultiply karo\\b") to "times",
        Regex("\\bbhaga\\b|\\bbhag\\b|\\bbhagaiye\\b|\\bdivide karo\\b|\\btaqseem\\b") to "divided by",
        Regex("\\bpratishat\\b|\\bfeesadi\\b") to "percent",
        Regex("\\bbarabar(?:\\s+hai)?\\b") to "equals",
        Regex("\\bbata\\b|\\bbt\\b|\\bhatta\\b|\\bbatta\\b|\\bbatte\\b") to "by",
        Regex("\\baadha\\b|\\badha\\b") to "half",
        Regex("\\bchauthai\\b") to "quarter",
        Regex("\\bvarg\\b") to "square",
        Regex("\\bghan\\b") to "cube"
    )

    private val CORRECTION_TRIGGER_REGEX = Regex("^(?:no wait|actually|correction|i meant|sorry)[,]?\\s*(.*)$", RegexOption.IGNORE_CASE)

    // =========================================================================
    // 2. PUBLIC API METHODS
    // =========================================================================

    /**
     * Checks if the utterance begins with a correction phrase (e.g. "no wait, 5 plus 5").
     * Returns the corrected portion of speech, or null if no trigger is present.
     */
    fun extractCorrection(raw: String): String? {
        val match = CORRECTION_TRIGGER_REGEX.find(raw.trim()) ?: return null
        val corrected = match.groupValues[1].trim()
        return if (corrected.isNotEmpty()) corrected else null
    }

    /**
     * Replaces common speech-to-text mathematical homophones.
     */
    fun normalizeHomophones(text: String): String {
        var t = text
        for ((regex, repl) in HOMOPHONE_RULES) {
            t = t.replace(regex, repl)
        }
        return t
    }

    /**
     * Normalizes Hindi and Hinglish operations, words, and idioms.
     */
    fun normalizeHinglish(text: String): String {
        var t = text
        for ((regex, repl) in HINGLISH_RULES) {
            t = t.replace(regex, repl)
        }
        return t
    }

    /**
     * Replaces both English ("twenty five", "three point five") and Hindi numerals ("ek sau pachas").
     */
    fun normalizeNumberWords(text: String): String {
        var t = replaceFractions(text)
        t = replaceDecimalPoints(t)
        t = replaceHindiNumberWords(t)
        t = replaceEnglishNumberWords(t)
        return t
    }

    /**
     * Normalizes all exponential and power phrasing in English and Hinglish:
     * - "2 ki power 2" -> "2**2"
     * - "2 ka power 3" -> "2**3"
     * - "2 ke power 4" -> "2**4"
     * - "2 power 3" -> "2**3"
     * - "2 to the power 3" -> "2**3"
     * - "2 raised to 3" -> "2**3"
     * - "2 ki ghat 3" -> "2**3"
     * - "x plus 1 whole square" -> "(x plus 1)^2"
     */
    fun normalizePowersAndExponents(text: String, useCaretForPowers: Boolean = false): String {
        var s = text

        // 1. Spoken whole powers: (x+1) whole square, x plus 1 whole square, x plus 1 ka whole square
        s = normalizeWholePowers(s)

        // 2. Exponentials / Powers: "number ki power number", "x ki power 2", "2 to the power 3"
        val powerOp = if (useCaretForPowers) " ^ " else "**"
        val powerRegex = Regex(
            "\\s*\\b(?:raised\\s+to\\s+(?:the\\s+)?power(?:\\s+of)?|" +
            "raised\\s+to|" +
            "to\\s+the\\s+power(?:\\s+of)?|" +
            "to\\s+power(?:\\s+of)?|" +
            "(?:ka|ki|ke)\\s+power(?:\\s+of)?|" +
            "(?:ka|ki|ke)\\s+ghat|" +
            "power(?:\\s+of)?|" +
            "ghat)\\b\\s*",
            RegexOption.IGNORE_CASE
        )
        s = s.replace(powerRegex, powerOp)

        return s
    }

    /**
     * Normalizes whole square, whole cube, and whole power N expressions:
     * - "x plus 1 whole square" -> "(x plus 1)^2"
     * - "x plus 1 ka whole square" -> "(x plus 1)^2"
     * - "(x + 1) whole square" -> "(x + 1)^2"
     * - "whole square of x plus 1" -> "(x plus 1)^2"
     * - "x plus 1 whole cube" -> "(x plus 1)^3"
     */
    fun normalizeWholePowers(text: String): String {
        var s = text

        // 1. "whole square of (something)" / "whole cube of (something)"
        s = s.replace(Regex("\\bwhole\\s+(?:square|sqaure|squar|varg)\\s+(?:of\\s+)?([^=;+*-]+?)(?=\\s+(?:plus|minus|equals|barabar|is\\s+equal|into|times|divided|\\+|-|\\*|\\/|=|;)|$)", RegexOption.IGNORE_CASE)) { mr ->
            "(${mr.groupValues[1].trim()})^2"
        }
        s = s.replace(Regex("\\bwhole\\s+(?:cube|cubed|ghan)\\s+(?:of\\s+)?([^=;+*-]+?)(?=\\s+(?:plus|minus|equals|barabar|is\\s+equal|into|times|divided|\\+|-|\\*|\\/|=|;)|$)", RegexOption.IGNORE_CASE)) { mr ->
            "(${mr.groupValues[1].trim()})^3"
        }

        // 2. Explicit bracketed expressions: "(x + 1) whole square", "(x + 1) ka whole square"
        s = s.replace(Regex("\\(([^()]+)\\)\\s*(?:ka\\s+|ki\\s+)?(?:whole\\s+)?(?:square|sqaure|squar|varg)\\b", RegexOption.IGNORE_CASE)) { mr ->
            "(${mr.groupValues[1]})^2"
        }
        s = s.replace(Regex("\\(([^()]+)\\)\\s*(?:ka\\s+|ki\\s+)?(?:whole\\s+)?(?:cube|cubed|ghan)\\b", RegexOption.IGNORE_CASE)) { mr ->
            "(${mr.groupValues[1]})^3"
        }
        s = s.replace(Regex("\\(([^()]+)\\)\\s*(?:ka\\s+|ki\\s+)?whole\\s+(?:power\\s*|ghat\\s*)(\\d+)\\b", RegexOption.IGNORE_CASE)) { mr ->
            "(${mr.groupValues[1]})^${mr.groupValues[2]}"
        }

        // 3. Binomial or polynomial expressions ending in "whole square":
        // Exclude reserved math keywords (whole, square, cube, varg, ghan) so terms like 'square' in previous expressions are not matched as variable terms
        val token = "(?!\\b(?:whole|square|sqaure|squar|cube|cubed|varg|ghan|equals|barabar)\\b)[a-zA-Z0-9_.]+"
        val binomialExpr = "(\\([a-zA-Z0-9_+\\-*\\/\\s.]+\\)|\\b$token(?:\\s*(?:[+\\-]|plus|minus)\\s*$token)+)"

        val binomialWholeSquare = Regex("$binomialExpr\\s+(?:ka\\s+|ki\\s+)?whole\\s+(?:square|sqaure|squar|varg)\\b", RegexOption.IGNORE_CASE)
        s = s.replace(binomialWholeSquare) { mr ->
            val expr = mr.groupValues[1].trim().removePrefix("(").removeSuffix(")")
            "($expr)^2"
        }

        val binomialWholeCube = Regex("$binomialExpr\\s+(?:ka\\s+|ki\\s+)?whole\\s+(?:cube|cubed|ghan)\\b", RegexOption.IGNORE_CASE)
        s = s.replace(binomialWholeCube) { mr ->
            val expr = mr.groupValues[1].trim().removePrefix("(").removeSuffix(")")
            "($expr)^3"
        }

        val binomialWholePowerN = Regex("$binomialExpr\\s+(?:ka\\s+|ki\\s+)?whole\\s+(?:power\\s*|ghat\\s*)(\\d+)\\b", RegexOption.IGNORE_CASE)
        s = s.replace(binomialWholePowerN) { mr ->
            val expr = mr.groupValues[1].trim().removePrefix("(").removeSuffix(")")
            val p = mr.groupValues[2]
            "($expr)^$p"
        }

        return s
    }

    /**
     * Converts spoken bracket terminology into mathematical parentheses:
     * - "open bracket ... close bracket" -> "( ... )"
     * - "bracket me 3 plus 4" -> "(3 plus 4)"
     * - "into whole 3 plus 4" -> "into (3 plus 4)"
     */
    fun normalizeSpokenBrackets(text: String): String {
        var s = text

        // 1. Explicit open and close bracket words
        s = s.replace(Regex("\\b(?:open\\s+(?:bracket|paren|parenthesis)|bracket\\s+open|paren\\s+open|bracket\\s+shuru)\\b", RegexOption.IGNORE_CASE), " ( ")
        s = s.replace(Regex("\\b(?:close\\s+(?:bracket|paren|parenthesis)|bracket\\s+close|paren\\s+close|bracket\\s+band|bracket\\s+khatam)\\b", RegexOption.IGNORE_CASE), " ) ")

        // 2. Preceded by operator: "into whole ...", "into bracket me ...", "times whole ...", "divided by whole ..."
        val opBracketRegex = Regex("(\\b(?:into|times|guna|divided\\s+by|over|bhag|bata|\\*|\\/|\\+|-)\\s+)(?:whole\\s+(?:of\\s+)?|bracket\\s+(?:mein|me|ke\\s+andar)\\b\\s*|in\\s+bracket\\s*|inside\\s+bracket\\s*|bracket\\s+)([^()]+?)(?=\\s+(?:into|times|guna|divided\\s+by|over|bhag|\\*|\\/|\\band\\b|\\baur\\b)|$)", RegexOption.IGNORE_CASE)
        s = s.replace(opBracketRegex) { mr ->
            val op = mr.groupValues[1]
            val inside = mr.groupValues[2].trim()
            "$op($inside)"
        }

        // 3. Leading "bracket me ...", "bracket mein ...", "whole ...":
        val leadBracketRegex = Regex("^(?:whole\\s+(?:of\\s+)?|bracket\\s+(?:mein|me|ke\\s+andar)\\b\\s*|in\\s+bracket\\s*|inside\\s+bracket\\s*)([^()]+?)(?=\\s+(?:into|times|guna|divided\\s+by|over|bhag|\\*|\\/|\\band\\b|\\baur\\b)|$)", RegexOption.IGNORE_CASE)
        s = s.replace(leadBracketRegex) { mr ->
            val inside = mr.groupValues[1].trim()
            "($inside)"
        }

        // 4. "2 bracket 3 plus 4" or "2 bracket me 3 plus 4" without explicit "into":
        val numBracketRegex = Regex("(\\d+(?:\\.\\d+)?|[a-zA-Z]+|\\))\\s+(?:bracket\\s+(?:mein|me|ke\\s+andar)\\b\\s*|bracket\\s+)([^()]+?)(?=\\s+(?:into|times|guna|divided\\s+by|over|bhag|\\*|\\/|\\band\\b|\\baur\\b)|$)", RegexOption.IGNORE_CASE)
        s = s.replace(numBracketRegex) { mr ->
            val prefix = mr.groupValues[1]
            val inside = mr.groupValues[2].trim()
            "$prefix * ($inside)"
        }

        return s
    }

    /**
     * Complete phonetic normalization pipeline for spoken queries.
     */
    fun normalize(raw: String): String {
        var t = raw.lowercase().trim()
        t = t.replace(Regex("[?!]"), "")
        t = t.replace("%", " percent ")
        t = t.replace(Regex("\\s+"), " ").trim()

        t = normalizeHomophones(t)
        t = normalizeHinglish(t)
        t = normalizeNumberWords(t)
        t = normalizeSpokenBrackets(t)
        t = normalizePowersAndExponents(t)

        return t
    }

    // =========================================================================
    // 3. PRIVATE NUMBER PARSERS
    // =========================================================================

    private fun replaceFractions(text: String): String {
        val fractionRegex = Regex(
            "\\b(a|an|one|two|three|four|five|six|seven|eight|nine)\\s+" +
            "(half|halves|third|thirds|quarter|quarters|fourth|fourths|fifth|fifths|" +
            "sixth|sixths|seventh|sevenths|eighth|eighths|ninth|ninths|tenth|tenths)\\s+of\\b",
            RegexOption.IGNORE_CASE
        )
        val wordToNum = mapOf("a" to 1L, "an" to 1L, "one" to 1L, "two" to 2L, "three" to 3L, "four" to 4L, "five" to 5L, "six" to 6L, "seven" to 7L, "eight" to 8L, "nine" to 9L)
        return fractionRegex.replace(text) { match ->
            val numeratorWord = match.groupValues[1].lowercase()
            val denomWord = match.groupValues[2].lowercase()
            val denom = FRACTION_WORDS[denomWord] ?: 1
            val num = wordToNum[numeratorWord] ?: 1L
            "(($num/$denom)*"
        }
    }

    private fun replaceDecimalPoints(text: String): String {
        val allWords = (SMALL_NUMBERS.keys + SCALE_WORDS.keys).joinToString("|")
        val digitWordsPattern = DIGIT_WORDS.keys.joinToString("|")
        val decimalRegex = Regex("\\b((?:(?:$allWords)[\\s-]*)+)point\\s+((?:(?:$digitWordsPattern)\\s*)+)", RegexOption.IGNORE_CASE)

        return decimalRegex.replace(text) { match ->
            val wholePart = wordsToNumber(match.groupValues[1].trim())
            val decPart = match.groupValues[2].trim().split(Regex("\\s+"))
                .mapNotNull { DIGIT_WORDS[it.lowercase()] }
                .joinToString("")
            "$wholePart.$decPart"
        }
    }

    private fun replaceEnglishNumberWords(text: String): String {
        val allNumWords = (SMALL_NUMBERS.keys + SCALE_WORDS.keys).sortedByDescending { it.length }.joinToString("|")
        val pattern = Regex("\\b(?:$allNumWords)(?:[\\s-]+(?:$allNumWords))*\\b", RegexOption.IGNORE_CASE)
        return pattern.replace(text) { match ->
            val num = wordsToNumber(match.value)
            num?.toString() ?: match.value
        }
    }

    private fun wordsToNumber(phrase: String): Long? {
        val tokens = phrase.trim().split(Regex("[\\s-]+"))
        var total = 0L
        var current = 0L
        var found = false

        for (rawTok in tokens) {
            val tok = rawTok.lowercase()
            if (SMALL_NUMBERS.containsKey(tok)) {
                current += SMALL_NUMBERS[tok]!!
                found = true
            } else if (tok == "hundred") {
                current = (if (current == 0L) 1L else current) * 100L
                found = true
            } else if (SCALE_WORDS.containsKey(tok)) {
                current = (if (current == 0L) 1L else current) * SCALE_WORDS[tok]!!
                total += current
                current = 0L
                found = true
            }
        }
        total += current
        return if (found) total else null
    }

    private fun replaceHindiNumberWords(text: String): String {
        val allHindi = (HINDI_NUMBERS.keys + HINDI_SCALE_WORDS.keys).sortedByDescending { it.length }.joinToString("|")
        val pattern = Regex("\\b(?:$allHindi)(?:[\\s-]+(?:$allHindi))*\\b", RegexOption.IGNORE_CASE)
        return pattern.replace(text) { match ->
            val num = hindiWordsToNumber(match.value)
            num?.toString() ?: match.value
        }
    }

    private fun hindiWordsToNumber(phrase: String): Long? {
        val tokens = phrase.trim().split(Regex("[\\s-]+"))
        var total = 0L
        var current = 0L
        var found = false

        for (rawTok in tokens) {
            val tok = rawTok.lowercase()
            if (HINDI_NUMBERS.containsKey(tok)) {
                current += HINDI_NUMBERS[tok]!!
                found = true
            } else if (tok == "sau") {
                current = (if (current == 0L) 1L else current) * 100L
                found = true
            } else if (HINDI_SCALE_WORDS.containsKey(tok)) {
                current = (if (current == 0L) 1L else current) * HINDI_SCALE_WORDS[tok]!!
                total += current
                current = 0L
                found = true
            }
        }
        total += current
        return if (found) total else null
    }
}
