package com.example.engine

import java.util.regex.Pattern

/**
 * Intelligent voice math parser matching and extending the Python Voice Calculator.
 * Converts spoken English, Hinglish, and math phrases into clean mathematical expressions.
 */
object SpokenMathParser {

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

    private val ROOT_DEGREE_WORDS = mapOf(
        "square" to 2, "cube" to 3, "third" to 3, "fourth" to 4, "fifth" to 5,
        "sixth" to 6, "seventh" to 7, "eighth" to 8, "ninth" to 9, "tenth" to 10
    )

    private val HINDI_NUMBERS = mapOf(
        "shunya" to 0L, "ek" to 1L, "do" to 2L, "teen" to 3L, "char" to 4L, "chaar" to 4L,
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

    private val HOMOPHONE_FIXES = listOf(
        Regex("\\bbecause\\b") to "cos",
        Regex("\\bcause\\b") to "cos",
        Regex("\\bcost\\b") to "cos",
        Regex("\\bsign\\b") to "sine",
        Regex("\\bsigh\\b") to "sine",
        Regex("\\bsine\\b") to "sin",
        Regex("\\bcosine\\b") to "cos",
        Regex("\\btangent\\b") to "tan",
        Regex("\\bsecant\\b") to "sec",
        Regex("\\bcosecant\\b") to "csc",
        Regex("\\bcosec\\b") to "csc",
        Regex("\\bcotangent\\b") to "cot",
        Regex("\\bsqaure\\b|\\bsquar\\b") to "square",
        Regex("\\bcub\\b") to "cube",
        Regex("\\blog in\\b") to "log",
        Regex("\\blogon\\b") to "log",
        Regex("\\bpie\\b") to "pi",
        Regex("\\bfactory all\\b") to "factorial",
        Regex("\\bfactoral\\b") to "factorial",
        Regex("\\bscared\\b") to "squared",
        Regex("\\bspared\\b") to "squared",
        Regex("\\bscarred\\b") to "squared",
        Regex("\\bsquirt\\b") to "squared",
        Regex("\\bsquad\\b") to "squared",
        Regex("\\bsquat\\b") to "squared",
        Regex("\\bsquire\\b") to "square",
        Regex("\\bsquare it\\b") to "squared",
        Regex("\\bcubed?\\s+it\\b") to "cubed"
    )

    private val HINGLISH_FIXES = listOf(
        Regex("\\bjodo\\b|\\bjoda\\b|\\bjod\\b|\\badd karo\\b") to "plus",
        Regex("\\bghatao\\b|\\bghata\\b|\\bghataiye\\b|\\bsubtract karo\\b") to "minus",
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

    private val WORD_TO_SMALL_NUM = mapOf(
        "a" to 1L, "an" to 1L, "one" to 1L, "two" to 2L, "three" to 3L, "four" to 4L,
        "five" to 5L, "six" to 6L, "seven" to 7L, "eight" to 8L, "nine" to 9L
    )

    private val CORRECTION_TRIGGER = Regex("^(?:no wait|actually|correction|i meant|sorry)[,]?\\s*(.*)$")

    /**
     * Checks if spoken string is a memory command.
     * Returns description of operation or null if not a memory command.
     */
    fun checkMemoryCommand(raw: String): MemoryAction? {
        val t = raw.lowercase().trim()
        if (t.contains(Regex("\\bmemory plus\\b")) || t.contains(Regex("\\badd to memory\\b"))) {
            return MemoryAction.ADD
        }
        if (t.contains(Regex("\\bmemory minus\\b")) || t.contains(Regex("\\bsubtract from memory\\b"))) {
            return MemoryAction.SUBTRACT
        }
        if (t.contains(Regex("\\brecall memory\\b")) || t.contains(Regex("\\bwhat'?s in memory\\b"))) {
            return MemoryAction.RECALL
        }
        if (t.contains(Regex("\\bclear memory\\b"))) {
            return MemoryAction.CLEAR
        }
        return null
    }

    /**
     * Detects if spoken or typed query uses Hinglish keywords.
     */
    fun isHinglishQuery(raw: String): Boolean {
        val lower = raw.lowercase()
        val hinglishWords = listOf(
            "karo", "kijiye", "hal", "nikalo", "batao", "bataye", "gyat", "samikaran",
            "barabar", "shunya", "shoonya", "sifar", "jodo", "joda", "ghatao", "ghata",
            "guna", "bhag", "ka", "ki", "ke", "mool", "rekhik", "dwighat", "trighat",
            "aur", "varg", "ghan", "ek", "do", "teen", "char", "chaar", "paanch",
            "panch", "che", "chhe", "saat", "aath", "nau", "das", "hai",
            "bata", "batta", "batte", "aadha", "adha", "chauthai", "mein", "andar"
        )
        return hinglishWords.any { Regex("\\b$it\\b").containsMatchIn(lower) } || lower.contains("bracket me")
    }

    fun isHinglishInput(raw: String): Boolean = isHinglishQuery(raw)

    /**
     * Normalizes spoken bracket phrases into clean parentheses:
     * - "open bracket ... close bracket" -> ( ... )
     * - "2 into whole 3 plus 4" -> 2 into (3 plus 4)
     * - "2 into bracket me 3 plus 4" -> 2 into (3 plus 4)
     * - "2 into bracket mein 3 plus 4" -> 2 into (3 plus 4)
     * - "2 bracket 3 plus 4" -> 2 * (3 plus 4)
     * - "bracket me 2 plus 3 into bracket me 4 minus 1" -> (2 plus 3) into (4 minus 1)
     */
    fun handleSpokenBrackets(text: String): String {
        return PhoneticMathNormalizer.normalizeSpokenBrackets(text)
    }

    /**
     * Normalizes spoken "whole square", "whole cube", and powers of expressions:
     * - "x plus 1 whole square - x minus 1 whole square" -> (x plus 1)^2 - (x minus 1)^2
     * - "x plus 1 ka whole square minus x minus 1 ka whole square" -> (x plus 1)^2 - (x minus 1)^2
     * - "x plus 1 whole cube" -> (x plus 1)^3
     * - "(x+1) whole square" -> (x+1)^2
     * - "whole square of x plus 1" -> (x plus 1)^2
     */
    fun handleSpokenWholePowers(text: String): String {
        return PhoneticMathNormalizer.normalizeWholePowers(text)
    }

    /**
     * Replaces spoken trigonometry functions, squares, cubes, and arbitrary powers:
     * - "sin square 30", "sin squared 30", "sin sqaure 30", "sin varg 30" -> (sin((30)*pi/180))^2
     * - "cos square 30", "cos squared 30", "cos varg 30" -> (cos((30)*pi/180))^2
     * - "sin cube 30", "sin cubed 30", "sin ghan 30" -> (sin((30)*pi/180))^3
     * - "sin power 4 30", "sin ki power 2 30", "sin to the power of 2 30" -> (sin((30)*pi/180))^2
     * - "sin 30 square", "sin 30 ka square", "sin 30 ka varg" -> (sin((30)*pi/180))^2
     * - "sin 30", "cos 60", "tan 45" -> sin((30)*pi/180), cos((60)*pi/180), tan((45)*pi/180)
     */
    fun replaceSpokenTrigFunctionsAndPowers(text: String): String {
        var s = text
        s = s.replace(Regex("\\bdegrees?\\b"), "")

        val trigFuncNames = "(?:sine|sin|cosine|cos|tangent|tan|secant|sec|cosecant|cosec|csc|cotangent|cot)"

        // A. Square before angle: "sin square 30", "sin squared 30", "sin sqaure 30", "sin varg 30", "sin square x", "sin ka square x"
        s = s.replace(Regex("\\b($trigFuncNames)\\s+(?:ka\\s+|ki\\s+)?(?:square|squared|sqaure|squar|varg)\\s+(?:of\\s+)?(-?\\d+(?:\\.\\d+)?|[xyz]|theta|pi(?:\\s*/\\s*\\d+)?)", RegexOption.IGNORE_CASE)) { mr ->
            val fn = normalizeTrigName(mr.groupValues[1])
            val rawAngle = mr.groupValues[2]
            val angle = if (rawAngle.equals("theta", ignoreCase = true)) "x" else rawAngle
            val angleArg = if (angle == "x" || angle == "y" || angle == "z" || angle.contains("pi")) angle else "($angle)*pi/180"
            "($fn($angleArg))^2"
        }

        // B. Cube before angle: "sin cube 30", "sin cubed 30", "sin cub 30", "sin ghan 30"
        s = s.replace(Regex("\\b($trigFuncNames)\\s+(?:ka\\s+|ki\\s+)?(?:cube|cubed|cub|ghan)\\s+(?:of\\s+)?(-?\\d+(?:\\.\\d+)?|[xyz]|theta|pi(?:\\s*/\\s*\\d+)?)", RegexOption.IGNORE_CASE)) { mr ->
            val fn = normalizeTrigName(mr.groupValues[1])
            val rawAngle = mr.groupValues[2]
            val angle = if (rawAngle.equals("theta", ignoreCase = true)) "x" else rawAngle
            val angleArg = if (angle == "x" || angle == "y" || angle == "z" || angle.contains("pi")) angle else "($angle)*pi/180"
            "($fn($angleArg))^3"
        }

        // C. Arbitrary power before angle: "sin power 4 30", "sin to the power of 4 30", "sin ki power 2 30", "sin ki ghat 2 30"
        s = s.replace(Regex("\\b($trigFuncNames)\\s+(?:(?:to the )?power(?: of)?|ka power|ki power|ka ghat|ki ghat|ghat|\\^)\\s*(\\d+(?:\\.\\d+)?)\\s*(?:of\\s+)?(-?\\d+(?:\\.\\d+)?|[xyz]|theta|pi(?:\\s*/\\s*\\d+)?)", RegexOption.IGNORE_CASE)) { mr ->
            val fn = normalizeTrigName(mr.groupValues[1])
            val p = mr.groupValues[2]
            val rawAngle = mr.groupValues[3]
            val angle = if (rawAngle.equals("theta", ignoreCase = true)) "x" else rawAngle
            val angleArg = if (angle == "x" || angle == "y" || angle == "z" || angle.contains("pi")) angle else "($angle)*pi/180"
            "($fn($angleArg))^$p"
        }

        // D. Power AFTER angle: "sin 30 square", "sin 30 ka square", "sin 30 ka varg", "sin 30 cubed"
        s = s.replace(Regex("\\b($trigFuncNames)\\s+(?:of\\s+)?(-?\\d+(?:\\.\\d+)?|[xyz]|theta|pi(?:\\s*/\\s*\\d+)?)\\s*(?:ka\\s+|ki\\s+)?(?:square|squared|sqaure|squar|varg)\\b", RegexOption.IGNORE_CASE)) { mr ->
            val fn = normalizeTrigName(mr.groupValues[1])
            val rawAngle = mr.groupValues[2]
            val angle = if (rawAngle.equals("theta", ignoreCase = true)) "x" else rawAngle
            val angleArg = if (angle == "x" || angle == "y" || angle == "z" || angle.contains("pi")) angle else "($angle)*pi/180"
            "($fn($angleArg))^2"
        }

        s = s.replace(Regex("\\b($trigFuncNames)\\s+(?:of\\s+)?(-?\\d+(?:\\.\\d+)?|[xyz]|theta|pi(?:\\s*/\\s*\\d+)?)\\s*(?:ka\\s+|ki\\s+)?(?:cube|cubed|cub|ghan)\\b", RegexOption.IGNORE_CASE)) { mr ->
            val fn = normalizeTrigName(mr.groupValues[1])
            val rawAngle = mr.groupValues[2]
            val angle = if (rawAngle.equals("theta", ignoreCase = true)) "x" else rawAngle
            val angleArg = if (angle == "x" || angle == "y" || angle == "z" || angle.contains("pi")) angle else "($angle)*pi/180"
            "($fn($angleArg))^3"
        }

        // E. Simple degree-1 trig functions: "sin 30", "cos 60", "tan 45", "sec 60", "csc 30", "cot 45"
        s = s.replace(Regex("\\b($trigFuncNames)\\s+(?:of\\s+)?(-?\\d+(?:\\.\\d+)?|[xyz]|theta|pi(?:\\s*/\\s*\\d+)?)", RegexOption.IGNORE_CASE)) { mr ->
            val fn = normalizeTrigName(mr.groupValues[1])
            val rawAngle = mr.groupValues[2]
            val angle = if (rawAngle.equals("theta", ignoreCase = true)) "x" else rawAngle
            val angleArg = if (angle == "x" || angle == "y" || angle == "z" || angle.contains("pi")) angle else "($angle)*pi/180"
            "$fn($angleArg)"
        }

        return s
    }

    private fun normalizeTrigName(raw: String): String {
        return when (raw.lowercase()) {
            "sine", "sin" -> "sin"
            "cosine", "cos" -> "cos"
            "tangent", "tan" -> "tan"
            "secant", "sec" -> "sec"
            "cosecant", "cosec", "csc" -> "csc"
            "cotangent", "cot" -> "cot"
            else -> raw.lowercase()
        }
    }

    /**
     * Tries to parse and solve any spoken equation:
     * - Linear: "solve 3x plus 5 equals 20", "5x minus 7 is equal to 3x plus 11", "SOLVE 3x+5 = 0"
     * - System: "solve system 2x plus y equals 7 and x minus y equals 2", "2x plus y barabar 7 aur x minus y barabar 2"
     * - Fractions: "(1/3)x + (2/5)x = 5", "1 by 3 x plus 2 by 5 x = 5", "1 bata 3 x plus 2 bata 5 x barabar 5"
     * - Quadratic: "x square minus 5x plus 6 is equal to 0", "dwighat samikaran x square minus 5x plus 6 barabar 0"
     * - Cubic: "solve x cubed minus 6x squared plus 11x minus 6 equals 0", "x cube + x square + x +2=0"
     * - Transcendental / Non-linear: "solve 2 to the power of x equals 16", "solve cos x equals x"
     */
    fun tryParseAnyEquationQuery(raw: String): UniversalEquationSolver.EquationSolution? {
        val isHinglish = isHinglishQuery(raw)
        var t = raw.lowercase().trim()

        val hasEquality = t.contains("=") || t.contains("equal") || t.contains("barabar")
        val hasVariable = Regex("(?<![a-zA-Z])[xyz](?![a-zA-Z])|\\btheta\\b").containsMatchIn(t)
        val hasExplicitSolveTrigger = t.contains("solve") || t.contains("equation") || t.contains("system") ||
                t.contains("root") || t.contains("hal") || t.contains("samikaran") || t.contains("mool") ||
                t.contains("nikalo") || t.contains("batao")

        // Must have either explicit equality, or a variable with solving intent.
        // Pure function evaluations (e.g. "sin 30", "cos 60", "tan 45", "sec 60") have no variable and no equality!
        val isEquationIntent = hasEquality || (hasVariable && hasExplicitSolveTrigger) || (hasVariable && (t.contains("sin") || t.contains("cos") || t.contains("tan") || t.contains("sec") || t.contains("csc") || t.contains("cot") || t.contains("^") || t.contains("square") || t.contains("cube")))

        if (!isEquationIntent) return null

        // Replace homophones
        // Homophones, Hinglish, and number words normalization via dedicated PhoneticMathNormalizer
        t = PhoneticMathNormalizer.normalizeHomophones(t)
        t = PhoneticMathNormalizer.normalizeHinglish(t)
        t = PhoneticMathNormalizer.normalizeNumberWords(t)

        // Conversational triggers in both English and Hinglish
        val triggers = listOf(
            "ko solve karo", "solve karo", "hal karo", "solve kijiye", "hal kijiye",
            "ka hal nikalo", "hal nikalo", "ka hal batao", "hal batao",
            "ka man nikalo", "ki value nikalo", "man nikalo", "value nikalo",
            "ka man gyat karo", "gyat karo", "ka man batao", "batao", "bataye",
            "ke roots nikalo", "ke roots batao", "ka root nikalo", "ke root batao",
            "ke mool nikalo", "mool nikalo", "ke mool batao", "mool batao",
            "rekhik samikaran", "dwighat samikaran", "trighat samikaran", "samikaran",
            "solve system of linear equations", "solve system of equations", "solve system",
            "system of equations", "system", "solve quadratic equation", "quadratic equation",
            "solve cubic equation", "cubic equation", "solve the equation", "solve equation",
            "solve for x", "solve for y", "solve for z", "solve", "find the roots of",
            "find roots of", "find roots for", "find root of", "roots of", "root of",
            "find x", "find y", "find z", "calculate", "please", "equation"
        )
        for (trig in triggers) {
            t = t.replace(Regex("\\b${Pattern.quote(trig)}\\b"), "")
        }

        // Replace equality words
        t = t.replace(Regex("\\b(?:is\\s+)?(?:equal(?:s)?\\s*(?:to)?|barabar(?:\\s+hai)?)\\b"), "=")
        t = t.replace(Regex("\\bzero\\b|\\bshunya\\b|\\bsifar\\b"), "0")

        // Inverse trig in equations: e.g. "sin inverse x equals 0.5", "arcsin x equals 0.5"
        for ((name, short, sym) in listOf(
            Triple("sine", "sin", "asin"),
            Triple("cosine", "cos", "acos"),
            Triple("tangent", "tan", "atan"),
            Triple("secant", "sec", "asec"),
            Triple("cosecant", "csc", "acsc"),
            Triple("cosec", "csc", "acsc"),
            Triple("cotangent", "cot", "acot")
        )) {
            t = t.replace(Regex("\\b(?:arc$name|arc$short|arc\\s+$name|arc\\s+$short|inverse\\s+$name|inverse\\s+$short)\\s*(?:of\\s+)?([xyz]|theta)\\b", RegexOption.IGNORE_CASE)) { "$sym(${it.groupValues[1]})" }
            t = t.replace(Regex("\\b(?:$name|$short)\\s+inverse\\s*(?:of\\s+)?([xyz]|theta)\\b", RegexOption.IGNORE_CASE)) { "$sym(${it.groupValues[1]})" }
        }

        // Handle spoken brackets (e.g. "2 into whole x plus 3 equals 14", "bracket me 2x plus 1")
        t = handleSpokenBrackets(t)
        t = replaceSpokenTrigFunctionsAndPowers(t)
        t = handleSpokenWholePowers(t)

        // 1. Spoken fraction variables: "1 by 3 x", "1 bata 3 x", "1/3 x", "2 by 5 x", "two by five x"
        t = t.replace(Regex("(\\d+(?:\\.\\d+)?)\\s*(?:by|over|bata|batta|batte|divided by|/)\\s*(\\d+(?:\\.\\d+)?)\\s*([xyz])\\b")) {
            " (${it.groupValues[1]}/${it.groupValues[2]})${it.groupValues[3]} "
        }

        // Spoken fraction before variable power: e.g. "1 by 2 x^2", "1 by 2 x square"
        t = t.replace(Regex("(\\d+(?:\\.\\d+)?)\\s*(?:by|over|bata|batta|batte|divided by|/)\\s*(\\d+(?:\\.\\d+)?)\\s*([xyz])\\s*(?:\\^|power)")) {
            " (${it.groupValues[1]}/${it.groupValues[2]})${it.groupValues[3]}^ "
        }

        // 2. Spoken constant fractions: "1 by 3", "2 bata 5", "5 by 2"
        t = t.replace(Regex("(\\d+(?:\\.\\d+)?)\\s*(?:by|over|bata|batta|batte|divided by)\\s*(\\d+(?:\\.\\d+)?)(?!\\s*[xyz])")) {
            " (${it.groupValues[1]}/${it.groupValues[2]}) "
        }

        // Half and quarter words
        t = t.replace(Regex("\\b(?:half|aadha|adha)\\s*([xyz])\\b"), " (1/2)$1 ")
        t = t.replace(Regex("\\b(?:quarter|chauthai)\\s*([xyz])\\b"), " (1/4)$1 ")
        t = t.replace(Regex("\\b(?:half|aadha|adha)\\b"), " (1/2) ")
        t = t.replace(Regex("\\b(?:quarter|chauthai)\\b"), " (1/4) ")

        // Powers, cubes, squares (ensure spaces so adjacent words like plus/minus are preserved)
        t = t.replace(Regex("\\b(\\d+)\\s*x\\s*(?:ka\\s+|ki\\s+)?(?:cubed|cube|ghan)\\b"), " $1x^3 ")
        t = t.replace(Regex("\\bx\\s*(?:ka\\s+|ki\\s+)?(?:cubed|cube|ghan)\\b"), " x^3 ")
        t = t.replace(Regex("\\b(\\d+)\\s*x\\s*(?:ka\\s+|ki\\s+)?(?:squared|square|varg)\\b"), " $1x^2 ")
        t = t.replace(Regex("\\bx\\s*(?:ka\\s+|ki\\s+)?(?:squared|square|varg)\\b"), " x^2 ")
        t = t.replace(Regex("x\\s*(?:ki\\s+)?power\\s*(?:3|teen)"), " x^3 ")
        t = t.replace(Regex("x\\s*(?:ki\\s+)?power\\s*(?:2|do)"), " x^2 ")
        t = t.replace(Regex("x\\s*(?:ki\\s+)?power\\s*(\\d+)"), " x^$1 ")
        t = t.replace(Regex("x\\s*\\^\\s*3|x\\s*³"), " x^3 ")
        t = t.replace(Regex("x\\s*\\^\\s*2|x\\s*²"), " x^2 ")
        val eqPowerRegex = Regex("\\b(?:raised\\s+to\\s+(?:the\\s+)?power(?:\\s+of)?|raised\\s+to|to\\s+the\\s+power(?:\\s+of)?|to\\s+power(?:\\s+of)?|(?:ka|ki|ke)\\s+power(?:\\s+of)?|(?:ka|ki|ke)\\s+ghat|power(?:\\s+of)?|ghat)\\b", RegexOption.IGNORE_CASE)
        t = t.replace(eqPowerRegex, " ^ ")

        // Operators
        t = t.replace(Regex("\\bplus\\b|\\bjodo\\b|\\bjoda\\b|\\bdhan\\b"), " + ")
        t = t.replace(Regex("\\bminus\\b|\\bghatao\\b|\\bghata\\b|\\brin\\b"), " - ")
        t = t.replace(Regex("\\btimes\\b|\\binto\\b|\\bguna\\b"), "*")
        t = t.replace(Regex("\\bdivided by\\b|\\bbhag\\b|\\bbata\\b"), "/")

        // In a system of equations, "and" or "aur" separates eq 1 and eq 2
        t = t.replace(Regex("\\s+\\band\\b\\s+|\\s+\\baur\\b\\s+"), " ; ")

        // Fix spaced variables: "5 x" -> "5x", "2 y" -> "2y"
        t = t.replace(Regex("(\\d+)\\s+([xyz])\\b"), "$1$2")
        t = t.replace(Regex("(\\d+)\\s+([xyz])\\^"), "$1$2^")
        t = t.replace(Regex("\\s+"), " ").trim()

        val sol = UniversalEquationSolver.solveAny(t)
        if (sol != null) {
            return if (isHinglish && sol.spokenSummaryHinglish.isNotBlank()) {
                sol.copy(spokenSummary = sol.spokenSummaryHinglish)
            } else {
                sol
            }
        }
        return null
    }

    /**
     * Tries to parse and solve a spoken quadratic equation query.
     * e.g. "x square minus 5x plus 6 is equal to 0", "solve x squared minus 5x plus 6 equals 0",
     * "roots of 2x squared plus 4x minus 6", "quadratic a 1 b -5 c 6", "x square minus 5x plus 6 ke roots".
     */
    fun tryParseQuadraticQuery(raw: String): QuadraticEquationSolver.QuadraticSolution? {
        var t = raw.lowercase().trim()
        val isQuadraticIntent = t.contains("quadratic") || t.contains("roots") || t.contains("root") ||
                t.contains("x squared") || t.contains("x square") || t.contains("x²") || t.contains("x^2") ||
                t.contains("x square") || t.contains("ke roots") ||
                (t.contains("x") && (t.contains("equal") || t.contains("=")))

        if (!isQuadraticIntent) return null

        // Replace homophones, hindi numbers, english number words via PhoneticMathNormalizer
        t = PhoneticMathNormalizer.normalizeHomophones(t)
        t = PhoneticMathNormalizer.normalizeHinglish(t)
        t = PhoneticMathNormalizer.normalizeNumberWords(t)

        // Check for explicit "a ... b ... c ..." format
        val abcRegex = Regex("a\\s*=?\\s*(-?\\d+(?:\\.\\d+)?)[,\\s]+b\\s*=?\\s*(-?\\d+(?:\\.\\d+)?)[,\\s]+c\\s*=?\\s*(-?\\d+(?:\\.\\d+)?)")
        val abcMatch = abcRegex.find(t)
        if (abcMatch != null) {
            val a = abcMatch.groupValues[1].toDoubleOrNull() ?: 1.0
            val b = abcMatch.groupValues[2].toDoubleOrNull() ?: 0.0
            val c = abcMatch.groupValues[3].toDoubleOrNull() ?: 0.0
            return QuadraticEquationSolver.solve(a, b, c)
        }

        // Clean conversational opening triggers
        val triggers = listOf(
            "solve quadratic equation", "quadratic equation", "solve quadratic",
            "find roots of", "find the roots of", "find roots for", "roots of", "root of",
            "solve", "please", "what are the roots of", "what is", "calculate",
            "ke roots", "ka root", "ke root"
        )
        for (trig in triggers) {
            t = t.replace(Regex("\\b${Pattern.quote(trig)}\\b"), "")
        }

        // Replace "is equal to", "is equals to", "equals to", "equal to", "equals", "barabar" with "="
        t = t.replace(Regex("\\b(?:is\\s+)?(?:equal(?:s)?\\s*(?:to)?|barabar(?:\\s+hai)?)\\b"), "=")
        t = t.replace(Regex("\\bzero\\b|\\bshunya\\b"), "0")

        // Remove filler words like "is", "the", "value of"
        t = t.replace(Regex("\\b(?:is|the|value\\s+of)\\b"), "")

        // Normalize math words & operators
        t = t.replace(Regex("(\\d+)?\\s*x\\s*(?:ka\\s+)?(?:squared|square|varg)\\b"), "$1x^2")
        t = t.replace(Regex("\\bx\\s*(?:ka\\s+)?(?:squared|square|varg)\\b"), "x^2")
        t = t.replace(Regex("x\\s*\\^\\s*2"), "x^2")
        t = t.replace(Regex("x\\s*²"), "x^2")
        t = t.replace(Regex("\\bplus\\b"), "+")
        t = t.replace(Regex("\\bminus\\b"), "-")
        t = t.replace(Regex("\\s+"), " ").trim()

        // "5 x" -> "5x"
        t = t.replace(Regex("(\\d+)\\s+x\\b"), "$1x")
        t = t.replace(Regex("(\\d+)\\s+x\\^2"), "$1x^2")

        if (!t.contains("=") && (t.contains("x^2") || t.contains("x²"))) {
            t += " = 0"
        }

        if (t.contains("x^2") || t.contains("x²") || Regex("(?<![a-zA-Z])x(?![a-zA-Z])").containsMatchIn(t)) {
            val solution = QuadraticEquationSolver.parseAndSolve(t)
            if (solution != null) return solution
        }

        return null
    }

    enum class MemoryAction {
        ADD, SUBTRACT, RECALL, CLEAR
    }

    /**
     * Detects and processes verbal corrections (e.g. "no wait, times not plus").
     */
    fun handleCorrection(newRaw: String, lastRaw: String?): String? {
        if (lastRaw == null) return null
        val match = CORRECTION_TRIGGER.find(newRaw.lowercase().trim()) ?: return null
        val remainder = match.groupValues[1].trim()
        if (remainder.isEmpty()) return null

        // "times not plus" -> swap "plus" with "times"
        val swapMatch = Regex("^(\\w+)\\s+not\\s+(\\w+)$").find(remainder)
        if (swapMatch != null) {
            val correctWord = swapMatch.groupValues[1]
            val wrongWord = swapMatch.groupValues[2]
            if (lastRaw.contains(Regex("\\b${Pattern.quote(wrongWord)}\\b", RegexOption.IGNORE_CASE))) {
                return lastRaw.replace(Regex("\\b${Pattern.quote(wrongWord)}\\b", RegexOption.IGNORE_CASE), correctWord)
            }
        }

        // "not plus but times" -> swap "plus" with "times"
        val swapMatch2 = Regex("^not\\s+(\\w+)\\s+but\\s+(\\w+)$").find(remainder)
        if (swapMatch2 != null) {
            val wrongWord = swapMatch2.groupValues[1]
            val correctWord = swapMatch2.groupValues[2]
            if (lastRaw.contains(Regex("\\b${Pattern.quote(wrongWord)}\\b", RegexOption.IGNORE_CASE))) {
                return lastRaw.replace(Regex("\\b${Pattern.quote(wrongWord)}\\b", RegexOption.IGNORE_CASE), correctWord)
            }
        }

        // Otherwise replace entire calculation with remainder
        return remainder
    }

    /**
     * Normalizes spoken text into a clean mathematical expression.
     */
    fun normalize(raw: String): String {
        var t = PhoneticMathNormalizer.normalizeHomophones(raw.lowercase().trim())
        t = PhoneticMathNormalizer.normalizeHinglish(t)
        t = t.replace(Regex("[?!]"), "")
        t = t.replace("%", " percent ")
        t = t.replace(Regex("\\s+"), " ").trim()

        // Remove filler phrases
        val fillerPhrases = listOf("what's", "whats", "what is", "calculate", "please", "equals", "equal to")
        for (phrase in fillerPhrases) {
            t = t.replace(Regex("\\b${Pattern.quote(phrase)}\\b"), "")
        }

        t = PhoneticMathNormalizer.normalizeNumberWords(t)

        // Handle spoken brackets and whole powers via PhoneticMathNormalizer
        t = PhoneticMathNormalizer.normalizeSpokenBrackets(t)
        t = PhoneticMathNormalizer.normalizeWholePowers(t)

        // Spoken constant fractions: "3 by 7" -> "(3/7)", "14 by 9" -> "(14/9)", "3 bata 7" -> "(3/7)"
        t = t.replace(Regex("(\\d+(?:\\.\\d+)?)\\s*(?:by|over|bata|batte|batta)\\s*(\\d+(?:\\.\\d+)?)(?!\\s*[xyz])")) { mr ->
            "(${mr.groupValues[1]}/${mr.groupValues[2]})"
        }
        t = t.replace(Regex("\\b(?:half|aadha|adha)\\b"), "(1/2)")
        t = t.replace(Regex("\\b(?:quarter|chauthai)\\b"), "(1/4)")

        t = t.replace(Regex("\\bnegative\\b"), "-")
        t = t.replace(Regex("-\\s+(\\d)"), "-$1")

        t = replaceCompoundFunctions(t)

        // Trigonometry functions, squares, cubes, and arbitrary powers
        t = replaceSpokenTrigFunctionsAndPowers(t)

        t = t.replace(Regex("\\bnatural log (?:of )?(-?\\d+(?:\\.\\d+)?)"), "log($1)")
        t = t.replace(Regex("\\blog (?:of )?(-?\\d+(?:\\.\\d+)?)"), "log($1,10)")
        t = t.replace(Regex("\\bsquare root (?:of )?(-?\\d+(?:\\.\\d+)?)"), "sqrt($1)")

        val replacements = listOf(
            Regex("\\bplus\\b") to "+",
            Regex("\\band\\b") to ";",
            Regex("\\bminus\\b") to "-",
            Regex("\\b(?:raised\\s+to\\s+(?:the\\s+)?power(?:\\s+of)?|raised\\s+to|to\\s+the\\s+power(?:\\s+of)?|to\\s+power(?:\\s+of)?|(?:ka|ki|ke)\\s+power(?:\\s+of)?|(?:ka|ki|ke)\\s+ghat|power(?:\\s+of)?|ghat)\\b", RegexOption.IGNORE_CASE) to "**",
            Regex("\\bthe\\b") to "",
            Regex("\\btimes\\b") to "*",
            Regex("\\binto\\b") to "*",
            Regex("\\bguna\\b|\\bgunaa\\b") to "*",
            Regex("\\bmultiplied by\\b") to "*",
            Regex("\\bmultiply(?:ing)? by\\b") to "*",
            Regex("\\bdivided by\\b") to "/",
            Regex("\\bdivide(?:d)? by\\b") to "/",
            Regex("\\bbhag\\b|\\bbhaga\\b") to "/",
            Regex("\\bbata\\b|\\bbatte\\b") to "/",
            Regex("\\bover\\b") to "/",
            Regex("\\bby\\b") to "/",
            Regex("\\bx\\b") to "*",
            Regex("×") to "*",
            Regex("÷") to "/",
            Regex("\\[|\\{") to "(",
            Regex("\\]|\\}") to ")",
            Regex("\\bsquared\\b") to "**2",
            Regex("\\bsquare\\b") to "**2",
            Regex("\\bcubed\\b") to "**3",
            Regex("\\bpercent of\\b") to "%OF%",
            Regex("\\bpercent\\b") to "/100",
            Regex("\\bopen bracket\\b|\\bbracket open\\b|\\bopen paren(?:thesis)?\\b") to "(",
            Regex("\\bclose bracket\\b|\\bbracket close\\b|\\bclose paren(?:thesis)?\\b") to ")"
        )

        for ((regex, repl) in replacements) {
            t = t.replace(regex, repl)
        }

        // Standalone 'e' -> 'E' (Euler's number)
        t = t.replace(Regex("\\be\\b"), "E")
        t = t.replace(Regex("\\s+"), " ").trim()

        return t
    }

    private fun replaceFractions(text: String): String {
        val fractionRegex = Regex(
            "\\b(a|an|one|two|three|four|five|six|seven|eight|nine)\\s+" +
            "(half|halves|third|thirds|quarter|quarters|fourth|fourths|fifth|fifths|" +
            "sixth|sixths|seventh|sevenths|eighth|eighths|ninth|ninths|tenth|tenths)\\s+of\\b"
        )
        return fractionRegex.replace(text) { match ->
            val numeratorWord = match.groupValues[1]
            val denomWord = match.groupValues[2]
            val denom = FRACTION_WORDS[denomWord] ?: 1
            val num = WORD_TO_SMALL_NUM[numeratorWord] ?: 1L
            "(($num/$denom)*"
        }
    }

    private fun replaceDecimalPoints(text: String): String {
        val allWords = (SMALL_NUMBERS.keys + SCALE_WORDS.keys).joinToString("|")
        val digitWordsPattern = DIGIT_WORDS.keys.joinToString("|")
        val decimalRegex = Regex("\\b((?:(?:$allWords)[\\s-]*)+)point\\s+((?:(?:$digitWordsPattern)\\s*)+)")

        return decimalRegex.replace(text) { match ->
            val wholePart = wordsToNumber(match.groupValues[1].trim())
            val digitTokens = match.groupValues[2].trim().split(Regex("\\s+"))
            val digits = digitTokens.mapNotNull { DIGIT_WORDS[it] }.joinToString("")
            if (wholePart != null && digits.isNotEmpty()) {
                "$wholePart.$digits"
            } else {
                match.value
            }
        }
    }

    private fun wordsToNumber(phrase: String): Long? {
        val tokens = phrase.trim().split(Regex("[\\s-]+"))
        var total = 0L
        var current = 0L
        var found = false

        for (tok in tokens) {
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

    private fun replaceNumberWords(text: String): String {
        val allWords = (SMALL_NUMBERS.keys + SCALE_WORDS.keys).sortedByDescending { it.length }.joinToString("|")
        val pattern = Regex("\\b(?:$allWords)(?:[\\s-]+(?:$allWords))*\\b")
        return pattern.replace(text) { match ->
            val num = wordsToNumber(match.value)
            num?.toString() ?: match.value
        }
    }

    private fun hindiWordsToNumber(phrase: String): Long? {
        val tokens = phrase.trim().split(Regex("[\\s-]+"))
        var total = 0L
        var current = 0L
        var found = false

        for (tok in tokens) {
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

    private fun replaceHindiNumberWords(text: String): String {
        val allHindi = (HINDI_NUMBERS.keys + HINDI_SCALE_WORDS.keys).sortedByDescending { it.length }.joinToString("|")
        val pattern = Regex("\\b(?:$allHindi)(?:[\\s-]+(?:$allHindi))*\\b")
        return pattern.replace(text) { match ->
            val num = hindiWordsToNumber(match.value)
            num?.toString() ?: match.value
        }
    }

    private fun replaceCompoundFunctions(raw: String): String {
        var t = raw

        // log base B of X -> log(X,B)
        t = t.replace(Regex("log base (-?\\d+(?:\\.\\d+)?) of (-?\\d+(?:\\.\\d+)?)"), "log($2,$1)")

        // word root of X (square/cube/fourth/fifth root of X) -> (X)**(1/degree)
        val rootWords = ROOT_DEGREE_WORDS.keys.joinToString("|")
        t = Regex("\\b($rootWords) root (?:of )?(-?\\d+(?:\\.\\d+)?)").replace(t) { match ->
            val word = match.groupValues[1]
            val num = match.groupValues[2]
            val degree = ROOT_DEGREE_WORDS[word] ?: 2
            "($num)**(1/$degree)"
        }

        // factorial of X / X factorial
        t = t.replace(Regex("factorial (?:of )?(-?\\d+(?:\\.\\d+)?)"), "factorial($1)")
        t = t.replace(Regex("(-?\\d+(?:\\.\\d+)?) factorial\\b"), "factorial($1)")

        // Hindi/Hinglish: "X ka square/varg" -> X**2, "X ka cube/ghan" -> X**3, "X ka vargmul" -> sqrt(X)
        t = t.replace(Regex("(-?\\d+(?:\\.\\d+)?)\\s*ka\\s+(?:square|varg)\\b"), "($1)**2")
        t = t.replace(Regex("(-?\\d+(?:\\.\\d+)?)\\s*ka\\s+(?:cube|ghan)\\b"), "($1)**3")
        t = t.replace(Regex("(-?\\d+(?:\\.\\d+)?)\\s*ka\\s+vargmul\\b"), "sqrt($1)")

        // Hindi/Hinglish: "X ki power Y" / "X ka power Y" -> X**Y
        t = t.replace(Regex("(-?\\d+(?:\\.\\d+)?)\\s*k[ai]\\s+power\\s+(-?\\d+(?:\\.\\d+)?)"), "($1)**($2)")
        t = t.replace(Regex("\\be\\s*k[ai]\\s+power\\s+(-?\\d+(?:\\.\\d+)?)"), "(E)**($1)")

        // absolute value of X
        t = t.replace(Regex("absolute value (?:of )?(-?\\d+(?:\\.\\d+)?)"), "Abs($1)")

        // X choose Y / combinations of X and Y
        t = t.replace(Regex("(-?\\d+(?:\\.\\d+)?) choose (-?\\d+(?:\\.\\d+)?)"), "binomial($1,$2)")
        t = t.replace(Regex("combinations? (?:of )?(-?\\d+(?:\\.\\d+)?) and (-?\\d+(?:\\.\\d+)?)"), "binomial($1,$2)")

        // permutations of X and Y -> X! / (X-Y)!
        t = t.replace(Regex("permutations? (?:of )?(-?\\d+(?:\\.\\d+)?) and (-?\\d+(?:\\.\\d+)?)"), "(factorial($1)/factorial(($1)-($2)))")

        // modulus
        t = t.replace(Regex("(-?\\d+(?:\\.\\d+)?) mod(?:ulo)? (-?\\d+(?:\\.\\d+)?)"), "Mod($1,$2)")

        // floor / ceiling
        t = t.replace(Regex("floor (?:of )?(-?\\d+(?:\\.\\d+)?)"), "floor($1)")
        t = t.replace(Regex("ceiling (?:of )?(-?\\d+(?:\\.\\d+)?)"), "ceiling($1)")

        // inverse trig -> in degrees: (180/pi)*asin(X)
        val trigPairs = listOf(
            Triple("sine", "sin", "asin"),
            Triple("cosine", "cos", "acos"),
            Triple("tangent", "tan", "atan"),
            Triple("secant", "sec", "asec"),
            Triple("cosecant", "csc", "acsc"),
            Triple("cosec", "csc", "acsc"),
            Triple("cotangent", "cot", "acot")
        )
        for ((name, short, sym) in trigPairs) {
            t = t.replace(Regex("\\b(?:arc$name|arc$short|arc\\s+$name|arc\\s+$short|inverse\\s+$name|inverse\\s+$short)\\s*(?:of\\s+)?(-?\\d+(?:\\.\\d+)?)", RegexOption.IGNORE_CASE), "((180/pi)*$sym($1))")
            t = t.replace(Regex("\\b(?:$name|$short)\\s+inverse\\s*(?:of\\s+)?(-?\\d+(?:\\.\\d+)?)", RegexOption.IGNORE_CASE), "((180/pi)*$sym($1))")
            t = t.replace(Regex("\\binverse\\s+of\\s+(?:$name|$short)\\s*(?:of\\s+)?(-?\\d+(?:\\.\\d+)?)", RegexOption.IGNORE_CASE), "((180/pi)*$sym($1))")
        }

        // hyperbolic trig
        for ((name, short, sym) in listOf(Triple("sine", "sin", "sinh"), Triple("cosine", "cos", "cosh"), Triple("tangent", "tan", "tanh"))) {
            t = t.replace(Regex("\\bhyperbolic (?:$name|$short) (?:of )?(-?\\d+(?:\\.\\d+)?)"), "$sym($1)")
        }

        return t
    }
}
