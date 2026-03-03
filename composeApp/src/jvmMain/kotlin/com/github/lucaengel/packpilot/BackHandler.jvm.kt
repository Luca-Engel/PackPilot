package com.github.lucaengel.packpilot

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op for JVM/Desktop
}
