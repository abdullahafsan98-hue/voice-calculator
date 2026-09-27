# Voice-Controlled Calculator & Universal Equation Engine
An Android application built with Kotlin and Jetpack Compose that solves complex equations 
via natural spoken voice in both English and Hinglish.

---

## 🧠 Architectural Architecture & Methodology

### 1. Bilingual Spoken Language Parser (`SpokenMathParser.kt`)
Traditional voice calculators break on colloquial speech. The parsing engine uses a 
multi-stage pipeline to tokenize and normalize spoken Hindi/Hinglish and English into 
canonical mathematical syntax:

* **Phonetic & Term Mapping**: Converts colloquial math vocabulary:
  - "bata" / "batte" / "by" / "over" ➔ `/`
  - "barabar" / "equals" ➔ `=`
  - "shunya" / "sifar" / "zero" ➔ `0`
  - "varg" / "square" ➔ `^2`
  - "ghan" / "cube" ➔ `^3`
* **Fraction Detection**: Converts spoken mixed phrases such as 
  `"1 by 3 x plus 2 by 5 x barabar 5"` or `"ek bata teen x jodo do bata paanch x = 5"` 
  into standardized fractional polynomials: `(1/3)x + (2/5)x = 5`.

---

### 2. Universal Equation Solver (`UniversalEquationSolver.kt`)
Once sanitized, expressions are parsed through dedicated algebraic solvers:

* **Fraction Rationalization**:
  Uses Continued Fraction Expansion algorithms to convert floating-point decimal results 
  into exact rational fractions (e.g., `6.818181...` ➔ `75/11`).

* **1-Variable Linear Equations**:
  Extracts coefficient sums on LHS and RHS:
  $$ax + b = 0 \implies x = -\frac{b}{a}$$

* **Quadratic & Cubic Equations**:
  - Quadratic equations solved via discriminant analysis ($\Delta = b^2 - 4ac$), 
    yielding real roots or complex conjugates.
  - Cubic equations solved algebraically via Cardano’s depressed cubic formula:
    $$t^3 + pt + q = 0$$

* **Multi-Variable Linear Systems (2x2 & 3x3)**:
  Extracts multi-variable coefficients and solves systems using Cramer's Rule determinants:
  $$x = \frac{D_x}{D}, \quad y = \frac{D_y}{D}, \quad z = \frac{D_z}{D}$$

* **Transcendental / Non-Linear Equations**:
  Applies the numerical Newton-Raphson approximation method:
  $$x_{n+1} = x_n - \frac{f(x_n)}{f'(x_n)}$$

---

### 3. Step-by-Step Pedagogical Engine & Bilingual TTS
* **Step Generation**: Each step of algebraic isolation, substitution, and determinant 
  calculation is logged with intermediate expressions.
* **Dual-Language Text-to-Speech**: Synthesizes spoken output in both English 
  (*"The solution is x equals 75 over 11"*) and Hindi/Hinglish 
  (*"Samikaran ka hal hai: x barabar 75 bata 11"*).

---

### 4. Modern Jetpack Compose UI
* **Material 3 Dynamic Theme** with adaptive layouts for phones and tablets.
* **Canvas Math Graphing**: Plots polynomial and trigonometric functions with axes and grid scaling.
* **Interactive Bottom Sheets & Preset Chips**: One-tap sample equations for rapid testing.
