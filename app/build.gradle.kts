plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.Explorer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.Explorer"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.room.runtime)
    implementation(libs.osmdroid.android)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.json)
    implementation(libs.picasso)
    implementation(libs.core)
    implementation(libs.play.services.location)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    annotationProcessor(libs.room.compiler)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}