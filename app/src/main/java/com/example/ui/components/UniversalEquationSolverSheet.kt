package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.UniversalEquationSolver
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalEquationSolverSheet(
    initialEquation: String? = null,
    onSpeakSolution: (String) -> Unit,
    onInsertResultToCalc: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Any Equation, 1: 2x2 System, 2: Polynomial
    var inputEquation by remember { mutableStateOf(initialEquation ?: "3x + 5 = 20") }

    // 2x2 System inputs
    var sysA1 by remember { mutableStateOf("2") }
    var sysB1 by remember { mutableStateOf("1") }
    var sysD1 by remember { mutableStateOf("7") }

    var sysA2 by remember { mutableStateOf("1") }
    var sysB2 by remember { mutableStateOf("-1") }
    var sysD2 by remember { mutableStateOf("2") }

    val clipboardManager = LocalClipboardManager.current

    val currentSolution by remember(selectedTab, inputEquation, sysA1, sysB1, sysD1, sysA2, sysB2, sysD2) {
        derivedStateOf {
            when (selectedTab) {
                0 -> {
                    if (inputEquation.isNotBlank()) UniversalEquationSolver.solveAny(inputEquation) else null
                }
                1 -> {
                    val eq1 = "${sysA1}x + ${sysB1}y = $sysD1"
                    val eq2 = "${sysA2}x + ${sysB2}y = $sysD2"
                    UniversalEquationSolver.solveSystem2Var(eq1, eq2)
                }
                else -> {
                    if (inputEquation.isNotBlank()) UniversalEquationSolver.solveAny(inputEquation) else null
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        modifier = Modifier.testTag("universal_equation_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            "f(x)=0",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Universal Equation Solver",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Linear, Quadratic, Cubic, Systems & Transcendental",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                currentSolution?.let { sol ->
                    IconButton(
                        onClick = { onSpeakSolution(sol.spokenSummary) },
                        modifier = Modifier.testTag("speak_equation_sol_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak solution",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Selector
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Any Equation") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("2×2 System") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Input Section
                item {
                    if (selectedTab == 0) {
                        Column {
                            OutlinedTextField(
                                value = inputEquation,
                                onValueChange = { inputEquation = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("universal_eq_input"),
                                label = { Text("Enter any equation") },
                                placeholder = { Text("e.g. 3x + 5 = 20, x³ - 6x² + 11x - 6 = 0, cos(x) = x") },
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Presets Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PresetEqChip("(1/3)x + (2/5)x = 5") { inputEquation = "(1/3)x + (2/5)x = 5" }
                                PresetEqChip("1 by 3 x plus 2 by 5 x = 5") { inputEquation = "1 by 3 x plus 2 by 5 x = 5" }
                                PresetEqChip("SOLVE 3x+5 = 0") { inputEquation = "SOLVE 3x+5 = 0" }
                                PresetEqChip("x cube + x square + x + 2 = 0") { inputEquation = "x cube + x square + x + 2 = 0" }
                                PresetEqChip("3x plus 5 barabar 0") { inputEquation = "3x plus 5 barabar 0" }
                                PresetEqChip("x² - 5x + 6 = 0") { inputEquation = "x^2 - 5x + 6 = 0" }
                                PresetEqChip("x³ - 6x² + 11x - 6 = 0") { inputEquation = "x^3 - 6x^2 + 11x - 6 = 0" }
                                PresetEqChip("2x + y = 7 ; x - y = 2") { inputEquation = "2x + y = 7 ; x - y = 2" }
                                PresetEqChip("cos(x) = x") { inputEquation = "cos(x) = x" }
                                PresetEqChip("2^x = 16") { inputEquation = "2^x = 16" }
                            }
                        }
                    } else {
                        // 2x2 System Matrix Inputs
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    "Linear System (Cramer's Rule):",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                // Row 1
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    MatrixCell(sysA1, "a₁", Modifier.weight(1f)) { sysA1 = it }
                                    Text("x +", fontWeight = FontWeight.Bold)
                                    MatrixCell(sysB1, "b₁", Modifier.weight(1f)) { sysB1 = it }
                                    Text("y =", fontWeight = FontWeight.Bold)
                                    MatrixCell(sysD1, "d₁", Modifier.weight(1f)) { sysD1 = it }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Row 2
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    MatrixCell(sysA2, "a₂", Modifier.weight(1f)) { sysA2 = it }
                                    Text("x +", fontWeight = FontWeight.Bold)
                                    MatrixCell(sysB2, "b₂", Modifier.weight(1f)) { sysB2 = it }
                                    Text("y =", fontWeight = FontWeight.Bold)
                                    MatrixCell(sysD2, "d₂", Modifier.weight(1f)) { sysD2 = it }
                                }
                            }
                        }
                    }
                }

                // Solution Overview
                currentSolution?.let { sol ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Category Tag
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val (catTitle, catColor) = when (sol.category) {
                                        UniversalEquationSolver.EquationCategory.LINEAR_1VAR -> "Linear Equation" to Color(0xFF10B981)
                                        UniversalEquationSolver.EquationCategory.QUADRATIC -> "Quadratic Equation" to Color(0xFF38BDF8)
                                        UniversalEquationSolver.EquationCategory.CUBIC -> "Cubic Equation (Cardano)" to Color(0xFFC084FC)
                                        UniversalEquationSolver.EquationCategory.SYSTEM_2VAR -> "2×2 Linear System" to Color(0xFFF59E0B)
                                        UniversalEquationSolver.EquationCategory.SYSTEM_3VAR -> "3×3 Linear System" to Color(0xFFEC4899)
                                        UniversalEquationSolver.EquationCategory.POLYNOMIAL -> "Polynomial Equation" to Color(0xFF6366F1)
                                        UniversalEquationSolver.EquationCategory.TRANSCENDENTAL_NUMERICAL -> "Transcendental / Non-linear" to Color(0xFF14B8A6)
                                        UniversalEquationSolver.EquationCategory.IDENTITY -> "Identity (Infinite)" to Color(0xFF6B7280)
                                        UniversalEquationSolver.EquationCategory.CONTRADICTION -> "Contradiction (No Solution)" to Color(0xFFEF4444)
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = catColor.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = catTitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = catColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Row {
                                        IconButton(
                                            onClick = { clipboardManager.setText(AnnotatedString(sol.rootsSummary)) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = { onInsertResultToCalc(sol.rootsSummary) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = "Insert in calc", modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Roots Result Highlight
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "Roots / Solution:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = sol.rootsSummary,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        if (sol.spokenSummaryHinglish.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("🇮🇳", fontSize = 14.sp)
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = sol.spokenSummaryHinglish,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = { onSpeakSolution(sol.spokenSummaryHinglish) },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                            contentDescription = "Speak Hinglish",
                                                            modifier = Modifier.size(16.dp),
                                                            tint = MaterialTheme.colorScheme.primary
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

                    // Step-by-Step Derivation Breakdown
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FormatListNumbered,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Step-by-Step Solution Breakdown",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                sol.steps.forEachIndexed { idx, step ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    "${idx + 1}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = step,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                lineHeight = 18.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
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
}

@Composable
private fun MatrixCell(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    )
}

@Composable
private fun PresetEqChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}
