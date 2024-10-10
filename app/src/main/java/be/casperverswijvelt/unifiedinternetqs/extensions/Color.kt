package be.casperverswijvelt.unifiedinternetqs.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

fun Color.contrastColor(): Color {
    // Calculate the luminance of the color
    return if (this.luminance() > 0.5) {
        Color.Black // Use black for light colors
    } else {
        Color.White // Use white for dark colors
    }
}