# Awake sample project

Proves that a project made from [the Awake project template](https://github.com/awakekt/awake-project-template),
using plugins that draw something of their own, opens in Awake Studio and plays on every platform.
Phase 1 (this repository today) covers a public runtime plugin and the rendering/platform half, before
the Awake Cloud catalog exists. See [awakekt/awake-studio#454](https://github.com/awakekt/awake-studio/issues/454)
for the full goal: public, private and Pro plugins, each enforced by Awake Cloud.

No consumer game or publisher is named anywhere in this repository.

## Contents

- **`plugins/glow`** — a public Kotlin Multiplatform runtime plugin (JVM/desktop, Android, iOS,
  wasmJs). Its `glow_emissive` scene component draws a pulsing emissive glow the engine has no
  built-in content for: a small ASL (Awake Shader Language) document, compiled through
  `com.awakekt.awake.asset:shader-document` and attached per entity through the renderer's
  `ContentFeatureHost` — the public, backend-free seam documented in Core's
  `docs/reference/render-extensibility.md` for a third party to draw its own material, with no fork
  of a backend module. It publishes with the plain `maven-publish` plugin to a repo-local Maven
  directory (`plugins/glow/build/repo`) for now; Awake Cloud's own Maven endpoint replaces this once
  it exists ([awakekt/awake-studio#445](https://github.com/awakekt/awake-studio/issues/445)).
- **`project/`** — made from awake-project-template, trimmed to desktop (Vulkan) and Android for this
  phase. Its scene uses the glow plugin's component on a mesh and this project's own `orbit_motion`
  component, a small capability that lives in `app/shared` with no published artifact.
  `awake.project.json` names both in `plugins[]`: glow by `artifact` plus `capabilityClass`, orbit
  motion by `capabilityClass` alone. `project/settings.gradle.kts` resolves glow from
  `plugins/glow`'s repo-local Maven directory.

## Building it

```bash
cd plugins/glow && ./gradlew publishAllPublicationsToLocalRepository
cd ../../project && ./gradlew :app:desktopApp:assemble :app:androidApp:assembleDebug
```

Desktop Vulkan needs a Vulkan loader and ICD on the machine running it: Mesa's lavapipe
(`mesa-vulkan-drivers`) on Linux, or Homebrew's `molten-vk` and `vulkan-loader` on macOS. CI installs
lavapipe; see `.github/workflows/ci.yml`.

## CI

[![CI](https://github.com/awakekt/awake-sample-project/actions/workflows/ci.yml/badge.svg)](https://github.com/awakekt/awake-sample-project/actions/workflows/ci.yml)

`.github/workflows/ci.yml` publishes glow, builds the project, runs the project's own capability
tests, builds the Android debug APK, and plays the scene headless on Vulkan (Mesa's lavapipe --
ubuntu-latest has no GPU) to compare it against a committed baseline
(`project/app/desktopApp/src/test/resources/render-baseline/main-scene.png`, recorded from CI's own
lavapipe run) with a small per-channel tolerance, uploading the render either way. WebGPU/web is
dropped from this phase: the WebGPU snapshot build for this Core release isn't published, independent
of Studio's own web-export gap (#331, #442).

## Matrix

Rows are a project's plugins; columns are where it has been proven to open and play. A green cell
was checked by this repository's CI or, where noted, programmatically against desktop Studio locally
(not in CI). A blocked cell names the issue that blocks it.

| | Desktop Studio | Web Studio | Desktop export | Android | Web | iOS |
|---|---|---|---|---|---|---|
| Public plugin (glow) | ✅ (Studio 27940295, programmatic) | #442 | ✅ CI | ✅ CI | #331 | #289 |
| Private plugin (owner / other) | #448 | #442, #448 | #448 | #448 | #331, #448 | #289, #448 |
| Pro plugin (Pro / free) | #449 | #442, #449 | #449 | #449 | #331, #449 | #289, #449 |
| Template with code (orbit motion) | ✅ (Studio 27940295, programmatic) | #442 | ✅ CI | ✅ CI | #331 | #289 |

The public plugin and "template with code" rows are the same CI run: the one scene uses both
`glow_emissive` (the published plugin) and `orbit_motion` (this project's own `app/shared` code).

## Desktop Studio check (local, not CI)

Checked against `awakekt/awake-studio` at `27940295` (Core 0.12.0) by a throwaway desktop test in a
Studio worktree, headless, with no mouse or keyboard. With glow published to `plugins/glow/build/repo`
first, the test opens `project/app/shared/src/commonMain/resources/project` through Studio's own
startup open and code-consent path, as Studio's `ProjectCapabilitiesDesktopTest` does, on a renderer
that draws nothing and records the content features attached to it:

1. Answering yes, Studio runs this project's own `./gradlew :app:shared:awakeStudioCode`, rebuilding
   the `shared-jvm.jar` the test had deleted, and loads both capabilities,
   `com.awakekt.samples.glow` and `com.awakekt.samples.project.orbit`, in the project's own class loader.
2. `CubeGlow`'s `glow_emissive` decodes as `GlowEmissiveSource` and `Orbiter`'s `orbit_motion` as
   `OrbitMotion`; neither is kept as unknown data.
3. Within 2 Play frames the `Orbiter` moves off (2, 0.5, 0) and glow's system attaches its
   `glow-emissive` content feature, which Stop detaches. The edited world's `Orbiter` stays put.
4. Opening another project drops both capabilities and the project's component registry.
5. Control: answering no, the scene opens with both components kept as data, and 120 Play frames
   leave the `Orbiter` at (2, 0.5, 0) with nothing attached.

There is no screenshot: Studio's headless screen path captures its UI only and leaves the 3D viewport
empty. This repository's CI render covers the scene's pixels. The test isn't kept in Studio, since it
reads this repository from a local checkout.

## Known limits

- **WebGPU/web dropped for phase 1.** The WebGPU snapshot this Core release would pin
  (`<version>-SNAPSHOT`) isn't published at the time of writing, independent of Studio's own
  web-export gap (#331, #442). `app/webApp` is left out of `project/` until that's back and web
  export is unblocked.
- **Two Core/Vulkan packaging gaps, found while getting CI green, both worked around or already
  fixed:**
  1. `com.awakekt.awake.asset:shader-compiler-desktop` 0.11.x embedded the Naga native shader
     compiler for macOS only, so `VulkanEngine` couldn't start on Linux (or Windows) from Maven
     Central alone -- fatal for this repository's headless render on ubuntu-latest. Filed and
     already fixed upstream: [awakekt/awake#795](https://github.com/awakekt/awake/issues/795),
     fixed by [awakekt/awake#798](https://github.com/awakekt/awake/pull/798), shipped in Core
     0.12.0, which this repository pins.
  2. `com.awakekt.awake.backend:vulkan` has its own, independent version train, and its published
     0.1.28 POM requests `shader-compiler-desktop:0.11.0` regardless of which newer Core version a
     consumer pins -- so pinning 0.12.0 alone wasn't enough; Gradle still resolved the old,
     macOS-only native transitively. `project/app/desktopApp/build.gradle.kts` adds a dependency
     constraint aligning it to the pinned Core version, Gradle's ordinary mechanism for this. A
     future Vulkan release that bumps its own Core pin would make the constraint redundant, not
     wrong, so it's safe to leave in place. Filed:
     [awakekt/awake#811](https://github.com/awakekt/awake/issues/811).

## License

Apache-2.0. See [LICENSE](LICENSE).
