<<<<<<< HEAD
//plugins {
//    alias(libs.plugins.android.application)
//    alias(libs.plugins.jetbrains.kotlin.android)
//
//}
//
//android {
//    namespace = "com.example.journalapp"
//    compileSdk = 34
//
//    defaultConfig {
//        applicationId = "com.example.journalapp"
//        minSdk = 24
//        targetSdk = 34
//        versionCode = 1
//        versionName = "1.0"
//
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//    }
//
//    buildTypes {
//        release {
//            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//        }
//    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_1_8
//        targetCompatibility = JavaVersion.VERSION_1_8
//    }
//
//    composeOptions {
//        kotlinCompilerExtensionVersion = "1.5.4"
//    }
//
//    kotlinOptions {
//        jvmTarget = "1.8"
//    }
//
//
//    buildFeatures {
//        viewBinding = true
//        compose = true // Enable Compose
//    }
//}
//
//dependencies {
//
//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.material)
//    implementation(libs.androidx.constraintlayout)
//    implementation(libs.androidx.navigation.fragment.ktx)
//    implementation(libs.androidx.navigation.ui.ktx)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//
//    implementation("androidx.core:core-ktx:1.10.1")
//    implementation("androidx.appcompat:appcompat:1.6.1")
//    implementation("com.google.android.material:material:1.9.0")
//    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
//    implementation("com.google.code.gson:gson:2.8.9")
//    implementation("androidx.recyclerview:recyclerview:1.2.1")
//
//    testImplementation("junit:junit:4.13.2")
//    androidTestImplementation("androidx.test.ext:junit:1.1.5")
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
//
//    implementation ("androidx.compose.ui:ui:1.5.0")
//    implementation ("androidx.compose.material:material:1.5.0")
//    implementation ("androidx.compose.ui:ui-tooling-preview:1.5.0")
//    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
//    implementation ("androidx.activity:activity-compose:1.7.0")
//    implementation ("androidx.compose.compiler:compiler:1.5.4")
//    implementation ("androidx.compose.material3:material3:1.1.0")
//    implementation ("androidx.compose.material3:material3-window-size-class:1.1.0") // For window size classes support
//}

plugins {
    alias(libs.plugins.android.application) // Alias for Android application plugin
    alias(libs.plugins.jetbrains.kotlin.android) // Alias for Kotlin Android plugin
=======
plugins {
    alias(libs.plugins.android.application) // Android Application Plugin
    alias(libs.plugins.jetbrains.kotlin.android) // Kotlin Plugin
>>>>>>> c86f773 (Reinitialize repository)
}

android {
    namespace = "com.example.journalapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.journalapp"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    composeOptions {
<<<<<<< HEAD
        kotlinCompilerExtensionVersion = "1.5.4" // Specify the Compose compiler version
    }

    buildFeatures {
        viewBinding = true // Enable ViewBinding
        compose = true // Enable Jetpack Compose
=======
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    buildFeatures {
        viewBinding = true
        compose = true
>>>>>>> c86f773 (Reinitialize repository)
    }
}

dependencies {
<<<<<<< HEAD
    // Core Android libraries
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation ("androidx.appcompat:appcompat:1.4.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.8.9")

    // RecyclerView for displaying notes
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    // Jetpack Compose libraries
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.material:material:1.5.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.0")
    implementation("androidx.compose.runtime:runtime:1.5.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation("androidx.compose.compiler:compiler:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.0")
    implementation("androidx.compose.material3:material3-window-size-class:1.1.0")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.7.2")
=======
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Jetpack Compose
>>>>>>> c86f773 (Reinitialize repository)
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
<<<<<<< HEAD

    // Navigation components for fragment navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.6.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.6.0")

    // Unit testing libraries
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Compose testing dependencies
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.0")

    //api connect
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

}
=======
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.7.2")

    // Navigation Components
    implementation("androidx.navigation:navigation-fragment-ktx:2.6.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.6.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.0")

    // Compose Debugging
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.0")
}


>>>>>>> c86f773 (Reinitialize repository)
