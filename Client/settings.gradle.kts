pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        // 카카오 SDK 저장소
        maven { url = uri("https://devrepo.kakao.com/nexus/content/groups/public/") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // 🔒 외부 저장소만 사용
    repositories {
        google()
        mavenCentral()
        // 카카오 저장소
        maven { url = uri("https://devrepo.kakao.com/nexus/content/groups/public/") }

        maven { url = uri("https://jitpack.io") }
    }
}
rootProject.name = "SUAL_Client"
include(":app")
