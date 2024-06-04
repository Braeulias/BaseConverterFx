package com.example.demo5

import javafx.application.Application
import javafx.beans.value.ChangeListener
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.math.BigInteger
import kotlin.math.pow
import javafx.scene.text.Text
import javafx.scene.text.TextFlow


class HelloApplication : Application() {
    override fun start(primaryStage: Stage) {
        primaryStage.title = "Base Converter"

        // Create UI components
        val numberInput = TextField()
        numberInput.promptText = "Enter number"
        styleTextField(numberInput)

        val originalBaseInput = TextField()
        originalBaseInput.promptText = "Original base"
        styleTextField(originalBaseInput)

        val destinationBaseInput = TextField()
        destinationBaseInput.promptText = "Destination base"
        styleTextField(destinationBaseInput)

        val fractionalDigitsSlider = Slider(0.0, 10000.0, 5000.0)
        fractionalDigitsSlider.isShowTickLabels = true
        fractionalDigitsSlider.isShowTickMarks = true
        fractionalDigitsSlider.majorTickUnit = 1000.0
        fractionalDigitsSlider.blockIncrement = 1.0
        styleSlider(fractionalDigitsSlider)

        val fractionalDigitsLabel = Label("Fractional digits: ${fractionalDigitsSlider.value.toInt()}")
        fractionalDigitsSlider.valueProperty().addListener { _, _, newValue ->
            fractionalDigitsLabel.text = "Fractional digits: ${newValue.toInt()}"
        }
        styleLabel(fractionalDigitsLabel)

        val convertButton = Button("Convert")
        styleButton(convertButton)

        val resultLabel = TextFlow()
        styleResultLabel(resultLabel)


        // Layout
        val layout = VBox(10.0) // 10px spacing
        layout.style = "-fx-padding: 20; -fx-background-color: #333;"
        layout.children.addAll(
            numberInput, originalBaseInput, destinationBaseInput,
            fractionalDigitsSlider, fractionalDigitsLabel, convertButton, resultLabel
        )

        convertButton.setOnAction { convert(resultLabel, numberInput, originalBaseInput, destinationBaseInput, fractionalDigitsSlider) }
        // Set scene and stage
        val scene = Scene(layout, 300.0, 350.0)
        primaryStage.scene = scene
        primaryStage.show()
    }

    private fun styleTextField(textField: TextField) {
        textField.style = "-fx-background-color: #555; -fx-text-fill: white; -fx-padding: 5;"
        textField.focusedProperty().addListener { _, _, isFocused ->
            if (isFocused) {
                textField.style = "-fx-background-color: #555; -fx-text-fill: white; -fx-padding: 5; -fx-border-color: #6a00f4; -fx-border-width: 2; -fx-border-radius: 5;"
            } else {
                textField.style = "-fx-background-color: #555; -fx-text-fill: white; -fx-padding: 5;"
            }
        }
    }

    private fun styleSlider(slider: Slider) {
        slider.style = """
            -fx-control-inner-background: #555;
            -fx-tick-label-fill: white;
            -fx-font-size: 12px;
        """.trimIndent()
    }

    private fun styleButton(button: Button) {
        button.style = "-fx-background-color: #555; -fx-text-fill: white;"
        button.addEventHandler(MouseEvent.MOUSE_ENTERED) { button.style = "-fx-background-color: #6a00f4; -fx-text-fill: white;" }
        button.addEventHandler(MouseEvent.MOUSE_EXITED) { button.style = "-fx-background-color: #555; -fx-text-fill: white;" }
    }

    private fun styleLabel(label: Label) {
        label.style = "-fx-text-fill: white; -fx-font-size: 12px;"
    }

    private fun styleResultLabel(textFlow: TextFlow) {
        textFlow.style = "-fx-text-fill: white; -fx-font-size: 14px;"
    }

}

fun main() {
    Application.launch(HelloApplication::class.java)
}

