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

// MARK: - HydroColors

/// Lavender brand palette — mirrors the Android `Color.kt` token set.
///
/// Light / dark fixed tokens match `HydroLightColors` / `HydroDarkColors` in Color.kt.
/// `primary` is accent-aware; `secondary` is a fixed lighter/darker periwinkle variant
/// (`#8B7BF0` light, `#A99CFF` dark) consistent with the Android scheme.
struct HydroColors {
    let background:        Color
    let surface:           Color
    let surfaceVariant:    Color
    let secondaryContainer: Color
    let ink:               Color
    let muted:             Color
    let primary:           Color
    let secondary:         Color

    /// Factory — call with the current `colorScheme` and the user's stored accent hex.
    /// `accentHex` is passed straight to `hexColor(_:)`; empty strings fall back to periwinkle.
    static func from(_ scheme: ColorScheme, accentHex: String) -> HydroColors {
        let accent = accentHex.isEmpty ? "#6C5CE7" : accentHex
        switch scheme {
        case .dark:
            return HydroColors(
                // Android: LavenderDarkBackground = 0xFF15131F
                background:         Color(.sRGB, red: 0x15/255.0, green: 0x13/255.0, blue: 0x1F/255.0, opacity: 1),
                // Android: LavenderDarkSurface = 0xFF211E33
                surface:            Color(.sRGB, red: 0x21/255.0, green: 0x1E/255.0, blue: 0x33/255.0, opacity: 1),
                // Android: surfaceVariant = 0xFF2E2A45
                surfaceVariant:     Color(.sRGB, red: 0x2E/255.0, green: 0x2A/255.0, blue: 0x45/255.0, opacity: 1),
                // Android: secondaryContainer dark = 0xFF2C2746
                secondaryContainer: Color(.sRGB, red: 0x2C/255.0, green: 0x27/255.0, blue: 0x46/255.0, opacity: 1),
                // Android: onBackground dark = 0xFFE7E4F5
                ink:                Color(.sRGB, red: 0xE7/255.0, green: 0xE4/255.0, blue: 0xF5/255.0, opacity: 1),
                // Android: onSurfaceVariant dark = 0xFFB4B0D0 (adjusted for contrast); using muted ≈ A7A2C4
                muted:              Color(.sRGB, red: 0xA7/255.0, green: 0xA2/255.0, blue: 0xC4/255.0, opacity: 1),
                // Accent-driven; default periwinkle dark tint = 0xFFA99CFF (mirrors Android dark primary)
                primary:            hexColor(accent),
                // Fixed lighter periwinkle variant for dark — 0xFFA99CFF (matches Android HydroDarkColors.secondary approx)
                secondary:          Color(.sRGB, red: 0xA9/255.0, green: 0x9C/255.0, blue: 0xFF/255.0, opacity: 1)
            )
        default: // .light
            return HydroColors(
                // Android: LavenderLightBackground = 0xFFEFEDFB
                background:         Color(.sRGB, red: 0xEF/255.0, green: 0xED/255.0, blue: 0xFB/255.0, opacity: 1),
                // Android: LavenderLightSurface = 0xFFFFFFFF
                surface:            Color(.sRGB, red: 1,           green: 1,           blue: 1,           opacity: 1),
                // Android: surfaceVariant light = 0xFFE4DFF7
                surfaceVariant:     Color(.sRGB, red: 0xE4/255.0, green: 0xDF/255.0, blue: 0xF7/255.0, opacity: 1),
                // Android: secondaryContainer light = 0xFFD9D3F5
                secondaryContainer: Color(.sRGB, red: 0xD9/255.0, green: 0xD3/255.0, blue: 0xF5/255.0, opacity: 1),
                // Android: Ink / onBackground light = 0xFF1E1B3A
                ink:                Color(.sRGB, red: 0x1E/255.0, green: 0x1B/255.0, blue: 0x3A/255.0, opacity: 1),
                // Muted text — onSurfaceVariant light (0xFF6E6A8F per brief; Android uses 0xFF4E4A6A for contrast)
                muted:              Color(.sRGB, red: 0x6E/255.0, green: 0x6A/255.0, blue: 0x8F/255.0, opacity: 1),
                // Accent-driven; default periwinkle = 0xFF6C5CE7
                primary:            hexColor(accent),
                // Fixed slightly lighter periwinkle for light — 0xFF8B7BF0 (brighter than primary, softer for secondary roles)
                secondary:          Color(.sRGB, red: 0x8B/255.0, green: 0x7B/255.0, blue: 0xF0/255.0, opacity: 1)
            )
        }
    }
}
