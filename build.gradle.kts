// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    id("com.google.firebase.crashlytics") version "3.0.4" apply false
    // For Kotlin 2.1.21 use "2.1.21-1.0.32", for 2.0.21 use "2.0.21-1.0.28", etc.
    id("com.google.devtools.ksp") version "2.1.20-1.0.32" apply false
}