plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    // Adding the KSP plugin with an explicit version
    id("com.google.devtools.ksp") version "1.9.20-1.0.13" apply false

}
