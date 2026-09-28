package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KeypadView(
    isScientific: Boolean,
    isListening: Boolean,
    rmsDb: Float,
    onToggleScientific: () -> Unit,
    onOpenQuadraticSolver: () -> Unit,
    onOpenUniversalSolver: () -> Unit,
    onAppend: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onEvaluate: () -> Unit,
    onToggleMic: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Scientific Functions Drawer
        AnimatedVisibility(visible = isScientific) {
            var isInverse by remember { mutableStateOf(false) }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Scientific Row 1: INV, sin/sin⁻¹, cos/cos⁻¹, tan/tan⁻¹, log/10ˣ, ln/eˣ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ScientificButton(
                        text = if (isInverse) "INV ●" else "INV",
                        onClick = { isInverse = !isInverse },
                        isHighlight = isInverse,
                        modifier = Modifier.weight(1f)
                    )
                    if (!isInverse) {
                        ScientificButton("sin", onClick = { onAppend("sin(") }, modifier = Modifier.weight(1f))
                        ScientificButton("cos", onClick = { onAppend("cos(") }, modifier = Modifier.weight(1f))
                        ScientificButton("tan", onClick = { onAppend("tan(") }, modifier = Modifier.weight(1f))
                        ScientificButton("log", onClick = { onAppend("log(") }, modifier = Modifier.weight(1f))
                        ScientificButton("ln", onClick = { onAppend("ln(") }, modifier = Modifier.weight(1f))
                    } else {
                        ScientificButton("sin⁻¹", onClick = { onAppend("asin(") }, modifier = Modifier.weight(1f))
                        ScientificButton("cos⁻¹", onClick = { onAppend("acos(") }, modifier = Modifier.weight(1f))
                        ScientificButton("tan⁻¹", onClick = { onAppend("atan(") }, modifier = Modifier.weight(1f))
                        ScientificButton("10ˣ", onClick = { onAppend("10^(") }, modifier = Modifier.weight(1f))
                        ScientificButton("eˣ", onClick = { onAppend("exp(") }, modifier = Modifier.weight(1f))
                    }
                }

                // Scientific Row 2: sec/sec⁻¹, csc/csc⁻¹, cot/cot⁻¹, sinh, cosh / √, π, e
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!isInverse) {
                        ScientificButton("sec", onClick = { onAppend("sec(") }, modifier = Modifier.weight(1f))
                        ScientificButton("csc", onClick = { onAppend("csc(") }, modifier = Modifier.weight(1f))
                        ScientificButton("cot", onClick = { onAppend("cot(") }, modifier = Modifier.weight(1f))
                        ScientificButton("√", onClick = { onAppend("sqrt(") }, modifier = Modifier.weight(1f))
                        ScientificButton("π", onClick = { onAppend("pi") }, modifier = Modifier.weight(1f))
                        ScientificButton("e", onClick = { onAppend("E") }, modifier = Modifier.weight(1f))
                    } else {
                        ScientificButton("sec⁻¹", onClick = { onAppend("asec(") }, modifier = Modifier.weight(1f))
                        ScientificButton("csc⁻¹", onClick = { onAppend("acsc(") }, modifier = Modifier.weight(1f))
                        ScientificButton("cot⁻¹", onClick = { onAppend("acot(") }, modifier = Modifier.weight(1f))
                        ScientificButton("sinh", onClick = { onAppend("sinh(") }, modifier = Modifier.weight(1f))
                        ScientificButton("cosh", onClick = { onAppend("cosh(") }, modifier = Modifier.weight(1f))
                        ScientificButton("tanh", onClick = { onAppend("tanh(") }, modifier = Modifier.weight(1f))
                    }
                }

                // Scientific Row 3: x², x³, xʸ, abs, mod, nCr, nPr
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ScientificButton("x²", onClick = { onAppend("x²") }, modifier = Modifier.weight(1f))
                    ScientificButton("x³", onClick = { onAppend("x³") }, modifier = Modifier.weight(1f))
                    ScientificButton("xʸ", onClick = { onAppend("^") }, modifier = Modifier.weight(1f))
                    ScientificButton("abs", onClick = { onAppend("abs(") }, modifier = Modifier.weight(1f))
                    ScientificButton("mod", onClick = { onAppend("%") }, modifier = Modifier.weight(1f))
                    ScientificButton("nCr", onClick = { onAppend("binomial(") }, modifier = Modifier.weight(1f))
                }

                // Scientific Row 4: Variables x, y, z, = and Universal Equation Solver Studio shortcut
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ScientificButton("x", onClick = { onAppend("x") }, modifier = Modifier.weight(1f))
                    ScientificButton("y", onClick = { onAppend("y") }, modifier = Modifier.weight(1f))
                    ScientificButton("z", onClick = { onAppend("z") }, modifier = Modifier.weight(1f))
                    ScientificButton("=", onClick = { onAppend(" = ") }, modifier = Modifier.weight(1f))
                    FilledTonalButton(
                        onClick = onOpenUniversalSolver,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .weight(2f)
                            .height(42.dp)
                            .testTag("open_universal_solver_keypad_btn")
                    ) {
                        Text("f(x)=0 Solve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Standard Top Row: fx toggle, Clear, Parentheses, Backspace, Divide
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilledTonalButton(
                onClick = onToggleScientific,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isScientific) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isScientific) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("toggle_scientific_btn")
            ) {
                Text(if (isScientific) "123" else "fx", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            KeypadActionBtn("C", onClick = onClear, modifier = Modifier.weight(1f), isDanger = true)
            KeypadActionBtn("(", onClick = { onAppend("(") }, modifier = Modifier.weight(1f))
            KeypadActionBtn(")", onClick = { onAppend(")") }, modifier = Modifier.weight(1f))
            KeypadOperatorBtn("÷", onClick = { onAppend("/") }, modifier = Modifier.weight(1f))
        }

        // Row 1: 7, 8, 9, ⌫, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadNumBtn("7", onClick = { onAppend("7") }, modifier = Modifier.weight(1f))
            KeypadNumBtn("8", onClick = { onAppend("8") }, modifier = Modifier.weight(1f))
            KeypadNumBtn("9", onClick = { onAppend("9") }, modifier = Modifier.weight(1f))
            KeypadBackspaceBtn(onClick = onBackspace, modifier = Modifier.weight(1f))
            KeypadOperatorBtn("×", onClick = { onAppend("*") }, modifier = Modifier.weight(1f))
        }

        // Row 2: 4, 5, 6, -, and center mic starts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadNumBtn("4", onClick = { onAppend("4") }, modifier = Modifier.weight(1f))
            KeypadNumBtn("5", onClick = { onAppend("5") }, modifier = Modifier.weight(1f))
            KeypadNumBtn("6", onClick = { onAppend("6") }, modifier = Modifier.weight(1f))
            KeypadActionBtn("^", onClick = { onAppend("**") }, modifier = Modifier.weight(1f))
            KeypadOperatorBtn("−", onClick = { onAppend("-") }, modifier = Modifier.weight(1f))
        }

        // Row 3 & 4 combined with the glowing Voice Mic FAB
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left block (1, 2, 3 and 0, ., %)
            Column(
                modifier = Modifier.weight(3f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    KeypadNumBtn("1", onClick = { onAppend("1") }, modifier = Modifier.weight(1f))
                    KeypadNumBtn("2", onClick = { onAppend("2") }, modifier = Modifier.weight(1f))
                    KeypadNumBtn("3", onClick = { onAppend("3") }, modifier = Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    KeypadNumBtn("0", onClick = { onAppend("0") }, modifier = Modifier.weight(1f))
                    KeypadNumBtn(".", onClick = { onAppend(".") }, modifier = Modifier.weight(1f))
                    KeypadActionBtn("%", onClick = { onAppend("%") }, modifier = Modifier.weight(1f))
                }
            }

            // Central Glowing Voice Mic Button
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .height(110.dp),
                contentAlignment = Alignment.Center
            ) {
                VoiceMicFab(
                    isListening = isListening,
                    rmsDb = rmsDb,
                    onToggleMic = onToggleMic
                )
            }

            // Right block (+ and =)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                KeypadOperatorBtn("+", onClick = { onAppend("+") }, modifier = Modifier.height(52.dp))
                Button(
                    onClick = onEvaluate,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("equals_btn")
                ) {
                    Text("=", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun VoiceMicFab(
    isListening: Boolean,
    rmsDb: Float,
    onToggleMic: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse"
    )

    val scaleModifier = if (isListening) {
        val extraScale = (rmsDb.coerceIn(0f, 10f) / 10f) * 0.15f
        Modifier.scale(pulseScale + extraScale)
    } else {
        Modifier
    }

    Box(contentAlignment = Alignment.Center) {
        if (isListening) {
            // Glowing outer rings
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .scale(pulseScale * 1.15f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
            )
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.38f))
            )
        }

        FloatingActionButton(
            onClick = onToggleMic,
            shape = CircleShape,
            containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            modifier = scaleModifier
                .size(64.dp)
                .testTag("voice_mic_fab")
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = if (isListening) "Stop voice listening" else "Start voice listening",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun KeypadNumBtn(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .height(52.dp)
            .testTag("keypad_num_$text")
    ) {
        Text(text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun KeypadOperatorBtn(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("keypad_op_$text")
    ) {
        Text(text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun KeypadActionBtn(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDanger: Boolean = false
) {
    FilledTonalButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isDanger) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            contentColor = if (isDanger) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .height(52.dp)
            .testTag("keypad_act_$text")
    ) {
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun KeypadBackspaceBtn(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .height(52.dp)
            .testTag("keypad_backspace")
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = "Backspace",
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ScientificButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHighlight: Boolean = false
) {
    FilledTonalButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
            contentColor = if (isHighlight) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onTertiaryContainer
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .height(42.dp)
            .testTag("sci_btn_$text")
    ) {
        Text(text, fontSize = 13.sp, fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium)
    }
}
