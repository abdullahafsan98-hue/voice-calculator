package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SpokenExampleCategory(
    val categoryName: String,
    val iconTag: String = "math",
    val examples: List<Triple<String, String, String>> // Spoken phrase, Math result/syntax, Function tag
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpokenExamplesSheet(
    onSelectExample: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

    val allCategories = remember {
        listOf(
            SpokenExampleCategory(
                categoryName = "Trig: Direct (sin, cos, tan)",
                iconTag = "sin",
                examples = listOf(
                    Triple("sine of 30 degrees", "sin(30°) = 0.5", "sin"),
                    Triple("sin 45", "sin(45°) ≈ 0.7071", "sin"),
                    Triple("cosine of 60 degrees", "cos(60°) = 0.5", "cos"),
                    Triple("cos 0", "cos(0°) = 1", "cos"),
                    Triple("tangent of 45 degrees", "tan(45°) = 1", "tan"),
                    Triple("tan 60", "tan(60°) = √3 ≈ 1.7321", "tan"),
                    Triple("sin tees degree", "sin(30°) = 0.5 (Hindi)", "sin"),
                    Triple("cos saath degree", "cos(60°) = 0.5 (Hindi)", "cos"),
                    Triple("tan paentalis", "tan(45°) = 1 (Hindi)", "tan")
                )
            ),
            SpokenExampleCategory(
                categoryName = "Trig: Reciprocal (sec, cosec, cot)",
                iconTag = "sec",
                examples = listOf(
                    Triple("secant of 60 degrees", "sec(60°) = 1/cos(60°) = 2", "sec"),
                    Triple("sec 0", "sec(0°) = 1", "sec"),
                    Triple("cosecant of 30 degrees", "csc(30°) = 1/sin(30°) = 2", "csc"),
                    Triple("cosec 90", "csc(90°) = 1", "cosec"),
                    Triple("csc 45", "csc(45°) = √2 ≈ 1.4142", "csc"),
                    Triple("cotangent of 45 degrees", "cot(45°) = 1", "cot"),
                    Triple("cot 30", "cot(30°) = √3 ≈ 1.7321", "cot"),
                    Triple("sec saath", "sec(60°) = 2 (Hinglish)", "sec"),
                    Triple("cosec tees", "csc(30°) = 2 (Hinglish)", "cosec"),
                    Triple("cot paentalis", "cot(45°) = 1 (Hinglish)", "cot")
                )
            ),
            SpokenExampleCategory(
                categoryName = "Trig: Inverse & Hyperbolic",
                iconTag = "asin",
                examples = listOf(
                    Triple("arcsine of 0.5", "asin(0.5) = 30°", "asin"),
                    Triple("inv sin 1", "asin(1) = 90°", "asin"),
                    Triple("arccosine of 0.5", "acos(0.5) = 60°", "acos"),
                    Triple("inv cos 0", "acos(0) = 90°", "acos"),
                    Triple("arctangent of 1", "atan(1) = 45°", "atan"),
                    Triple("inv tan 0", "atan(0) = 0°", "atan"),
                    Triple("hyperbolic sine of 1", "sinh(1) ≈ 1.1752", "sinh"),
                    Triple("hyperbolic cosine of 1", "cosh(1) ≈ 1.5431", "cosh"),
                    Triple("hyperbolic tan of 1", "tanh(1) ≈ 0.7616", "tanh")
                )
            ),
            SpokenExampleCategory(
                categoryName = "Trig Equations, Powers & Identities",
                iconTag = "id",
                examples = listOf(
                    Triple("sin square x plus cos square x equals 1", "sin²(x)+cos²(x) ≡ 1 (Identity ∀x∈ℝ)", "identity"),
                    Triple("sin^2x + cos^2x = 5", "Contradiction (1 ≠ 5, No real roots)", "contradiction"),
                    Triple("sec square x minus tan square x equals 1", "sec²(x)-tan²(x) ≡ 1 (Identity)", "identity"),
                    Triple("sec^2x - tan^2x = 5", "Contradiction (1 ≠ 5, No real roots)", "contradiction"),
                    Triple("csc square x minus cot square x equals 1", "csc²(x)-cot²(x) ≡ 1 (Identity)", "identity"),
                    Triple("sin x equals 0.5", "x = 30°, 150° (+ 360°k)", "equation"),
                    Triple("cos x equals 0", "x = 90°, 270° (+ 360°k)", "equation"),
                    Triple("tan x equals 1", "x = 45°, 225° (+ 360°k)", "equation"),
                    Triple("cot x equals 1", "x = 45°, 225° (+ 360°k)", "equation"),
                    Triple("sec x equals 2", "x = 60°, 300° (+ 360°k)", "equation"),
                    Triple("cosec x equals 2", "x = 30°, 150° (+ 360°k)", "equation"),
                    Triple("sin square x equals 0.25", "x = 30°, 150°, 210°, 330°", "equation"),
                    Triple("tan square x equals 3", "x = 60°, 120°, 240°, 300°", "equation"),
                    Triple("2 sin square x minus sin x minus 1 equals 0", "x = 90°, 210°, 330°", "equation"),
                    Triple("sin ka square x plus cos ka square x barabar 5", "Contradiction (Hinglish)", "contradiction")
                )
            ),
            SpokenExampleCategory(
                categoryName = "Multiplication: Self & Mixed Functions",
                iconTag = "mult",
                examples = listOf(
                    Triple("sin x into sin x equals 0.25", "sin(x)*sin(x) = 0.25 -> 30°, 150°", "self-mult"),
                    Triple("sinx * sinx + cosx * cosx = 1", "Identity: True for all real x", "identity"),
                    Triple("sinx * sinx + cosx * cosx = 5", "Contradiction: 1 ≠ 5 (No solution)", "contradiction"),
                    Triple("sin x into cos x equals 0.5", "sin(x)*cos(x) = 0.5 -> 45°, 225°", "mixed-trig"),
                    Triple("sin x cos x barabar point 5", "sin(x)*cos(x) = 0.5 (Hinglish)", "mixed-trig"),
                    Triple("tan x into cot x equals 1", "tan(x)*cot(x) ≡ 1 (Reciprocal Identity)", "identity"),
                    Triple("tan x into cot x equals 5", "Contradiction (1 ≠ 5)", "contradiction"),
                    Triple("x into sin x equals 1", "x*sin(x) = 1 -> x ≈ 1.1142, 2.7726", "mixed-alg"),
                    Triple("x into exp x equals 2", "x*e^x = 2 -> x ≈ 0.8526", "mixed-alg"),
                    Triple("x into natural log x equals 1", "x*ln(x) = 1 -> x ≈ 1.7632", "mixed-alg"),
                    Triple("ln x into ln x equals 4", "ln²(x) = 4 -> x = e², e⁻²", "self-mult")
                )
            ),
            SpokenExampleCategory(
                categoryName = "Linear & Systems of Equations",
                iconTag = "eq",
                examples = listOf(
                    Triple("solve 3x plus 5 equals 20", "3x + 5 = 20 -> x = 5", "linear"),
                    Triple("solve 5x minus 7 equals 3x plus 11", "2x = 18 -> x = 9", "linear"),
                    Triple("2x plus y equals 7 and x minus y equals 2", "x = 3, y = 1 (2x2 Cramer)", "system2"),
                    Triple("solve 3x plus 2y equals 16 and x plus y equals 6", "x = 4, y = 2", "system2"),
                    Triple("x plus y plus z equals 6 and 2x minus y plus z equals 3 and x plus 2y minus z equals 2", "x = 1, y = 2, z = 3 (3x3)", "system3"),
                    Triple("3x plus 5 barabar 0", "3x + 5 = 0 -> x = -1.6667 (Hinglish)", "linear"),
                    Triple("2x plus y barabar 7 aur x minus y barabar 2", "x = 3, y = 1 (Hinglish System)", "system2")
                )
            ),
            SpokenExampleCategory(
                categoryName = "Quadratic & Cubic Equations",
                iconTag = "quad",
                examples = listOf(
                    Triple("solve x squared minus 5x plus 6 equals 0", "x₁ = 2, x₂ = 3 (Real roots)", "quadratic"),
                    Triple("solve x squared plus 4 equals 0", "x₁ = 2i, x₂ = -2i (Complex roots)", "quadratic"),
                    Triple("roots of 2x squared plus 4x minus 6", "x₁ = 1, x₂ = -3", "quadratic"),
                    Triple("roots of x squared minus 6x plus 9", "x = 3 (Repeated double root)", "quadratic"),
                    Triple("quadratic a 1 b minus 5 c 6", "x₁ = 3, x₂ = 2 (By coefficients)", "quadratic"),
                    Triple("solve x cubed minus 6x squared plus 11x minus 6 equals 0", "x₁ = 3, x₂ = 2, x₃ = 1 (Cardano)", "cubic"),
                    Triple("solve x cubed minus 8 equals 0", "x = 2 (Real cubic root)", "cubic"),
                    Triple("x square minus 5x plus 6 barabar 0", "x₁ = 3, x₂ = 2 (Hinglish)", "quadratic"),
                    Triple("x cube plus x square plus x plus 2 barabar 0", "x ≈ -1.3532 (Hinglish Cubic)", "cubic")
                )
            ),
            SpokenExampleCategory(
                categoryName = "Fraction Equations & Binomials",
                iconTag = "frac",
                examples = listOf(
                    Triple("(1/3)x + (2/5)x = 5", "x = 75/11 ≈ 6.8182", "frac-eq"),
                    Triple("1 by 3 x plus 2 by 5 x equals 5", "(1/3)x + (2/5)x = 5 -> 75/11", "frac-eq"),
                    Triple("1 bata 3 x plus 2 bata 5 x barabar 5", "x = 75/11 (Hinglish)", "frac-eq"),
                    Triple("x plus 1 whole square minus x minus 1 whole square equals 8", "4x = 8 -> x = 2", "binomial"),
                    Triple("x plus 1 ka whole square minus x minus 1 ka whole square barabar 8", "4x = 8 -> x = 2 (Hinglish)", "binomial"),
                    Triple("x plus 1 whole cube minus x cube equals 7", "3x² + 3x - 6 = 0 -> x = 1, -2", "binomial"),
                    Triple("(x+1)^2 + (x-1)^2 = 20", "2x² + 2 = 20 -> x = ±3", "binomial"),
                    Triple("1 by 2 x plus 1 by 3 y equals 5 and x minus y equals 2", "System with fractions", "system-frac")
                )
            ),
            SpokenExampleCategory(
                categoryName = "Powers, Exponentials & Roots",
                iconTag = "pow",
                examples = listOf(
                    Triple("five squared", "5² = 25", "pow"),
                    Triple("two cubed", "2³ = 8", "pow"),
                    Triple("two to the power of ten", "2¹⁰ = 1024", "pow"),
                    Triple("2 ki power 2", "2² = 4 (Hinglish)", "pow"),
                    Triple("2 ka power 3", "2³ = 8 (Hinglish)", "pow"),
                    Triple("10 ki power 4", "10⁴ = 10,000", "pow"),
                    Triple("2 ki power minus 2", "2⁻² = 0.25", "pow"),
                    Triple("exponential of 2", "exp(2) = e² ≈ 7.3891", "exp"),
                    Triple("e to the power of 3", "e³ ≈ 20.0855", "exp"),
                    Triple("square root of 81", "sqrt(81) = 9", "sqrt"),
                    Triple("cube root of 27", "cbrt(27) = 3", "cbrt"),
                    Triple("fourth root of 16", "16^(1/4) = 2", "root"),
                    Triple("vargmool 81", "sqrt(81) = 9 (Hindi)", "sqrt"),
                    Triple("ghanmool 27", "cbrt(27) = 3 (Hindi)", "cbrt")
                )
            ),
            SpokenExampleCategory(
                categoryName = "Logarithms & Scientific Constants",
                iconTag = "log",
                examples = listOf(
                    Triple("log of 100", "log₁₀(100) = 2", "log"),
                    Triple("log of 100 base 10", "log₁₀(100) = 2", "log"),
                    Triple("log base 2 of 8", "log₂(8) = 3", "log"),
                    Triple("natural log of 10", "ln(10) ≈ 2.3026", "ln"),
                    Triple("ln of e", "ln(e) = 1", "ln"),
                    Triple("pi", "π ≈ 3.14159265", "constant"),
                    Triple("2 pi", "2π ≈ 6.2831853", "constant"),
                    Triple("euler constant e", "e ≈ 2.7182818", "constant"),
                    Triple("golden ratio phi", "φ ≈ 1.6180339", "constant")
                )
            ),
            SpokenExampleCategory(
                categoryName = "Factorials, Permutations & Combinations",
                iconTag = "fact",
                examples = listOf(
                    Triple("5 factorial", "5! = 120", "fact"),
                    Triple("factorial of 6", "6! = 720", "fact"),
                    Triple("paanch ka factorial", "5! = 120 (Hinglish)", "fact"),
                    Triple("5 choose 2", "5C2 = 10", "nCr"),
                    Triple("combination of 5 and 2", "5C2 = 10", "nCr"),
                    Triple("permutations of 5 and 2", "5P2 = 5! / 3! = 20", "nPr"),
                    Triple("permutation 6 3", "6P3 = 120", "nPr")
                )
            ),
            SpokenExampleCategory(
                categoryName = "Percentages & Fractions",
                iconTag = "pct",
                examples = listOf(
                    Triple("20 percent of 80", "20% * 80 = 16", "pct"),
                    Triple("50 percent of 250", "50% * 250 = 125", "pct"),
                    Triple("a third of 90", "(1/3) * 90 = 30", "frac"),
                    Triple("two thirds of 60", "(2/3) * 60 = 40", "frac"),
                    Triple("half of 100", "(1/2) * 100 = 50", "frac"),
                    Triple("aadha of 100", "50 (Hinglish)", "frac"),
                    Triple("chauthai of 200", "50 (1/4th in Hindi)", "frac")
                )
            ),
            SpokenExampleCategory(
                categoryName = "Modulus, Absolute, Floor & Ceil",
                iconTag = "mod",
                examples = listOf(
                    Triple("17 mod 5", "17 % 5 = 2", "mod"),
                    Triple("25 modulo 4", "25 % 4 = 1", "mod"),
                    Triple("absolute value of negative 5", "|-5| = 5", "abs"),
                    Triple("abs of minus 12.5", "|-12.5| = 12.5", "abs"),
                    Triple("floor of 5.7", "floor(5.7) = 5", "floor"),
                    Triple("ceiling of 5.2", "ceil(5.2) = 6", "ceil")
                )
            ),
            SpokenExampleCategory(
                categoryName = "Nested Brackets & Complex Math",
                iconTag = "nest",
                examples = listOf(
                    Triple("2 into bracket me 3 plus 4", "2 * (3 + 4) = 14", "bracket"),
                    Triple("bracket me 2 plus 3 into bracket me 4 minus 1 divided by 5", "((2+3)*(4-1))/5 = 3", "bracket"),
                    Triple("2(3+4)", "2 * 7 = 14", "implicit"),
                    Triple("3 by 7 into 14 by 9", "(3/7) * (14/9) = 2/3", "fraction"),
                    Triple("3 bata 7 guna 14 bata 9", "(3/7) * (14/9) = 2/3 (Hindi)", "fraction")
                )
            ),
            SpokenExampleCategory(
                categoryName = "Memory & Voice Corrections",
                iconTag = "mem",
                examples = listOf(
                    Triple("5 plus 5 and 10 minus 3", "Multi-command execution: 10 ; 7", "chain"),
                    Triple("memory plus", "M+: Add current result to memory", "memory"),
                    Triple("memory minus", "M-: Subtract current result", "memory"),
                    Triple("recall memory", "MR: Speak and retrieve memory", "memory"),
                    Triple("clear memory", "MC: Reset memory register to 0", "memory"),
                    Triple("no wait, times not plus", "Correction: swaps previous operator", "correct"),
                    Triple("actually 20 plus 15", "Correction: replaces with new expression", "correct")
                )
            )
        )
    }

    val filteredCategories = remember(searchQuery, selectedCategoryIndex, allCategories) {
        val baseList = if (selectedCategoryIndex == 0) {
            allCategories
        } else {
            listOf(allCategories[selectedCategoryIndex - 1])
        }

        if (searchQuery.trim().isEmpty()) {
            baseList
        } else {
            val q = searchQuery.lowercase().trim()
            baseList.mapNotNull { cat ->
                val matching = cat.examples.filter { (spoken, math, tag) ->
                    spoken.lowercase().contains(q) ||
                    math.lowercase().contains(q) ||
                    tag.lowercase().contains(q) ||
                    cat.categoryName.lowercase().contains(q)
                }
                if (matching.isNotEmpty()) cat.copy(examples = matching) else null
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        modifier = Modifier.testTag("spoken_examples_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Voice Command Guide",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Every function installed: Tap any example to solve or speak it!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Filter Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by function: sin, cot, log, quadratic, Hinglish...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voice_guide_search_field")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryIndex == 0,
                        onClick = { selectedCategoryIndex = 0 },
                        label = { Text("All (${allCategories.sumOf { it.examples.size }})") }
                    )
                }
                allCategories.forEachIndexed { index, cat ->
                    item {
                        FilterChip(
                            selected = selectedCategoryIndex == index + 1,
                            onClick = { selectedCategoryIndex = index + 1 },
                            label = { Text(cat.categoryName.substringBefore(":").substringBefore("(").trim()) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Examples List
            if (filteredCategories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No voice commands matching \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    filteredCategories.forEach { category ->
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category.categoryName,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = "${category.examples.size} examples",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        items(category.examples) { (spoken, math, tag) ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectExample(spoken)
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "\"$spoken\"",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = tag,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = math,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Test voice command",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
