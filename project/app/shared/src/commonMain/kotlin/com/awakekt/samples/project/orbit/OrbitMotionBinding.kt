/*
 * SPDX-FileCopyrightText: 2026 The Awake Sample Project authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.awakekt.samples.project.orbit

import com.awakekt.awake.ecs.Entity
import com.awakekt.awake.ecs.World
import com.awakekt.awake.scene.binding.SceneComponentBinding
import com.awakekt.awake.scene.binding.SceneResolutionContext
import kotlin.reflect.KClass

/** Built-in bi-directional binding connecting live [OrbitMotion] components with serializable [SceneOrbitMotion] document components. */
object OrbitMotionBinding : SceneComponentBinding<OrbitMotion, SceneOrbitMotion> {
    override val componentClass: KClass<OrbitMotion> = OrbitMotion::class
    override val schemaClass: KClass<SceneOrbitMotion> = SceneOrbitMotion::class
    override val serializer = SceneOrbitMotion.serializer()

    override fun attachTyped(
        world: World,
        entity: Entity,
        component: SceneOrbitMotion,
        context: SceneResolutionContext,
    ) {
        world.add(
            entity,
            OrbitMotion().also {
                it.radius = component.radius
                it.speed = component.speed
            },
        )
    }

    override fun export(world: World, entity: Entity, component: OrbitMotion): SceneOrbitMotion =
        SceneOrbitMotion(radius = component.radius, speed = component.speed)
}
