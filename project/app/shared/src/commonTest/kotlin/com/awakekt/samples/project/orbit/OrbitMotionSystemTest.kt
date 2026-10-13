/*
 * SPDX-FileCopyrightText: 2026 The Awake Sample Project authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.awakekt.samples.project.orbit

import com.awakekt.awake.ecs.World
import com.awakekt.awake.scene.core.transform.Transform
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

class OrbitMotionSystemTest {
    @Test
    fun startsExactlyWhereTheNodeWasPlacedWithNoJump() {
        val world = World()
        val entity = world.create()
        world.add(entity, Transform().apply { position.set(2f, 0.5f, 0f) })
        world.add(entity, OrbitMotion().apply { radius = 2f; speed = 1f })

        OrbitMotionSystem().update(world, 0f)

        val position = world.get(entity, Transform::class)!!.position
        assertEquals(2f, position.x, TOLERANCE)
        assertEquals(0.5f, position.y, TOLERANCE)
        assertEquals(0f, position.z, TOLERANCE)
    }

    @Test
    fun walksAQuarterCircleInAQuarterPeriod() {
        val world = World()
        val entity = world.create()
        world.add(entity, Transform().apply { position.set(2f, 0.5f, 0f) })
        world.add(entity, OrbitMotion().apply { radius = 2f; speed = (PI / 2).toFloat() })
        val system = OrbitMotionSystem()

        // One second at pi/2 rad/s advances the angle by a quarter turn.
        system.update(world, 1f)

        val position = world.get(entity, Transform::class)!!.position
        assertEquals(0f, position.x, TOLERANCE)
        assertEquals(0.5f, position.y, TOLERANCE)
        assertEquals(2f, position.z, TOLERANCE)
    }

    private companion object {
        const val TOLERANCE = 0.001f
    }
}