fun convert(result: TextFlow, input: TextField, base1: TextField, base2: TextField, slider: Slider){
    val number = input.text.trim()
    val originalBase =  base1.text.toIntOrNull() ?: return
    val destinationBase = base2.text.toIntOrNull() ?: return
    val fractionals = slider.value.toInt()
    val (integral, fractional) = number.split('.')
    val integralResult = integralConv(integral.replace(':', ' '), originalBase, destinationBase)
    val fractionalResult = if (fractional.contains("(")) {
        fractionalConvRepeating(fractional, originalBase, destinationBase, fractionals)
    } else {
        fractionalConvSimple(
            fractional.replace(':', ' '),
            originalBase,
            destinationBase,
            fractionals
        )
    }
    val resultText = "Result in Base $destinationBase: $integralResult.$fractionalResult"
    val chunkSize = 1000 // Define the chunk size based on your requirement
    result.children.clear()
    resultText.chunked(chunkSize).forEach { chunk ->
        val text = Text(chunk)
        text.style = "-fx-fill: white;"
        result.children.add(text)
        result.children.add(Text("\n"))
    }
}



fun test() {
    println("Enter the Number you want to convert (with . between integral and fractional part and : between digits also write digits in decimal; if no fraction enter .0)")
    val input = readLine()!!.filterNot { it.isWhitespace() }
    val (integral, fractional) = input.split('.')

    println("Now enter from what base to what base it should be converted (use -> to separate)")
    val (base1, base2) = readLine()!!.filterNot { it.isWhitespace() }.split("->").map(String::toInt)

    println("Enter the number of fractional digits you want in the output:")
    val fractionalDigits = readLine()!!.toInt()

    val integralResult = integralConv(integral.replace(':', ' '), base1, base2)
    val fractionalResult = if (fractional.contains("(")) {
        fractionalConvRepeating(fractional, base1, base2, fractionalDigits)
    } else {
        fractionalConvSimple(
            fractional.replace(':', ' '),
            base1,
            base2,
            fractionalDigits
        )
    }
    println("Result in Base $base2: $integralResult.$fractionalResult")

}

fun integralConv(input: String, base1: Int, base2: Int): String {
    if (input == "0") {
        return "0"
    }
    val parts: List<String> = input.split(" ")
    val decimal = convertToBase10(parts, base1)
    return convertFromBase10(decimal, base2)
}

fun fractionalConvSimple(input: String, base1: Int, base2: Int, fractionalPositions: Int): String {
    if (input.isEmpty() || input == "0") return "0"
    val parts = input.split(" ")
    val decimal = convertToBase10(parts, base1)
    return convertFractionalBase10ToBaseN(decimal, base1, base2, parts.size, fractionalPositions)
}

fun fractionalConvRepeating(input: String, base1: Int, base2: Int, fractionalPositions: Int): String {

    val (numerator, denominator) = convertToFraction(base1, input)
    val numeratorInTargetBase = convertFromBase10_2(numerator, base2)
    val denominatorInTargetBase = convertFromBase10_2(denominator, base2)


    val result = divideInBase(numeratorInTargetBase, denominatorInTargetBase, base2, fractionalPositions)
    return (result.drop(1))
}

fun divideInBase(dividend: String, divisor: String, base: Int, decimalPlaces: Int): String {
    val decDividend = convertToDecimal(dividend, base)
    val decDivisor = convertToDecimal(divisor, base)

    if (decDivisor == 0) {
        throw IllegalArgumentException("Division by zero is not allowed")
    }
    var remainder = decDividend % decDivisor
    var quotient = decDividend / decDivisor
    val integerPart = convertFromDecimal(quotient, base)
    val fractionalParts = mutableListOf<String>()

    for (i in 1..decimalPlaces) {
        remainder *= base
        quotient = remainder / decDivisor
        remainder %= decDivisor
        val fractionalPart = convertFromDecimal(quotient, base)
        fractionalParts.add(if (fractionalPart.isEmpty()) "0" else fractionalPart)
    }
    return if (fractionalParts.isNotEmpty()) "$integerPart:${fractionalParts.joinToString(":")}" else integerPart
}

fun convertToDecimal(baseValue: String, base: Int): Int {
    val parts = baseValue.split(':')
    return parts.reversed().mapIndexed { index, part ->
        part.toInt() * base.toDouble().pow(index).toInt()
    }.sum()
}

