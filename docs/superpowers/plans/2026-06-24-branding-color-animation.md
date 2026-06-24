# Branding, Color Consistency & Water Animation — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship a droplet-in-ring brand identity (icon/splash/in-app) on Android + iOS, bring the native iOS UI onto the lavender color system, and make the water animation work on both platforms.

**Architecture:** Mark source is `brand/mark.svg`. Android icon = vector drawables (adaptive) + magick-generated legacy mipmaps; in-app marks are drawn programmatically (Compose `Canvas`, SwiftUI `Shape`) so they theme-tint and stay crisp. iOS gets a generated 1024 PNG icon, a lavender `Theme.swift`, a recolored `ContentView.swift`, and a ported wave animation.

**Tech Stack:** Kotlin Multiplatform, Jetpack Compose, SwiftUI, Material 3, ImageMagick (icon raster), androidx.core.splashscreen.

## Global Constraints
- Lavender tokens (verbatim): primary `#6C5CE7`, bright `#8B7BF0`, deep `#5A4FD0`, light(dark-mode primary) `#A99CFF`; light bg `#EFEDFB`, surface `#FFFFFF`, surfaceVariant `#E4DFF7`, secondaryContainer `#D9D3F5`, ink `#1E1B3A`, muted `#6E6A8F`; dark bg `#15131F`, surface `#211E33`, surfaceVariant `#2E2A45`, ink `#E7E4F5`, muted `#A7A2C4`.
- Mark geometry (120-unit viewBox): teardrop `M60 26 C60 26 84 54 84 74 C84 87.3 73.3 98 60 98 C46.7 98 36 87.3 36 74 C36 54 60 26 60 26 Z`; ring = circle cx60 cy60 r51 stroke 7.5 (full ring for the app icon; ~75% open arc acceptable for in-app).
- Android Compose colors come from `MaterialTheme.colorScheme`; iOS colors come from the new `Theme.swift` `HydroColors` (no hardcoded `Color(red:)`/`.cyan`/`.blue`/`.indigo` for content/surfaces).
- Verification is compile + visual (no UI unit tests): `./gradlew :androidApp:assembleDebug` and `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' -configuration Debug build` must both report SUCCESS.
- Commit per task; end messages with `Claude-Session: https://claude.ai/code/session_01GGVhUyiPP7Aa6pTkjDDEra`. Do NOT git-add anything under `.superpowers/`.

---

## File Structure
- Create: `brand/mark-icon.svg` (icon lockup), generated `icon-1024.png`; Android `drawable/ic_launcher_background.xml`, `drawable/ic_launcher_foreground.xml` (rewrite); regenerated `mipmap-*/ic_launcher*.png`; `components/HydroLogo.kt`; iOS `Theme.swift`, `HydroLogo.swift` (or inline in ContentView).
- Modify: `androidApp/.../AndroidManifest.xml` (icon ref stays), `values/themes.xml` (+`values-night`), `MainActivity.kt`, `androidApp/build.gradle.kts` (splashscreen dep), `HomeScreen.kt` (header), `iosApp/iosApp/ContentView.swift` (recolor + wave + header), `Info.plist` (launch), `AppIcon.appiconset/Contents.json`, `AccentColor.colorset/Contents.json`.

---

## Task 1: Generate the app-icon raster (ImageMagick primitives)

**Files:** Create `brand/build-icon.sh`, `brand/icon-1024.png`.

**Interfaces:** Produces `brand/icon-1024.png` (1024×1024, periwinkle gradient bg + white droplet-in-ring), the source for iOS AppIcon + Android legacy mipmaps.

- [ ] **Step 1: Write `brand/build-icon.sh`** drawing with magick PRIMITIVES (the MSVG engine drops gradients/clips — do NOT rasterize the SVG). Mark path scaled ×8.533 (120→1024):

```bash
#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
S=1024
# periwinkle radial-gradient background (full square; iOS masks corners itself)
magick -size ${S}x${S} radial-gradient:'#8B7BF0'-'#5A4FD0' bg.png
# white mark on transparent: full ring + teardrop (path scaled 120->1024, factor 8.5333)
magick -size ${S}x${S} xc:none -fill none -stroke white -strokewidth 64 \
  -draw "circle 512,512 512,77" \
  -fill white -stroke none \
  -draw "path 'M512 222 C512 222 717 461 717 632 C717 745 631 836 512 836 C393 836 307 745 307 632 C307 461 512 222 512 222 Z'" \
  mark.png
magick bg.png mark.png -compose over -composite icon-1024.png
rm -f bg.png mark.png
echo "wrote icon-1024.png"
```

