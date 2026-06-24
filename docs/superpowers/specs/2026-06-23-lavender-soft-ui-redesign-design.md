# Lavender Soft-UI Redesign — Design Spec

**Date:** 2026-06-23
**Status:** Approved (design direction confirmed via Home mockup)

## Context

HydroHabit currently uses a "Glassmorphism" design language: frosted translucent cards
(`glassCard()`), an animated glowing-blob background (`GlassyBackground()`), and an
aqua/teal palette with light + dark themes and a user accent-color picker.

The user wants to re-skin the app to match the **HydroTrack** Dribbble reference
(`images/app-ui.webp`, `images/ui.webp`, `images/ui3.png`): a soft, solid, **lavender**
look — white cards with soft shadows on a pale lilac ground, periwinkle accent, a signature
bottom nav with a raised center droplet FAB.

This is a **full re-theme** of all five screens plus the design tokens. Outcome: the app
adopts the lavender soft-UI identity while keeping its existing MVI logic, light/dark
support, and accent-color picker intact.

### Confirmed decisions
- **Scope:** full app re-theme (all 5 screens + design tokens).
- **Theme:** keep **light + dark**, lavender as the default light theme, design a matching
  dark variant; **keep the accent-color picker working** (re-tints over the lavender base).
- **Fitness row** (Calories/Heart Rate/Workout) from the reference is **omitted** — the app
  is water-only.
- **Bottom nav:** **4 tabs + center Quick-Add FAB** — Home · History · [💧 FAB] · Stats ·
  Settings. **Achievements** moves to a 🏆 button in the Home header.
- **Home Daily Drink Target** card uses a circular ring + **wave-fill** gauge as its hero
  (confirmed in mockup). No search bar (app has no search feature).

## Design Tokens

Reference mockup: `scratchpad/home-mockup.html` (artifact
`https://claude.ai/code/artifact/69d2d03c-35c5-4f1f-9b7a-5a71510afec7`).

### Colors (`sharedUI/.../theme/Color.kt`)

| Token | Light | Dark |
|---|---|---|
| `primary` (periwinkle) | `#6C5CE7` | `#A99CFF` |
| `onPrimary` | `#FFFFFF` | `#241B52` |
| `primaryContainer` | `#EDEAFB` | `#3A3270` |
| `secondary` / brand-2 | `#7B6FE8` | `#B9AEF0` |
| `secondaryContainer` (tinted "Today" block) | `#D9D3F5` | `#2C2746` |
| `background` (lilac ground) | `#EFEDFB` | `#15131F` |
| `surface` (cards) | `#FFFFFF` | `#211E33` |
| `surfaceVariant` | `#E4DFF7` | `#2E2A45` |
| `onSurface` (ink) | `#1E1B3A` | `#E7E4F5` |
| `onSurfaceVariant` (muted) | `#6E6A8F` | `#A7A2C4` |
| `error` | keep current `#BA1A1A` / `#FFB4AB` | — |

All foreground/background pairs must meet WCAG AA (≥4.5:1 for text). `#6C5CE7` on white ≈
4.5:1; verify and darken slightly if needed (the codebase already follows this pattern, see
`Aqua` comment in current `Color.kt`).

`generateDynamicColorScheme(accentHex, isDark)` is **kept** and re-anchored: backgrounds,
surfaces, and surfaceVariant use the lavender constants above (not the old `#F6FBFD`/blue
values), while `primary`/`secondary`/containers continue to derive from the accent hue. This
keeps the accent picker functional over a lavender base.

### Shape & elevation
- Corner radii: cards `28dp` (large), tiles/stat cards `22dp`, buttons/pills `16dp`,
  fully-rounded chips/avatars `50%`.
- **Soft shadow** replaces the frosted border. Use `Modifier.shadow(elevation, shape,
  spotColor = purple-tinted, ambientColor = purple-tinted)`. Two presets: card (`~10dp`)
  and small (`~6dp`).

### Typography
Keep Material 3 typography defaults but increase heading weight toward the rounded/bold feel
(use `FontWeight.Bold`/`ExtraBold` on titles in components). No new font dependency required.
If a rounded font is desired later, add to a `HydroTypography` in `Theme.kt` — out of scope
for this pass.

## Component Changes (the reuse lever)

Every screen already calls `glassCard()` and is wrapped in `GlassyBackground()`. Reimplement
**both in place** so the new look propagates with minimal per-screen edits.

