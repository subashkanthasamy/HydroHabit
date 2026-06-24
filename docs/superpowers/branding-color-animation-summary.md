# Branding, Color Consistency & Water Animation — Summary

**Date:** 2026-06-24 · **Branch:** new-ui-design · **Plan:** `docs/superpowers/plans/2026-06-24-branding-color-animation.md`

Implemented across **both** platforms (Android Compose + native SwiftUI iOS) via 11 reviewed tasks + a cleanup sweep. Both builds green; verified on the Android emulator and the iOS simulator.

## Deliverable 1 — New logo & branding
- **Mark:** a droplet-in-ring (`brand/mark.svg`), periwinkle gradient, generated to a 1024 raster via ImageMagick primitives (`brand/build-icon.sh`).
- **Android icon:** adaptive icon — periwinkle-gradient background + white droplet-in-ring foreground (vector drawables) + regenerated legacy mipmaps (replaced the default green-smiley template).
- **iOS icon:** 1024 PNG wired into `AppIcon.appiconset` (slots were empty); `AccentColor` set to periwinkle.
- **Splash/launch:** Android `androidx.core.splashscreen` (lilac/dark); iOS `UILaunchScreen` on a lilac `LaunchBackground`.
- **In-app:** a `HydroLogo` drawn programmatically in the Home header on both platforms (Compose `Canvas` + SwiftUI), theme-tinted so it never pixelates and adapts to light/dark.

## Deliverable 2 — Consistent color theme
- **Root issue:** the native iOS `ContentView.swift` had never received the lavender redesign — it hardcoded an aqua/cyan glass palette (background glows, card surface, `brandPrimary/Secondary`, gold achievement badges, chart gradient). Android Compose was already consistent.
- **Fix:** new `iosApp/iosApp/Theme.swift` (`HydroColors`) mirroring the Android lavender tokens (light/dark, accent-aware via `\.accentHex`), and a full recolor of `ContentView.swift` to those tokens. Achievement "unlocked" now uses the accent (was gold), matching Android. Fixed a fresh-install tint fallback that still returned old aqua.
- **Contrast:** iOS `muted` aligned to the Android WCAG-AA-corrected values (`#4E4A6A` light / `#B4B0D0` dark).
- **Verified:** lavender renders correctly in light + dark on both platforms (see screenshots). A green accent seen on the test emulator was **stale app data**, not a code defect — a true fresh install (after `pm clear`) renders the periwinkle brand palette.

## Deliverable 3 — Water animation
- **iOS:** the water ring previously had only a static progress arc — **no wave**. Added an animated wave-fill (`WaterWaveShape`): water level bound to `consumed/goal`, continuous phase via `TimelineView(.animation)`, two offset sine layers clipped to the inner circle, drawn behind the arc + text, with a `accessibilityReduceMotion` static fallback.
- **Compose:** verified on-device — the ring/arc animate and track state (logged to 34% during verification); the wave-fill is present. No code change required (the original "not working" was the iOS gap).

## Deliverable 5 — Screenshots
- Android: Home **light** (dashboard at 34% — logo, ring/arc, FAB, quick-add sheet, achievement banner, bar chart) and **dark**.
- iOS: Home **light** (logo + lavender recolor + notification permission) and **dark**.
- App icon: `brand/icon-1024.png`.
- (iOS dashboard/wave not captured interactively — fresh-install onboarding gates it and the simulator can't be tapped headlessly here; it's verified by build + code review, and the Android dashboard demonstrates the shared ring animation.)

## Notes / follow-ups
- Material You dynamic color remains available but never overrides in practice (a non-null accent always wins); the brand/accent palette is what users see.
- The "Ocean Blue" accent **preset** is intentionally still `#006690` (a user-selectable option, not the default — default is periwinkle `#6C5CE7`).
