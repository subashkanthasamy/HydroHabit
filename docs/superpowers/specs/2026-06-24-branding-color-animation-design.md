# Branding, Color Consistency & Water Animation — Design Spec

**Date:** 2026-06-24 · **Branch:** new-ui-design
**Scope:** Both platforms — Android (Compose) **and** the native SwiftUI iOS app.

## Context
Three gaps remain after the lavender redesign + M3 pass:
1. **No brand identity.** Android ships the default green-smiley template icon; iOS `AppIcon.appiconset` slots are **empty**; neither platform has a splash/launch screen; in-app branding is the plain text "HydroHabit" + 💧 emoji.
2. **Color inconsistency.** The Android Compose UI is consistent (lavender tokens). The native iOS `ContentView.swift` is a parallel hand-written UI that **never got the lavender redesign** — it hardcodes an aqua/cyan glass palette (`brandPrimary`/`brandSecondary`, `GlassyBackground` glows, `GlassCardModifier`, gold achievement badges) and the iOS `AccentColor` is empty.
3. **Water animation.** Compose has an animated wave-fill `WaterRing` (looks correct in code); **iOS has no wave at all** — only a static progress arc.

Outcome: a single, recognizable **droplet-in-ring** brand mark wired across icon/splash/header on both platforms; the iOS UI brought onto the lavender design system; and a working, state-driven water animation on both platforms.

Approved mark source: **`brand/mark.svg`** (droplet-in-ring; periwinkle gradient `#8B7BF0`→`#6C5CE7`, inner water line, open progress ring). Mockup: artifact `b0c9a4cd-6336-416b-ae89-96ce4aaa10b5`.

---

## 1. Logo & branding

**Source of truth:** `brand/mark.svg` (committed). All assets derive from it.

### Mark components (for programmatic in-app draws)
- Open progress ring (≈75% arc, rounded cap) + centered teardrop with an inner water line. Two render modes: **gradient** (icon/splash/wordmark) and **mono/currentColor** (theme-tinted in-app header).

### Android
- **Adaptive icon:** replace `androidApp/src/main/res/drawable/ic_launcher_background.xml` (→ periwinkle gradient, `#8B7BF0`→`#5A4FD0`) and `drawable-v24/ic_launcher_foreground.xml` (→ white droplet-in-ring **vector drawable**, hand-authored `<vector>`/`<path>`, centered in the 108dp safe zone). Keep existing `mipmap-anydpi-v26/ic_launcher*.xml` references. Regenerate the legacy `mipmap-*/ic_launcher.png` densities from the SVG (ImageMagick) so pre-API-26 launchers match.
- **Splash:** add `androidx.core:core-splashscreen`; define `Theme.HydroHabit.Starting` in `androidApp/src/main/res/values/themes.xml` with `windowSplashScreenBackground` = lilac `#EFEDFB` (and a dark `values-night` = `#15131F`) and `windowSplashScreenAnimatedIcon` = the foreground drawable; call `installSplashScreen()` first in `MainActivity.onCreate`.
- **In-app header:** new `components/HydroLogo.kt` — a Compose `Canvas` that draws the mark, `tint: Color = MaterialTheme.colorScheme.primary`. Replace the 💧-emoji avatar box in `HomeScreen.kt` `HeaderRow` with `HydroLogo` (and add a small wordmark text). Resolution-independent + theme-tinted (periwinkle light / light-periwinkle dark via the existing tokens).

