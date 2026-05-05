// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.sonarqube)
}

sonarqube {
    properties {
        property("sonar.projectKey", "itachi1706_CheesecakeAppUpdater")
        property("sonar.organization", "itachi1706")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.androidLint.reportPaths", "app/build/reports/lint-results-debug.xml,appupdater/build/reports/lint-results-debug.xml")
        property("sonar.projectVersion", project(":appupdater").ext.get("version") ?: "1.0")
        property("sonar.tests", "src/test/java,src/test/kotlin")
        property("sonar.coverage.jacoco.xmlReportPaths", "**/build/reports/coverage/test/debug/report.xml")
        property("sonar.java.binaries", "**/build/intermediates/compile_app_classes_jar/debug/bundleDebugClassesToCompileJar/classes.jar,**/build/intermediates/compile_library_classes_jar/debug/bundleLibCompileToJarDebug/classes.jar")
    }
}
