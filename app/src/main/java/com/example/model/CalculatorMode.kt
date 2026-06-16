package com.example.model

enum class CalculatorMode(val displayName: String, val codeName: String, val desc: String) {
    COMP("Arithmetic (COMP)", "COMP", "Normal flow, multi-line logic, fractions & variables"),
    CMPLX("Complex (CMPLX)", "CMPLX", "Complex numbers in rectangular (a+bi) or polar (r∠θ) format"),
    BASE_N("Base-N (BASE-N)", "BASE-N", "Binary, Octal, Decimal, Hexadecimal conversion & logic"),
    EQN("Equation (EQN)", "EQN", "Linear systems (2/3 variables), Quadratic & Cubic solvers"),
    MATRIX("Matrix (MATRIX)", "MATRIX", "Matrices (up to 3x3), det, transpose, inversions & arithmetic"),
    TABLE("Table (TABLE)", "TABLE", "Generate a table of values for f(x) over a range of step inputs"),
    STAT("Statistics (STAT)", "STAT", "1-variable analysis (mean, sum, SD) & regression calculations"),
    CONV("Conversion (CONV)", "CONV", "Unit conversions (length, weight, volume, speed, temp)"),
    CONST("Scientific Constants (CONST)", "CONST", "Scientific & physical constants registry (universal, atomic, chemistry)")
}
