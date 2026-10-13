/*
 * SPDX-FileCopyrightText: 2026 The Awake Sample Project authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.awakekt.samples.project.orbit

import com.awakekt.awake.ecs.System
import com.awakekt.awake.ecs.World
import com.awakekt.awake.scene.core.transform.Transform
import kotlin.math.cos
import kotlin.math.sin

/**
 * Drives [Transform.position] in a circle, starting from wherever the node was placed: the centre
 * is derived from that position and [OrbitMotion.radius] so the first frame doesn't jump.
 */
class OrbitMotionSystem : System {
    override fun update(world: World, delta: Float) {
        world.queryEach(Transform::class, OrbitMotion::class) { _, transform, orbit ->
            if (!orbit.started) {
                orbit.centerX = transform.position.x - orbit.radius
                orbit.centerY = transform.position.y
                orbit.centerZ = transform.position.z
                orbit.started = true
            }
            orbit.angle += orbit.speed * delta
            transform.position.set(
                orbit.centerX + cos(orbit.angle) * orbit.radius,
                orbit.centerY,
                orbit.centerZ + sin(orbit.angle) * orbit.radius,
            )
        }
    }
}