fun convertFromDecimal(number: Int, base: Int): String {
    var num = number
    val digits = mutableListOf<Int>()
    while (num > 0) {
        digits.add(num % base)
        num /= base
    }
    return digits.reversed().joinToString(":")
}



fun convertToFraction(base: Int, combinedInput: String): Pair<BigInteger, BigInteger> {
    val regex = Regex("""(?:(\d+):)?\(([\d:]+)\)""")
    val matchResult = regex.find(combinedInput)
    if (matchResult == null) {
        throw IllegalArgumentException("Input format is incorrect")
    }

    val nonRepeatingPart = matchResult.groupValues[1].ifEmpty { "0" }
    val repeatingPart = matchResult.groupValues[2]

    val repeatLengthBase = repeatingPart.split(":").size
    val basePowerRepeatLengthBase = BigInteger.valueOf(base.toLong()).pow(repeatLengthBase)
    val basePowerRepeating = basePowerRepeatLengthBase - BigInteger.ONE

    val nonRepeatingDigits = nonRepeatingPart.split(":").map { it.toInt() }
    val nonRepeatingValue = nonRepeatingDigits.fold(BigInteger.ZERO) { acc, digit -> acc * BigInteger.valueOf(base.toLong()) + BigInteger.valueOf(digit.toLong()) }
    val nonRepeatingDenominator = BigInteger.valueOf(base.toLong()).pow(nonRepeatingDigits.size)

    val repeatingDigits = repeatingPart.split(":").map { it.toInt() }
    val repeatingValue = repeatingDigits.fold(BigInteger.ZERO) { acc, digit -> acc * BigInteger.valueOf(base.toLong()) + BigInteger.valueOf(digit.toLong()) }
    val repeatingDenominator = BigInteger.valueOf(base.toLong()).pow(repeatLengthBase) - BigInteger.ONE

    val nonRepeatingFractionNumerator = nonRepeatingValue * repeatingDenominator
    val repeatingFractionNumerator = repeatingValue
    val totalDenominator = nonRepeatingDenominator * basePowerRepeating
    val totalNumerator = nonRepeatingFractionNumerator + repeatingFractionNumerator
    val gcdValue = gcd(totalNumerator, totalDenominator)

    return Pair(totalNumerator / gcdValue, totalDenominator / gcdValue)
}

fun gcd(a: BigInteger, b: BigInteger): BigInteger {
    var x = a
    var y = b
    while (y != BigInteger.ZERO) {
        val temp = y
        y = x % y
        x = temp
    }
    return x
}



fun convertToBase10(parts: List<String>, fromBase: Int): BigInteger {
    val base = BigInteger.valueOf(fromBase.toLong())
    return parts
        .reversed()
        .mapIndexed { i, part ->
            val digit = part.toBigInteger()
            digit * base.pow(i)
        }
        .fold(BigInteger.ZERO, BigInteger::add)
}

fun convertFromBase10(decimal: BigInteger, toBase: Int): String {
    var result = mutableListOf<String>()
    var current = decimal
    while (current > BigInteger.ZERO) {
        result.add((current.mod(toBase.toBigInteger())).toString(toBase))
        current = current.divide(toBase.toBigInteger())
    }
    return result.reversed().joinToString(":")
}

fun convertFromBase10_2(decimal: BigInteger, toBase: Int): String {
    if (decimal == BigInteger.ZERO) return "0"
    var current = decimal
    val result = mutableListOf<String>()
    while (current > BigInteger.ZERO) {
        result.add(current.mod(BigInteger.valueOf(toBase.toLong())).toString())
        current = current.divide(BigInteger.valueOf(toBase.toLong()))
    }
    return result.reversed().joinToString(":")
}



fun convertFractionalBase10ToBaseN(decimal: BigInteger, base1: Int, base2: Int, digits: Int, fractionalPositions: Int): String {
    val baseBI = BigInteger.valueOf(base2.toLong())
    var fraction = decimal
    var result = mutableListOf<String>()
    for (i in 1..fractionalPositions) {
        fraction *= baseBI
        val digit = fraction / BigInteger.valueOf(base1.toLong()).pow(digits)
        result.add(digit.toString(base2))
        fraction -= digit * BigInteger.valueOf(base1.toLong()).pow(digits)
    }
    return result.joinToString(":")
}











