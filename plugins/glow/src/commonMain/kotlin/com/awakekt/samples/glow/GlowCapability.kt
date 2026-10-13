/*
 * SPDX-FileCopyrightText: 2026 The Awake Sample Project authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.awakekt.samples.glow

import com.awakekt.awake.asset.shaders.ContentFeatureHost
import com.awakekt.awake.project.runtime.SceneCapability
import com.awakekt.awake.project.runtime.SceneSystemPlan
import com.awakekt.awake.project.runtime.uses
import com.awakekt.awake.scene.binding.SceneComponentBinding
import com.awakekt.awake.scene.document.SceneDocument

/**
 * The `glow` plugin's runtime capability: a scene using [SceneGlowEmissive] gets one
 * [GlowEmissiveSystem], drawn through the renderer's `ContentFeatureHost`.
 *
 * A project names this capability in `awake.project.json`:
 * ```json
 * { "id": "com.awakekt.samples.glow", "capabilityClass": "com.awakekt.samples.glow.GlowCapability",
 *   "artifact": { "group": "com.awakekt.samples", "name": "glow", "version": "0.1.0" } }
 * ```
 * and passes `GlowCapability` to `loadProject`'s `capabilities`, since Kotlin/Native and wasmJs
 * cannot resolve a plugin by class name at run time.
 */
object GlowCapability : SceneCapability {
    override val id: String = "com.awakekt.samples.glow"

    override val components: List<SceneComponentBinding<*, *>> = listOf(GlowEmissiveBinding)

    override fun plan(scene: SceneDocument, plan: SceneSystemPlan) {
        if (!scene.uses(SceneGlowEmissive::class) || !plan.hasRenderer) return
        plan.frame("glow-emissive") { GlowEmissiveSystem(it.renderer as? ContentFeatureHost) }
    }
}
