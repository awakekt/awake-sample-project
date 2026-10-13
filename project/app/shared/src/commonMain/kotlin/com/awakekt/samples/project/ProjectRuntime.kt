package com.awakekt.samples.project

import com.awakekt.awake.core.io.AssetSource
import com.awakekt.awake.physics.PhysicsWorld
import com.awakekt.awake.project.runtime.SceneCapability
import com.awakekt.awake.project.runtime.loadProject as loadCoreProject
import com.awakekt.awake.project.runtime.runProject as runCoreProject
import com.awakekt.awake.scene.authoring.SceneAppDsl

/** A loaded project. */
typealias LoadedProject = com.awakekt.awake.project.runtime.LoadedProject

/**
 * Loads the project's entry scene using the selected physics backend, with [capabilities] besides
 * Core's own: the glow plugin (published, resolved from plugins/glow's repo-local Maven repo) and
 * this project's own orbit-motion capability (code in this module, no published artifact). Kotlin/
 * Native and wasmJs can't resolve a plugin by class name, so the app passes the list in code; the
 * same ids are named in `awake.project.json` for an editor or export tool to read.
 */
internal suspend fun loadProject(
    files: AssetSource,
    capabilities: List<SceneCapability>,
    physicsWorld: suspend () -> PhysicsWorld,
): LoadedProject = loadCoreProject(files, capabilities = capabilities, physicsWorld = physicsWorld)

/** Runs the loaded project in this application's scene. */
internal fun SceneAppDsl.runProject(project: LoadedProject, touchControls: Boolean) {
    runCoreProject(project, touchControls)
}