- [ ] **Step 2: Run it and verify the render.** `bash brand/build-icon.sh` then open/read `brand/icon-1024.png`. Expected: a white droplet centered inside a white ring on a periwinkle gradient. If the droplet/ring overlap badly or the drop is mis-scaled, adjust the `circle` radius (currently 512→77 ≈ r435) and teardrop coordinates and re-run. Do NOT proceed until the raster looks correct.

- [ ] **Step 3: Commit.**
```bash
git add brand/build-icon.sh brand/icon-1024.png
git commit -m "Add icon raster generator (droplet-in-ring on periwinkle)"
```

---

## Task 2: iOS app icon + accent color

**Files:** Modify `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/Contents.json`; create `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/icon-1024.png`; modify `AccentColor.colorset/Contents.json`.

- [ ] **Step 1: Copy the icon** `cp brand/icon-1024.png iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/icon-1024.png`.
- [ ] **Step 2: Read the existing `Contents.json`** (3 universal 1024 slots: any/dark/tinted). Set the default (`idiom":"universal","platform":"ios","size":"1024x1024"` with no `appearances`) `"filename":"icon-1024.png"`. Leave the dark/tinted slots without a filename (iOS falls back to default) OR point them at the same file. Keep JSON valid.
- [ ] **Step 3: Set `AccentColor.colorset/Contents.json`** to periwinkle with a dark variant:
```json
{ "colors":[
  {"idiom":"universal","color":{"color-space":"srgb","components":{"red":"0x6C","green":"0x5C","blue":"0xE7","alpha":"1.000"}}},
  {"idiom":"universal","appearances":[{"appearance":"luminosity","value":"dark"}],"color":{"color-space":"srgb","components":{"red":"0xA9","green":"0x9C","blue":"0xFF","alpha":"1.000"}}}
], "info":{"author":"xcode","version":1} }
```
- [ ] **Step 4: Build** `xcodebuild … -sdk iphonesimulator … build` → BUILD SUCCEEDED (asset catalog compiles). 
- [ ] **Step 5: Commit** the appiconset + accent color.

---

## Task 3: Android adaptive icon (vector drawables) + legacy mipmaps

**Files:** Rewrite `androidApp/src/main/res/drawable/ic_launcher_background.xml` and `drawable-v24/ic_launcher_foreground.xml`; regenerate `mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher.png` + `ic_launcher_round.png`.

- [ ] **Step 1: Background vector** — replace `drawable/ic_launcher_background.xml` with a 108dp vector, periwinkle gradient fill:
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
  <path android:pathData="M0,0h108v108h-108z">
    <aapt:attr name="android:fillColor">
      <gradient android:type="linear" android:startX="0" android:startY="0" android:endX="108" android:endY="108"
        android:startColor="#8B7BF0" android:endColor="#5A4FD0"/>
    </aapt:attr>
  </path>
</vector>
```
- [ ] **Step 2: Foreground vector** — replace `drawable-v24/ic_launcher_foreground.xml` with the white droplet-in-ring centered in the 108dp canvas safe zone (mark in the central 72dp; viewport 108, mark scaled/offset ×0.6 + centered). Use white fill:
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
  <!-- ring: circle r~30 centered (54,54), stroke 4.2, white -->
  <path android:strokeColor="#FFFFFF" android:strokeWidth="4.2" android:fillColor="@android:color/transparent"
    android:pathData="M54,24 a30,30 0 1,0 0.01,0 z"/>
  <!-- teardrop (mark path scaled 120->~46 and centered): -->
  <path android:fillColor="#FFFFFF"
    android:pathData="M54,37 C54,37 63.2,47.7 63.2,55.4 C63.2,60.4 59.2,64.5 54,64.5 C48.8,64.5 44.8,60.4 44.8,55.4 C44.8,47.7 54,37 54,37 Z"/>
</vector>
```
(If the math looks off at preview, nudge scale/offset so the drop sits inside the ring; keep within the 66dp safe diameter.)
- [ ] **Step 3: Regenerate legacy mipmaps** from the Task-1 raster (round + square; densities 48/72/96/144/192):
```bash
cd /Users/subash-14497/AndroidStudioProjects/HydroHabit
declare -A D=( [mdpi]=48 [hdpi]=72 [xhdpi]=96 [xxhdpi]=144 [xxxhdpi]=192 )
for d in "${!D[@]}"; do px=${D[$d]}
  magick brand/icon-1024.png -resize ${px}x${px} androidApp/src/main/res/mipmap-$d/ic_launcher.png
  magick brand/icon-1024.png -resize ${px}x${px} \( +clone -alpha extract -fill black -colorize 100 -fill white -draw "circle $((px/2)),$((px/2)) $((px/2)),0" \) -alpha off -compose CopyOpacity -composite androidApp/src/main/res/mipmap-$d/ic_launcher_round.png
done
```
- [ ] **Step 4: Build** `./gradlew :androidApp:assembleDebug` → BUILD SUCCESSFUL (vector drawables + mipmaps compile).
- [ ] **Step 5: Commit** drawables + mipmaps.

