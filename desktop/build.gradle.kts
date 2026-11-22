plugins {
    kotlin("jvm") version "2.1.21"
    application
}

kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
java { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }

// The phone app's own code (text tools, characters, AI, dictionary, commands...) is built in here as it is,
// on top of a tiny stand-in for the two Android classes it uses (settings storage).
val shared = listOf(
    "Adapt", "Ai", "Audio", "Characters", "Clean", "Commands", "Cpu", "Downloader", "Features", "Instant", "Learn",
    "Limit", "Packs", "Prefs", "RobloxSafe", "Royal", "SkinArt", "Stats", "Styles", "TextTools", "Whisper", "WikiResearch", "Words",
)
sourceSets {
    main {
        kotlin {
            srcDir("../app/src/main/java")
            include("android/**", "com/voicebubble/desktop/**", "com/voicebubble/shared/**")
            shared.forEach { include("com/voicebubble/$it.kt") }
        }
        resources { srcDir("../app/src/main/assets") }
    }
}

dependencies {
    implementation("org.json:json:20240303")
    implementation("com.anthropic:anthropic-java:3.6.0")
    implementation("com.github.kwhat:jnativehook:2.2.2")
    implementation("net.java.dev.jna:jna-platform:5.14.0")
}

application {
    mainClass.set("com.voicebubble.desktop.MainKt")
    applicationName = "VoiceBubble"
}

val appVersion = (System.getenv("VERSION_CODE") ?: "0")
tasks.jar { manifest { attributes("Implementation-Version" to appVersion) } }
