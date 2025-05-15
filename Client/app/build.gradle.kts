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
    // 이미 setting.gradle.kts 에서는 관리하고 있는 내용이므로 삭제합니다
//    repositories {
//        google()
//        mavenCentral()
//    }
}