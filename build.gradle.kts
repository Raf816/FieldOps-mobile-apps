// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
    // OWASP Dependency-Check — scans dependencies for known vulnerabilities (Lecture 11)
    // Run: ./gradlew dependencyCheckAnalyze (generates HTML report in build/reports/)
    id("org.owasp.dependencycheck") version "12.1.1"
}
