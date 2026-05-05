import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sonarqube)
}

ext.set("version", "4.2.0")
ext.set("versionCode", 701)
// we increment by 1 until commit count past the versionCode. 4.1.1 had a wrong code

android {
    namespace = "com.itachi1706.appupdater"
    compileSdk = 36

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildTypes {
        getByName("debug") {
            enableUnitTestCoverage = true
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.appcompat)
    implementation(libs.core.ktx)
    implementation(libs.preference.ktx)
    implementation(libs.material)
    implementation(libs.browser)
    implementation(libs.kotlinx.serialization.json)
    api(libs.helperlib)
}

sonarqube {
    properties {
        // Manually provide compiled classes paths to satisfy the JavaSensor
        // These paths cover both Java and Kotlin compiled outputs for the debug variant
        property("sonar.android.variant", "debug")

//        property("sonar.java.binaries", "build/intermediates/javac/debug/compileDebugJavaWithJavac/classes,build/tmp/kotlin-classes/debug")
        property("sonar.java.binaries", "build")

        // Ensure sources and tests are indexed correctly
//        property("sonar.sources", "src/main/java")
//        property("sonar.tests", "src/test/java")
    }
}

apply(from = "./publish.gradle")
