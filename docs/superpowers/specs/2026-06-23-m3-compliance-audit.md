# Material 3 Compliance Audit & Fix Plan — HydroHabit

**Date:** 2026-06-23 · **Branch:** new-ui-design
**Constraint (binding):** keep the bespoke lavender soft-UI look; make the implementation M3-correct. No swapping custom components for stock M3 where the custom look is intentional.
**Source audits:** `scratchpad/audit-A-designsystem.md`, `audit-B-home-history.md`, `audit-C-analytics-settings.md` (97 raw findings → deduped below).

---

## Deliverable 1 — Issue list (by theme, deduped)

### T1. Color & contrast (Critical) — root causes that cascade
- **`Color.kt:33` light `onSurfaceVariant #6E6A8F` on `surfaceVariant #E4DFF7` ≈ 3.9:1** (fails AA 4.5:1). Cascades to BarChart labels, WeekStrip day labels, SegmentedControl unselected text, QuickAddSheet/Settings hint text.
- **`Color.kt:23` light `secondary #7B6FE8` + white `onSecondary` ≈ 3.8:1** (fails AA for text on secondary).
- **`Color.kt:52` dark `onSurfaceVariant #A7A2C4` on `surfaceVariant #2E2A45` ≈ 4.3:1** (marginal).
- **`Color.kt` dynamic scheme** hardcodes `onPrimary`/`onSecondary` regardless of accent lightness → a light accent yields failing on-color. Compute on-color by luminance.
- **Token pairing**: `onSurface` used on `secondaryContainer` backgrounds (should be `onSecondaryContainer`) — Home streak chip & "This Week" pill, Analytics StatTile & date pill.
- **Low-alpha tints** hurting visibility/contrast: Achievements card tint `primaryContainer α0.30` + badge `primary α0.12`/`onSurface α0.06`; Settings divider `outlineVariant α0.5`; WeekStrip unselected `surface α0.6` (near-invisible on lilac); BarChart unhighlighted bar `surfaceVariant α0.7`.
- **`Glassmorphism.kt:44`** `softCard` shadow `spotColor` hardcoded purple `#4C3C8C` → won't follow dynamic accent. Use `colorScheme.primary`.

### T2. Touch targets < 48dp (Critical) — affects core journeys
WeekStrip day columns; SegmentedControl segments (~40dp); BottomNavBar NavTab (~44dp) + FAB effective upper half (offset); Home quick-add chips (forced 32dp) + SoftIconButton (44dp); History prev/next nav (40dp) + delete; Achievements back button (40dp); Settings "Preview" button + sound-row controls. Fix via `minimumInteractiveComponentSize()` / `sizeIn(min=48.dp)` while keeping the small visual size.

### T3. Accessibility semantics (Critical/Important)
- FAB: no `contentDescription`/`Role.Button` (emoji read literally).
- `SoftIconButton`: accepts `contentDesc` but never applies it to semantics (🏆/🔔 read as emoji names).
- NavTab: add `Role.Tab` + `selected` + merged label; mark icon decorative (stops double-announce).
- WeekStrip / SegmentedControl: add selection `Role` + state semantics.
- History delete & Achievements back: add `contentDescription`.
- **Color-only state** (WCAG 1.4.1): Analytics daily-completion rows and Achievements locked/unlocked convey meaning by color/glyph only → add merged row/card `contentDescription` ("goal met"/"unlocked, NN%").
- BarChart: add merged `contentDescription` summarizing values.
- Decorative emoji (avatar 💧, History source icons, insight emoji) → mark decorative/empty description.
- Loading indicators → `contentDescription`; empty states → `liveRegion = Polite`.

### T4. Button variants & emphasis (Important)
- History prev/next: `TextButton` (lowest emphasis) used for primary nav → `IconButton` (48dp).
- History delete: `TextButton`+"✕" → `IconButton` + `Icon(Close)` + `contentDescription`, error-tinted.
- Settings "Preview": hand-recolored filled `Button` → `FilledTonalButton`.
- Settings "Apply interval": secondary sub-action → `FilledTonalButton` (keep Save as the one Filled primary).
- QuickAddSheet chips: `FilterChip` with permanent `selected=false` + dead selected-colors → `SuggestionChip` (one-shot triggers); remove the `softCard` wrapper (double-surface).

### T5. Forms & input fields (Important)
Settings weight/age/interval/hex and QuickAddSheet amount fields: add `label`, `supportingText`, `isError` states; validate ranges; hex regex `^#[0-9A-Fa-f]{6}$`; correct `keyboardOptions` (number / hex chars, no autocorrect); surface why Save/Apply no-ops; fix accent hex/preset state race (validate before emit).

### T6. Overflow & responsiveness (Critical/Important)
- Settings accent preset chips: non-wrapping `Row` clips right-most off-screen → `FlowRow` (also theme/strategy/sound rows).
- Settings sound rows: chip + Preview `SpaceBetween` collide on narrow → weight/FlowRow.
- Analytics BarChart: ~30 monthly bars become hairlines + colliding labels → horizontal scroll for long periods / thin to every Nth label.

