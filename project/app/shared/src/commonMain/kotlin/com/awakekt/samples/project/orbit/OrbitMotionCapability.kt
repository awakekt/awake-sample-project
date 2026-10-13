/*
 * SPDX-FileCopyrightText: 2026 The Awake Sample Project authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.awakekt.samples.project.orbit

import com.awakekt.awake.project.runtime.SceneCapability
import com.awakekt.awake.project.runtime.SceneSystemPlan
import com.awakekt.awake.project.runtime.uses
import com.awakekt.awake.scene.binding.SceneComponentBinding
import com.awakekt.awake.scene.document.SceneDocument

/**
 * This project's own capability: the "something small that moves" `app/shared` ships as code, not
 * as a published artifact. `awake.project.json` names it by `capabilityClass` with no `path`.
 */
object OrbitMotionCapability : SceneCapability {
    override val id: String = "com.awakekt.samples.project.orbit"

    override val components: List<SceneComponentBinding<*, *>> = listOf(OrbitMotionBinding)

    override fun plan(scene: SceneDocument, plan: SceneSystemPlan) {
        if (scene.uses(SceneOrbitMotion::class)) plan.frame("orbit-motion") { OrbitMotionSystem() }
    }
}
