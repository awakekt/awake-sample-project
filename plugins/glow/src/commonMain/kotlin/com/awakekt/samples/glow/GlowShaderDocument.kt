/*
 * SPDX-FileCopyrightText: 2026 The Awake Sample Project authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.awakekt.samples.glow

import com.awakekt.awake.asset.shaderdocument.CompiledShaderDocument
import com.awakekt.awake.asset.shaderdocument.ShaderDocuments

/**
 * A pulsing emissive glow, written in ASL (Awake's checked shader-document format, see
 * `docs/reference/shader-document.md` in awakekt/awake) rather than hand-written WGSL: the same
 * public seam a project's own `.shader.json` file would use, embedded here as source instead of a
 * project asset, since a plugin ships as code.
 *
 * A `plane` surface draws an additively blended quad in the node's XZ plane: `tint` scaled by
 * `intensity`, a radial falloff from the plane's centre, and a `sin(time * pulseSpeed)` pulse.
 * Compiled once; every entity with [GlowEmissive] gets its own [CompiledShaderDocument.contentFeature].
 */
internal object GlowShaderDocument {
    private val source = """
        {
          "formatVersion": 1,
          "name": "glow-emissive",
          "surface": "plane",
          "blend": "additive",
          "plane": { "size": [2.4, 2.4], "segments": 1 },
          "parameters": [
            { "name": "tint", "type": "color", "default": [1.0, 0.6, 0.1, 1.0] },
            { "name": "intensity", "type": "float", "default": [1.5] },
            { "name": "pulseSpeed", "type": "float", "default": [2.0] }
          ],
          "fragment": {
            "statements": [
              { "statement": "let", "name": "center", "value": { "op": "sub",
                "a": { "op": "input", "input": "uv" }, "b": { "op": "const", "value": [0.5, 0.5] } } },
              { "statement": "let", "name": "dist", "value": { "op": "call", "fn": "length",
                "args": [ { "op": "local", "name": "center" } ] } },
              { "statement": "let", "name": "falloff", "value": { "op": "call", "fn": "saturate",
                "args": [ { "op": "sub", "a": { "op": "const", "value": [1.0] },
                  "b": { "op": "mul", "a": { "op": "local", "name": "dist" }, "b": { "op": "const", "value": [2.0] } } } ] } },
              { "statement": "let", "name": "pulse", "value": { "op": "add",
                "a": { "op": "mul", "a": { "op": "call", "fn": "sin",
                    "args": [ { "op": "mul", "a": { "op": "input", "input": "time" }, "b": { "op": "param", "name": "pulseSpeed" } } ] },
                  "b": { "op": "const", "value": [0.5] } },
                "b": { "op": "const", "value": [0.5] } } }
            ],
            "color": { "op": "vec4", "args": [
              { "op": "mul",
                "a": { "op": "swizzle", "value": { "op": "param", "name": "tint" }, "components": "rgb" },
                "b": { "op": "mul", "a": { "op": "param", "name": "intensity" },
                  "b": { "op": "mul", "a": { "op": "local", "name": "falloff" }, "b": { "op": "local", "name": "pulse" } } } },
              { "op": "mul",
                "a": { "op": "mul", "a": { "op": "local", "name": "falloff" }, "b": { "op": "local", "name": "pulse" } },
                "b": { "op": "swizzle", "value": { "op": "param", "name": "tint" }, "components": "a" } }
            ] }
          }
        }
    """.trimIndent()

    /** Compiled once; every attached effect shares it and gets its own inputs. */
    val compiled: CompiledShaderDocument by lazy { ShaderDocuments.compile(source) }
}
