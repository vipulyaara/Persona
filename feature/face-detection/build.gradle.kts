plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "app.persona.feature.face.detection"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(projects.ui.media.detection)
    implementation(projects.ui.components)
    implementation(projects.ui.theme)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.material3)

    implementation(libs.accompanist.permissions)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel)

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
