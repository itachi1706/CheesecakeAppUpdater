plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jacoco)
}

ext.set("version", "4.1.1")
ext.set("versionCode", 700)

android {
    namespace = "com.itachi1706.appupdater"
    compileSdk = 36

    defaultConfig {
        minSdk = 21

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
    kotlinOptions {
        jvmTarget = "17"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }

//    jacoco {
//        version = "0.8.8"
//    }
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

//tasks.register<JacocoReport>("jacocoTestReport") {
//    dependsOn("testDebugUnitTest")
//    group = "verification"
//    description = "Generate Jacoco coverage reports for the debug build."
//
//    reports {
//        xml.required.set(true)
//        html.required.set(true)
//    }
//
//    val fileFilter = listOf(
//        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
//        "**/*Test*.*", "android/**/*.*"
//    )
//
//    val buildDir = layout.buildDirectory.get().asFile.path
//
//    val debugTree = fileTree("$buildDir/intermediates/javac/debug/classes") {
//        exclude(fileFilter)
//    }
//    val kotlinDebugTree = fileTree("$buildDir/tmp/kotlin-classes/debug") {
//        exclude(fileFilter)
//    }
//
//    val mainSrc = "$projectDir/src/main/java"
//    val kotlinSrc = "$projectDir/src/main/kotlin"
//
//    sourceDirectories.setFrom(files(mainSrc, kotlinSrc))
//    classDirectories.setFrom(files(debugTree, kotlinDebugTree))
//
//    executionData.setFrom(
//        fileTree(buildDir) {
//            include(
//                "jacoco/testDebugUnitTest.exec",
//                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"
//            )
//        }
//    )
//
//}

apply(from = "./publish.gradle")