---

## Task 4: Android splash screen

**Files:** Modify `androidApp/build.gradle.kts`, `androidApp/src/main/res/values/themes.xml` (create if absent) + `values-night/themes.xml`, `androidApp/src/main/kotlin/com/bose/hydrohabit/MainActivity.kt`, `AndroidManifest.xml` (apply theme).

- [ ] **Step 1: Add dependency** `implementation("androidx.core:core-splashscreen:1.0.1")` to `androidApp/build.gradle.kts` dependencies.
- [ ] **Step 2: Themes** — create `values/themes.xml`:
```xml
<resources xmlns:tools="http://schemas.android.com/tools">
  <style name="Theme.HydroHabit.Starting" parent="Theme.SplashScreen">
    <item name="windowSplashScreenBackground">#EFEDFB</item>
    <item name="windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>
    <item name="postSplashScreenTheme">@android:style/Theme.Material.Light.NoActionBar</item>
  </style>
</resources>
```
and `values-night/themes.xml` with `windowSplashScreenBackground">#15131F`.
- [ ] **Step 3: Apply theme** in `AndroidManifest.xml` — set `android:theme="@style/Theme.HydroHabit.Starting"` on `<application>` (or the launch `<activity>`).
- [ ] **Step 4: Install splash** in `MainActivity.onCreate` — call `installSplashScreen()` BEFORE `super.onCreate()`/`setContent` (import `androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen`).
- [ ] **Step 5: Build** → BUILD SUCCESSFUL.
- [ ] **Step 6: Commit.**

---

## Task 5: Compose `HydroLogo` + Home header

**Files:** Create `sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/components/HydroLogo.kt`; modify `HomeScreen.kt` `HeaderRow`.

**Interfaces:** Produces `@Composable fun HydroLogo(modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.primary)`.

- [ ] **Step 1: Implement `HydroLogo`** — a `Canvas` that draws the mark (ring arc + teardrop + inner water line) using `tint`. Use `Path` for the teardrop (scale the 120-unit coords to the canvas size), `drawArc` for the ~75% ring (stroke, rounded cap), and a clipped wave (reuse the approach from `WaterRing.kt` — read it for the wave helper). Keep it self-contained.
- [ ] **Step 2: Wire into `HeaderRow`** in `HomeScreen.kt` — replace the 💧-emoji avatar `Box` with `HydroLogo(Modifier.size(44.dp))`; keep the greeting; optionally add a small "HydroHabit" wordmark `Text` (titleMedium, bold) beside it. Preserve the 🏆/🔔 buttons + all wiring.
- [ ] **Step 3: Build** → BUILD SUCCESSFUL.
- [ ] **Step 4: Visual check** (deferred to Task 11 device pass) — logo tints periwinkle (light) / light-periwinkle (dark).
- [ ] **Step 5: Commit.**

---

## Task 6: iOS `Theme.swift` (lavender, accent-aware)

**Files:** Create `iosApp/iosApp/Theme.swift`.

**Interfaces:** Produces `struct HydroColors` with `init(scheme: ColorScheme, accentHex: String)` and members `background, surface, surfaceVariant, secondaryContainer, ink, muted, primary, secondary` (SwiftUI `Color`), plus a `hexColor(_:) -> Color` helper. A SwiftUI `EnvironmentValues.hydro` or a simple `@Environment(\.colorScheme)`-based factory used by views.

- [ ] **Step 1: Implement** the palette with the Global-Constraints hex values, light/dark via `scheme`. `primary`/`secondary` parse `accentHex` (default `#6C5CE7`); on parse failure fall back to periwinkle. Add `static func from(_ scheme: ColorScheme, accentHex: String) -> HydroColors`.
- [ ] **Step 2: Build** → BUILD SUCCEEDED.
- [ ] **Step 3: Commit.**

---

## Task 7: iOS `ContentView.swift` recolor

**Files:** Modify `iosApp/iosApp/ContentView.swift`.

**Interfaces:** Consumes `HydroColors` (Task 6).

