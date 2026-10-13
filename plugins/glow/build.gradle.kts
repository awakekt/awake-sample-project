import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    `maven-publish`
}

group = "com.awakekt.samples"
version = "0.1.0"

kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    android {
        namespace = "com.awakekt.samples.glow"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    sourceSets {
        commonMain.dependencies {
            // The shader-document path is the public, backend-free seam a third-party plugin uses to
            // draw its own material: compile ASL to WGSL, then attach through ContentFeatureHost.
            api(libs.awake.asset.shader.document)
            api(libs.awake.scene.document)
            api(libs.awake.scene.binding)
            api(libs.awake.scene.core)
            api(libs.awake.ecs)
            // SceneCapability: how this plugin's component and system join a played project.
            api(libs.awake.project.runtime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

publishing {
    repositories {
        // Repo-local for now; Awake Cloud's Maven endpoint replaces this once it exists
        // (awakekt/awake-studio#445).
        maven(uri(layout.buildDirectory.dir("repo"))) { name = "local" }
    }
}
