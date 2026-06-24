import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(projects.sharedUI)
    implementation(projects.sharedLogic)
    implementation(libs.koin.android)
    implementation(libs.androidx.work.runtime)
    implementation(libs.kotlinx.datetime)

    implementation(libs.androidx.activity.compose)
    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.koin.core)
}

android {
    namespace = "com.bose.hydrohabit"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.bose.hydrohabit"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}