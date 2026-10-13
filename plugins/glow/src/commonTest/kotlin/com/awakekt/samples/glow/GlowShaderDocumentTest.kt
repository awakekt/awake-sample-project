/*
 * SPDX-FileCopyrightText: 2026 The Awake Sample Project authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.awakekt.samples.glow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlowShaderDocumentTest {
    @Test
    fun compilesAndPacksItsThreeParameters() {
        val compiled = GlowShaderDocument.compiled
        assertEquals(3, compiled.document.parameters.size)
        val inputs = compiled.newInputs()
        val issues = compiled.packParameters(mapOf("tint" to listOf(1f, 0f, 0f, 1f), "intensity" to listOf(2f), "pulseSpeed" to listOf(3f)), inputs)
        assertTrue(issues.isEmpty(), issues.toString())
    }

    @Test
    fun sceneComponentRejectsAWrongSizedTint() {
        val issues = SceneGlowEmissive(tint = listOf(1f, 1f, 1f)).validate("Cube")
        assertTrue(issues.any { it.message.contains("tint") })
    }

    @Test
    fun sceneComponentRejectsANegativeIntensity() {
        val issues = SceneGlowEmissive(intensity = -1f).validate("Cube")
        assertTrue(issues.any { it.message.contains("intensity") })
    }
}
