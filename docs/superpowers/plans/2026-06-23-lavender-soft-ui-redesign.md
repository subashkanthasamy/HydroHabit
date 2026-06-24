# Lavender Soft-UI Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Re-skin all five HydroHabit screens and the design tokens from the current glassmorphism look to a lavender soft-UI matching the HydroTrack reference, keeping light/dark themes, the accent-color picker, and all MVI logic intact.

**Architecture:** The whole UI funnels through three shared files — `theme/Color.kt` (color schemes), `theme/Glassmorphism.kt` (`glassCard()` modifier + `GlassyBackground()`), and `MainScreen.kt` (nav). Reimplementing `glassCard()`/`GlassyBackground()` in place propagates the new card/background style to every screen with no per-screen edits, so screen tasks become layout refinements rather than rewrites. New small components (`WeekStrip`, `SegmentedControl`, `BarChart`, `BottomNavBar`, wave-fill `WaterRing`) carry the reference's signature elements.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Material 3. All UI in `sharedUI/src/commonMain`. No new dependencies.

## Global Constraints

- All edited Composables live in `sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/` (shared by Android + iOS). Android-specific theme code in `sharedUI/src/androidMain/`.
- **UI/theme only** — do NOT change anything under `sharedLogic/` (stores, contracts, domain, data). The only new wiring is connecting the Quick-Add FAB and Achievements header button to **existing** intents/screens.
- Keep **light + dark** themes; lavender is the default light theme. All text/background pairs must meet **WCAG AA ≥ 4.5:1** (codebase already follows this — see `Color.kt` darkening comments).
- Keep the **accent-color picker** working: `generateDynamicColorScheme()` must still derive primary/secondary from the accent hue, over the lavender background/surface constants.
- Compile check per task: `./gradlew :androidApp:assembleDebug`. There is no UI unit-test harness; verification is compile + visual against the mockup artifact `https://claude.ai/code/artifact/69d2d03c-35c5-4f1f-9b7a-5a71510afec7` and reference `images/app-ui.webp`.
- Commit after each task. End commit messages with `Claude-Session: https://claude.ai/code/session_01GGVhUyiPP7Aa6pTkjDDEra`.
- Preserve every existing call site's state wiring (the `state`/`dispatch`/`onX` callbacks passed into each screen). Restyle the rendering, not the data flow.

---

## File Structure

**Modified:**
- `theme/Color.kt` — lavender light/dark palettes + re-anchored dynamic scheme.
- `theme/Glassmorphism.kt` — `glassCard()` → soft card; `GlassyBackground()` → lilac bg; add `softCard()` alias + shadow helpers.
- `MainScreen.kt` — 4-tab nav + Quick-Add FAB + Achievements overlay; use new `BottomNavBar`.
- `HomeScreen.kt`, `HistoryScreen.kt`, `AnalyticsScreen.kt`, `AchievementsScreen.kt`, `SettingsScreen.kt` — layout refinements.
- `components/WaterRing.kt` — add wave-fill option.

**Created (in `components/`):**
- `WeekStrip.kt`, `SegmentedControl.kt`, `BarChart.kt`, `BottomNavBar.kt`, `QuickAddSheet.kt`.

---

## Task 1: Lavender color tokens

**Files:**
- Modify: `sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/theme/Color.kt`

**Interfaces:**
- Produces: `HydroLightColors`, `HydroDarkColors` (lavender), `generateDynamicColorScheme(accentColorHex: String, isDark: Boolean): ColorScheme` (signature unchanged). Adds public constants `LavenderLightBackground`, `LavenderLightSurface`, `LavenderDarkBackground`, `LavenderDarkSurface` for reuse by backgrounds.

