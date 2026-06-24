import SwiftUI

// MARK: - Hex parser

/// Parses a `#RRGGBB` (or `RRGGBB`) string into a SwiftUI `Color`.
/// Falls back to periwinkle `#6C5CE7` on any parse failure.
func hexColor(_ hex: String) -> Color {
    let clean = hex.hasPrefix("#") ? String(hex.dropFirst()) : hex
    guard clean.count == 6, let value = UInt64(clean, radix: 16) else {
        return Color(.sRGB, red: 0x6C / 255.0, green: 0x5C / 255.0, blue: 0xE7 / 255.0, opacity: 1)
    }
    let r = Double((value >> 16) & 0xFF) / 255.0
    let g = Double((value >>  8) & 0xFF) / 255.0
    let b = Double( value        & 0xFF) / 255.0
    return Color(.sRGB, red: r, green: g, blue: b, opacity: 1)
}

// MARK: - HSL helpers (private)

/// Converts sRGB components (each 0…1) to HSL.
/// Returns (hue: 0…360, saturation: 0…1, lightness: 0…1).
private func rgbToHSL(r: Double, g: Double, b: Double) -> (h: Double, s: Double, l: Double) {
    let maxC = max(r, g, b)
    let minC = min(r, g, b)
    let delta = maxC - minC
    let l = (maxC + minC) / 2.0

    guard delta > 0 else { return (0, 0, l) }

    let s = delta / (1 - abs(2 * l - 1))

    let h: Double
    switch maxC {
    case r: h = 60 * (((g - b) / delta).truncatingRemainder(dividingBy: 6))
    case g: h = 60 * (((b - r) / delta) + 2)
    default: h = 60 * (((r - g) / delta) + 4)
    }

    return ((h + 360).truncatingRemainder(dividingBy: 360), s, l)
}

/// Converts HSL to a SwiftUI `Color`.
private func hslToColor(h: Double, s: Double, l: Double) -> Color {
    let c = (1 - abs(2 * l - 1)) * s
    let x = c * (1 - abs((h / 60).truncatingRemainder(dividingBy: 2) - 1))
    let m = l - c / 2

    let (r1, g1, b1): (Double, Double, Double)
    switch h {
    case 0 ..< 60:  (r1, g1, b1) = (c, x, 0)
    case 60 ..< 120: (r1, g1, b1) = (x, c, 0)
    case 120 ..< 180: (r1, g1, b1) = (0, c, x)
    case 180 ..< 240: (r1, g1, b1) = (0, x, c)
    case 240 ..< 300: (r1, g1, b1) = (x, 0, c)
    default:         (r1, g1, b1) = (c, 0, x)
    }

    return Color(.sRGB, red: r1 + m, green: g1 + m, blue: b1 + m, opacity: 1)
}

// MARK: - HydroColors

/// Lavender brand palette — mirrors the Android `Color.kt` token set.
///
/// Light / dark fixed tokens match `HydroLightColors` / `HydroDarkColors` in Color.kt.
/// `primary` is accent-aware; `secondary` is derived from the accent's hue shifted +30°
/// (mod 360), mirroring Android's `generateDynamicColorScheme` behaviour.
struct HydroColors {
    let background:        Color
    let surface:           Color
    let surfaceVariant:    Color
    let primary:           Color
    let secondary:         Color

    /// Factory — call with the current `colorScheme` and the user's stored accent hex.
    /// `accentHex` is passed straight to `hexColor(_:)`; empty strings fall back to periwinkle.
    static func from(_ scheme: ColorScheme, accentHex: String) -> HydroColors {
        let accent = accentHex.isEmpty ? "#6C5CE7" : accentHex

        // Derive secondary: parse accent → RGB → HSL → shift hue +30° → HSL→Color.
        // Falls back to fixed periwinkle sibling on parse failure.
        let secondaryColor: Color = {
            let clean = accent.hasPrefix("#") ? String(accent.dropFirst()) : accent
            guard clean.count == 6, let value = UInt64(clean, radix: 16) else {
                // Fallback: fixed periwinkle sibling
                return scheme == .dark
                    ? Color(.sRGB, red: 0xA9/255.0, green: 0x9C/255.0, blue: 0xFF/255.0, opacity: 1)
                    : Color(.sRGB, red: 0x8B/255.0, green: 0x7B/255.0, blue: 0xF0/255.0, opacity: 1)
            }
            let r = Double((value >> 16) & 0xFF) / 255.0
            let g = Double((value >>  8) & 0xFF) / 255.0
            let b = Double( value        & 0xFF) / 255.0
            let hsl = rgbToHSL(r: r, g: g, b: b)
            let shiftedH = (hsl.h + 30).truncatingRemainder(dividingBy: 360)
            // Mirror Android: light secondary lightness ~0.42, dark ~0.78
            let lightness = scheme == .dark ? 0.78 : 0.42
            return hslToColor(h: shiftedH, s: hsl.s, l: lightness)
        }()

        switch scheme {
        case .dark:
            return HydroColors(
                // Android: LavenderDarkBackground = 0xFF15131F
                background:     Color(.sRGB, red: 0x15/255.0, green: 0x13/255.0, blue: 0x1F/255.0, opacity: 1),
                // Android: LavenderDarkSurface = 0xFF211E33
                surface:        Color(.sRGB, red: 0x21/255.0, green: 0x1E/255.0, blue: 0x33/255.0, opacity: 1),
                // Android: surfaceVariant = 0xFF2E2A45
                surfaceVariant: Color(.sRGB, red: 0x2E/255.0, green: 0x2A/255.0, blue: 0x45/255.0, opacity: 1),
                // Accent-driven; default periwinkle dark tint = 0xFFA99CFF (mirrors Android dark primary)
                primary:        hexColor(accent),
                // Accent-hue +30°, lightness 0.78 — mirrors Android generateDynamicColorScheme secondary
                secondary:      secondaryColor
            )
        default: // .light
            return HydroColors(
                // Android: LavenderLightBackground = 0xFFEFEDFB
                background:     Color(.sRGB, red: 0xEF/255.0, green: 0xED/255.0, blue: 0xFB/255.0, opacity: 1),
                // Android: LavenderLightSurface = 0xFFFFFFFF
                surface:        Color(.sRGB, red: 1,           green: 1,           blue: 1,           opacity: 1),
                // Android: surfaceVariant light = 0xFFE4DFF7
                surfaceVariant: Color(.sRGB, red: 0xE4/255.0, green: 0xDF/255.0, blue: 0xF7/255.0, opacity: 1),
                // Accent-driven; default periwinkle = 0xFF6C5CE7
                primary:        hexColor(accent),
                // Accent-hue +30°, lightness 0.42 — mirrors Android generateDynamicColorScheme secondary
                secondary:      secondaryColor
            )
        }
    }
}