1. **`Modifier.glassCard()`** (`theme/Glassmorphism.kt`) — keep name & signature; replace
   body with a **soft solid card**: `surface` fill, rounded `shape`, soft purple-tinted
   shadow, no frosted gradient/translucency. Existing params (`shape`, `shadowElevation`)
   still honored; `lightAlpha`/`darkAlpha` become no-ops (kept for source compatibility).
   Add a `softCard()` alias for new call sites; optionally rename later.
2. **`GlassyBackground()`** (`theme/Glassmorphism.kt`) — keep name; replace glowing cyan
   blobs with a **flat lilac background** (`background` token) plus the faint sparkle accents
   from the reference. Optionally a very subtle top/bottom lilac gradient.

### New reusable components (`sharedUI/.../components/`)
- **`WeekStrip`** — 7-day Sun–Sat row, today highlighted (filled periwinkle circle). On
  Home: highlights today; tapping a day deep-links to History (or read-only for v1).
- **`SegmentedControl`** — Day/Week/Month pill toggle for Analytics.
- **`BarChart`** — lavender vertical bars with a highlighted bar; used on Home (Hydration
  Stats) and Analytics.
- **`WaterRing`** (extend existing `components/WaterRing.kt`) — add a **wave-fill** interior
  option (animated wave clipped to inner circle) under the existing gradient progress arc.
- **`BottomNavBar`** (new, extracted from `MainScreen.kt`) — rounded white bar, 4 tabs +
  raised center squircle FAB.

## Per-Screen Changes

All screens: `commonMain` Composables under `sharedUI/.../`.

- **`HomeScreen.kt`** — header (avatar + greeting + 🏆 Achievements + 🔔 Reminders), date
  row with streak, `WeekStrip` in a lilac "Today" card, **Daily Drink Target** white card
  (WaterRing wave-fill gauge + "Drink Nml" button + quick-add chips from
  `state.quickAddOptions`), **Hydration Stats** `BarChart`. Keep onboarding card + error
  card, restyled. Omit fitness row and search.
- **`HistoryScreen.kt`** — "records" list: soft white rows, drink icon + time + amount,
  keep date nav + delete.
- **`AnalyticsScreen.kt`** — `SegmentedControl` (Day/Week/Month), weekly-completion checks,
  `BarChart`, summary tiles (avg, completion %, best/worst). Maps to existing
  `AnalyticsState`.
- **`AchievementsScreen.kt`** — same 2-col grid, restyled soft cards. Reachable from the
  Home 🏆 button (see nav).
- **`SettingsScreen.kt`** — "My Profile": centered avatar, two summary cards (Daily Water
  Goal / Reminder Interval), grouped list rows. **Theme switcher, accent-color picker, and
  notification-sound picker stay** as styled rows.

## Navigation (`MainScreen.kt`)

- Reduce bottom tabs to **4**: `HOME, HISTORY, ANALYTICS, SETTINGS`.
- Add a **center Quick-Add FAB** (periwinkle squircle, droplet icon) that triggers quick-add
  water — opens a small bottom sheet / quick-add row, dispatching
  `HomeIntent.AddWater` / `AddQuickAdd` (reuse existing intents).
- **Achievements** is no longer a tab. Add a 🏆 button in the Home header that navigates to
  `AchievementsScreen`. Introduce a lightweight "show achievements" overlay/state in
  `MainScreen` (e.g. a `showAchievements` boolean or a nullable overlay route) since there's
  no nav library.
- Restyle the bar via the new `BottomNavBar` component.

## Out of Scope
- New fonts, onboarding/login/select-drink screens from the reference (app has its own flow).
- Changes to MVI stores, domain, or data layers — **UI/theme only**. The one functional
  addition is wiring the Quick-Add FAB and the Achievements header button to existing
  intents/screens.

## Verification

1. **Build both targets:** `./gradlew :androidApp:assembleDebug` (and the iOS framework if
   convenient) compiles clean.
2. **Run the Android app** (emulator) and visually confirm against the mockup:
   - Home matches the artifact (header, week strip, ring+wave gauge, bar chart, nav + FAB).
   - All 5 destinations reachable; Quick-Add FAB adds water and updates the ring; 🏆 opens
     Achievements.
   - History, Analytics, Achievements, Settings all render in the lavender style with no
     leftover glass/cyan.
3. **Theme checks:** toggle **dark mode** — matching dark variant, AA contrast holds.
   Change **accent color** in Settings — UI re-tints over the lavender base, backgrounds stay
   lilac/indigo.
4. **No regressions:** existing flows (log water, view history, streak, achievements unlock
   banner, settings save) still work.
