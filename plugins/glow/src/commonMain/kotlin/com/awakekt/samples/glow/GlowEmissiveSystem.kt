/*
 * SPDX-FileCopyrightText: 2026 The Awake Sample Project authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.awakekt.samples.glow

import com.awakekt.awake.asset.shaderdocument.ShaderEffectInputs
import com.awakekt.awake.asset.shaders.AttachedContentFeature
import com.awakekt.awake.asset.shaders.ContentFeatureHost
import com.awakekt.awake.core.math.Mat4
import com.awakekt.awake.ecs.Entity
import com.awakekt.awake.ecs.System
import com.awakekt.awake.ecs.World
import com.awakekt.awake.scene.core.transform.Transform
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * Draws every [GlowEmissiveSource] entity by attaching [GlowShaderDocument] to [host], once per
 * entity, and keeps it running: each frame it advances the glow's clock, writes its current
 * `tint`/`intensity`/`pulseSpeed`, copies the entity's place into the document's model matrix, and
 * shows or hides it by [SceneGlowEmissive.enabled].
 *
 * Attaching is `suspend` (the host uploads GPU resources for it), so it starts from [update] on an
 * unconfined coroutine and its result is applied on a later [update] through [attached] -- the frame
 * loop itself never suspends. [close] it when the scene is disposed.
 *
 * @param host What the content feature attaches to. Null, as with a renderer that draws nothing,
 * runs the system without drawing; a failed attach is simply not drawn, and nothing retries it.
 */
class GlowEmissiveSystem(private val host: ContentFeatureHost?) :
    System,
    AutoCloseable {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
    private val tracked = ArrayList<Tracked>()
    private val attached = Channel<Attached>(Channel.UNLIMITED)
    private val model = Mat4()

    override fun update(world: World, delta: Float) {
        if (host == null) return
        refreshTracked(world)
        applyAttached()
        world.queryEach(GlowEmissiveSource::class) { entity, source ->
            if (tracked.none { it.entity == entity }) startAttach(Tracked(entity, source).also { tracked += it })
        }
        for (index in tracked.indices) advance(world, tracked[index], delta)
    }

    /** Detaches every glow this system attached and cancels what is still attaching. */
    override fun close() {
        tracked.forEach(Tracked::release)
        tracked.clear()
        while (true) {
            val pending = attached.tryReceive().getOrNull() ?: break
            pending.shown.feature.detach()
        }
        scope.cancel()
    }

    /** Drops the glows whose entity or component is gone, and takes in a replaced component. */
    private fun refreshTracked(world: World) {
        for (index in tracked.indices.reversed()) {
            val entry = tracked[index]
            val current = if (world.isAlive(entry.entity)) world.get<GlowEmissiveSource>(entry.entity) else null
            if (current == null) {
                entry.release()
                tracked.removeAt(index)
            } else {
                entry.source = current
            }
        }
    }

    /** A result for a glow that has since gone is freed straight away. */
    private fun applyAttached() {
        while (true) {
            val result = attached.tryReceive().getOrNull() ?: return
            val entry = result.entry.takeIf { candidate -> tracked.any { it === candidate } }
            if (entry == null) {
                result.shown.feature.detach()
            } else {
                entry.shown?.feature?.detach()
                entry.shown = result.shown
            }
        }
    }

    @Suppress("TooGenericExceptionCaught") // whatever stops one glow attaching must not stop the frame.
    private fun startAttach(entry: Tracked) {
        val attachTo = checkNotNull(host)
        val inputs = GlowShaderDocument.compiled.newInputs()
        entry.job = scope.launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                val source = GlowShaderDocument.compiled.contentFeature(inputs, name = "glow-emissive")
                val feature = attachTo.attachContentFeature(source)
                attached.trySend(Attached(entry, Shown(inputs, feature)))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                // Nothing drives a retry for a sample plugin; the entity simply stays unlit.
            }
        }
    }

    private fun advance(world: World, entry: Tracked, delta: Float) {
        entry.timeSeconds += delta
        val shown = entry.shown ?: return
        val settings = entry.source.settings
        val issues = GlowShaderDocument.compiled.packParameters(
            mapOf("tint" to settings.tint, "intensity" to listOf(settings.intensity), "pulseSpeed" to listOf(settings.pulseSpeed)),
            shown.inputs,
        )
        shown.inputs.timeSeconds = entry.timeSeconds
        shown.inputs.deltaSeconds = delta
        shown.inputs.visible = settings.enabled && issues.isEmpty()
        val transform = world.get<Transform>(entry.entity) ?: return
        // The transform pass runs after frame systems, so a root node's world matrix is last frame's;
        // its local matrix is current, and is its world matrix. A child's is as of the last pass.
        shown.inputs.setModel(if (transform.parent == null) transform.computeLocalMatrix(model) else transform.worldMatrix)
    }

    private class Tracked(val entity: Entity, var source: GlowEmissiveSource) {
        var job: Job? = null
        var shown: Shown? = null
        var timeSeconds = 0f

        fun release() {
            job?.cancel()
            shown?.feature?.detach()
            shown = null
        }
    }

    private class Shown(val inputs: ShaderEffectInputs, val feature: AttachedContentFeature)

    private class Attached(val entry: Tracked, val shown: Shown)
}