### T7. Loading / empty / error states (Critical/Important)
- History: **no loading guard** → "No entries" flashes before data. Add `isLoading` branch (mirror Home's `LoadingState`).
- Analytics: loading vs empty conflated (both show "log some water") → distinct loading branch.
- Home ErrorCard: `.background(...)` after `.clip()` paints outside rounded corners on older APIs → fix modifier order; add dismiss or auto-clear.

### T8. Typography type-scale bypass (Minor, pervasive)
Many `fontWeight = Bold/SemiBold/ExtraBold` layered on typed `MaterialTheme.typography.*` styles (Home, History, WaterRing, WeekStrip, QuickAddSheet, Analytics, Achievements, Settings); a few `Text`s with no explicit `style` (Home RecentActivity). Prefer the type scale; where a heavier weight is genuinely wanted, use the next-heavier role or define it once in `HydroTypography`.

### T9. Misc (Minor)
`glassCard` ignored params → `@Deprecated`; `SoftIconButton` `tonalElevation 0` suppresses hover/focus tint; no `animateColorAsState` on selected↔unselected transitions; Home Reminders icon is a no-op (disable until wired); Achievements root no-op `clickable`; Settings avatar pencil is a false affordance (remove or wire); App theme mode is stringly-typed (note only).

---

## Deliverable plan — fix tasks (one file each → reviewable units)

Foundational first (cascades), then components, then screens.

1. **Color.kt** — darken light `onSurfaceVariant`→~`#4E4A6A`, lighten dark→~`#B4B0D0`; lower light `secondary`→~`#5A4FD0` (or keep decorative + ensure no text on it); luminance-based `onPrimary`/`onSecondary` in `generateDynamicColorScheme`. Re-verify all pairs AA in light+dark.
2. **Glassmorphism.kt** — `softCard` spot/ambient → `colorScheme.primary.copy(alpha=…)`; `@Deprecated` the unused `glassCard` params.
3. **WeekStrip.kt** — `minimumInteractiveComponentSize()`, clip ripple to circle, unselected bg → `surfaceVariant` (solid), `Role`+selected semantics, drop weight override.
4. **BarChart.kt** — solid two-stop gradient (no alpha), merged `contentDescription`.
5. **SegmentedControl.kt** — segment `heightIn(min=48.dp)`, `Role.Tab`+`selected`/`stateDescription`, pressed-state via interactionSource, `animateColorAsState`.
6. **BottomNavBar.kt** — FAB `contentDescription`+`Role.Button` (+size so offset halves ≥48dp); NavTab `minimumInteractiveComponentSize()`+`Role.Tab`+`selected`+merged label, icon decorative; `animateColorAsState` for tint.
7. **WaterRing.kt** — drop `displaySmall` Bold override; decorative-wave comment (keep existing good semantics).
8. **QuickAddSheet.kt** — chips → `SuggestionChip` (remove `softCard`, drop dead selected colors), add `label` to amount field + validation, title typography to a heavier role.
9. **HomeScreen.kt** — quick-add chip + SoftIconButton 48dp targets; apply `contentDesc`+`Role` in SoftIconButton, raise `tonalElevation`; decorative emoji semantics (avatar/insight); `onSecondaryContainer` pairing (streak/“This Week”); ErrorCard modifier order + behavior; loading `contentDescription`; empty `liveRegion`; explicit text styles; disable no-op Reminders button.
10. **HistoryScreen.kt** — add loading guard; prev/next → `IconButton` 48dp; delete → `IconButton`+`Icon(Close)`+desc; source emoji decorative; merged row semantics; empty `liveRegion`; typography.
11. **AnalyticsScreen.kt** — distinct loading branch; daily-completion row merged semantics (not color-alone); `onSecondaryContainer` pairing; BarChart long-period overflow (scroll/thin labels); typography.
12. **AchievementsScreen.kt** — back button 48dp+desc+ripple clip; remove root no-op clickable; card merged semantics (locked/unlocked+%); raise low-alpha tints for contrast; progress track contrast; typography.
13. **SettingsScreen.kt** — `FlowRow` for accent/theme/strategy/sound chip groups; Preview & Apply → `FilledTonalButton` 48dp; form validation (isError/supportingText/hex regex/keyboard) + fix preset↔hex state race; divider full-alpha; `onSecondaryContainer` pairing; avatar pencil (remove false affordance); typography.
14. **Verification** — clean build; install on device; screenshots of major changes (Home, History, Analytics, Achievements, Settings, FAB sheet) in light + dark; spot-check contrast and 48dp targets; confirm accent picker + no regressions.

**Severity totals (deduped):** ~10 Critical themes, ~20 Important, ~18 Minor.
