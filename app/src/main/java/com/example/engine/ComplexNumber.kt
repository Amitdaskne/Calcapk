package com.example.engine

import kotlin.math.*

data class ComplexNumber(val real: Double, val imag: Double = 0.0) {
    
    operator fun plus(other: ComplexNumber) = ComplexNumber(real + other.real, imag + other.imag)
    operator fun plus(other: Double) = ComplexNumber(real + other, imag)
    
    operator fun minus(other: ComplexNumber) = ComplexNumber(real - other.real, imag - other.imag)
    operator fun minus(other: Double) = ComplexNumber(real - other, imag)
    
    operator fun times(other: ComplexNumber) = ComplexNumber(
        real * other.real - imag * other.imag,
        real * other.imag + imag * other.real
    )
    operator fun times(other: Double) = ComplexNumber(real * other, imag * other)
    
    operator fun div(other: ComplexNumber): ComplexNumber {
        val denom = other.real * other.real + other.imag * other.imag
        if (denom == 0.0) throw ArithmeticException("Division by zero in complex calculation")
        return ComplexNumber(
            (real * other.real + imag * other.imag) / denom,
            (imag * other.real - real * other.imag) / denom
        )
    }
    operator fun div(other: Double): ComplexNumber {
        if (other == 0.0) throw ArithmeticException("Division by zero")
        return ComplexNumber(real / other, imag / other)
    }

    fun conjugate() = ComplexNumber(real, -imag)

    fun magnitude() = sqrt(real * real + imag * imag)

    fun argument(useDegrees: Boolean = true): Double {
        val rad = atan2(imag, real)
        return if (useDegrees) Math.toDegrees(rad) else rad
    }

    override fun toString(): String {
        return when {
            imag == 0.0 -> formatDouble(real)
            real == 0.0 -> "${formatDouble(imag)}i"
            imag < 0.0 -> "${formatDouble(real)} - ${formatDouble(-imag)}i"
            else -> "${formatDouble(real)} + ${formatDouble(imag)}i"
        }
    }

    fun toPolarString(useDegrees: Boolean = true): String {
        val r = magnitude()
        val theta = argument(useDegrees)
        return "${formatDouble(r)}∠${formatDouble(theta)}°"
    }

    companion object {
        val ZERO = ComplexNumber(0.0, 0.0)
        val ONE = ComplexNumber(1.0, 0.0)
        val I = ComplexNumber(0.0, 1.0)

        private fun formatDouble(d: Double): String {
            if (d.isNaN()) return "NaN"
            if (d.isInfinite()) return if (d < 0) "-∞" else "∞"
            // clean formatting
            var s = String.format("%.8f", d)
            s = s.replace(Regex("0+$"), "")
            s = s.replace(Regex("\\.$"), "")
            if (s == "-0") s = "0"
            return s
        }

        fun fromPolar(r: Double, thetaRad: Double) = ComplexNumber(r * cos(thetaRad), r * sin(thetaRad))
    }
}
