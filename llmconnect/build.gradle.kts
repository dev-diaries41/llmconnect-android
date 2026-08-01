plugins {
    alias(libs.plugins.android.library)
    kotlin("plugin.serialization") version "2.0.21"
    id("maven-publish")
}

android {
    namespace = "com.fpflabs.llmconnect"
    compileSdk {
        version = release(37) {
        }
    }

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    publishing {
        singleVariant("release")
    }
}

dependencies {
    // Requests
    implementation(libs.okhttp)

    // Serelization
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

}

val gitVersion = providers.exec {
    commandLine("git", "describe", "--tags", "--abbrev=0")
    isIgnoreExitValue = true
}.standardOutput
    .asText
    .map { output ->
        output.trim()
            .removePrefix("v")
            .ifBlank { "1.0.0" }
    }
    .orElse("1.0.0")



publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.devdiaries41.llmconnect-android"
            artifactId = project.name
            version = gitVersion.get()

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}