### iOS
- **App icon:** render `brand/mark.svg` (white mark on periwinkle gradient) to a **1024×1024 PNG**, add it to `AppIcon.appiconset` (single-size universal slot) and update `Contents.json`. (Rasterize via a real SVG renderer — install `librsvg`/`rsvg-convert` if needed, or render through the browser; ImageMagick's MSVG engine drops gradients/clips, so it is NOT acceptable for the final PNG.)
- **AccentColor:** set `AccentColor.colorset/Contents.json` to periwinkle `#6C5CE7` (with a dark variant `#A99CFF`).
- **Launch screen:** add `UILaunchScreen` to `Info.plist` with background = lilac, or a minimal launch storyboard showing the mark centered on lilac.
- **In-app header:** new SwiftUI `HydroLogo: View` (a `Shape`/`Canvas` mirroring the mark) tinted with the theme primary; place it in the Home header next to the title.

---

## 2. Color consistency (iOS → lavender design system)

The fix is confined to the native iOS UI; Compose is already consistent.

- **New `iosApp/iosApp/Theme.swift`** — a `HydroColors` palette mirroring `sharedLogic`/`Color.kt`, resolved per `@Environment(\.colorScheme)`:
  - light: `background #EFEDFB`, `surface #FFFFFF`, `surfaceVariant #E4DFF7`, `ink #1E1B3A`, `muted #6E6A8F`, `secondaryContainer #D9D3F5`.
  - dark: `background #15131F`, `surface #211E33`, `surfaceVariant #2E2A45`, `ink #E7E4F5`, `muted #A7A2C4`.
  - **Accent-aware primary:** parse `state.reminderSettings.accentColor` (hex) for `primary`/`secondary` — mirroring Android's `generateDynamicColorScheme` (lavender base + accent-derived primary). Default `#6C5CE7`. Reuse a small hex→Color parser.
- **Rewrite `ContentView.swift` color sites** (from the audit): `GlassyBackground` base + glows → lavender background + primary-tinted soft glows (or flat lavender to match the Compose `GlassyBackground`); `GlassCardModifier` base → `surface`; `brandPrimary`/`brandSecondary` → `HydroColors.primary`/`.secondary`; achievement "unlocked" gold (`~line 562`) → theme primary; the water-ring track/arc → primary over `surfaceVariant`. No hardcoded `Color(red:…)`/`Color.cyan`/`.blue`/`.indigo` left for content/surfaces.
- **Contrast:** verify text-on-surface pairs meet WCAG AA in light + dark (reuse the values already AA-verified in `Color.kt`).

---

## 3. Water animation

- **iOS (`ContentView.swift` water ring, ~lines 102–146):** add an animated **wave-fill** inside the ring, mirroring Compose `WaterRing`:
  - Fill **level** bound to `consumed/goal` (clamped), animated with `.easeInOut` on change.
  - **Wave motion:** horizontal phase animated continuously via `TimelineView(.animation)` (respect Reduce Motion → static fill), two offset sine paths in `secondary`/`primary`, clipped to the inner circle, drawn behind the existing arc + center text.
  - Keep the existing progress arc on top.
- **Compose (`components/WaterRing.kt`):** verify on-device that the wave animates and the level tracks `progress` (state already flows via `HomeState.progress` → `animateFloatAsState`). The code reads correct; if the on-device check shows it static, root-cause then (likely candidates: `progress` staying 0 with no goal — expected; or a recomposition issue). Fix only if a real defect is observed — do not change working code speculatively.
- Both: smooth, responsive to logging, correct across screen sizes (the ring already takes a size modifier).

---

## Deliverables
1. **Brand assets** — `brand/mark.svg`; Android adaptive-icon vector drawables + regenerated mipmaps; iOS 1024 AppIcon PNG + AccentColor; splash/launch on both; in-app `HydroLogo` (Compose + SwiftUI).
2. **Consistent theme** — iOS recolored to the lavender system (`Theme.swift` + `ContentView.swift`), accent-aware, AA in light/dark.
3. **Water animation** — working state-driven wave on iOS + verified on Compose.
4. **Summary** of issues found & fixes (final write-up).
5. **Screenshots** — Android (device) + iOS (simulator): home (light+dark) showing logo + animation, app icon on springboard/launcher, splash.

## Verification
- **Android:** `./gradlew :androidApp:assembleDebug`; install on device; confirm new launcher icon, splash, in-app logo (light+dark), animated wave logging water.
- **iOS:** `xcodebuild -sdk iphonesimulator … build`; run on simulator; confirm app icon, launch screen, lavender colors everywhere (no aqua), animated wave, accent-color change re-tints.
- **Contrast/responsive:** spot-check AA pairs and small/large screens both platforms.
- No regressions to existing flows (logging, history, analytics, settings, reminders).
