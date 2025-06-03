plugins {
    id("com.android.application") version "8.2.2" apply false
    kotlin("android") version "1.9.0" apply false
}

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2")
        classpath("com.google.gms:google-services:4.4.0")
    }
}