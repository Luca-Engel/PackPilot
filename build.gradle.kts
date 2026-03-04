plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.7" apply false
}
