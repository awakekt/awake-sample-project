import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.process.ProcessForkOptions
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":app:shared"))
    implementation(libs.awake.backend.vulkan)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)

    // A headless render, for RenderBaselineTest: the same project (loaded with its own capabilities,
    // unlike `awake render`, which only resolves Core's) played off-screen and read back as pixels.
    testImplementation(libs.awake.engine.platform)
    testImplementation(libs.awake.scene.runtime)
    testImplementation(libs.kotlin.test)
}

// Libraries ship their keep rules in META-INF/proguard, where Android's R8 finds them. Compose's
// ProGuard doesn't look there, so this collects them for it: Awake's keep what its native code finds
// by name.
val libraryKeepRules = tasks.register<Sync>("libraryKeepRules") {
    val libraries = configurations.runtimeClasspath.get().incoming
        .artifactView { componentFilter { it is ModuleComponentIdentifier } }
        .files
    from(libraries.elements.map { jars -> jars.filter { it.asFile.name.endsWith(".jar") }.map { zipTree(it.asFile) } }) {
        include("META-INF/proguard/*.pro")
    }
    into(layout.buildDirectory.dir("library-keep-rules"))
    eachFile { path = name }
    includeEmptyDirs = false
}

compose.desktop {
    application {
        mainClass = "com.awakekt.samples.project.MainKt"
        // macOS only lets the first thread open windows; the Vulkan window opens from main().
        if (System.getProperty("os.name").startsWith("Mac")) jvmArgs += "-XstartOnFirstThread"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.awakekt.samples.project"
            packageVersion = "1.0.0"
        }

        // The release (`runRelease`, `packageRelease…`) is shrunk and obfuscated by ProGuard, with
        // the rules every library ships for it and your own in proguard-rules.pro.
        buildTypes.release.proguard {
            obfuscate.set(true)
            // Compose's default, ProGuard 7.7.0, drops classes from a Kotlin sealed interface's
            // permitted subclasses, and the release then fails to load them. 7.10.0 keeps them all.
            version.set("7.10.0")
            configurationFiles.from(project.file("proguard-rules.pro"), files(libraryKeepRules).asFileTree)
        }
    }
}

// macOS has no system Vulkan: `run` and `test` point the loader at Homebrew's MoltenVK
// (`brew install molten-vk vulkan-loader`). Other systems find their driver without help; CI's
// ubuntu-latest runner uses lavapipe instead (see the render-baseline workflow job).
fun Task.useHomebrewVulkanOnMac() {
    if (System.getProperty("os.name").startsWith("Mac")) {
        val icd = listOf("/opt/homebrew/Cellar/molten-vk", "/usr/local/Cellar/molten-vk")
            .flatMap { File(it).listFiles().orEmpty().toList() }
            .map { File(it, "etc/vulkan/icd.d/MoltenVK_icd.json") }
            .firstOrNull { it.isFile }
        (this as? ProcessForkOptions)?.let { fork ->
            icd?.let { fork.environment("VK_ICD_FILENAMES", it.absolutePath) }
            fork.environment("DYLD_FALLBACK_LIBRARY_PATH", "/opt/homebrew/opt/vulkan-loader/lib:/opt/homebrew/lib:/usr/local/lib")
        }
    }
}

tasks.withType<JavaExec>().configureEach { useHomebrewVulkanOnMac() }
tasks.withType<Test>().configureEach { useHomebrewVulkanOnMac() }
