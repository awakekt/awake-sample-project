/*
 * SPDX-FileCopyrightText: 2026 The Awake Sample Project authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.awakekt.samples.project.orbit

import com.awakekt.awake.ecs.Poolable

/**
 * Live pose for a [SceneOrbitMotion]-driven entity. [OrbitMotionSystem] captures the centre from
 * [Transform.position][com.awakekt.awake.scene.core.transform.Transform] the first time it sees the
 * entity, then advances [angle] and writes a new position every frame -- the same "control carries
 * state, system only applies it" split [com.awakekt.awake.scene.core.transform.SpinControl] uses.
 */
class OrbitMotion : Poolable {
    var radius: Float = 2f
    var speed: Float = 1f
    var angle: Float = 0f
    var centerX: Float = 0f
    var centerZ: Float = 0f
    var centerY: Float = 0f
    var started: Boolean = false

    override fun reset() {
        radius = 2f
        speed = 1f
        angle = 0f
        centerX = 0f
        centerZ = 0f
        centerY = 0f
        started = false
    }
}