- [ ] **Step 1: Read `ContentView.swift`** and locate every hardcoded color site from the audit: `GlassyBackground` baseBg + glow1/2/3 (~lines 12–15), `GlassCardModifier` baseColor (~51), `brandPrimary`/`brandSecondary` (~88–95), achievement gold (~562), water-ring track/arc (~117).
- [ ] **Step 2: Replace** each with `HydroColors` tokens resolved from `@Environment(\.colorScheme)` + the current `accentColor` (read from the relevant store's `reminderSettings.accentColor`; the Home/Settings models already expose state). `GlassyBackground` → `colors.background` base with faint `colors.primary`-tinted glows (or flat lavender to match Compose); `GlassCardModifier` → `colors.surface`; brand colors → `colors.primary`/`.secondary`; achievement gold → `colors.primary`. No `Color(red:)`/`.cyan`/`.blue`/`.indigo` left for content/surfaces.
- [ ] **Step 3: Build** → BUILD SUCCEEDED.
- [ ] **Step 4: Commit.**

---

## Task 8: iOS in-app `HydroLogo` (SwiftUI) + launch screen

**Files:** Add `HydroLogo` SwiftUI view (in `ContentView.swift` or `HydroLogo.swift`); modify the Home header; modify `iosApp/iosApp/Info.plist`.

- [ ] **Step 1: Implement SwiftUI `HydroLogo`** — a `Canvas`/`Path` view drawing the same mark (teardrop + ring), `tint: Color` defaulting to the theme primary. Place it in the Home header next to the title.
- [ ] **Step 2: Launch screen** — add to `Info.plist`:
```xml
<key>UILaunchScreen</key>
<dict><key>UIColorName</key><string>LaunchBackground</string></dict>
```
and add a `LaunchBackground` color set (lilac `#EFEDFB`) to `Assets.xcassets`. (A bare lilac launch background is acceptable; the mark appears once the app draws.)
- [ ] **Step 3: Build** → BUILD SUCCEEDED.
- [ ] **Step 4: Commit.**

---

## Task 9: iOS water wave animation

**Files:** Modify the iOS water ring in `ContentView.swift` (~lines 102–146).

- [ ] **Step 1: Read** the current `WaterRing`/gauge view. Keep the existing progress arc.
- [ ] **Step 2: Add an animated wave-fill** behind the arc and center text:
  - Level = `consumed/goal` clamped 0…1, fill rises from bottom; animate level changes with `.easeInOut(duration:0.9)`.
  - Wave motion via `TimelineView(.animation)` advancing a phase; draw two offset sine `Path`s (`colors.secondary`/`.primary`) clipped to the inner circle. Respect Reduce Motion (`@Environment(\.accessibilityReduceMotion)` → static fill, no phase animation).
- [ ] **Step 3: Build** → BUILD SUCCEEDED.
- [ ] **Step 4: Commit.**

---

## Task 10: Verify Compose water animation

**Files:** none unless a defect is found in `components/WaterRing.kt`.

- [ ] **Step 1:** Install + run the Android app on device; log water and watch the Home `WaterRing` — the wave should animate (horizontal motion) and the fill level should rise with `consumed/goal`.
- [ ] **Step 2:** If it animates and tracks state → no change; record the observation. If it is static, root-cause (candidates: `progress` 0 due to no goal — expected, set a profile first; or a recomposition/`waveFill` issue) and apply the minimal fix, then rebuild. Do NOT change working code speculatively.
- [ ] **Step 3:** Commit only if a fix was made.

---

## Task 11: Full verification, screenshots & summary

**Files:** none (verification); plus a short summary write-up.

- [ ] **Step 1: Clean builds** — `./gradlew clean :androidApp:assembleDebug` and the iOS simulator build; both SUCCESS.
- [ ] **Step 2: Android device** — install; capture screenshots: launcher icon, splash, Home light + dark (logo + animated wave), and confirm no color regressions.
- [ ] **Step 3: iOS simulator** — boot, install, launch; capture: app icon on springboard, launch screen, Home light + dark showing the new logo, lavender colors (no aqua), and the animated wave; toggle a different accent and confirm re-tint.
- [ ] **Step 4: Write the summary** (deliverable #4) — issues found + fixes applied, with screenshot references.
- [ ] **Step 5: Final commit** for any fixups + the summary.

---

## Self-Review
**Spec coverage:** logo source ✓(committed); Android icon T3 + raster T1; iOS icon T2; Android splash T4; iOS launch T8; Compose in-app logo T5; iOS in-app logo T8; iOS recolor T6–T7; iOS AccentColor T2; iOS wave T9; Compose wave verify T10; deliverables/screenshots/summary T11. All spec sections covered.
**Placeholder scan:** foundational pieces (icon script, vector drawables, themes, Theme.swift, Contents.json) ship complete content; the larger edits (HydroLogo Canvas, ContentView recolor, iOS wave) give concrete structure + exact color/coordinate values and instruct reading the existing file first (ContentView is large and hand-maintained; reproducing it verbatim is impractical and error-prone). The mark coordinates and color hexes are exact throughout.
**Type consistency:** `HydroLogo(modifier, tint)` (Compose) / `HydroLogo(tint:)` (SwiftUI); `HydroColors` members reused in T7/T8/T9; mark path constant identical across SVG/vector/magick (scaled). Consistent.
