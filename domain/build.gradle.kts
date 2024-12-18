plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "app.persona.domain"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

}
