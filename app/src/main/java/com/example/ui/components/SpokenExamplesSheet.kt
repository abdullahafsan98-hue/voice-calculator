package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SpokenExampleCategory(
    val categoryName: String,
    val examples: List<Pair<String, String>> // Spoken phrase to Expected Math
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpokenExamplesSheet(
    onSelectExample: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val categories = listOf(
        SpokenExampleCategory(
            categoryName = "Linear & Systems of Equations",
            examples = listOf(
                "solve 3x plus 5 equals 20" to "x = 5 (Linear equation in 1 variable)",
                "solve 5x minus 7 equals 3x plus 11" to "x = 9",
                "solve 2x plus y equals 7 and x minus y equals 2" to "x = 3, y = 1 (2x2 System)",
                "solve 3x plus 2y equals 16 and x plus y equals 6" to "x = 4, y = 2"
            )
        ),
        SpokenExampleCategory(
            categoryName = "Quadratic Equations (All Roots)",
            examples = listOf(
                "solve x squared minus 5x plus 6 equals 0" to "x₁ = 2, x₂ = 3 (Two real roots)",
                "roots of 2x squared plus 4x minus 6" to "x₁ = 1, x₂ = -3 (Two real roots)",
                "solve x squared plus 4 equals 0" to "x₁ = 2i, x₂ = -2i (Complex roots)",
                "roots of x squared minus 6x plus 9" to "x = 3 (One repeated double root)",
                "quadratic a 1 b minus 5 c 6" to "Solves by direct coefficients a, b, c",
                "x square minus 5x plus 6 ke roots" to "Hinglish quadratic equation solving"
            )
        ),
        SpokenExampleCategory(
            categoryName = "Cubic & Transcendental Equations",
            examples = listOf(
                "solve x cubed minus 6x squared plus 11x minus 6 equals 0" to "x₁ = 3, x₂ = 2, x₃ = 1",
                "solve x cubed minus 8 equals 0" to "x = 2",
                "solve 2 to the power of x equals 16" to "x = 4",
                "solve cos x equals x" to "x ≈ 0.739"
            )
        ),
        SpokenExampleCategory(
            categoryName = "Basic Arithmetic",
            examples = listOf(
                "twelve plus fifteen" to "12 + 15",
                "50 minus 20" to "50 - 20",
                "6 times 7" to "6 * 7",
                "20 divided by 4" to "20 / 4"
            )
        ),
        SpokenExampleCategory(
            categoryName = "Percentages & Fractions",
            examples = listOf(
                "20 percent of 80" to "((20)/100)*(80)",
                "a third of 90" to "((1/3)*90)",
                "two thirds of 60" to "((2/3)*60)",
                "half of 100" to "((1/2)*100)"
            )
        ),
        SpokenExampleCategory(
            categoryName = "Powers & Roots",
            examples = listOf(
                "five squared" to "5**2",
                "two to the power of ten" to "2**10",
                "square root of 81" to "sqrt(81)",
                "cube root of 27" to "(27)**(1/3)",
                "fourth root of 16" to "(16)**(1/4)"
            )
        ),
        SpokenExampleCategory(
            categoryName = "Trigonometry (in degrees)",
            examples = listOf(
                "sine of 30 degrees" to "sin((30)*pi/180)",
                "cosine of 60 degrees" to "cos((60)*pi/180)",
                "arcsine of 0.5" to "((180/pi)*asin(0.5))",
                "hyperbolic sine of 2" to "sinh(2)"
            )
        ),
        SpokenExampleCategory(
            categoryName = "Logarithms",
            examples = listOf(
                "log of 100" to "log(100, 10)",
                "natural log of 10" to "log(10)",
                "log base 2 of 8" to "log(8, 2)"
            )
        ),
        SpokenExampleCategory(
            categoryName = "Factorial & Combinatorics",
            examples = listOf(
                "5 factorial" to "factorial(5)",
                "factorial of 6" to "factorial(6)",
                "5 choose 2" to "binomial(5, 2)",
                "permutations of 5 and 2" to "5! / (5-2)!"
            )
        ),
        SpokenExampleCategory(
            categoryName = "Modulus, Absolute & Floor",
            examples = listOf(
                "17 mod 5" to "17 % 5",
                "absolute value of negative 5" to "abs(-5)",
                "floor of 5.7" to "floor(5.7)",
                "ceiling of 5.2" to "ceil(5.2)"
            )
        ),
        SpokenExampleCategory(
            categoryName = "Fraction Equations (English & Hinglish)",
            examples = listOf(
                "(1/3)x + (2/5)x = 5" to "x = 75/11 ≈ 6.8182",
                "1 by 3 x plus 2 by 5 x = 5" to "(1/3)x + (2/5)x = 5 -> 75/11",
                "1 bata 3 x plus 2 bata 5 x barabar 5" to "(1/3)x + (2/5)x = 5 (Hinglish)",
                "one by three x plus two by five x equals five" to "Fraction linear solution",
                "ek bata teen x jodo do bata paanch x barabar paanch" to "x = 75/11 ≈ 6.8182",
                "1 by 2 x plus 1 by 3 y equals 5 and x minus y equals 2" to "System with fractions",
                "1 by 2 x square minus 3 by 4 x plus 1 by 8 equals 0" to "Quadratic with fractions"
            )
        ),
        SpokenExampleCategory(
            categoryName = "Hindi / Hinglish Equation & Math Support",
            examples = listOf(
                "SOLVE 3x+5 = 0" to "3x + 5 = 0 -> x = -1.6667",
                "x cube + x square + x +2=0" to "x³ + x² + x + 2 = 0 (Cubic roots)",
                "3x plus 5 barabar 0" to "3x + 5 = 0 -> x = -1.6667",
                "teen x plus paanch barabar shunya" to "3x + 5 = 0 -> x = -1.6667",
                "x cube plus x square plus x plus 2 barabar 0" to "x³ + x² + x + 2 = 0",
                "x square minus 5x plus 6 barabar 0" to "x₁ = 3, x₂ = 2",
                "2x plus y barabar 7 aur x minus y barabar 2" to "x = 3, y = 1 (2x2 System)",
                "ek sau jodo pachas" to "100 + 50",
                "dus guna teen" to "10 * 3",
                "paanch ka square" to "5² = 25",
                "do ki power aath" to "2^8 = 256"
            )
        ),
        SpokenExampleCategory(
            categoryName = "Chained & Memory Commands",
            examples = listOf(
                "5 plus 5 and 10 minus 3" to "5+5 ; 10-3",
                "memory plus" to "Add last result to memory",
                "recall memory" to "Speak/show current memory",
                "clear memory" to "Reset memory to 0"
            )
        ),
        SpokenExampleCategory(
            categoryName = "Speech Corrections",
            examples = listOf(
                "no wait, times not plus" to "Swaps operator in previous calculation",
                "actually 20 plus 15" to "Replaces previous input with new math"
            )
        )
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        modifier = Modifier.testTag("spoken_examples_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Voice Commands Guide",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap any example to test or speak it with the mic!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                categories.forEach { category ->
                    item {
                        Text(
                            text = category.categoryName,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(category.examples) { (spoken, math) ->
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
                                    Text(
                                        text = "\"$spoken\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = math,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Try",
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
