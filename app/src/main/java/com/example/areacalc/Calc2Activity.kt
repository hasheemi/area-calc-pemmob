package com.example.areacalc

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.pow

class Calc2Activity : AppCompatActivity() {

    private lateinit var tvDisplay: TextView
    private lateinit var tvExpression: TextView

    private var currentInput = "0"
    private var firstOperand: Double? = null
    private var pendingOperator: String? = null
    private var isNewInput = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calc2)

        tvDisplay = findViewById(R.id.tvDisplay)
        tvExpression = findViewById(R.id.tvExpression)
    }

    fun onDigitClick(view: View) {
        val number = (view as Button).text.toString()
        onNumberClick(number)
    }

    fun onOperatorClick(view: View) {
        val operator = (view as Button).text.toString()
        handleOperator(operator)
    }

    fun onTitikClick(view: View) {
        if (isNewInput) {
            currentInput = "0."
            isNewInput = false
        } else if (!currentInput.contains(".")) {
            currentInput += "."
        }
        updateDisplay()
    }

    fun onSamaDenganClick(view: View) {
        calculateResult()
    }

    fun onClearClick(view: View) {
        currentInput = "0"
        firstOperand = null
        pendingOperator = null
        isNewInput = true
        tvExpression.text = ""
        updateDisplay()
    }

    fun onSwapClick(view: View) {
        if (currentInput != "0" && !currentInput.startsWith("-")) {
            currentInput = "-$currentInput"
        } else if (currentInput.startsWith("-")) {
            currentInput = currentInput.substring(1)
        }
        updateDisplay()
    }

    /** Tombol % (dipisah dari operator biasa) → android:onClick="onModClick" */
    fun onModClick(view: View) {
        handleOperator("%")
    }

    /** Tombol ^ → android:onClick="onPangkatClick" */
    fun onPangkatClick(view: View) {
        handleOperator("^")
    }

    // ================= LOGIKA INTERNAL =================

    private fun onNumberClick(number: String) {
        if (isNewInput) {
            currentInput = number
            isNewInput = false
        } else {
            if (currentInput == "0") {
                currentInput = number
            } else {
                currentInput += number
            }
        }
        updateDisplay()
    }

    private fun handleOperator(operator: String) {
        val inputValue = currentInput.toDoubleOrNull() ?: return

        // Chained operation: jika sudah ada operator tertunda & bukan input baru
        if (pendingOperator != null && !isNewInput) {
            calculateResult(keepOperator = false)
        }

        firstOperand = inputValue
        pendingOperator = operator
        isNewInput = true

        tvExpression.text = "${formatNumber(firstOperand!!)} $operator"
        updateDisplay()
    }

    private fun calculateResult(keepOperator: Boolean = false) {
        val secondOperand = currentInput.toDoubleOrNull() ?: return
        val first = firstOperand ?: return
        val operator = pendingOperator ?: return

        val result: Double = when (operator) {
            "+" -> first + secondOperand
            "-" -> first - secondOperand
            "×" -> first * secondOperand
            "÷" -> if (secondOperand == 0.0) Double.NaN else first / secondOperand
            "%" -> first % secondOperand
            "^" -> first.pow(secondOperand)
            else -> secondOperand
        }

        tvExpression.text = "${formatNumber(first)} $operator ${formatNumber(secondOperand)} ="

        currentInput = if (result.isNaN()) {
            "Error"
        } else {
            formatNumber(result)
        }

        if (!keepOperator) {
            firstOperand = null
            pendingOperator = null
        } else {
            firstOperand = result
        }
        isNewInput = true
        updateDisplay()
    }

    private fun updateDisplay() {
        tvDisplay.text = currentInput
    }

    private fun formatNumber(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            value.toString()
        }
    }
}