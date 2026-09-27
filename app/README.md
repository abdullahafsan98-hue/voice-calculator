# 🎙️ Voice-Controlled Scientific Calculator & Universal Equation Solver

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Platform-Android%208.0%2B-green.svg?style=flat&logo=android)](https://www.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Status](https://img.shields.io/badge/Status-Active%20%2F%20Production%20Ready-brightgreen.svg)]()

> A modern, voice-first Android scientific calculator and algebraic equation engine that understands natural human speech in both **English** and **Hinglish** (Hindi + English), computes exact fractional roots, displays step-by-step pedagogical derivations, and reads solutions aloud.

🌐 **Try the Live Web Demo in your browser:**  
👉 **[Launch Interactive App](https://ais-pre-xrrmfs7wecdk4middk3mne-961250422033.asia-southeast1.run.app)**

---

## 🌟 Key Highlights & Capabilities

### 1. 🗣️ Bilingual Voice & Natural Language Math Parser
Rigid calculators fail when users speak colloquially. This app includes an on-device phonetic and grammar normalizer (`SpokenMathParser.kt`) that handles:
* **Fractions & Equations in English**:
  * *"1 by 3 x plus 2 by 5 x equals 5"* ➔ `(1/3)x + (2/5)x = 5`
  * *"one by three x plus two by five x equals five"*
* **Hinglish (Hindi + English) Math Speech**:
  * *"1 bata 3 x plus 2 bata 5 x barabar 5"*
  * *"teen x plus paanch barabar shunya"* ➔ `3x + 5 = 0`
  * *"x square minus 5x plus 6 barabar 0"*
  * Terms supported: `bata`, `batte`, `barabar`, `varg`, `ghan`, `shunya`, `sifar`, `jodo`, `ghatao`, `guna`, `bhag`.

### 2. 📐 Universal Algebraic Equation Engine (`UniversalEquationSolver.kt`)
* **Fraction Simplification**: Converts floating-point values into exact rational fractions via Continued Fraction Expansion (e.g., $6.818181... \to \frac{75}{11}$).
* **Linear Equations (1 Variable)**: Standard form reduction $ax + b = 0 \implies x = -\frac{b}{a}$.
* **Quadratic Equations**: Analytical discriminant $\Delta = b^2 - 4ac$ providing real or complex conjugate roots ($x = \alpha \pm \beta i$).
* **Cubic Equations**: Solved algebraically using **Cardano’s Depressed Cubic** formula ($t^3 + pt + q = 0$).
* **Systems of Linear Equations (2x2 & 3x3)**: Solved using **Cramer's Rule** determinants ($D, D_x, D_y, D_z$).
* **Transcendental & Non-Linear Functions**: Solved using numerical **Newton-Raphson approximation**.

### 3. 🔊 Step-by-Step Pedagogy & Bilingual Audio Synthesis
* Generates clear, step-by-step mathematical reasoning.
* Narrates the answer aloud using Text-to-Speech (TTS) in **English** (*"The solution is x equals 75 over 11"*) and **Hindi** (*"Samikaran ka hal hai: x barabar 75 bata 11"*).

### 4. 📊 Canvas Function Graphing & Scientific Toolkit
* **2D Cartesian Graphing**: Visualizes polynomial, trigonometric, and exponential curves with coordinate grids and zero-crossings.
* **Full Scientific Functions**: Trigonometry ($\sin, \cos, \tan$, inverses), logarithms ($\ln, \log_{10}$), factorials, constants ($\pi, e$).
* **Matrix Operations**: Determinants, inversions, additions, and scalar multiplications.
* **Unit Converter**: Mass, length, temperature, speed, volume, and data conversions.

---

## 🏗️ Architecture & Pipeline

```text
┌──────────────────────────┐
│   Spoken Audio Input     │  (e.g., "1 by 3 x plus 2 by 5 x equals 5")
└─────────────┬────────────┘
              │ Android SpeechRecognizer
              ▼
┌──────────────────────────┐
│    SpokenMathParser      │  • Bilingual tokenization & phonetic replacement
│     (NLP Pipeline)       │  • Fraction normalization: "1 by 3" -> "(1/3)"
└─────────────┬────────────┘  • Variable extraction: x, y, z
              │ Canonical Equation String
              ▼
┌──────────────────────────┐
│ UniversalEquationSolver  │  • Continued Fraction Expansion
│    (Algebraic Engine)    │  • Cramer's Rule (2x2 & 3x3)
└─────────────┬────────────┘  • Cardano's Formula (Cubics) / Newton-Raphson
              │ EquationSolution (roots, fractions, steps, summaries)
              ▼
┌──────────────────────────┐
│   Jetpack Compose UI     │  • Step-by-step Cards & Preset Equation Chips
│  + Bilingual TTS Audio   │  • English & Hindi Audio Readout
└──────────────────────────┘
```

---

## 📂 Project Structure

```text
app/src/main/java/com/example/
├── engine/
│   ├── SpokenMathParser.kt           # Speech-to-math grammar & Hinglish parsing
│   ├── UniversalEquationSolver.kt    # Exact fraction, linear, quadratic, cubic, & matrix engine
│   └── ScientificMathEvaluator.kt    # Shunting-yard evaluator for arithmetic & trigonometry
├── ui/
│   ├── components/
│   │   ├── UniversalEquationSolverSheet.kt  # Bottom sheet solver with instant preset chips
│   │   ├── SpokenExamplesSheet.kt          # Categorized guide of spoken voice commands
│   │   ├── MathGraphView.kt                # Canvas-based 2D function plotter
│   │   └── MatrixCalculatorSheet.kt        # 2x2 and 3x3 matrix manipulation tool
│   ├── theme/                              # Material 3 ColorScheme, Typography & Shapes
│   └── MainActivity.kt                     # Compose entry point & speech lifecycle
└── data/                                   # History persistence & local state
```

---

## ⚡ Tested Equation Prompts

| Intent | Spoken Input | Standardized Equation | Solution |
| :--- | :--- | :--- | :--- |
| **Fraction Linear** | *"1 by 3 x plus 2 by 5 x equals 5"* | `(1/3)x + (2/5)x = 5` | $x = \frac{75}{11} \approx 6.8182$ |
| **Hinglish Fraction** | *"1 bata 3 x plus 2 bata 5 x barabar 5"* | `(1/3)x + (2/5)x = 5` | $x = \frac{75}{11} \approx 6.8182$ |
| **Hindi Linear** | *"teen x plus paanch barabar shunya"* | `3x + 5 = 0` | $x = -1.6667$ |
| **Quadratic** | *"x square minus 5x plus 6 equals 0"* | `x^2 - 5x + 6 = 0` | $x_1 = 3, \; x_2 = 2$ |
| **Cubic Equation** | *"x cube plus x square plus x plus 2 barabar 0"* | `x^3 + x^2 + x + 2 = 0` | $x_1 = -1.3532, \; x_{2,3} = 0.1766 \pm 1.2028i$ |
| **2x2 System** | *"2x plus y barabar 7 aur x minus y barabar 2"* | `2x + y = 7; x - y = 2` | $x = 3, \; y = 1$ |
| **3x3 System** | *"x + y + z = 6 and 2y + 5z = -4 and 2x + 5y - z = 27"* | Matrix $3 \times 3$ | $x = 5, \; y = 3, \; z = -2$ |

---

## 🚀 Getting Started

### Prerequisites
* **Android Studio** Hedgehog (2023.1.1) or newer
* **JDK**: 17 or 21
* **Android SDK**: `minSdkVersion 26`, `targetSdkVersion 35`

### Building from Source
1. Clone this repository:
   ```bash
   git clone https://github.com/your-username/voice-equation-solver-android.git
   cd voice-equation-solver-android
   ```
2. Build the Debug APK using Gradle:
   ```bash
   gradle :app:assembleDebug
   ```
3. Run the unit tests:
   ```bash
   gradle :app:testDebugUnitTest
   ```
4. Output APK location:
   `app/build/outputs/apk/debug/app-debug.apk`

---

## 🛠️ Built With
* **Language**: [Kotlin](https://kotlinlang.org/)
* **UI Framework**: [Jetpack Compose (Material 3)](https://developer.android.com/jetpack/compose)
* **Speech Integration**: Android `SpeechRecognizer` + `android.speech.tts.TextToSpeech`
* **Architecture**: MVVM with Kotlin Coroutines & StateFlow

---

## 📄 License
This project is open-source and available under the [MIT License](LICENSE).
