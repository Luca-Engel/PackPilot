plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
    id("org.jetbrains.kotlinx.kover")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.kotlinx.datetime)
                implementation(libs.navigation.compose)
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.turbine)
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
            }
        }
    }
}

// Use explicit LibraryExtension to avoid the deprecated android {} block in AGP 9.0+
extensions.configure<com.android.build.api.dsl.LibraryExtension> {
    namespace = "com.github.lucaengel.packpilot"
    compileSdk = 36
    defaultConfig {
        minSdk = 23
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kover {
    reports {
        verify {
            rule {
                bound {
                    // In Kover 0.9.x, minValue is a Property<Int>
                    minValue.set(80)
                }
            }
        }
    }
}
