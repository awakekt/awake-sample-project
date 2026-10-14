package com.awakekt.samples.project

import com.awakekt.awake.engine.bootstrap.dsl.app
import com.awakekt.awake.engine.platform.HeadlessSurface
import com.awakekt.awake.engine.platform.dsl.requireService
import com.awakekt.awake.project.runtime.runProject
import com.awakekt.awake.render.capture.FramebufferAttachment
import com.awakekt.awake.scene.authoring.scene
import com.awakekt.awake.scene.runtime.SceneAppLifecycleRuntime
import com.awakekt.awake.vulkan.application.VulkanEngine
import kotlinx.coroutines.runBlocking
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.fail

/**
 * Plays this project's bundled scene -- its glow plugin and its own orbit-motion capability both
 * running -- headless on Vulkan, the way a played project draws, and compares what the primary
 * camera sees against a committed baseline.
 *
 * CI runs this on Mesa's lavapipe (ubuntu-latest has no GPU); `-Dawake.recordBaseline=true`
 * re-records [BASELINE] from this machine's render instead of checking it -- do that only from the
 * CI job, whose lavapipe is what every future run compares against, not from a GPU-backed desktop,
 * whose anti-aliasing differs enough to make every run fail.
 */
class RenderBaselineTest {
    @Test
    fun matchesTheCommittedBaseline() = runBlocking {
        val project = loadGame()
        project.use {
            val game = app { scene("render") { runProject(project, touchControls = false) } }
            val renderer = HeadlessVulkan(game).boot(HeadlessSurface(WIDTH, HEIGHT))
            try {
                game.ready(renderer)
                val runtime = game.requireService<SceneAppLifecycleRuntime>()
                repeat(FRAMES) { game.update(STEP, WIDTH.toFloat(), HEIGHT.toFloat()) }
                val frame = runtime.readbackAttachment(WIDTH, HEIGHT, FramebufferAttachment.Color0)
                if (!frame.available) fail("The scene has nothing to render: ${frame.reason}")
                val actual = toImage(frame.width, frame.height, frame.channels, frame.values)
                val actualFile = File("build/render-baseline/actual.png").also { it.parentFile.mkdirs() }
                ImageIO.write(actual, "png", actualFile)

                if (System.getProperty("awake.recordBaseline") == "true") {
                    BASELINE.parentFile.mkdirs()
                    ImageIO.write(actual, "png", BASELINE)
                    return@runBlocking
                }
                if (!BASELINE.isFile) {
                    fail(
                        "No baseline at $BASELINE. Rendered this run to $actualFile for review; " +
                            "once it looks right, re-run with -Dawake.recordBaseline=true and commit the result.",
                    )
                }
                val meanDiff = meanAbsoluteDifference(ImageIO.read(BASELINE), actual)
                if (meanDiff > TOLERANCE) {
                    fail("Rendered frame differs from $BASELINE by $meanDiff (tolerance $TOLERANCE); see $actualFile")
                }
            } finally {
                renderer.waitIdle()
                renderer.destroy()
                game.dispose()
            }
        }
    }

    /** Vulkan with a windowless swapchain, drawing [AwakeRenderPlan]: the same shape as Core's own `awake render`. */
    private class HeadlessVulkan(game: com.awakekt.awake.engine.platform.lifecycle.AwakeAppLifecycle) :
        VulkanEngine(game, AwakeRenderPlan) {
        suspend fun boot(surface: HeadlessSurface) = createBackendResources(surface).renderer
    }

    private companion object {
        const val WIDTH = 640
        const val HEIGHT = 360
        const val FRAMES = 30
        const val STEP = 1f / 60f

        // A few percent of full brightness, averaged over every channel: enough to absorb a driver's
        // own anti-aliasing noise, not enough to miss the glow or the orbiter being gone.
        const val TOLERANCE = 6.0

        val BASELINE = File("src/test/resources/render-baseline/main-scene.png")

        fun toImage(width: Int, height: Int, channels: Int, values: FloatArray): BufferedImage {
            val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            for (pixel in 0 until width * height) {
                fun channel(index: Int) = if (index < channels) values[pixel * channels + index] else 1f
                val r = (channel(0).coerceIn(0f, 1f) * BYTE_MAX).toInt()
                val g = (channel(1).coerceIn(0f, 1f) * BYTE_MAX).toInt()
                val b = (channel(2).coerceIn(0f, 1f) * BYTE_MAX).toInt()
                image.setRGB(pixel % width, pixel / width, (BYTE_MAX shl ALPHA_SHIFT) or (r shl RED_SHIFT) or (g shl GREEN_SHIFT) or b)
            }
            return image
        }

        fun meanAbsoluteDifference(expected: BufferedImage, actual: BufferedImage): Double {
            require(expected.width == actual.width && expected.height == actual.height) {
                "Baseline is ${expected.width}x${expected.height}; this render is ${actual.width}x${actual.height}"
            }
            var total = 0L
            for (y in 0 until expected.height) {
                for (x in 0 until expected.width) {
                    val e = expected.getRGB(x, y)
                    val a = actual.getRGB(x, y)
                    for (shift in intArrayOf(RED_SHIFT, GREEN_SHIFT, 0)) {
                        total += kotlin.math.abs(((e shr shift) and BYTE_MAX) - ((a shr shift) and BYTE_MAX))
                    }
                }
            }
            return total.toDouble() / (expected.width * expected.height * RGB_CHANNELS)
        }

        const val BYTE_MAX = 255
        const val ALPHA_SHIFT = 24
        const val RED_SHIFT = 16
        const val GREEN_SHIFT = 8
        const val RGB_CHANNELS = 3
    }
}