- [ ] **Step 1: Read the current file** to preserve `parseHexColor`, `toHsl`, `hslToColor` (keep them verbatim — they're reused by the accent picker).

- [ ] **Step 2: Replace the palette definitions and dynamic-scheme background anchors.** Replace the top brand constants and both color schemes with:

```kotlin
// Lavender soft-UI brand palette — periwinkle accents on a pale lilac ground.
private val Periwinkle = Color(0xFF6C5CE7)      // primary, AA on white (verify ≥4.5:1)
private val PeriwinkleBright = Color(0xFF7B6FE8) // decorative brand-2 (arcs, FAB)
private val Ink = Color(0xFF1E1B3A)              // primary text on light

val LavenderLightBackground = Color(0xFFEFEDFB)
val LavenderLightSurface = Color(0xFFFFFFFF)
val LavenderDarkBackground = Color(0xFF15131F)
val LavenderDarkSurface = Color(0xFF211E33)

val HydroLightColors = lightColorScheme(
    primary = Periwinkle,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDEAFB),
    onPrimaryContainer = Color(0xFF221A52),
    secondary = PeriwinkleBright,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9D3F5),
    onSecondaryContainer = Color(0xFF2A2455),
    tertiary = Color(0xFF7A6E9E),
    background = LavenderLightBackground,
    onBackground = Ink,
    surface = LavenderLightSurface,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE4DFF7),
    onSurfaceVariant = Color(0xFF6E6A8F),
    error = Color(0xFFBA1A1A),
)

val HydroDarkColors = darkColorScheme(
    primary = Color(0xFFA99CFF),
    onPrimary = Color(0xFF241B52),
    primaryContainer = Color(0xFF3A3270),
    onPrimaryContainer = Color(0xFFEDEAFB),
    secondary = Color(0xFFB9AEF0),
    onSecondary = Color(0xFF241B52),
    secondaryContainer = Color(0xFF2C2746),
    onSecondaryContainer = Color(0xFFE7E4F5),
    tertiary = Color(0xFFC9C0EA),
    background = LavenderDarkBackground,
    onBackground = Color(0xFFE7E4F5),
    surface = LavenderDarkSurface,
    onSurface = Color(0xFFE7E4F5),
    surfaceVariant = Color(0xFF2E2A45),
    onSurfaceVariant = Color(0xFFA7A2C4),
    error = Color(0xFFFFB4AB),
)
```

- [ ] **Step 3: Re-anchor `generateDynamicColorScheme`'s backgrounds/surfaces** to the lavender constants. In the function, change the dark branch's `background`/`surface`/`surfaceVariant` to `LavenderDarkBackground`/`LavenderDarkSurface`/`Color(0xFF2E2A45)`, and the light branch's to `LavenderLightBackground`/`LavenderLightSurface`/`Color(0xFFE4DFF7)`. Update the hard-coded `onBackground`/`onSurface` from `DeepBlue` to `Ink` (light) and `Color(0xFFE7E4F5)` (dark). Leave the hue-derived primary/secondary/container logic unchanged. Update the `parseHexColor` fallback color from `0xFF006690` to `0xFF6C5CE7`.

- [ ] **Step 4: Compile.** Run: `./gradlew :androidApp:assembleDebug`
Expected: BUILD SUCCESSFUL (any unresolved `DeepBlue`/`Aqua` references mean a leftover — remove it).

- [ ] **Step 5: Commit.**

```bash
git add sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/theme/Color.kt
git commit -m "Replace aqua palette with lavender soft-UI tokens

Claude-Session: https://claude.ai/code/session_01GGVhUyiPP7Aa6pTkjDDEra"
```

---

## Task 2: Soft card + lilac background

**Files:**
- Modify: `sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/theme/Glassmorphism.kt`

**Interfaces:**
- Consumes: `LavenderLightBackground`/`LavenderDarkBackground` from Task 1.
- Produces: `Modifier.glassCard(shape, borderWidth, lightAlpha, darkAlpha, shadowElevation)` (signature unchanged — `lightAlpha`/`darkAlpha` retained but unused), `Modifier.softCard(shape, elevation)` alias, `GlassyBackground(modifier, content)` (signature unchanged).

- [ ] **Step 1: Reimplement `glassCard()`** as a solid soft card. Replace the body so it renders a `surface`-filled rounded card with a soft purple-tinted shadow and no translucency:

```kotlin
@Composable
fun Modifier.glassCard(
    shape: Shape = RoundedCornerShape(24.dp),
    borderWidth: Dp = 0.dp,                 // retained for source compat; unused
    lightAlpha: Float = 0.65f,              // retained for source compat; unused
    darkAlpha: Float = 0.12f,               // retained for source compat; unused
    shadowElevation: Dp = 10.dp,
): Modifier = this.softCard(shape = shape, elevation = shadowElevation)

@Composable
fun Modifier.softCard(
    shape: Shape = RoundedCornerShape(24.dp),
    elevation: Dp = 10.dp,
): Modifier {
    val spot = Color(0xFF4C3C8C)
    return this
        .shadow(elevation = elevation, shape = shape, ambientColor = spot, spotColor = spot)
        .background(color = MaterialTheme.colorScheme.surface, shape = shape)
        .clip(shape)
}
```

Add imports: `androidx.compose.material3.MaterialTheme`, `androidx.compose.ui.draw.clip`. Remove now-unused `border`, `Brush`, `isSystemInDarkTheme` imports if the compiler flags them.

- [ ] **Step 2: Reimplement `GlassyBackground()`** as a flat lilac background with faint sparkle accents:

```kotlin
@Composable
fun GlassyBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier = modifier.fillMaxSize().background(scheme.background)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sparkle = scheme.primary.copy(alpha = 0.10f)
            drawCircle(sparkle, radius = size.minDimension * 0.012f, center = Offset(size.width * 0.12f, size.height * 0.08f))
            drawCircle(sparkle, radius = size.minDimension * 0.016f, center = Offset(size.width * 0.88f, size.height * 0.06f))
            drawCircle(sparkle, radius = size.minDimension * 0.010f, center = Offset(size.width * 0.78f, size.height * 0.30f))
        }
        content()
    }
}
```

- [ ] **Step 3: Compile.** Run: `./gradlew :androidApp:assembleDebug`
Expected: BUILD SUCCESSFUL. All existing `glassCard()` call sites now render solid soft cards.

- [ ] **Step 4: Visual smoke check.** Launch the app (`./gradlew :androidApp:installDebug` then open it, or Android Studio run). Every screen should now show white cards on a lilac background — no frosted glass, no cyan glow. Layouts will be rough (refined per-screen later); confirm the new card/background base is live.

- [ ] **Step 5: Commit.**

```bash
git add sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/theme/Glassmorphism.kt
git commit -m "Reimplement glassCard and GlassyBackground as lavender soft-UI

Claude-Session: https://claude.ai/code/session_01GGVhUyiPP7Aa6pTkjDDEra"
```

---

## Task 3: WeekStrip component

**Files:**
- Create: `sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/components/WeekStrip.kt`

**Interfaces:**
- Produces: `@Composable fun WeekStrip(today: LocalDate, modifier: Modifier = Modifier, onDayClick: (LocalDate) -> Unit = {})`.

- [ ] **Step 1: Implement** a Sun–Sat row for the week containing `today`, today highlighted with a filled `primary` circle. Use `kotlinx.datetime.LocalDate` and compute the week's Sunday by subtracting `today.dayOfWeek.ordinal+1 mod 7` days (DayOfWeek MONDAY=0..SUNDAY=6 in kotlinx; map so Sunday starts the row to match the reference). Each day: a `Column` with weekday abbreviation (`Sun`,`Mon`,…) above a 34dp circle holding the day number. Selected: `primary` fill + white text; else translucent white fill + muted text. Wrap the row in a `secondaryContainer`-colored rounded card (the lilac "Today" block) — or expose just the strip and let HomeScreen wrap it. Implement as just the strip; HomeScreen wraps.

```kotlin
@Composable
fun WeekStrip(
    today: LocalDate,
    modifier: Modifier = Modifier,
    onDayClick: (LocalDate) -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    // kotlinx DayOfWeek: MONDAY..SUNDAY. Reference week starts Sunday.
    val daysFromSunday = today.dayOfWeek.isoDayNumber % 7  // Sun=0, Mon=1,..., Sat=6
    val sunday = today.minus(daysFromSunday, DateTimeUnit.DAY)
    val labels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        repeat(7) { i ->
            val day = sunday.plus(i, DateTimeUnit.DAY)
            val selected = day == today
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.clickable { onDayClick(day) }
            ) {
                Text(labels[i], style = MaterialTheme.typography.labelSmall,
                    color = if (selected) scheme.primary else scheme.onSurfaceVariant)
                Box(
                    modifier = Modifier.size(34.dp).clip(CircleShape)
                        .background(if (selected) scheme.primary else Color.White.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${day.dayOfMonth}", style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) scheme.onPrimary else scheme.onSurface)
                }
            }
        }
    }
}
```

Add imports as needed (`kotlinx.datetime.*`, compose layout/material3/ui). Use the project's existing kotlinx-datetime usage in `HistoryScreen.kt`/stores as the reference for available APIs (`isoDayNumber`, `minus`, `plus`, `DateTimeUnit`).

- [ ] **Step 2: Compile.** Run: `./gradlew :androidApp:assembleDebug`
Expected: BUILD SUCCESSFUL. If `isoDayNumber`/`DateTimeUnit` resolve differently in the installed kotlinx-datetime version, adjust to the API the project already uses (grep `DateTimeUnit` / `dayOfWeek` in the repo).

- [ ] **Step 3: Commit.**

```bash
git add sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/components/WeekStrip.kt
git commit -m "Add WeekStrip component for Home today card

Claude-Session: https://claude.ai/code/session_01GGVhUyiPP7Aa6pTkjDDEra"
```

---

## Task 4: BarChart component

**Files:**
- Create: `sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/components/BarChart.kt`

**Interfaces:**
- Produces: `@Composable fun BarChart(bars: List<BarDatum>, modifier: Modifier = Modifier, height: Dp = 108.dp)` and `data class BarDatum(val label: String, val fraction: Float, val highlighted: Boolean = false)` where `fraction` is 0f..1f.

- [ ] **Step 1: Implement** a row of vertical bars with rounded tops, gradient lavender fill, highlighted bar using `primary`→`secondary` gradient, label beneath each.

```kotlin
data class BarDatum(val label: String, val fraction: Float, val highlighted: Boolean = false)

@Composable
fun BarChart(bars: List<BarDatum>, modifier: Modifier = Modifier, height: Dp = 108.dp) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth().height(height),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        bars.forEach { b ->
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                val frac = b.fraction.coerceIn(0f, 1f).coerceAtLeast(0.04f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight(frac)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                        .background(
                            Brush.verticalGradient(
                                if (b.highlighted) listOf(scheme.secondary, scheme.primary)
                                else listOf(scheme.surfaceVariant, scheme.surfaceVariant.copy(alpha = 0.7f))
                            )
                        )
                )
                Spacer(Modifier.height(8.dp))
                Text(b.label, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
            }
        }
    }
}
```

- [ ] **Step 2: Compile.** Run: `./gradlew :androidApp:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit.**

```bash
git add sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/components/BarChart.kt
git commit -m "Add BarChart component

Claude-Session: https://claude.ai/code/session_01GGVhUyiPP7Aa6pTkjDDEra"
```

---

## Task 5: SegmentedControl component

**Files:**
- Create: `sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/components/SegmentedControl.kt`

**Interfaces:**
- Produces: `@Composable fun SegmentedControl(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier)`.

- [ ] **Step 1: Implement** a pill-shaped segmented toggle: a `surfaceVariant`/lilac rounded track holding equal-weight segments; the selected segment gets a `primary` fill + `onPrimary` text, others muted.

```kotlin
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(50))
            .background(scheme.surfaceVariant).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { i, label ->
            val selected = i == selectedIndex
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(50))
                    .background(if (selected) scheme.primary else Color.Transparent)
                    .clickable { onSelect(i) }.padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) scheme.onPrimary else scheme.onSurfaceVariant)
            }
        }
    }
}
```

- [ ] **Step 2: Compile.** Run: `./gradlew :androidApp:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit.**

