package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.db.HistoryDao
import com.example.db.HistoryItem
import com.example.db.VariableDao
import com.example.db.VariableEntity
import com.example.engine.ComplexNumber
import com.example.engine.MathParser
import com.example.model.CalculatorMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.*

class CalculatorViewModel(
    private val historyDao: HistoryDao,
    private val variableDao: VariableDao
) : ViewModel() {

    // Global Calculator State
    var activeMode by mutableStateOf(CalculatorMode.COMP)
    var expression by mutableStateOf("")
    var cursorPosition by mutableStateOf(0)
    var resultDisplay by mutableStateOf("")
    var decimalDisplayState by mutableStateOf("") // For S-D Conversions
    var isShowingFraction by mutableStateOf(false)
    var activeError by mutableStateOf<String?>(null)

    // Modifiers
    var isShiftActive by mutableStateOf(false)
    var isAlphaActive by mutableStateOf(false)

    // Variable register storage
    private val _variableMap = MutableStateFlow<Map<String, ComplexNumber>>(
        mapOf(
            "A" to ComplexNumber.ZERO,
            "B" to ComplexNumber.ZERO,
            "C" to ComplexNumber.ZERO,
            "D" to ComplexNumber.ZERO,
            "E" to ComplexNumber.ZERO,
            "F" to ComplexNumber.ZERO,
            "X" to ComplexNumber.ZERO,
            "Y" to ComplexNumber.ZERO,
            "M" to ComplexNumber.ZERO
        )
    )
    val variableMap: StateFlow<Map<String, ComplexNumber>> = _variableMap.asStateFlow()

    // Last evaluated result
    var ansValue by mutableStateOf(ComplexNumber.ZERO)

    // Setup Option: DEG, RAD, GRA
    var angleUnit by mutableStateOf("DEG") // DEG, RAD, GRA

    // --- REPLAY / DB HISTORY ---
    val calculationHistory: StateFlow<List<HistoryItem>> = historyDao.getAllHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Load saved variables from Database
        viewModelScope.launch {
            variableDao.getAllVariables().collect { savedList ->
                if (savedList.isNotEmpty()) {
                    val currentMap = _variableMap.value.toMutableMap()
                    savedList.forEach { entity ->
                        currentMap[entity.name] = ComplexNumber(entity.realValue, entity.imagValue)
                    }
                    _variableMap.value = currentMap
                }
            }
        }
    }

    // --- KEY ACTIONS ---

    fun onKeyPress(btnLabel: String) {
        val shift = isShiftActive
        val alpha = isAlphaActive
        
        // Reset modifiers when key is pressed
        isShiftActive = false
        isAlphaActive = false

        when (btnLabel) {
            "AC" -> clearAll()
            "DEL" -> deleteAtCursor()
            "=" -> evaluateCurrentExpression()
            "SHIFT" -> isShiftActive = !shift
            "ALPHA" -> isAlphaActive = !alpha
            "S⇔D" -> toggleDecimalFractionString()
            "M+" -> {
                // Add current answer to M
                addToMemory()
            }
            "ANS" -> insertText("Ans")
            "CONST" -> {
                // Constant shortcut selection handled in UI
            }
            "CONV" -> {
                // Conversion shortcut selection handled in UI
            }
            else -> {
                // Normal operations insertion
                handleNormalInput(btnLabel, shift, alpha)
            }
        }
    }

    private fun handleNormalInput(btn: String, shift: Boolean, alpha: Boolean) {
        if (alpha) {
            // Variable selection
            val variableList = listOf("A", "B", "C", "D", "E", "F", "X", "Y", "M")
            if (btn in variableList) {
                insertText(btn)
                return
            }
        }

        if (shift) {
            when (btn) {
                "sin" -> insertText("asin(")
                "cos" -> insertText("acos(")
                "tan" -> insertText("atan(")
                "x²" -> insertText("³") // cubic
                "√" -> insertText("³√(") // cbrt
                "log" -> insertText("logbase(") // logbase
                "ln" -> insertText("e^")
                "(" -> insertText("!")
                ")" -> insertText("%")
                "×" -> insertText("nPr(")
                "÷" -> insertText("nCr(")
                "+" -> insertText("Pol(")
                "-" -> insertText("Rec(")
                "STO" -> {
                    // STO mode triggers saving the last evaluated result to the next variable pressed
                    return
                }
                else -> {}
            }
            return
        }

        // Standard mappings
        when (btn) {
            "x²" -> insertText("²")
            "x^y" -> insertText("^")
            "√" -> insertText("√(")
            "log" -> insertText("log(")
            "ln" -> insertText("ln(")
            "sin" -> insertText("sin(")
            "cos" -> insertText("cos(")
            "tan" -> insertText("tan(")
            "EXP" -> insertText("*10^")
            "i" -> insertText("i")
            "-" -> insertText("−") // subtraction sign
            "( - )" -> insertText("−") // unary negation
            else -> insertText(btn)
        }
    }

    fun insertText(text: String) {
        activeError = null
        val curExpr = expression
        val pos = cursorPosition
        val before = curExpr.substring(0, pos)
        val after = curExpr.substring(pos)
        expression = before + text + after
        cursorPosition = pos + text.length
    }

    fun deleteAtCursor() {
        activeError = null
        val curExpr = expression
        val pos = cursorPosition
        if (pos > 0) {
            val before = curExpr.substring(0, pos - 1)
            val after = curExpr.substring(pos)
            expression = before + after
            cursorPosition = pos - 1
        }
    }

    fun clearAll() {
        expression = ""
        cursorPosition = 0
        resultDisplay = ""
        decimalDisplayState = ""
        isShowingFraction = false
        activeError = null
    }

    fun storeVariable(varName: String, value: ComplexNumber) {
        viewModelScope.launch {
            variableDao.saveVariable(VariableEntity(varName, value.real, value.imag))
            val currentMap = _variableMap.value.toMutableMap()
            currentMap[varName] = value
            _variableMap.value = currentMap
        }
    }

    private fun addToMemory() {
        if (resultDisplay.isNotEmpty() && activeError == null) {
            try {
                val valueToAdd = ansValue
                val currentM = _variableMap.value["M"] ?: ComplexNumber.ZERO
                val newM = currentM + valueToAdd
                storeVariable("M", newM)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    // --- EVALUATION ENGINE ---

    fun evaluateCurrentExpression() {
        if (expression.isEmpty()) return
        activeError = null

        try {
            // Prepare Variable contexts
            val varContext = _variableMap.value.toMutableMap()
            varContext["Ans"] = ansValue

            // Execute Parser
            val parser = MathParser(
                rawExpression = expression,
                variables = varContext,
                useDegrees = (angleUnit == "DEG"),
                useGradians = (angleUnit == "GRA")
            )

            val parsedValue = parser.parse()
            ansValue = parsedValue

            // Format results
            if (activeMode == CalculatorMode.CMPLX) {
                // Complex support outputs
                resultDisplay = parsedValue.toString()
                isShowingFraction = false
                decimalDisplayState = parsedValue.toPolarString(angleUnit == "DEG")
            } else {
                // COMP standard real-centric outputs
                val realPart = parsedValue.real
                val imagPart = parsedValue.imag
                
                if (abs(imagPart) > 1e-9) {
                    // Result actually has an imaginary component, display as complex automatically
                    resultDisplay = parsedValue.toString()
                    decimalDisplayState = ""
                } else {
                    // Purely real number
                    resultDisplay = formatDouble(realPart)
                    
                    // Generate fraction conversion
                    val fracStr = doubleToFraction(realPart)
                    if (fracStr != null && fracStr != resultDisplay) {
                        decimalDisplayState = fracStr
                        isShowingFraction = true
                    } else {
                        decimalDisplayState = ""
                        isShowingFraction = false
                    }
                }
            }

            // Save to database History
            val historyExpr = expression
            val historyResult = resultDisplay
            val modeName = activeMode.codeName
            viewModelScope.launch {
                historyDao.insertHistory(
                    HistoryItem(
                        expression = historyExpr,
                        result = historyResult,
                        mode = modeName
                    )
                )
            }

        } catch (e: Exception) {
            activeError = e.message ?: "Math Error"
            resultDisplay = "Math ERROR"
            decimalDisplayState = ""
            isShowingFraction = false
        }
    }

    private fun toggleDecimalFractionString() {
        if (decimalDisplayState.isNotEmpty()) {
            // Toggle swap display values
            val temp = resultDisplay
            resultDisplay = decimalDisplayState
            decimalDisplayState = temp
        }
    }

    private fun doubleToFraction(value: Double, maxDenominator: Int = 1000): String? {
        if (value.isNaN() || value.isInfinite()) return null
        if (abs(value) < 1e-10) return "0"
        
        val sign = if (value < 0) "-" else ""
        val absoluteVal = abs(value)
        
        // If it's already an integer, return it
        if (abs(absoluteVal - absoluteVal.roundToInt()) < 1e-9) {
            return "$sign${absoluteVal.roundToInt()}"
        }

        var h1 = 1L
        var h2 = 0L
        var k1 = 0L
        var k2 = 1L
        var b = absoluteVal
        do {
            val a = b.toLong()
            val aux = h1
            h1 = a * h1 + h2
            h2 = aux
            val aux2 = k1
            k1 = a * k1 + k2
            k2 = aux2
            
            val diff = b - a
            if (abs(diff) < 1e-12) break
            b = 1.0 / diff
        } while (abs(absoluteVal - h1.toDouble() / k1.toDouble()) > 1e-7 && k1 < maxDenominator)

        val approxResult = h1.toDouble() / k1.toDouble()
        if (abs(absoluteVal - approxResult) < 1e-5 && k1 > 1) {
            return "$sign$h1/$k1"
        }
        return null
    }

    private fun formatDouble(d: Double): String {
        if (d.isNaN()) return "NaN"
        if (d.isInfinite()) return if (d < 0) "-∞" else "∞"
        
        // Rounded representation
        var s = String.format("%.10f", d)
        // Strip trailing zeros after period
        if (s.contains(".")) {
            s = s.replace(Regex("0+$"), "")
            s = s.replace(Regex("\\.$"), "")
        }
        if (s == "-0") s = "0"
        return s
    }

    // --- OTHER MODES SPECIFIC DATA & FLOWS ---

    // 1. BASE-N MODE STATE
    var baseNNumberSystem by mutableStateOf("DEC") // BIN, OCT, DEC, HEX
    var baseNInput by mutableStateOf("0")

    fun onBaseNKeyPress(key: String) {
        when (key) {
            "AC" -> baseNInput = "0"
            "HEX" -> convertBaseNSystem("HEX")
            "DEC" -> convertBaseNSystem("DEC")
            "OCT" -> convertBaseNSystem("OCT")
            "BIN" -> convertBaseNSystem("BIN")
            else -> {
                // Validate if character is valid for current system
                if (isValidForBase(key, baseNNumberSystem)) {
                    if (baseNInput == "0") {
                        baseNInput = key
                    } else {
                        baseNInput += key
                    }
                }
            }
        }
    }

    private fun isValidForBase(char: String, base: String): Boolean {
        if (char.length != 1) return false
        val c = char[0]
        return when (base) {
            "BIN" -> c in '0'..'1'
            "OCT" -> c in '0'..'7'
            "DEC" -> c in '0'..'9'
            "HEX" -> c in '0'..'9' || c in 'A'..'F'
            else -> false
        }
    }

    private fun convertBaseNSystem(targetBase: String) {
        try {
            val decimalVal = when (baseNNumberSystem) {
                "BIN" -> baseNInput.toLong(2)
                "OCT" -> baseNInput.toLong(8)
                "DEC" -> baseNInput.toLong(10)
                "HEX" -> baseNInput.toLong(16)
                else -> 0L
            }
            baseNNumberSystem = targetBase
            baseNInput = when (targetBase) {
                "BIN" -> decimalVal.toString(2)
                "OCT" -> decimalVal.toString(8)
                "DEC" -> decimalVal.toString(10)
                "HEX" -> decimalVal.toString(16).uppercase()
                else -> "0"
            }
        } catch (e: Exception) {
            baseNInput = "0"
            baseNNumberSystem = targetBase
        }
    }


    // 2. EQUATION SOLVER STATE
    var eqnType by mutableStateOf("QUADRATIC") // LINEAR2, LINEAR3, QUADRATIC, CUBIC
    
    // Coefficients
    var eqnCoefficients = MutableStateFlow(Array(4) { DoubleArray(4) { 0.0 } })
    var eqnResults by mutableStateOf<List<String>>(emptyList())

    fun updateEqnCoefficient(row: Int, col: Int, value: Double) {
        val current = eqnCoefficients.value
        current[row][col] = value
        // Force state update
        eqnCoefficients.value = Array(4) { r -> DoubleArray(4) { c -> current[r][c] } }
    }

    fun solveEquation() {
        val coeffs = eqnCoefficients.value
        val list = mutableListOf<String>()
        try {
            when (eqnType) {
                "LINEAR2" -> {
                    // a1 x + b1 y = c1
                    // a2 x + b2 y = c2
                    // coeffs: row 0: a1, b1, c1; row 1: a2, b2, c2
                    val a1 = coeffs[0][0]; val b1 = coeffs[0][1]; val c1 = coeffs[0][2]
                    val a2 = coeffs[1][0]; val b2 = coeffs[1][1]; val c2 = coeffs[1][2]
                    val det = a1 * b2 - a2 * b1
                    if (abs(det) < 1e-11) {
                        list.add("No unique solution (Det = 0)")
                    } else {
                        val x = (c1 * b2 - c2 * b1) / det
                        val y = (a1 * c2 - a2 * c1) / det
                        list.add("x = ${formatDouble(x)}")
                        list.add("y = ${formatDouble(y)}")
                    }
                }
                "LINEAR3" -> {
                    // 3 Equations:
                    // a1 x + b1 y + c1 z = d1
                    // a2 x + b2 y + c2 z = d2
                    // a3 x + b3 y + c3 z = d3
                    val a1 = coeffs[0][0]; val b1 = coeffs[0][1]; val c1 = coeffs[0][2]; val d1 = coeffs[0][3]
                    val a2 = coeffs[1][0]; val b2 = coeffs[1][1]; val c2 = coeffs[1][2]; val d2 = coeffs[1][3]
                    val a3 = coeffs[2][0]; val b3 = coeffs[2][1]; val c3 = coeffs[2][2]; val d3 = coeffs[2][3]

                    val detMain = determinant3x3(
                        a1, b1, c1,
                        a2, b2, c2,
                        a3, b3, c3
                    )
                    if (abs(detMain) < 1e-12) {
                        list.add("No unique solution (Det = 0)")
                    } else {
                        val detX = determinant3x3(d1, b1, c1, d2, b2, c2, d3, b3, c3)
                        val detY = determinant3x3(a1, d1, c1, a2, d2, c2, a3, d3, c3)
                        val detZ = determinant3x3(a1, b1, d1, a2, b2, d2, a3, b3, d3)
                        
                        list.add("x = ${formatDouble(detX / detMain)}")
                        list.add("y = ${formatDouble(detY / detMain)}")
                        list.add("z = ${formatDouble(detZ / detMain)}")
                    }
                }
                "QUADRATIC" -> {
                    // a x^2 + b x + c = 0
                    // coeffs: row 0: a, b, c
                    val a = coeffs[0][0]
                    val b = coeffs[0][1]
                    val c = coeffs[0][2]
                    if (a == 0.0) {
                        if (b == 0.0) {
                            list.add("Invalid equation (0 = 0)")
                        } else {
                            list.add("x = ${formatDouble(-c / b)}")
                        }
                    } else {
                        val delta = b * b - 4 * a * c
                        if (delta >= 0.0) {
                            val x1 = (-b + sqrt(delta)) / (2 * a)
                            val x2 = (-b - sqrt(delta)) / (2 * a)
                            list.add("x1 = ${formatDouble(x1)}")
                            list.add("x2 = ${formatDouble(x2)}")
                        } else {
                            val realPart = -b / (2 * a)
                            val imagPart = sqrt(-delta) / (2 * a)
                            list.add("x1 = ${formatDouble(realPart)} + ${formatDouble(imagPart)}i")
                            list.add("x2 = ${formatDouble(realPart)} - ${formatDouble(imagPart)}i")
                        }
                    }
                }
                "CUBIC" -> {
                    // a x^3 + b x^2 + c x + d = 0
                    // coeffs: row 0: a, b, c, d
                    val a = coeffs[0][0]
                    val b = coeffs[0][1]
                    val c = coeffs[0][2]
                    val d = coeffs[0][3]
                    
                    val roots = solveCubicAnalytical(a, b, c, d)
                    for (i in roots.indices) {
                        list.add("x${i+1} = ${roots[i]}")
                    }
                }
            }
        } catch (e: Exception) {
            list.add("Solver error: ${e.message}")
        }
        eqnResults = list
    }

    private fun determinant3x3(
        a1: Double, b1: Double, c1: Double,
        a2: Double, b2: Double, c2: Double,
        a3: Double, b3: Double, c3: Double
    ): Double {
        return a1 * (b2 * c3 - c2 * b3) - b1 * (a2 * c3 - c2 * a3) + c1 * (a2 * b3 - b2 * a3)
    }

    private fun solveCubicAnalytical(a: Double, b: Double, c: Double, d: Double): List<ComplexNumber> {
        if (a == 0.0) {
            // Deflates to Quadratic
            val delta = c * c - 4 * b * d
            if (b == 0.0) return emptyList()
            if (delta >= 0.0) {
                return listOf(
                    ComplexNumber((-c + sqrt(delta)) / (2 * b)),
                    ComplexNumber((-c - sqrt(delta)) / (2 * b))
                )
            } else {
                return listOf(
                    ComplexNumber(-c / (2 * b), sqrt(-delta) / (2 * b)),
                    ComplexNumber(-c / (2 * b), -sqrt(-delta) / (2 * b))
                )
            }
        }

        // Newton-Raphson approximation of 1 real root to perform deflation (highly robust)
        var x0 = 0.0
        val guesses = listOf(0.0, 1.0, -1.0, 10.0, -10.0, 50.0)
        var hasRoot = false
        for (g in guesses) {
            var curr = g
            for (i in 0..150) {
                val fVal = a * curr.pow(3) + b * curr.pow(2) + c * curr + d
                val dfVal = 3 * a * curr.pow(2) + 2 * b * curr + c
                if (abs(dfVal) < 1e-12) break
                val next = curr - fVal / dfVal
                if (abs(next - curr) < 1e-10) {
                    x0 = next
                    hasRoot = true
                    break
                }
                curr = next
            }
            if (hasRoot) break
        }

        // Deflate ax^3 + bx^2 + cx + d with (x - x0) to get general quadratic Ax^2 + Bx + C
        // Ax^2 + Bx + C = 0 where A = a, B = b + a*x0, C = c + B*x0
        val quadA = a
        val quadB = b + a * x0
        val quadC = c + quadB * x0

        val list = mutableListOf<ComplexNumber>()
        list.add(ComplexNumber(x0, 0.0))

        // Solve Quadratic
        val delta = quadB * quadB - 4 * quadA * quadC
        if (delta >= 0.0) {
            list.add(ComplexNumber((-quadB + sqrt(delta)) / (2 * quadA)))
            list.add(ComplexNumber((-quadB - sqrt(delta)) / (2 * quadA)))
        } else {
            list.add(ComplexNumber(-quadB / (2 * quadA), sqrt(-delta) / (2 * quadA)))
            list.add(ComplexNumber(-quadB / (2 * quadA), -sqrt(-delta) / (2 * quadA)))
        }
        return list
    }


    // 3. MATRIX SOLVER STATE
    var activeMatrixCellRow by mutableStateOf(0)
    var activeMatrixCellCol by mutableStateOf(0)
    var selectedMatrixName by mutableStateOf("A") // A, B, C
    
    // 3 matrices (3x3 each)
    var matrixA = MutableStateFlow(Array(3) { DoubleArray(3) { 0.0 } })
    var matrixB = MutableStateFlow(Array(3) { DoubleArray(3) { 0.0 } })
    var matrixC = MutableStateFlow(Array(3) { DoubleArray(3) { 0.0 } })

    var matrixResultDisplay by mutableStateOf<String?>(null)

    fun updateMatrixCell(matrixName: String, row: Int, col: Int, value: Double) {
        val targetFlow = when (matrixName) {
            "A" -> matrixA
            "B" -> matrixB
            else -> matrixC
        }
        val current = targetFlow.value
        current[row][col] = value
        // Force flow state trigger
        targetFlow.value = Array(3) { r -> DoubleArray(3) { c -> current[r][c] } }
    }

    fun calculateMatrixDeterminant(name: String) {
        val data = when (name) {
            "A" -> matrixA.value
            "B" -> matrixB.value
            else -> matrixC.value
        }
        val d0 = data[0][0] * (data[1][1] * data[2][2] - data[1][2] * data[2][1])
        val d1 = data[0][1] * (data[1][0] * data[2][2] - data[1][2] * data[2][0])
        val d2 = data[0][2] * (data[1][0] * data[2][1] - data[1][1] * data[2][0])
        val det = d0 - d1 + d2
        matrixResultDisplay = "Det(Mat $name) = ${formatDouble(det)}"
    }

    fun calculateMatrixTranspose(name: String) {
        val data = when (name) {
            "A" -> matrixA.value
            "B" -> matrixB.value
            else -> matrixC.value
        }
        val sb = StringBuilder("Trn(Mat $name):\n")
        for (i in 0..2) {
            sb.append("[ ")
            for (j in 0..2) {
                sb.append(formatDouble(data[j][i])).append("    ")
            }
            sb.append(" ]\n")
        }
        matrixResultDisplay = sb.toString()
    }

    fun calculateInverseMatrix(name: String) {
        val data = when (name) {
            "A" -> matrixA.value
            "B" -> matrixB.value
            else -> matrixC.value
        }
        val d0 = data[0][0] * (data[1][1] * data[2][2] - data[1][2] * data[2][1])
        val d1 = data[0][1] * (data[1][0] * data[2][2] - data[1][2] * data[2][0])
        val d2 = data[0][2] * (data[1][0] * data[2][1] - data[1][1] * data[2][0])
        val det = d0 - d1 + d2

        if (abs(det) < 1e-11) {
            matrixResultDisplay = "Mat $name is singular (Det=0). No Inverse!"
            return
        }

        // Adjugate
        val adj = Array(3) { DoubleArray(3) }
        adj[0][0] = (data[1][1] * data[2][2] - data[1][2] * data[2][1])
        adj[0][1] = -(data[0][1] * data[2][2] - data[0][2] * data[2][1])
        adj[0][2] = (data[0][1] * data[1][2] - data[0][2] * data[1][1])
        
        adj[1][0] = -(data[1][0] * data[2][2] - data[1][2] * data[2][0])
        adj[1][1] = (data[0][0] * data[2][2] - data[0][2] * data[2][0])
        adj[1][2] = -(data[0][0] * data[1][2] - data[0][2] * data[1][0])
        
        adj[2][0] = (data[1][0] * data[2][1] - data[1][1] * data[2][0])
        adj[2][1] = -(data[0][0] * data[2][1] - data[0][1] * data[2][0])
        adj[2][2] = (data[0][0] * data[1][1] - data[0][1] * data[1][0])

        val sb = StringBuilder("Mat $name⁻¹:\n")
        for (i in 0..2) {
            sb.append("[ ")
            for (j in 0..2) {
                // Transpose adj and divide by det
                val cellVal = adj[j][i] / det
                sb.append(formatDouble(cellVal)).append("    ")
            }
            sb.append(" ]\n")
        }
        matrixResultDisplay = sb.toString()
    }

    fun multiplyMatrices(m1: String, m2: String) {
        val d1 = when (m1) {
            "A" -> matrixA.value
            "B" -> matrixB.value
            else -> matrixC.value
        }
        val d2 = when (m2) {
            "A" -> matrixA.value
            "B" -> matrixB.value
            else -> matrixC.value
        }

        val res = Array(3) { DoubleArray(3) }
        val sb = StringBuilder("Mat $m1 × Mat $m2:\n")
        for (i in 0..2) {
            sb.append("[ ")
            for (j in 0..2) {
                var sum = 0.0
                for (k in 0..2) {
                    sum += d1[i][k] * d2[k][j]
                }
                res[i][j] = sum
                sb.append(formatDouble(sum)).append("    ")
            }
            sb.append(" ]\n")
        }
        matrixResultDisplay = sb.toString()
    }


    // 4. TABLE MODE STATE
    var tableFunction = mutableStateOf("X^2 − X + 2")
    var tableStart by mutableStateOf(1.0)
    var tableEnd by mutableStateOf(5.0)
    var tableStep by mutableStateOf(1.0)
    var tableRows by mutableStateOf<List<Pair<Double, String>>>(emptyList())

    fun generateTable() {
        val rows = mutableListOf<Pair<Double, String>>()
        var currX = tableStart
        val end = tableEnd
        val step = tableStep
        
        if (step <= 0) {
            tableRows = listOf(0.0 to "Step size must be > 0!")
            return
        }

        var count = 0
        while (currX <= end + 1e-9 && count < 100) {
            // Replace X with current X value in function and solve
            try {
                val varContext = _variableMap.value.toMutableMap()
                varContext["X"] = ComplexNumber(currX, 0.0)

                val parser = MathParser(
                    rawExpression = tableFunction.value,
                    variables = varContext,
                    useDegrees = (angleUnit == "DEG"),
                    useGradians = (angleUnit == "GRA")
                )
                val out = parser.parse()
                rows.add(currX to out.toString())
            } catch (e: Exception) {
                rows.add(currX to (e.message ?: "ERROR"))
            }
            currX += step
            count++
        }
        tableRows = rows
    }


    // 5. STATISTICS MODE STATE
    var statInputText by mutableStateOf("")
    var statDataSet = MutableStateFlow<List<Pair<Double, Double>>>(emptyList()) // X, Y values
    var statSummaryResult by mutableStateOf<Map<String, String>>(emptyMap())

    fun addStatDataPoint(x: Double, y: Double = 0.0) {
        val current = statDataSet.value.toMutableList()
        current.add(x to y)
        statDataSet.value = current
        statInputText = ""
        runStatSummary()
    }

    fun clearStatDataSet() {
        statDataSet.value = emptyList()
        statSummaryResult = emptyMap()
    }

    private fun runStatSummary() {
        val data = statDataSet.value
        if (data.isEmpty()) {
            statSummaryResult = emptyMap()
            return
        }

        val n = data.size
        var sumX = 0.0
        var sumX2 = 0.0
        var sumY = 0.0
        var sumY2 = 0.0
        var sumXY = 0.0

        for (p in data) {
            sumX += p.first
            sumX2 += p.first.pow(2)
            sumY += p.second
            sumY2 += p.second.pow(2)
            sumXY += p.first * p.second
        }

        val meanX = sumX / n
        val meanY = sumY / n
        
        val minX = data.minOf { it.first }
        val maxX = data.maxOf { it.first }

        val map = mutableMapOf<String, String>()
        map["N (Size)"] = n.toString()
        map["Σx (Sum x)"] = formatDouble(sumX)
        map["Σx² (Sum x²)"] = formatDouble(sumX2)
        map["Mean x"] = formatDouble(meanX)
        map["Min x"] = formatDouble(minX)
        map["Max x"] = formatDouble(maxX)

        if (n > 1) {
            val varianceXSample = (sumX2 - (sumX.pow(2) / n)) / (n - 1)
            val sdXSample = sqrt(max(0.0, varianceXSample))
            map["Sample SD (sx)"] = formatDouble(sdXSample)

            val varianceXPop = (sumX2 - (sumX.pow(2) / n)) / n
            val sdXPop = sqrt(max(0.0, varianceXPop))
            map["Population SD (σx)"] = formatDouble(sdXPop)
        }

        // If regression variables are active (any y input are non-zero)
        val hasYData = data.any { it.second != 0.0 }
        if (hasYData && n > 1) {
            map["Σy (Sum y)"] = formatDouble(sumY)
            map["Σy² (Sum y²)"] = formatDouble(sumY2)
            map["Σxy (Sum xy)"] = formatDouble(sumXY)
            map["Mean y"] = formatDouble(meanY)

            // Linear Reg: y = a + bx
            val d = n * sumX2 - sumX.pow(2)
            if (abs(d) > 1e-12) {
                val b = (n * sumXY - sumX * sumY) / d
                val a = (sumY - b * sumX) / n
                map["Intercept (a)"] = formatDouble(a)
                map["Slope (b)"] = formatDouble(b)

                // Correlation Coefficient (r)
                val denomCorr = sqrt((n * sumX2 - sumX.pow(2)) * (n * sumY2 - sumY.pow(2)))
                if (denomCorr > 1e-12) {
                    val r = (n * sumXY - sumX * sumY) / denomCorr
                    map["Correlation (r)"] = formatDouble(r)
                }
            }
        }

        statSummaryResult = map
    }

    // --- REPLAY/HISTORY LOGIC ---
    fun selectHistoryItem(item: HistoryItem) {
        expression = item.expression
        cursorPosition = item.expression.length
        resultDisplay = item.result
        decimalDisplayState = ""
        isShowingFraction = false
        activeError = null
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyDao.clearHistory()
        }
    }
}

// Factories for Android ViewModel instantiation
class CalculatorViewModelFactory(
    private val historyDao: HistoryDao,
    private val variableDao: VariableDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalculatorViewModel(historyDao, variableDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
