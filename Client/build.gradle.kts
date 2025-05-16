// Top-level build file for Java-only Android project (Kotlin DSL)

plugins {
    id("com.android.application") version "8.2.0" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.0")
    }
}

allprojects {
    // settings.gradle.kts 에서 repositories 설정을 강제하도록 설정된 경우,
    // 아래 블록은 생략해야 하므로 주석 처리
    // repositories {
    //     google()
    //     mavenCentral()
    // }
}