import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
    id("org.jetbrains.kotlinx.kover")
    jacoco
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

    buildTypes {
        getByName("debug") {
            enableAndroidTestCoverage = true
            enableUnitTestCoverage = true
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kover {
    reports {
        filters {
            excludes {
                // Exclude UI, Navigation, and Entry point classes from coverage
                // as they typically require UI tests (Kaspresso/Espresso) rather than unit tests.
                classes(
                    "*.ui.*",
                    "com.github.lucaengel.packpilot.App*",
                    "com.github.lucaengel.packpilot.BackHandler*",
                    "com.github.lucaengel.packpilot.Screen*",
                )
            }
        }
        verify {
            rule {
                bound {
                    // Set threshold to 75% focused on business logic
                    minValue.set(60)
                }
            }
        }
    }
}

tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    // If you don't have instrumentation tests in composeApp yet,
    // you might want to remove "createDebugCoverageReport" until you add them.
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter =
        listOf(
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*",
            "android/**/*.*",
        )

    val debugTree =
        fileTree("${layout.buildDirectory.get()}/intermediates/javac/debug/classes") {
            exclude(fileFilter)
        }
    // KMP Kotlin classes are stored in a different location
    val kotlinTree =
        fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
            exclude(fileFilter)
        }

    sourceDirectories.setFrom(
        files(
            "${project.projectDir}/src/commonMain/kotlin",
            "${project.projectDir}/src/androidMain/kotlin",
        ),
    )
    classDirectories.setFrom(files(debugTree, kotlinTree))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "outputs/code_coverage/debugAndroidTest/connected/*/coverage.ec",
            )
        },
    )
}

tasks.register<Copy>("copyEmmaReport") {
    from(layout.buildDirectory.dir("outputs/code_coverage/debugAndroidTest/connected/Pixel_2_API_33(AVD) - 13/"))
    include("coverage.ec")
    into(layout.buildDirectory.dir("outputs/code_coverage/debugAndroidTest/connected/"))
    rename { it.replace(".ec", ".exec") }
}

tasks.named("connectedCheck") {
    finalizedBy("jacocoTestReport")
}

tasks.register("ideaCoverage") {
    dependsOn("connectedCheck")
    finalizedBy("copyEmmaReport")
}
