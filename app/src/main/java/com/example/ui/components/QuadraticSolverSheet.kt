package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.QuadraticEquationSolver
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuadraticSolverSheet(
    initialEquation: String? = null,
    onSpeakSolution: (String) -> Unit,
    onInsertResultToCalc: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var aText by remember { mutableStateOf("1") }
    var bText by remember { mutableStateOf("-5") }
    var cText by remember { mutableStateOf("6") }

    var naturalEquationText by remember { mutableStateOf(initialEquation ?: "") }
    var inputMode by remember { mutableIntStateOf(0) } // 0: Coefficients (a, b, c), 1: Natural Equation

    val clipboardManager = LocalClipboardManager.current

    // Parse solution reactively
    val solution by remember(aText, bText, cText, naturalEquationText, inputMode) {
        derivedStateOf {
            if (inputMode == 1 && naturalEquationText.isNotBlank()) {
                QuadraticEquationSolver.parseAndSolve(naturalEquationText)
            } else {
                val a = aText.toDoubleOrNull() ?: 1.0
                val b = bText.toDoubleOrNull() ?: 0.0
                val c = cText.toDoubleOrNull() ?: 0.0
                QuadraticEquationSolver.solve(a, b, c)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        modifier = Modifier.testTag("quadratic_solver_sheet")
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
                            "x²",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Quadratic Solver",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ax² + bx + c = 0",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                solution?.let { sol ->
                    IconButton(
                        onClick = { onSpeakSolution(sol.spokenSummary) },
                        modifier = Modifier.testTag("speak_quadratic_btn")
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

            // Input Mode Switch Tabs
            TabRow(selectedTabIndex = inputMode) {
                Tab(
                    selected = inputMode == 0,
                    onClick = { inputMode = 0 },
                    text = { Text("Coefficients (a, b, c)") }
                )
                Tab(
                    selected = inputMode == 1,
                    onClick = { inputMode = 1 },
                    text = { Text("Equation Input") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                // Inputs section
                item {
                    if (inputMode == 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CoefficientInputField("a (x²)", aText, modifier = Modifier.weight(1f)) { aText = it }
                            CoefficientInputField("b (x)", bText, modifier = Modifier.weight(1f)) { bText = it }
                            CoefficientInputField("c (const)", cText, modifier = Modifier.weight(1f)) { cText = it }
                        }
                    } else {
                        OutlinedTextField(
                            value = naturalEquationText,
                            onValueChange = { naturalEquationText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("equation_input_field"),
                            placeholder = { Text("e.g. 2x² - 5x + 3 = 0, or x^2 + 4 = 0") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }

                // Preset Example Chips
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PresetChip("x² - 5x + 6 = 0") {
                            aText = "1"; bText = "-5"; cText = "6"
                            naturalEquationText = "x^2 - 5x + 6 = 0"
                        }
                        PresetChip("x² + 4 = 0 (Complex)") {
                            aText = "1"; bText = "0"; cText = "4"
                            naturalEquationText = "x^2 + 4 = 0"
                        }
                        PresetChip("x² - 6x + 9 = 0 (Double)") {
                            aText = "1"; bText = "-6"; cText = "9"
                            naturalEquationText = "x^2 - 6x + 9 = 0"
                        }
                    }
                }

                // Solution Overview Cards
                solution?.let { sol ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Root Type Tag & Discriminant
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val (typeLabel, badgeColor) = when (sol.rootType) {
                                        QuadraticEquationSolver.RootType.TWO_REAL -> "Two Real Roots (Δ > 0)" to Color(0xFF10B981)
                                        QuadraticEquationSolver.RootType.ONE_REAL_REPEATED -> "One Repeated Root (Δ = 0)" to Color(0xFF38BDF8)
                                        QuadraticEquationSolver.RootType.COMPLEX_CONJUGATE -> "Complex Conjugate Roots (Δ < 0)" to Color(0xFFC084FC)
                                        QuadraticEquationSolver.RootType.LINEAR -> "Linear Equation" to Color(0xFFF59E0B)
                                        QuadraticEquationSolver.RootType.NO_SOLUTION -> "No Solution" to Color(0xFFEF4444)
                                        QuadraticEquationSolver.RootType.INFINITE_SOLUTIONS -> "Infinite Solutions" to Color(0xFF6B7280)
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = badgeColor.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = typeLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Text(
                                        text = "Δ = ${String.format(java.util.Locale.US, "%.2f", sol.discriminant)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Roots Display Boxes
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    RootDisplayCard(
                                        label = "x₁",
                                        value = sol.root1Text,
                                        modifier = Modifier.weight(1f),
                                        onCopy = { clipboardManager.setText(AnnotatedString(sol.root1Text)) },
                                        onInsert = { onInsertResultToCalc(sol.root1Text) }
                                    )
                                    RootDisplayCard(
                                        label = "x₂",
                                        value = sol.root2Text,
                                        modifier = Modifier.weight(1f),
                                        onCopy = { clipboardManager.setText(AnnotatedString(sol.root2Text)) },
                                        onInsert = { onInsertResultToCalc(sol.root2Text) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Parabola Details (Vertex & Direction)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                "Vertex (h, k)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                "(${String.format(java.util.Locale.US, "%.2f", sol.vertexX)}, ${String.format(java.util.Locale.US, "%.2f", sol.vertexY)})",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                "Parabola",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                if (sol.a > 0) "Opens Upward (∪)" else "Opens Downward (∩)",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Parabola Graph Visualizer Canvas
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Parabola Curve Graph",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ParabolaGraphCanvas(
                                    a = sol.a,
                                    b = sol.b,
                                    c = sol.c,
                                    vertexX = sol.vertexX,
                                    vertexY = sol.vertexY,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                )
                            }
                        }
                    }

                    // Step-by-Step Breakdown Accordion
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
                                        text = "Step-by-Step Derivation",
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
private fun CoefficientInputField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    )
}

@Composable
private fun PresetChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun RootDisplayCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onCopy: () -> Unit,
    onInsert: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row {
                    IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy root",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(onClick = onInsert, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Use root in calc",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ParabolaGraphCanvas(
    a: Double,
    b: Double,
    c: Double,
    vertexX: Double,
    vertexY: Double,
    modifier: Modifier = Modifier
) {
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    val curveColor = MaterialTheme.colorScheme.primary
    val vertexColor = MaterialTheme.colorScheme.tertiary
    val rootColor = Color(0xFF10B981)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Determine dynamic domain centered around vertex
        val xRange = 8.0
        val minX = vertexX - (xRange / 2)
        val maxX = vertexX + (xRange / 2)

        // Find min and max y in range to scale correctly
        val yVals = (0..50).map { i ->
            val x = minX + (i / 50.0) * xRange
            a * x * x + b * x + c
        }
        val rawMinY = min(vertexY, yVals.minOrNull() ?: 0.0)
        val rawMaxY = max(vertexY, yVals.maxOrNull() ?: 10.0)
        val yPadding = max(2.0, (rawMaxY - rawMinY) * 0.2)
        val minY = rawMinY - yPadding
        val maxY = rawMaxY + yPadding

        fun toScreenX(x: Double): Float = ((x - minX) / (maxX - minX) * width).toFloat()
        fun toScreenY(y: Double): Float = (height - ((y - minY) / (maxY - minY) * height)).toFloat()

        // Draw Axes
        val originScreenX = toScreenX(0.0)
        val originScreenY = toScreenY(0.0)

        // X-axis (if in visible bounds)
        if (originScreenY in 0f..height) {
            drawLine(
                color = axisColor,
                start = Offset(0f, originScreenY),
                end = Offset(width, originScreenY),
                strokeWidth = 1.5.dp.toPx()
            )
        }
        // Y-axis (if in visible bounds)
        if (originScreenX in 0f..width) {
            drawLine(
                color = axisColor,
                start = Offset(originScreenX, 0f),
                end = Offset(originScreenX, height),
                strokeWidth = 1.5.dp.toPx()
            )
        }

        // Draw Parabola Curve
        val path = Path()
        var first = true
        val steps = 100
        for (i in 0..steps) {
            val x = minX + (i.toDouble() / steps) * (maxX - minX)
            val y = a * x * x + b * x + c
            val sx = toScreenX(x)
            val sy = toScreenY(y).coerceIn(-height, height * 2)

            if (first) {
                path.moveTo(sx, sy)
                first = false
            } else {
                path.lineTo(sx, sy)
            }
        }

        drawPath(
            path = path,
            color = curveColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw Vertex Dot
        val vxScreen = toScreenX(vertexX)
        val vyScreen = toScreenY(vertexY)
        if (vxScreen in 0f..width && vyScreen in 0f..height) {
            drawCircle(
                color = vertexColor,
                radius = 5.dp.toPx(),
                center = Offset(vxScreen, vyScreen)
            )
        }

        // Draw Real Roots Dots if discriminant >= 0
        val d = b * b - 4 * a * c
        if (abs(a) > 1e-9 && d >= 0) {
            val sqrtD = kotlin.math.sqrt(d)
            val r1 = (-b + sqrtD) / (2 * a)
            val r2 = (-b - sqrtD) / (2 * a)

            val r1Screen = toScreenX(r1)
            val r2Screen = toScreenX(r2)
            val yZeroScreen = toScreenY(0.0)

            if (r1Screen in 0f..width && yZeroScreen in 0f..height) {
                drawCircle(color = rootColor, radius = 5.5.dp.toPx(), center = Offset(r1Screen, yZeroScreen))
            }
            if (r2Screen in 0f..width && yZeroScreen in 0f..height) {
                drawCircle(color = rootColor, radius = 5.5.dp.toPx(), center = Offset(r2Screen, yZeroScreen))
            }
        }
    }
}
