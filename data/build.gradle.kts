plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "app.persona.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.hilt.android)

    implementation(libs.mediapipe.face.detection) {
        exclude(group = "com.google.mediapipe", module = "solution-core")
    }
    implementation(libs.mediapipe.tasks.core) {
        exclude(group = "com.google.mediapipe", module = "solution-core")
    }
    implementation(libs.mediapipe.tasks.vision) {
        exclude(group = "com.google.mediapipe", module = "solution-core")
        exclude(group = "com.google.mediapipe", module = "tasks-core")
    }
}
