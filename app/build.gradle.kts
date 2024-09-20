plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id ("kotlin-kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.fp.funny.video.call"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.fp.funny.video.call"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures{
        buildConfig = true
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.config)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.sdp.android)
    implementation (libs.ssp.android)
    implementation (libs.lottie)
    implementation (libs.androidx.viewpager2)
    implementation (libs.androidx.fragment.ktx)
    implementation (libs.androidx.room.runtime)
    //noinspection KaptUsageInsteadOfKsp
    kapt (libs.androidx.room.compiler)
    implementation (libs.androidx.lifecycle.runtime.ktx)

    // Kotlin Coroutines
    implementation (libs.kotlinx.coroutines.core)
    implementation (libs.kotlinx.coroutines.android)
    implementation (libs.androidx.room.ktx)
    implementation (libs.androidx.camera.core)
    implementation (libs.androidx.camera.camera2)
    implementation (libs.androidx.camera.lifecycle)
    implementation (libs.androidx.camera.extensions)
    implementation (libs.androidx.camera.view)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.exoplayer)
    implementation (libs.glide.v4110)
    //noinspection KaptUsageInsteadOfKsp
    kapt (libs.compiler.v4110)
    // Annotation processor for Glide
    //noinspection KaptUsageInsteadOfKsp
    kapt (libs.compiler)
    implementation (libs.okhttp)
    implementation (libs.gson)
    implementation (libs.exoplayer.core.v2183)
    implementation (libs.exoplayer.ui.v2183)
    implementation (libs.ambilwarna)
    implementation (libs.shimmer)
    implementation(platform(libs.firebase.bom))
    implementation(libs.play.services.ads)
    implementation(libs.androidx.lifecycle.process)
    implementation (libs.billing)
    implementation (libs.listenablefuture)
    implementation (libs.guava)

}