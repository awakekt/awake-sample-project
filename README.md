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

`.github/workflows/ci.yml` publishes glow, builds the project, runs the project's own capability
tests, builds the Android debug APK, and plays the scene headless on Vulkan (Mesa's lavapipe --
ubuntu-latest has no GPU) to compare it against a committed baseline with a small per-channel
tolerance, uploading the render either way. WebGPU/web is dropped from this phase: the WebGPU
snapshot build for this Core release isn't published (see "Known limits" below), and web export is
blocked on Studio's own side regardless (#331, #442).

## Matrix

Rows are a project's plugins; columns are where it has been proven to open and play. A green cell
was checked by this repository's CI or, where noted, by hand against desktop Studio locally (not yet
automated). A blocked cell names the issue that blocks it.

| | Desktop Studio | Web Studio | Desktop export | Android | Web | iOS |
|---|---|---|---|---|---|---|
| Public plugin (glow) | built and discovered; see "Desktop Studio check" | #442 | ✅ CI | ✅ CI | #331 | #289 |
| Private plugin (owner / other) | #448 | #442, #448 | #448 | #448 | #331, #448 | #289, #448 |
| Pro plugin (Pro / free) | #449 | #442, #449 | #449 | #449 | #331, #449 | #289, #449 |
| Template with code (orbit motion) | built and discovered; see "Desktop Studio check" | #442 | ✅ CI | ✅ CI | #331 | #289 |

The public plugin and "template with code" rows are the same CI run: the one scene uses both
`glow_emissive` (the published plugin) and `orbit_motion` (this project's own `app/shared` code).

## Desktop Studio check (local, not CI)

Opening `project/app/shared/src/commonMain/resources/project` in a desktop Studio built from
`awakekt/awake-studio`'s `origin/main` (a fresh worktree, no local Core build needed since this
project resolves Core from Maven Central): Studio's own log confirmed

```
studio.discovery  Discovered project 'Awake Sample' at '.../project' with 1 scenes
```

Reading Studio's own `AppProjectBuild.kt` / `PlatformPluginLoader.desktop.kt` /
`StudioEditorBridge.kt` shows it loads a project's capabilities by shelling out to the project's own
`./gradlew :app:shared:awakeStudioCode` and then resolving each `capabilityClass` from the printed
runtime classpath -- the same, uniform mechanism for an `artifact`-named plugin (glow) and a
no-`path` one (orbit motion), since both already sit on `app/shared`'s own dependency graph. That
graph, including glow's repo-local Maven directory, is exactly what this repository's own CI already
builds successfully, so there's no Studio-specific resolution step left to fail.

What wasn't completed: clicking through Studio's own code-consent prompt, Play, and a screenshot.
The only display available in this environment is the operator's own live desktop, shared at the time
with several other concurrent sessions (visible background builds, an open system dialog); driving
mouse/keyboard automation on it to finish this one interactive step wasn't a safe thing to do. This
is an environment limitation of this check, not a defect found in Studio -- nothing above suggests
Studio can't resolve the plugin.

## Known limits

- **WebGPU/web dropped for phase 1.** The WebGPU snapshot this Core release would pin
  (`<version>-SNAPSHOT`) isn't published for 0.12.0 at the time of writing, independent of Studio's
  own web-export gap (#331, #442). `app/webApp` is left out of `project/` until that's back and web
  export is unblocked.
- **Naga desktop native (Core-side, found and already fixed).** Building and headless-rendering this
  repository's own scene on ubuntu-latest first failed with a published-artifact gap, not anything in
  this repository: `com.awakekt.awake.asset:shader-compiler-desktop` embedded the Naga native shader
  compiler for macOS only, so `VulkanEngine` can't start on Linux (or Windows) from Maven Central
  alone. Filed and already fixed upstream:
  [awakekt/awake#795](https://github.com/awakekt/awake/issues/795), fixed by
  [awakekt/awake#798](https://github.com/awakekt/awake/pull/798), shipping in Core 0.12.0. This
  repository pins 0.12.0 once it finishes publishing to Maven Central.

## License

Apache-2.0. See [LICENSE](LICENSE).
