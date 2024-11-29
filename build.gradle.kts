buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
<<<<<<< HEAD
        classpath ("com.android.tools.build:gradle:8.7.1")
=======
        classpath ("com.android.tools.build:gradle:8.7.2")
>>>>>>> c86f773 (Reinitialize repository)
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    // Adding the KSP plugin with an explicit version
    id("com.google.devtools.ksp") version "1.9.20-1.0.13" apply false

}
