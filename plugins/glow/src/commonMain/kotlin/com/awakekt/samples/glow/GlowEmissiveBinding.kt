/*
 * SPDX-FileCopyrightText: 2026 The Awake Sample Project authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.awakekt.samples.glow

import com.awakekt.awake.ecs.Entity
import com.awakekt.awake.ecs.World
import com.awakekt.awake.scene.binding.SceneComponentBinding
import com.awakekt.awake.scene.binding.SceneResolutionContext
import kotlin.reflect.KClass

/** A [SceneGlowEmissive] on a live entity; [GlowEmissiveSystem] draws it. */
class GlowEmissiveSource(val settings: SceneGlowEmissive)

/** Loads [SceneGlowEmissive] as the entity's [GlowEmissiveSource], and exports it back unchanged. */
object GlowEmissiveBinding : SceneComponentBinding<GlowEmissiveSource, SceneGlowEmissive> {
    override val componentClass: KClass<GlowEmissiveSource> = GlowEmissiveSource::class
    override val schemaClass: KClass<SceneGlowEmissive> = SceneGlowEmissive::class
    override val serializer = SceneGlowEmissive.serializer()

    override fun attachTyped(
        world: World,
        entity: Entity,
        component: SceneGlowEmissive,
        context: SceneResolutionContext,
    ) {
        world.add(entity, GlowEmissiveSource(component))
    }

    override fun export(world: World, entity: Entity, component: GlowEmissiveSource): SceneGlowEmissive =
        component.settings
}
