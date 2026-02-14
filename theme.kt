package com.example.smarttipmanager.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun SmartTipTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {

    val colors = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF81C784),
            background = Color(0xFF121212)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF4CAF50),
            background = Color(0xFFF5F5F5)
        )
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
