package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.CalculationRecord
import com.example.data.repository.CalculatorRepository
import com.example.engine.QuadraticEquationSolver
import com.example.engine.ScientificMathEvaluator
import com.example.engine.SpokenMathParser
import com.example.engine.UniversalEquationSolver
import com.example.engine.VoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.LinkedList

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CalculatorRepository
    val voiceManager: VoiceManager = VoiceManager(application)

    init {
        val db = AppDatabase.getInstance(application)
        repository = CalculatorRepository(db.calculationDao())

        voiceManager.onSpeechResultReceived = { rawTranscript ->
            processSpokenInput(rawTranscript)
        }
    }

    val historyList: StateFlow<List<CalculationRecord>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteList: StateFlow<List<CalculationRecord>> = repository.favoriteHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _displayExpression = MutableStateFlow("")
    val displayExpression: StateFlow<String> = _displayExpression.asStateFlow()

    private val _displayResult = MutableStateFlow("0")
    val displayResult: StateFlow<String> = _displayResult.asStateFlow()

    private val _spokenTranscript = MutableStateFlow("")
    val spokenTranscript: StateFlow<String> = _spokenTranscript.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _memoryValue = MutableStateFlow(0.0)
    val memoryValue: StateFlow<Double> = _memoryValue.asStateFlow()

    private val _lastResult = MutableStateFlow<Double?>(null)
    val lastResult: StateFlow<Double?> = _lastResult.asStateFlow()

    private val _isTtsEnabled = MutableStateFlow(true)
    val isTtsEnabled: StateFlow<Boolean> = _isTtsEnabled.asStateFlow()

    private val _isScientificMode = MutableStateFlow(false)
    val isScientificMode: StateFlow<Boolean> = _isScientificMode.asStateFlow()

    private val _activeQuadraticSolution = MutableStateFlow<QuadraticEquationSolver.QuadraticSolution?>(null)
    val activeQuadraticSolution: StateFlow<QuadraticEquationSolver.QuadraticSolution?> = _activeQuadraticSolution.asStateFlow()

    private val _activeUniversalSolution = MutableStateFlow<UniversalEquationSolver.EquationSolution?>(null)
    val activeUniversalSolution: StateFlow<UniversalEquationSolver.EquationSolution?> = _activeUniversalSolution.asStateFlow()

    // Last 5 transcripts heard, matching the Python script's deque(maxlen=5)
    private val transcriptDeque = LinkedList<String>()
    private val _transcriptHistory = MutableStateFlow<List<String>>(emptyList())
    val transcriptHistory: StateFlow<List<String>> = _transcriptHistory.asStateFlow()

    private var lastRawTranscript: String? = null

    fun setQuadraticSolution(sol: QuadraticEquationSolver.QuadraticSolution?) {
        _activeQuadraticSolution.value = sol
    }

    fun setUniversalSolution(sol: UniversalEquationSolver.EquationSolution?) {
        _activeUniversalSolution.value = sol
    }

    fun toggleTts() {
        _isTtsEnabled.value = !_isTtsEnabled.value
        if (!_isTtsEnabled.value) {
            voiceManager.stopSpeaking()
        }
    }

    fun toggleScientificMode() {
        _isScientificMode.value = !_isScientificMode.value
    }

    fun setSpeechRate(rate: Float) {
        voiceManager.speechRate = rate
    }

    fun setSpeechPitch(pitch: Float) {
        voiceManager.speechPitch = pitch
    }

    fun toggleListening() {
        if (voiceManager.speechState.value is VoiceManager.SpeechState.Listening) {
            voiceManager.stopListening()
        } else {
            voiceManager.startListening()
        }
    }

    fun processSpokenInput(raw: String) {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return

        // Append to transcript history
        if (transcriptDeque.size >= 5) {
            transcriptDeque.removeFirst()
        }
        transcriptDeque.add(trimmed)
        _transcriptHistory.value = transcriptDeque.toList()

        // Check for quit / stop / exit
        if (Regex("\\b(quit|exit|stop)\\b", RegexOption.IGNORE_CASE).containsMatchIn(trimmed)) {
            speakIfEnabled("Goodbye.")
            _statusMessage.value = "Stopped"
            return
        }

        // Check for verbal correction ("no wait, times not plus", "actually 5 plus 6")
        val corrected = SpokenMathParser.handleCorrection(trimmed, lastRawTranscript)
        val textToProcess = if (corrected != null) {
            _statusMessage.value = "Correction applied"
            lastRawTranscript = corrected
            corrected
        } else {
            lastRawTranscript = trimmed
            trimmed
        }

        _spokenTranscript.value = textToProcess

        // Check for general equations (linear, systems, quadratic, cubic, transcendental)
        val equationSol = SpokenMathParser.tryParseAnyEquationQuery(textToProcess)
        if (equationSol != null) {
            _activeUniversalSolution.value = equationSol
            if (equationSol.category == UniversalEquationSolver.EquationCategory.QUADRATIC) {
                _activeQuadraticSolution.value = QuadraticEquationSolver.parseAndSolve(equationSol.originalEquation)
            }
            _displayExpression.value = equationSol.originalEquation
            _displayResult.value = equationSol.rootsSummary
            _statusMessage.value = "${equationSol.category} Solved"

            speakIfEnabled(equationSol.spokenSummary)

            viewModelScope.launch {
                repository.saveRecord(
                    CalculationRecord(
                        spokenInput = textToProcess,
                        parsedExpression = equationSol.originalEquation,
                        resultText = equationSol.rootsSummary,
                        isSuccess = true
                    )
                )
            }
            return
        }

        // Check for quadratic equation queries ("solve x squared minus 5x plus 6", "quadratic a 1 b -5 c 6", etc.)
        val quadraticSolution = SpokenMathParser.tryParseQuadraticQuery(textToProcess)
        if (quadraticSolution != null) {
            _activeQuadraticSolution.value = quadraticSolution
            val eqStr = "Quadratic: ${if (quadraticSolution.a == 1.0) "" else quadraticSolution.a}x² + ${quadraticSolution.b}x + ${quadraticSolution.c} = 0"
            _displayExpression.value = eqStr
            val rootStr = "x₁ = ${quadraticSolution.root1Text},  x₂ = ${quadraticSolution.root2Text}"
            _displayResult.value = rootStr
            _statusMessage.value = "Roots solved (Δ = ${String.format(java.util.Locale.US, "%.1f", quadraticSolution.discriminant)})"

            speakIfEnabled(quadraticSolution.spokenSummary)

            viewModelScope.launch {
                repository.saveRecord(
                    CalculationRecord(
                        spokenInput = textToProcess,
                        parsedExpression = eqStr,
                        resultText = rootStr,
                        isSuccess = true
                    )
                )
            }
            return
        }

        // Check for memory commands
        val memAction = SpokenMathParser.checkMemoryCommand(textToProcess)
        if (memAction != null) {
            handleMemoryAction(memAction)
            return
        }

        // Normalize spoken text to math expression
        val normalized = SpokenMathParser.normalize(textToProcess)
        _displayExpression.value = normalized

        // Evaluate
        val evaluation = ScientificMathEvaluator.evaluate(normalized)
        if (evaluation.isSuccess && evaluation.values.isNotEmpty()) {
            val finalVal = evaluation.values.last()
            _lastResult.value = finalVal

            val resultString = if (evaluation.formattedOutputs.size == 1) {
                evaluation.formattedOutputs.first()
            } else {
                evaluation.formattedOutputs.joinToString(", ")
            }
            _displayResult.value = resultString
            _statusMessage.value = null

            // TTS feedback
            val speechReply = if (evaluation.formattedOutputs.size == 1) {
                "The answer is ${evaluation.formattedOutputs.first()}"
            } else {
                "The answers are ${evaluation.formattedOutputs.joinToString(", then ")}"
            }
            speakIfEnabled(speechReply)

            // Save to Room DB
            viewModelScope.launch {
                repository.saveRecord(
                    CalculationRecord(
                        spokenInput = textToProcess,
                        parsedExpression = normalized,
                        resultText = resultString,
                        isSuccess = true
                    )
                )
            }
        } else {
            _statusMessage.value = evaluation.errorMessage ?: "Could not evaluate expression"
            speakIfEnabled("Sorry, I didn't catch that calculation.")
            viewModelScope.launch {
                repository.saveRecord(
                    CalculationRecord(
                        spokenInput = textToProcess,
                        parsedExpression = normalized,
                        resultText = evaluation.errorMessage ?: "Evaluation error",
                        isSuccess = false
                    )
                )
            }
        }
    }

    private fun handleMemoryAction(action: SpokenMathParser.MemoryAction) {
        val currentLast = _lastResult.value
        when (action) {
            SpokenMathParser.MemoryAction.ADD -> {
                if (currentLast != null) {
                    _memoryValue.value += currentLast
                    val formattedMem = ScientificMathEvaluator.formatResult(_memoryValue.value)
                    val msg = "Added to memory. Memory is now $formattedMem"
                    _statusMessage.value = msg
                    speakIfEnabled(msg)
                } else {
                    val msg = "There is no result yet to add to memory."
                    _statusMessage.value = msg
                    speakIfEnabled(msg)
                }
            }
            SpokenMathParser.MemoryAction.SUBTRACT -> {
                if (currentLast != null) {
                    _memoryValue.value -= currentLast
                    val formattedMem = ScientificMathEvaluator.formatResult(_memoryValue.value)
                    val msg = "Subtracted from memory. Memory is now $formattedMem"
                    _statusMessage.value = msg
                    speakIfEnabled(msg)
                } else {
                    val msg = "There is no result yet to subtract from memory."
                    _statusMessage.value = msg
                    speakIfEnabled(msg)
                }
            }
            SpokenMathParser.MemoryAction.RECALL -> {
                val formattedMem = ScientificMathEvaluator.formatResult(_memoryValue.value)
                val msg = "Memory is $formattedMem"
                _displayResult.value = formattedMem
                _statusMessage.value = msg
                speakIfEnabled(msg)
            }
            SpokenMathParser.MemoryAction.CLEAR -> {
                _memoryValue.value = 0.0
                val msg = "Memory cleared."
                _statusMessage.value = msg
                speakIfEnabled(msg)
            }
        }
    }

    fun memoryAdd() = handleMemoryAction(SpokenMathParser.MemoryAction.ADD)
    fun memorySub() = handleMemoryAction(SpokenMathParser.MemoryAction.SUBTRACT)
    fun memoryRecall() = handleMemoryAction(SpokenMathParser.MemoryAction.RECALL)
    fun memoryClear() = handleMemoryAction(SpokenMathParser.MemoryAction.CLEAR)

    fun onExpressionTextChange(newText: String) {
        _displayExpression.value = newText
    }

    fun appendExpression(token: String) {
        _displayExpression.value += token
    }

    fun deleteLastChar() {
        val curr = _displayExpression.value
        if (curr.isNotEmpty()) {
            _displayExpression.value = curr.dropLast(1)
        }
    }

    fun clearAll() {
        _displayExpression.value = ""
        _displayResult.value = "0"
        _spokenTranscript.value = ""
        _statusMessage.value = null
    }

    fun evaluateManualInput() {
        val expr = _displayExpression.value.trim()
        if (expr.isEmpty()) return

        // Check if manual input is any equation (linear, systems, cubic, quadratic, transcendental)
        val anyEqSol = SpokenMathParser.tryParseAnyEquationQuery(expr)
        if (anyEqSol != null) {
            _activeUniversalSolution.value = anyEqSol
            if (anyEqSol.category == UniversalEquationSolver.EquationCategory.QUADRATIC) {
                _activeQuadraticSolution.value = QuadraticEquationSolver.parseAndSolve(anyEqSol.originalEquation)
            }
            _displayExpression.value = anyEqSol.originalEquation
            _displayResult.value = anyEqSol.rootsSummary
            _statusMessage.value = "${anyEqSol.category} Solved"

            speakIfEnabled(anyEqSol.spokenSummary)

            viewModelScope.launch {
                repository.saveRecord(
                    CalculationRecord(
                        spokenInput = expr,
                        parsedExpression = anyEqSol.originalEquation,
                        resultText = anyEqSol.rootsSummary,
                        isSuccess = true
                    )
                )
            }
            return
        }

        // Check if manual input is a quadratic equation
        val quadraticSolution = SpokenMathParser.tryParseQuadraticQuery(expr)
        if (quadraticSolution != null) {
            _activeQuadraticSolution.value = quadraticSolution
            val eqStr = "Quadratic: ${if (quadraticSolution.a == 1.0) "" else quadraticSolution.a}x² + ${quadraticSolution.b}x + ${quadraticSolution.c} = 0"
            _displayExpression.value = eqStr
            val rootStr = "x₁ = ${quadraticSolution.root1Text},  x₂ = ${quadraticSolution.root2Text}"
            _displayResult.value = rootStr
            _statusMessage.value = "Roots solved (Δ = ${String.format(java.util.Locale.US, "%.1f", quadraticSolution.discriminant)})"

            speakIfEnabled(quadraticSolution.spokenSummary)

            viewModelScope.launch {
                repository.saveRecord(
                    CalculationRecord(
                        spokenInput = expr,
                        parsedExpression = eqStr,
                        resultText = rootStr,
                        isSuccess = true
                    )
                )
            }
            return
        }

        // Support word normalization even if typed manually (e.g. "log 200", "20 percent of 80")
        val normalized = SpokenMathParser.normalize(expr)
        val evaluation = ScientificMathEvaluator.evaluate(normalized)

        if (evaluation.isSuccess && evaluation.values.isNotEmpty()) {
            val finalVal = evaluation.values.last()
            _lastResult.value = finalVal

            val resultString = if (evaluation.formattedOutputs.size == 1) {
                evaluation.formattedOutputs.first()
            } else {
                evaluation.formattedOutputs.joinToString(", ")
            }
            _displayResult.value = resultString
            _statusMessage.value = null

            val speechReply = if (evaluation.formattedOutputs.size == 1) {
                "The answer is ${evaluation.formattedOutputs.first()}"
            } else {
                "The answers are ${evaluation.formattedOutputs.joinToString(", then ")}"
            }
            speakIfEnabled(speechReply)

            viewModelScope.launch {
                repository.saveRecord(
                    CalculationRecord(
                        spokenInput = expr,
                        parsedExpression = normalized,
                        resultText = resultString,
                        isSuccess = true
                    )
                )
            }
        } else {
            _statusMessage.value = evaluation.errorMessage ?: "Invalid expression"
            speakIfEnabled("Still couldn't calculate that.")
        }
    }

    fun speakCurrentResult() {
        val res = _displayResult.value
        if (res.isNotEmpty() && res != "0") {
            voiceManager.speak("The answer is $res")
        } else if (_spokenTranscript.value.isNotEmpty()) {
            voiceManager.speak(_spokenTranscript.value)
        }
    }

    fun speakText(text: String) {
        voiceManager.speak(text)
    }

    private fun speakIfEnabled(text: String) {
        if (_isTtsEnabled.value) {
            voiceManager.speak(text)
        }
    }

    fun toggleFavorite(record: CalculationRecord) {
        viewModelScope.launch {
            repository.toggleFavorite(record)
        }
    }

    fun deleteHistoryRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteRecord(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun loadCalculation(spoken: String, parsed: String, result: String) {
        _spokenTranscript.value = spoken
        _displayExpression.value = parsed.ifEmpty { spoken }
        _displayResult.value = result
        _statusMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.release()
    }
}
