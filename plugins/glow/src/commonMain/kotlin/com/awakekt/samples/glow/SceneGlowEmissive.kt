/*
 * SPDX-FileCopyrightText: 2026 The Awake Sample Project authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.awakekt.samples.glow

import com.awakekt.awake.scene.document.SceneComponent
import com.awakekt.awake.scene.document.SceneValidationIssue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A pulsing emissive glow drawn flat in this node's XZ plane: a quad the engine has no built-in
 * content for, added by the `glow` plugin (awakekt/awake-sample-project).
 *
 * @property tint Colour the glow pulses towards: red, green, blue, alpha.
 * @property intensity Brightness multiplier on top of [tint].
 * @property pulseSpeed How fast the glow pulses, in radians per second.
 * @property enabled Whether the glow draws. Its clock runs either way.
 */
@Serializable
@SerialName("glow_emissive")
data class SceneGlowEmissive(
    val tint: List<Float> = listOf(1f, 0.6f, 0.1f, 1f),
    val intensity: Float = 1.5f,
    val pulseSpeed: Float = 2f,
    val enabled: Boolean = true,
) : SceneComponent {
    override fun validate(path: String): List<SceneValidationIssue> = buildList {
        if (tint.size != 4) add(SceneValidationIssue(path, "glow_emissive.tint needs 4 numbers"))
        if (tint.any { !it.isFinite() }) add(SceneValidationIssue(path, "glow_emissive.tint has a value that is not a finite number"))
        if (!intensity.isFinite() || intensity < 0f) add(SceneValidationIssue(path, "glow_emissive.intensity must be a finite number that is not negative"))
        if (!pulseSpeed.isFinite()) add(SceneValidationIssue(path, "glow_emissive.pulseSpeed must be a finite number"))
    }
}
