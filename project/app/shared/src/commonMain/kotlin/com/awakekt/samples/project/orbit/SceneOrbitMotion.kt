/*
 * SPDX-FileCopyrightText: 2026 The Awake Sample Project authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.awakekt.samples.project.orbit

import com.awakekt.awake.scene.document.SceneComponent
import com.awakekt.awake.scene.document.SceneValidationIssue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Circles this node, in the XZ plane, around the point it started at: this project's own capability
 * (`app/shared`, no published artifact), named in `awake.project.json` by `capabilityClass` alone.
 *
 * @property radius Distance from the centre, in metres.
 * @property speed Radians per second; negative circles the other way.
 */
@Serializable
@SerialName("orbit_motion")
data class SceneOrbitMotion(
    val radius: Float = 2f,
    val speed: Float = 1f,
) : SceneComponent {
    override fun validate(path: String): List<SceneValidationIssue> = buildList {
        if (!radius.isFinite() || radius < 0f) add(SceneValidationIssue(path, "orbit_motion.radius must be a finite number that is not negative"))
        if (!speed.isFinite()) add(SceneValidationIssue(path, "orbit_motion.speed must be a finite number"))
    }
}
