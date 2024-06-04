package com.example.demo5

import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.Slider

class HelloController {
    @FXML
    private lateinit var numberInput: TextField
    @FXML
    private lateinit var originalBaseInput: TextField
    @FXML
    private lateinit var destinationBaseInput: TextField
    @FXML
    private lateinit var resultLabel: Label
    @FXML
    private lateinit var fractionalDigitsSlider: Slider

    @FXML
    private fun handleConvertAction() {
        val number = numberInput.text
        val originalBase = originalBaseInput.text.toIntOrNull() ?: return
        val destinationBase = destinationBaseInput.text.toIntOrNull() ?: return

        // Add here the conversion logic from your CLI version, for example:
        val result = convertBase(number, originalBase, destinationBase)
        resultLabel.text = "Result: $result"
    }

    private fun convertBase(number: String, originalBase: Int, destinationBase: Int): String {
        // Conversion logic here
        // Return the converted number as String
        return "Converted Number"
    }
}
