import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    jvm()

    android {
       namespace = "com.awakekt.samples.project.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()

       compilerOptions {
           jvmTarget = JvmTarget.JVM_17
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }

    sourceSets {
        // The Android library packages only its own Java resources, so it takes the project from commonMain's.
        androidMain {
            resources.srcDir("src/commonMain/resources")
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            implementation(libs.awake.engine.bootstrap)
            implementation(libs.awake.asset.shaders)
            implementation(libs.awake.asset.shader.pack)
            implementation(libs.awake.core.host)
            implementation(libs.awake.scene.authoring)
            implementation(libs.awake.scene.document)
            implementation(libs.awake.scene.binding)
            implementation(libs.awake.scene.core)
            implementation(libs.awake.ecs)
            // api: loadGame and createGame hand LoadedProject to every app module.
            api(libs.awake.project.runtime)
            // The project's own capability (OrbitMotion) is code in this module; the glow plugin's
            // capability is a published artifact, resolved from plugins/glow's repo-local Maven repo.
            implementation(libs.awake.samples.glow)
            implementation(libs.awake.backend.jolt)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        iosMain.dependencies {
            implementation(libs.awake.backend.vulkan)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
