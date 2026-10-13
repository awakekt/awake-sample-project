package com.awakekt.samples.project

import com.awakekt.awake.vulkan.application.runVulkanDesktopGame
import kotlinx.coroutines.runBlocking

fun main() {
    val project = runBlocking { loadGame() }
    runVulkanDesktopGame(createGame(project), AwakeRenderPlan)
}
