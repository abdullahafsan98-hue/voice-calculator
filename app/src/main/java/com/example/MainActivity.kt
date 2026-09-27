package com.example

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.engine.VoiceManager
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                VoiceCalculatorApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceCalculatorApp(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // State collections
    val expression by viewModel.displayExpression.collectAsStateWithLifecycle()
    val result by viewModel.displayResult.collectAsStateWithLifecycle()
    val spokenTranscript by viewModel.spokenTranscript.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val memoryValue by viewModel.memoryValue.collectAsStateWithLifecycle()
    val isTtsEnabled by viewModel.isTtsEnabled.collectAsStateWithLifecycle()
    val isScientific by viewModel.isScientificMode.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val favoriteList by viewModel.favoriteList.collectAsStateWithLifecycle()

    val speechState by viewModel.voiceManager.speechState.collectAsStateWithLifecycle()
    val partialTranscript by viewModel.voiceManager.partialTranscript.collectAsStateWithLifecycle()
    val rmsDb by viewModel.voiceManager.rmsDb.collectAsStateWithLifecycle()
    val isTtsSpeaking by viewModel.voiceManager.isTtsSpeaking.collectAsStateWithLifecycle()

    val isListening = speechState is VoiceManager.SpeechState.Listening

    // Bottom sheet dialog states
    var showHistorySheet by remember { mutableStateOf(false) }
    var showExamplesSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showQuadraticSheet by remember { mutableStateOf(false) }
    var showUniversalEquationSheet by remember { mutableStateOf(false) }
    var showTextInputBar by remember { mutableStateOf(false) }
    var manualTypedText by remember { mutableStateOf("") }

    val activeQuadraticSolution by viewModel.activeQuadraticSolution.collectAsStateWithLifecycle()
    val activeUniversalSolution by viewModel.activeUniversalSolution.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Fallback system speech recognition intent launcher
    val speechIntentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            val spoken = activityResult.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                viewModel.processSpokenInput(spoken)
            }
        }
    }

    // Audio recording permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (viewModel.voiceManager.isSpeechRecognitionAvailable()) {
                viewModel.toggleListening()
            } else {
                // Launch system speech intent
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a math calculation...")
                }
                try {
                    speechIntentLauncher.launch(intent)
                } catch (_: Exception) {
                    showTextInputBar = true
                }
            }
        } else {
            showTextInputBar = true
        }
    }

    fun requestVoiceCalculation() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            if (viewModel.voiceManager.isSpeechRecognitionAvailable()) {
                viewModel.toggleListening()
            } else {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a math calculation...")
                }
                try {
                    speechIntentLauncher.launch(intent)
                } catch (_: Exception) {
                    showTextInputBar = true
                }
            }
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val quickExamples = listOf(
        "solve 3x plus 5 equals 20",
        "solve 2x plus y equals 7 and x minus y equals 2",
        "solve x squared minus 5x plus 6 equals 0",
        "solve x cubed minus 6x squared plus 11x minus 6 equals 0",
        "solve 2 to the power of x equals 16",
        "20 percent of 80",
        "square root of 81",
        "sine of 30 degrees",
        "5 factorial",
        "ek sau jodo pachas"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Voice Calc",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isListening) "Listening for math..." else "Scientific Edition",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Universal Equation Solver Shortcut
                    IconButton(
                        onClick = { showUniversalEquationSheet = true },
                        modifier = Modifier.testTag("top_universal_solver_btn")
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "f(x)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

                    // Quadratic Equation Solver Direct Shortcut
                    IconButton(
                        onClick = { showQuadraticSheet = true },
                        modifier = Modifier.testTag("top_quadratic_btn")
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "x²",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Spoken answers toggle
                    IconButton(
                        onClick = { viewModel.toggleTts() },
                        modifier = Modifier.testTag("toggle_tts_top_btn")
                    ) {
                        Icon(
                            imageVector = if (isTtsEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = if (isTtsEnabled) "Mute spoken answers" else "Unmute spoken answers",
                            tint = if (isTtsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Type text toggle
                    IconButton(
                        onClick = { showTextInputBar = !showTextInputBar },
                        modifier = Modifier.testTag("toggle_text_input_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "Type calculation",
                            tint = if (showTextInputBar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Voice Guide / Examples
                    IconButton(
                        onClick = { showExamplesSheet = true },
                        modifier = Modifier.testTag("open_examples_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lightbulb,
                            contentDescription = "Voice commands guide",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    // History with counter badge
                    BadgedBox(
                        badge = {
                            if (historyList.isNotEmpty()) {
                                Badge { Text(historyList.size.coerceAtMost(99).toString()) }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = { showHistorySheet = true },
                            modifier = Modifier.testTag("open_history_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Settings
                    IconButton(
                        onClick = { showSettingsSheet = true },
                        modifier = Modifier.testTag("open_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Animated Voice Wave Visualizer
                VoiceWaveVisualizer(
                    isListening = isListening,
                    rmsDb = rmsDb,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )

                // Main Calculation Display Box
                CalculatorDisplay(
                    spokenTranscript = spokenTranscript,
                    partialTranscript = partialTranscript,
                    expression = expression,
                    result = result,
                    statusMessage = statusMessage,
                    memoryValue = memoryValue,
                    isListening = isListening,
                    isSpeaking = isTtsSpeaking,
                    onExpressionChange = { viewModel.onExpressionTextChange(it) },
                    onSpeakResult = { viewModel.speakCurrentResult() },
                    onMemoryAdd = { viewModel.memoryAdd() },
                    onMemorySub = { viewModel.memorySub() },
                    onMemoryRecall = { viewModel.memoryRecall() },
                    onMemoryClear = { viewModel.memoryClear() }
                )

                // Interactive Quadratic Result Banner (if a quadratic equation was just solved)
                AnimatedVisibility(visible = activeQuadraticSolution != null) {
                    activeQuadraticSolution?.let { sol ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { showQuadraticSheet = true }
                                .testTag("quadratic_solution_banner")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Analytics,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Roots: ${sol.root1Text}, ${sol.root2Text} (Δ=${String.format(java.util.Locale.US, "%.1f", sol.discriminant)})",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "Steps & Graph →",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Interactive Universal Equation Banner (Linear, System, Cubic, Transcendental)
                AnimatedVisibility(visible = activeUniversalSolution != null) {
                    activeUniversalSolution?.let { sol ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { showUniversalEquationSheet = true }
                                .testTag("universal_solution_banner")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Functions,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${sol.rootsSummary} (${sol.category})",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "Steps Breakdown →",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }

                // Optional Manual Text Bar
                AnimatedVisibility(visible = showTextInputBar) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = manualTypedText,
                                onValueChange = { manualTypedText = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("manual_calc_input"),
                                placeholder = { Text("Type calculation (e.g. 5 squared + 10)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (manualTypedText.isNotBlank()) {
                                        viewModel.processSpokenInput(manualTypedText)
                                        manualTypedText = ""
                                        keyboardController?.hide()
                                    }
                                })
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilledIconButton(
                                onClick = {
                                    if (manualTypedText.isNotBlank()) {
                                        viewModel.processSpokenInput(manualTypedText)
                                        manualTypedText = ""
                                        keyboardController?.hide()
                                    }
                                },
                                modifier = Modifier.testTag("manual_calc_submit")
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = "Calculate")
                            }
                        }
                    }
                }

                // Spoken Suggestions Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showExamplesSheet = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Try Saying:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }

                    quickExamples.forEach { example ->
                        SuggestionChip(
                            onClick = {
                                viewModel.processSpokenInput(example)
                            },
                            label = {
                                Text(
                                    text = example,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Interactive Scientific & Standard Keypad
            KeypadView(
                isScientific = isScientific,
                isListening = isListening,
                rmsDb = rmsDb,
                onToggleScientific = { viewModel.toggleScientificMode() },
                onOpenQuadraticSolver = { showQuadraticSheet = true },
                onOpenUniversalSolver = { showUniversalEquationSheet = true },
                onAppend = { token -> viewModel.appendExpression(token) },
                onBackspace = { viewModel.deleteLastChar() },
                onClear = { viewModel.clearAll() },
                onEvaluate = { viewModel.evaluateManualInput() },
                onToggleMic = { requestVoiceCalculation() },
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }

    // Bottom Sheets
    if (showUniversalEquationSheet) {
        UniversalEquationSolverSheet(
            initialEquation = activeUniversalSolution?.originalEquation,
            onSpeakSolution = { text -> viewModel.speakText(text) },
            onInsertResultToCalc = { res ->
                viewModel.appendExpression(res)
                showUniversalEquationSheet = false
            },
            onDismiss = { showUniversalEquationSheet = false }
        )
    }

    if (showQuadraticSheet) {
        QuadraticSolverSheet(
            initialEquation = activeQuadraticSolution?.let { sol ->
                "${if (sol.a == 1.0) "" else sol.a}x² + ${sol.b}x + ${sol.c} = 0"
            },
            onSpeakSolution = { text -> viewModel.speakText(text) },
            onInsertResultToCalc = { rootText ->
                viewModel.appendExpression(rootText)
                showQuadraticSheet = false
            },
            onDismiss = { showQuadraticSheet = false }
        )
    }

    if (showHistorySheet) {
        HistorySheet(
            historyList = historyList,
            favoriteList = favoriteList,
            onSelectRecord = { record ->
                viewModel.loadCalculation(
                    spoken = record.spokenInput,
                    parsed = record.parsedExpression,
                    result = record.resultText
                )
            },
            onToggleFavorite = { record -> viewModel.toggleFavorite(record) },
            onDeleteRecord = { id -> viewModel.deleteHistoryRecord(id) },
            onClearAll = { viewModel.clearHistory() },
            onSpeakText = { text -> viewModel.speakText(text) },
            onDismiss = { showHistorySheet = false }
        )
    }

    if (showExamplesSheet) {
        SpokenExamplesSheet(
            onSelectExample = { example ->
                viewModel.processSpokenInput(example)
            },
            onDismiss = { showExamplesSheet = false }
        )
    }

    if (showSettingsSheet) {
        SettingsSheet(
            isTtsEnabled = isTtsEnabled,
            onToggleTts = { viewModel.toggleTts() },
            onSetSpeechRate = { viewModel.setSpeechRate(it) },
            onSetSpeechPitch = { viewModel.setSpeechPitch(it) },
            onTestSpeak = { viewModel.speakText(it) },
            onDismiss = { showSettingsSheet = false }
        )
    }
}
