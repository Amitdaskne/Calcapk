package com.example.engine

import kotlin.math.*

class MathParser(
    private val rawExpression: String,
    private val variables: Map<String, ComplexNumber> = emptyMap(),
    private val useDegrees: Boolean = true,
    private val useGradians: Boolean = false // Degree, Radian, Gradian
) {
    private var pos = -1
    private var ch = ' '
    private lateinit var expr: String

    fun parse(): ComplexNumber {
        expr = preprocess(rawExpression)
        nextChar()
        val x = parseExpression()
        if (pos < expr.length) {
            throw IllegalArgumentException("Unexpected character: '" + ch + "' at position " + pos)
        }
        return x
    }

    private fun preprocess(input: String): String {
        // Normalize characters
        var res = input
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("–", "-")
            .replace("π", "pi")

        // Postfix squares/cubes
        res = res.replace("²", "^2")
        res = res.replace("³", "^3")

        // Replace other custom notations
        res = res.replace("³√", "cbrt")
        res = res.replace("√", "sqrt")

        // Handle variables
        for ((varName, value) in variables) {
            // Replace with formatted string or value wrapper
            // To prevent recursion in variables, we replace the variable token with its numerical form
            val repStr = "(${value.real}${if (value.imag >= 0) "+" else ""}${value.imag}i)"
            res = replaceVariableToken(res, varName, repStr)
        }

        res = replaceVariableToken(res, "pi", "(${Math.PI})")
        res = replaceVariableToken(res, "e", "(${Math.E})")

        // Insert implicit multiplications
        res = insertImplicitMultiplications(res)

        return res
    }

    private fun replaceVariableToken(expr: String, token: String, replacement: String): String {
        val boundary = "[^a-zA-Z0-9_]"
        // Replace token only when not part of another word
        // Since Kotlin's Regex doesn't have lookbehinds/lookaheads on all versions safely,
        // we can perform a simple parser-safe replacement
        val pattern = "\\b$token\\b".toRegex()
        return expr.replace(pattern, replacement)
    }

    private fun insertImplicitMultiplications(input: String): String {
        val sb = StringBuilder()
        val len = input.length
        for (i in 0 until len) {
            val curr = input[i]
            sb.append(curr)
            if (i < len - 1) {
                val next = input[i + 1]
                
                val isCurrNumOrClose = (curr.isDigit() || curr == ')' || curr == 'i' || curr == '²' || curr == '³' || curr == '%')
                // A next element is start of function or parenthesis or variable / pi / e
                val isNextLetterOrOpen = (next == '(' || next.isLetter() || next == '√')
                
                val isCurrCloseAndNextNum = (curr == ')' && next.isDigit())

                if ((isCurrNumOrClose && isNextLetterOrOpen) || isCurrCloseAndNextNum) {
                    sb.append('*')
                }
            }
        }
        return sb.toString()
    }

    private fun nextChar() {
        pos++
        ch = if (pos < expr.length) expr[pos] else '\u0000'
    }

    private fun eat(charToEat: Char): Boolean {
        while (ch == ' ') nextChar()
        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    private fun parseExpression(): ComplexNumber {
        var x = parseTerm()
        while (true) {
            when {
                eat('+') -> x += parseTerm()
                eat('-') -> x -= parseTerm()
                else -> return x
            }
        }
    }

    private fun parseTerm(): ComplexNumber {
        var x = parseFactor()
        while (true) {
            when {
                eat('*') -> x *= parseFactor()
                eat('/') -> x /= parseFactor()
                else -> return x
            }
        }
    }

    private fun parseFactor(): ComplexNumber {
        var x = parseUnary()
        if (eat('^')) {
            val exp = parseFactor() // Right-associative exponentiation
            x = complexPower(x, exp)
        }
        return x
    }

    private fun parseUnary(): ComplexNumber {
        if (eat('+')) return parseUnary()
        if (eat('-')) return parseUnary() * -1.0

        var x: ComplexNumber
        val startPos = this.pos
        
        if (eat('(')) { // Parentheses
            x = parseExpression()
            if (!eat(')')) throw IllegalArgumentException("Missing closed parenthesis")
            x = parsePostfix(x)
        } else if ((ch in '0'..'9') || ch == '.') { // Numbers
            while ((ch in '0'..'9') || ch == '.') nextChar()
            val numStr = expr.substring(startPos, this.pos)
            var doubleVal = numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $numStr")
            x = ComplexNumber(doubleVal, 0.0)
            if (eat('i')) {
                x = ComplexNumber(0.0, doubleVal)
            }
            x = parsePostfix(x)
        } else if (ch == 'i') { // Pure Imaginary literal
            nextChar()
            x = ComplexNumber.I
            x = parsePostfix(x)
        } else if (ch.isLetter()) { // Functions
            while (ch.isLetter() || ch.isDigit() || ch == '_') nextChar()
            val func = expr.substring(startPos, this.pos)

            if (eat('(')) {
                val args = mutableListOf<ComplexNumber>()
                if (ch != ')') {
                    args.add(parseExpression())
                    while (eat(',')) {
                        args.add(parseExpression())
                    }
                }
                if (!eat(')')) throw IllegalArgumentException("Missing closed parenthesis after function: $func")

                x = evaluateFunction(func, args)
                x = parsePostfix(x)
            } else {
                throw IllegalArgumentException("Expected function arguments for '$func'")
            }
        } else {
            throw IllegalArgumentException("Unexpected character: '$ch'")
        }
        return x
    }

    private fun parsePostfix(x: ComplexNumber): ComplexNumber {
        var current = x
        while (true) {
            when {
                eat('%') -> {
                    current *= 0.01
                }
                eat('!') -> {
                    if (current.imag != 0.0) throw IllegalArgumentException("Factorial is only defined for real integers")
                    val n = current.real.roundToInt()
                    if (n < 0) throw IllegalArgumentException("Factorial requires non-negative integers")
                    current = ComplexNumber(factorial(n).toDouble(), 0.0)
                }
                else -> return current
            }
        }
    }

    private fun evaluateFunction(func: String, args: List<ComplexNumber>): ComplexNumber {
        if (args.isEmpty()) throw IllegalArgumentException("Function '$func' requires at least 1 argument")
        val arg = args[0]

        return when (func.lowercase()) {
            "sin" -> complexSin(arg)
            "cos" -> complexCos(arg)
            "tan" -> complexTan(arg)
            "asin" -> {
                if (arg.imag != 0.0) throw IllegalArgumentException("Asin complex inputs not supported in current mode")
                val rad = asin(arg.real)
                ComplexNumber(toOutputAngle(rad), 0.0)
            }
            "acos" -> {
                if (arg.imag != 0.0) throw IllegalArgumentException("Acos complex inputs not supported in current mode")
                val rad = acos(arg.real)
                ComplexNumber(toOutputAngle(rad), 0.0)
            }
            "atan" -> {
                if (arg.imag != 0.0) throw IllegalArgumentException("Atan complex inputs not supported in current mode")
                val rad = atan(arg.real)
                ComplexNumber(toOutputAngle(rad), 0.0)
            }
            "sinh" -> complexSinh(arg)
            "cosh" -> complexCosh(arg)
            "tanh" -> complexTanh(arg)
            "ln" -> complexLn(arg)
            "log" -> {
                // If 2 arguments, log(base, value). Else standard log base 10.
                if (args.size == 2) {
                    val base = args[0]
                    val value = args[1]
                    complexLn(value) / complexLn(base)
                } else {
                    complexLn(arg) / complexLn(ComplexNumber(10.0, 0.0))
                }
            }
            "logbase" -> {
                if (args.size != 2) throw IllegalArgumentException("logBase requires exactly 2 arguments: (base, value)")
                val base = args[0]
                val value = args[1]
                complexLn(value) / complexLn(base)
            }
            "sqrt" -> complexSqrt(arg)
            "cbrt" -> {
                if (arg.imag != 0.0) throw IllegalArgumentException("Cube root is only supported for real numbers")
                ComplexNumber(Math.signum(arg.real) * abs(arg.real).pow(1.0/3.0), 0.0)
            }
            "npr" -> {
                if (args.size != 2) throw IllegalArgumentException("nPr requires exactly 2 arguments (n, r)")
                val n = args[0].real.roundToInt()
                val r = args[1].real.roundToInt()
                if (n < 0 || r < 0 || r > n) throw IllegalArgumentException("Invalid arguments for nPr($n, $r)")
                val res = factorial(n) / factorial(n - r)
                ComplexNumber(res.toDouble(), 0.0)
            }
            "ncr" -> {
                if (args.size != 2) throw IllegalArgumentException("nCr requires exactly 2 arguments (n, r)")
                val n = args[0].real.roundToInt()
                val r = args[1].real.roundToInt()
                if (n < 0 || r < 0 || r > n) throw IllegalArgumentException("Invalid arguments for nCr($n, $r)")
                val res = factorial(n) / (factorial(r) * factorial(n - r))
                ComplexNumber(res.toDouble(), 0.0)
            }
            "abs" -> ComplexNumber(arg.magnitude(), 0.0)
            "arg" -> ComplexNumber(arg.argument(!useDegrees && !useGradians), 0.0) // argument returns degrees based on param
            "conj" -> arg.conjugate()
            "real" -> ComplexNumber(arg.real, 0.0)
            "imag" -> ComplexNumber(arg.imag, 0.0)
            else -> throw IllegalArgumentException("Unknown function: $func")
        }
    }

    private fun toInputAngle(raw: Double): Double {
        return when {
            useDegrees -> Math.toRadians(raw)
            useGradians -> raw * (Math.PI / 200.0)
            else -> raw // Radians
        }
    }

    private fun toOutputAngle(rad: Double): Double {
        return when {
            useDegrees -> Math.toDegrees(rad)
            useGradians -> rad * (200.0 / Math.PI)
            else -> rad // Radians
        }
    }

    // Mathematical approximations of complex trig

    private fun complexSin(z: ComplexNumber): ComplexNumber {
        val x = toInputAngle(z.real)
        val y = z.imag
        // sin(x + iy) = sin(x)cosh(y) + i cos(x)sinh(y)
        return ComplexNumber(sin(x) * cosh(y), cos(x) * sinh(y))
    }

    private fun complexCos(z: ComplexNumber): ComplexNumber {
        val x = toInputAngle(z.real)
        val y = z.imag
        // cos(x + iy) = cos(x)cosh(y) - i sin(x)sinh(y)
        return ComplexNumber(cos(x) * cosh(y), -sin(x) * sinh(y))
    }

    private fun complexTan(z: ComplexNumber): ComplexNumber {
        return complexSin(z) / complexCos(z)
    }

    private fun complexSinh(z: ComplexNumber): ComplexNumber {
        // sinh(x + iy) = sinh(x)cos(y) + i cosh(x)sin(y)
        return ComplexNumber(sinh(z.real) * cos(z.imag), cosh(z.real) * sin(z.imag))
    }

    private fun complexCosh(z: ComplexNumber): ComplexNumber {
        // cosh(x + iy) = cosh(x)cos(y) + i sinh(x)sin(y)
        return ComplexNumber(cosh(z.real) * cos(z.imag), sinh(z.real) * sin(z.imag))
    }

    private fun complexTanh(z: ComplexNumber): ComplexNumber {
        return complexSinh(z) / complexCosh(z)
    }

    private fun complexLn(z: ComplexNumber): ComplexNumber {
        // ln(r * e^iθ) = ln(r) + iθ
        val r = z.magnitude()
        if (r == 0.0) throw ArithmeticException("Logarithm of zero")
        val theta = atan2(z.imag, z.real)
        return ComplexNumber(ln(r), theta)
    }

    private fun complexSqrt(z: ComplexNumber): ComplexNumber {
        // sqrt(r * e^iθ) = sqrt(r) * e^(iθ/2)
        val r = z.magnitude()
        val theta = atan2(z.imag, z.real)
        return ComplexNumber(sqrt(r) * cos(theta / 2.0), sqrt(r) * sin(theta / 2.0))
    }

    private fun complexPower(base: ComplexNumber, exp: ComplexNumber): ComplexNumber {
        if (base.real == 0.0 && base.imag == 0.0) {
            return if (exp.real == 0.0 && exp.imag == 0.0) ComplexNumber.ONE else ComplexNumber.ZERO
        }
        // base ^ exp = e ^ (exp * ln(base))
        val lnBase = complexLn(base)
        val term = exp * lnBase
        val r = exp(term.real)
        return ComplexNumber(r * cos(term.imag), r * sin(term.imag))
    }

    private fun factorial(n: Int): Long {
        if (n < 0 || n > 20) {
            // Factorial up to 20 is exact in Long, let's Approximate using Gamma / double for larger numbers up to 69
            if (n >= 0 && n <= 69) {
                var res = 1.0
                for (i in 1..n) res *= i
                return res.toLong()
            }
            throw IllegalArgumentException("Factorial out of range (requires 0 to 69)")
        }
        var res = 1L
        for (i in 1..n) res *= i
        return res
    }
}