```bash
git add sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/components/SegmentedControl.kt
git commit -m "Add SegmentedControl component

Claude-Session: https://claude.ai/code/session_01GGVhUyiPP7Aa6pTkjDDEra"
```

---

## Task 6: Wave-fill option for WaterRing

**Files:**
- Modify: `sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/components/WaterRing.kt`

**Interfaces:**
- Consumes: existing `WaterRing(progress, consumedMl, goalMl, ...)`.
- Produces: same signature plus a new boolean param `waveFill: Boolean = false`; when true the ring's interior renders an animated wave filled to `progress` height inside the inner circle, behind the existing center text.

- [ ] **Step 1: Read the current `WaterRing.kt`** to learn its exact signature, sizes, and how it draws the arc/center text (preserve all of it).

- [ ] **Step 2: Add the `waveFill` parameter and wave drawing.** Inside the existing `Canvas`/draw scope, before the progress arc, when `waveFill` is true, clip to the inner circle and draw two offset sine-ish wave paths filled with `secondary`/`primary` up to `(1 - progress)` from the top. Animate horizontal phase with `rememberInfiniteTransition` (guarded so it's subtle). Use Compose `Path` with `quadraticBezierTo` segments. Respect that the existing center `%`/ml text must still render on top. Pseudostructure:

```kotlin
// new param
fun WaterRing(progress: Float, consumedMl: Int, goalMl: Int, modifier: Modifier = Modifier, waveFill: Boolean = false) {
    val phase by rememberInfiniteTransition(label = "wave").animateFloat(
        0f, 1f, infiniteRepeatable(tween(3500, easing = LinearEasing)), label = "phase"
    )
    // ... existing animated progress ...
    Canvas(...) {
        val innerR = /* existing inner radius */
        if (waveFill) {
            clipPath(Path().apply { addOval(Rect(center - Offset(innerR, innerR), Size(innerR*2, innerR*2))) }) {
                val level = center.y - innerR + (2 * innerR) * (1f - animatedProgress)
                drawWave(level, phase, color = secondary.copy(alpha = 0.55f), amp = 6.dp.toPx(), shift = 0f)
                drawWave(level + 6.dp.toPx(), phase + 0.5f, color = primary.copy(alpha = 0.9f), amp = 6.dp.toPx())
            }
        }
        // ... existing track + gradient progress arc ...
    }
}
```

Add a private `DrawScope.drawWave(levelY, phase, color, amp, shift)` helper building a `Path` across the width with `quadraticBezierTo` peaks/troughs, closing to the bottom. Keep all existing arc/text code unchanged.

- [ ] **Step 3: Compile.** Run: `./gradlew :androidApp:assembleDebug`
Expected: BUILD SUCCESSFUL. Existing callers (no `waveFill` arg) keep the old ring.

- [ ] **Step 4: Commit.**

```bash
git add sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/components/WaterRing.kt
git commit -m "Add wave-fill option to WaterRing

Claude-Session: https://claude.ai/code/session_01GGVhUyiPP7Aa6pTkjDDEra"
```

---

## Task 7: QuickAddSheet + BottomNavBar

**Files:**
- Create: `sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/components/QuickAddSheet.kt`
- Create: `sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/components/BottomNavBar.kt`

**Interfaces:**
- Produces:
  - `@Composable fun BottomNavBar(items: List<NavItem>, selectedId: String, onSelect: (String) -> Unit, onFabClick: () -> Unit, modifier: Modifier = Modifier)` and `data class NavItem(val id: String, val label: String, val icon: String)`. Renders a rounded `surface` bar with the items split left/right of a raised center periwinkle squircle FAB (rotated 45°, droplet icon upright).
  - `@Composable fun QuickAddSheet(options: List<QuickAddOption>, onPick: (QuickAddOption) -> Unit, onCustom: (Int) -> Unit, onDismiss: () -> Unit)` — a `ModalBottomSheet` (Material 3) with the quick-add option chips + a custom-ml input, styled as soft cards.

- [ ] **Step 1: Implement `BottomNavBar`.** Two equal `Row`s of up to 2 tabs each, with a spacer gap in the middle, inside a `softCard`-styled rounded bar; a `Box`-positioned FAB (`Modifier.offset(y = (-18).dp)`, 60dp, `RoundedCornerShape(20.dp)`, `rotate(45f)` on the box and `rotate(-45f)` on the droplet child, `primary`→`secondary` gradient). Selected tab: `primary` icon/label; else `onSurfaceVariant`.

```kotlin
data class NavItem(val id: String, val label: String, val icon: String)

@Composable
fun BottomNavBar(
    items: List<NavItem>,            // exactly 4: [Home, History, Stats, Settings]
    selectedId: String,
    onSelect: (String) -> Unit,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().softCard(RoundedCornerShape(28.dp), elevation = 14.dp)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            items.take(2).forEach { NavTab(it, it.id == selectedId, onSelect, scheme) }
            Spacer(Modifier.width(52.dp))  // room for FAB
            items.drop(2).forEach { NavTab(it, it.id == selectedId, onSelect, scheme) }
        }
        Box(
            modifier = Modifier.align(Alignment.TopCenter).offset(y = (-18).dp)
                .size(60.dp).rotate(45f).clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(scheme.secondary, scheme.primary)))
                .clickable { onFabClick() },
            contentAlignment = Alignment.Center
        ) { Text("💧", modifier = Modifier.rotate(-45f), style = MaterialTheme.typography.titleLarge) }
    }
}

@Composable
private fun NavTab(item: NavItem, selected: Boolean, onSelect: (String) -> Unit, scheme: ColorScheme) {
    val tint = if (selected) scheme.primary else scheme.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clip(RoundedCornerShape(14.dp)).clickable { onSelect(item.id) }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(item.icon, style = MaterialTheme.typography.titleMedium)
        Text(item.label, style = MaterialTheme.typography.labelSmall, color = tint,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}
```

- [ ] **Step 2: Implement `QuickAddSheet`** using `androidx.compose.material3.ModalBottomSheet`. Render `options` (the existing `QuickAddOption` type from `domain.model`/home contract — confirm import path by grepping `QuickAddOption`) as chips calling `onPick`, plus a text field + Add button calling `onCustom(amountMl)`. Container uses `surface`. Confirm `ModalBottomSheet` is available in the project's Compose Multiplatform version; if not, fall back to an `AlertDialog`-based picker with the same callbacks.

- [ ] **Step 3: Compile.** Run: `./gradlew :androidApp:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit.**

```bash
git add sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/components/BottomNavBar.kt sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/components/QuickAddSheet.kt
git commit -m "Add BottomNavBar and QuickAddSheet components

Claude-Session: https://claude.ai/code/session_01GGVhUyiPP7Aa6pTkjDDEra"
```

---

## Task 8: MainScreen — 4-tab nav, Quick-Add FAB, Achievements overlay

**Files:**
- Modify: `sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/MainScreen.kt`

**Interfaces:**
- Consumes: `BottomNavBar`, `NavItem` (Task 7), `QuickAddSheet` (Task 7); existing stores & intents.
- Produces: unchanged `MainScreen(...)` signature.

- [ ] **Step 1: Reduce the `Tab` enum to four** (`HOME`, `HISTORY`, `ANALYTICS`, `SETTINGS`) and update icons to match nav (`🏠 📜 📊 ⚙️`). Remove `ACHIEVEMENTS` from the enum.

- [ ] **Step 2: Add overlay + sheet state** near the existing `tab` state:

```kotlin
var showAchievements by rememberSaveable { mutableStateOf(false) }
var showQuickAdd by rememberSaveable { mutableStateOf(false) }
```

- [ ] **Step 3: Replace the `bottomBar` lambda** to use `BottomNavBar`, mapping the four tabs and routing the FAB to `showQuickAdd = true`:

```kotlin
bottomBar = {
    BottomNavBar(
        items = listOf(
            NavItem("HOME", "Home", "🏠"),
            NavItem("HISTORY", "History", "📜"),
            NavItem("ANALYTICS", "Stats", "📊"),
            NavItem("SETTINGS", "Settings", "⚙️"),
        ),
        selectedId = tab.name,
        onSelect = { tab = Tab.valueOf(it) },
        onFabClick = { showQuickAdd = true },
    )
}
```

- [ ] **Step 4: Pass an `onShowAchievements` callback into `HomeScreen`** (added in Task 9) — wire `onShowAchievements = { showAchievements = true }` in the `Tab.HOME` branch.

- [ ] **Step 5: Render the Achievements overlay and Quick-Add sheet** after the `when (tab)` block, inside the Scaffold content `Box`:

```kotlin
if (showAchievements) {
    val achState by achievementsStore.state.collectAsState()
    // Full-screen overlay with a back affordance that sets showAchievements = false
    AchievementsScreen(state = achState, onBack = { showAchievements = false }, modifier = content)
}
if (showQuickAdd) {
    val homeState by homeStore.state.collectAsState()
    QuickAddSheet(
        options = homeState.quickAddOptions,
        onPick = { homeStore.dispatch(HomeIntent.AddQuickAdd(it)); showQuickAdd = false },
        onCustom = { homeStore.dispatch(HomeIntent.AddWater(it)); showQuickAdd = false },
        onDismiss = { showQuickAdd = false },
    )
}
```

(Add an `onBack` param to `AchievementsScreen` in Task 12; until then, gate this behind that task or add a temporary back button.)

- [ ] **Step 6: Compile.** Run: `./gradlew :androidApp:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Visual check.** Nav shows 4 tabs + center FAB; FAB opens quick-add and logging updates Home; tabs switch screens.

- [ ] **Step 8: Commit.**

```bash
git add sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/MainScreen.kt
git commit -m "Restructure nav to 4 tabs + Quick-Add FAB, Achievements overlay

Claude-Session: https://claude.ai/code/session_01GGVhUyiPP7Aa6pTkjDDEra"
```

---

## Task 9: HomeScreen redesign

**Files:**
- Modify: `sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/HomeScreen.kt`

**Interfaces:**
- Consumes: `WeekStrip`, `BarChart`/`BarDatum`, `WaterRing(waveFill=true)`, theme tokens.
- Produces: `HomeScreen(...)` gains one param `onShowAchievements: () -> Unit = {}` (used by MainScreen Task 8). Keep all existing params (`state`, `onQuickAdd`, `onAddCustom`, `onCreateProfile`, `onDismissUnlocked`, `modifier`).

- [ ] **Step 1: Read the current `HomeScreen.kt`** to preserve state usage (`state.progress`, `state.streak`, `state.quickAddOptions`, `state.recentEntries`, `state.insights`, `state.newlyUnlocked`, `state.error`, onboarding when `state.progress == null`/no profile).

- [ ] **Step 2: Rebuild the screen body** as a `Column` in a `verticalScroll`, matching the mockup, reusing existing state:
  - **Header** `Row`: avatar circle (initial from profile name or 💧), `Column` greeting ("Good Morning" + name), spacer, 🏆 `IconButton`-style soft circle calling `onShowAchievements`, 🔔 soft circle (Reminders — route to existing reminders/settings or no-op for now).
  - **Date row**: "Today, <formatted date>" + streak chip (`🔥 N-day streak` from `state.streak`).
  - **Today card**: `secondaryContainer` rounded card wrapping `WeekStrip(today = state.date ?: today)`.
  - **Daily Drink Target card** (`glassCard`): title + "Goal <goalMl> ml · <pct>% complete"; a `Row` with `WaterRing(progress, consumedMl, goalMl, waveFill = true)` on the left and a right `Column` with a primary "Drink <defaultMl> ml" button (`onAddCustom`/`onQuickAdd`) + quick-add chips from `state.quickAddOptions` (calling `onQuickAdd`).
  - **Hydration Stats card**: title + "This Week" pill; `BarChart` built from recent days. If Home state lacks weekly series, derive a simple 7-bar list from `state.recentEntries` grouped by day, or show today's fraction; keep it honest (no fabricated data) — if no weekly data is available, render the bars from whatever real data exists and label accordingly.
  - **Onboarding card**, **insights**, **recent activity**, **error card**: keep existing logic, restyle with `glassCard` (already inherited).
  - Omit the fitness-metrics row and search bar.

- [ ] **Step 3: Add the `onShowAchievements` param** to the function signature (default `{}`).

- [ ] **Step 4: Compile.** Run: `./gradlew :androidApp:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Visual check against the mockup** (`https://claude.ai/code/artifact/69d2d03c-35c5-4f1f-9b7a-5a71510afec7`): header, week strip, ring+wave gauge, drink button, bar chart, no fitness row/search.

- [ ] **Step 6: Commit.**

```bash
git add sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/HomeScreen.kt
git commit -m "Redesign Home screen in lavender soft-UI

Claude-Session: https://claude.ai/code/session_01GGVhUyiPP7Aa6pTkjDDEra"
```

---

## Task 10: HistoryScreen restyle

**Files:**
- Modify: `sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/HistoryScreen.kt`

- [ ] **Step 1: Read the current file** to preserve date nav (`onPreviousDay`/`onNextDay`) and `onDelete`.

- [ ] **Step 2: Restyle entries** as soft white rows (`glassCard`): leading drink icon in a tinted circle, time + label column, amount on the right, delete affordance. Keep the date header with prev/next as soft circular buttons. Match the reference "Today's records" rows.

- [ ] **Step 3: Compile.** Run: `./gradlew :androidApp:assembleDebug` — Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Visual check** — list renders in lavender style, nav + delete still work.

- [ ] **Step 5: Commit.**

```bash
git add sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/HistoryScreen.kt
git commit -m "Restyle History screen in lavender soft-UI

Claude-Session: https://claude.ai/code/session_01GGVhUyiPP7Aa6pTkjDDEra"
```

---

## Task 11: AnalyticsScreen restyle

**Files:**
- Modify: `sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/AnalyticsScreen.kt`

**Interfaces:**
- Consumes: `SegmentedControl` (Task 5), `BarChart`/`BarDatum` (Task 4).

- [ ] **Step 1: Read the current file** to preserve `onSelectPeriod` and the stats it shows (average, completion %, best/worst, daily breakdown).

- [ ] **Step 2: Restyle** with a `SegmentedControl` for the period (map indices to the existing period enum used by `AnalyticsIntent.SelectPeriod`), a `BarChart` for the daily breakdown (build `BarDatum`s from the real series in `AnalyticsState`), weekly-completion check row, and summary tiles as soft cards. No fabricated data — use what `AnalyticsState` provides.

- [ ] **Step 3: Compile.** Run: `./gradlew :androidApp:assembleDebug` — Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Visual check** — segmented control switches periods; chart + tiles render in lavender.

- [ ] **Step 5: Commit.**

```bash
git add sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/AnalyticsScreen.kt
git commit -m "Restyle Analytics screen in lavender soft-UI

Claude-Session: https://claude.ai/code/session_01GGVhUyiPP7Aa6pTkjDDEra"
```

---

## Task 12: AchievementsScreen restyle + back affordance

**Files:**
- Modify: `sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/AchievementsScreen.kt`

**Interfaces:**
- Produces: `AchievementsScreen(state, modifier, onBack: () -> Unit = {})` — adds optional `onBack` used by the Home-triggered overlay (Task 8).

- [ ] **Step 1: Read the current file** to preserve the achievements grid + unlock/progress rendering.

- [ ] **Step 2: Add `onBack` param** and a top bar with a back soft-circle button + "Achievements" title. Restyle the 2-col grid cards as soft cards (badge, title, progress bar). Keep unlock-state logic.

- [ ] **Step 3: Compile.** Run: `./gradlew :androidApp:assembleDebug` — Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Visual check** — opening 🏆 from Home shows the grid; back returns to Home.

- [ ] **Step 5: Commit.**

```bash
git add sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/AchievementsScreen.kt
git commit -m "Restyle Achievements screen + add back affordance

Claude-Session: https://claude.ai/code/session_01GGVhUyiPP7Aa6pTkjDDEra"
```

---

## Task 13: SettingsScreen restyle

**Files:**
- Modify: `sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/SettingsScreen.kt`

- [ ] **Step 1: Read the current file** to preserve profile editor (`onSaveProfile`), reminders (`onUpdateReminders`), theme switcher, accent-color picker, notification-sound picker, and `soundPlayer` usage.

- [ ] **Step 2: Restyle as "My Profile"**: centered avatar (with edit affordance), name + email, two summary cards (Daily Water Goal / Reminder Interval), then grouped soft-card list rows for Notifications, Light/Dark (theme switcher), Accent Color (picker), Sound (picker), Language/FAQ/etc. as present. Keep every existing control wired — restyle only.

- [ ] **Step 3: Compile.** Run: `./gradlew :androidApp:assembleDebug` — Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Visual check** — theme switch toggles dark/light; accent picker re-tints; sound picker plays; profile saves.

- [ ] **Step 5: Commit.**

```bash
git add sharedUI/src/commonMain/kotlin/com/bose/hydrohabit/SettingsScreen.kt
git commit -m "Restyle Settings screen as lavender My Profile

Claude-Session: https://claude.ai/code/session_01GGVhUyiPP7Aa6pTkjDDEra"
```

---

## Task 14: Full verification pass

**Files:** none (verification only).

- [ ] **Step 1: Clean build both targets.** Run: `./gradlew :androidApp:assembleDebug` (and, if iOS tooling is available, build the shared framework). Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: Run on an Android emulator** (`./gradlew :androidApp:installDebug`, launch). Walk the verification checklist from the spec:
  - Home matches the mockup (header, week strip, ring+wave, bar chart, nav + FAB).
  - All 4 tabs reachable; Quick-Add FAB logs water and updates the ring; 🏆 opens Achievements and back works.
  - History, Analytics, Achievements, Settings all render lavender with no leftover glass/cyan.

- [ ] **Step 3: Theme + accent checks.** Toggle dark mode (matching dark variant, AA holds). Change accent color in Settings (UI re-tints; backgrounds stay lilac/indigo).

- [ ] **Step 4: Regression checks.** Log water, view history, streak increments, achievement-unlock banner, settings save — all still work.

- [ ] **Step 5: Final commit** (if any fixups were needed during verification).

```bash
git add -A
git commit -m "Verification fixups for lavender soft-UI redesign

Claude-Session: https://claude.ai/code/session_01GGVhUyiPP7Aa6pTkjDDEra"
```

---

## Self-Review

**Spec coverage:** tokens → T1; soft card/bg → T2; WeekStrip/SegmentedControl/BarChart/WaterRing/BottomNavBar → T3–T7; nav 4-tabs+FAB+Achievements move → T8; Home → T9; History → T10; Analytics → T11; Achievements → T12; Settings (incl. theme/accent/sound pickers kept) → T13; dark mode + accent picker + verification → T1/T2/T14. Fitness row omitted (T9). No search bar (T9). All spec sections covered.

**Placeholder scan:** Foundational files (T1, T2, T3, T4, T5, T7) ship complete code. Screen tasks (T9–T13) give concrete structure + reuse instructions but intentionally instruct the implementer to **read the existing file first** and preserve exact state wiring rather than reproducing large existing files verbatim — the state shapes (`HomeState`, `AnalyticsState`, etc.) live in `sharedLogic` and must not be guessed. T6 shows pseudostructure for the wave (exact inner-radius/text code is read from the existing file). This is deliberate for a re-skin of large existing Composables, not a placeholder gap.

**Type consistency:** `glassCard`/`softCard`, `WeekStrip(today,…)`, `BarChart(bars: List<BarDatum>)`, `SegmentedControl(options, selectedIndex, onSelect)`, `BottomNavBar(items: List<NavItem>, selectedId, onSelect, onFabClick)`, `QuickAddSheet(options, onPick, onCustom, onDismiss)`, `WaterRing(…, waveFill)`, `HomeScreen(…, onShowAchievements)`, `AchievementsScreen(…, onBack)` — names/signatures match across producing and consuming tasks.
