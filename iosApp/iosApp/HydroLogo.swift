import SwiftUI

/// Brand mark: a teardrop droplet inside a 270° open ring.
///
/// The mark is drawn in a 120×120 unit space and scaled to whatever
/// frame the caller gives it.  Pass `tint` explicitly or let it default
/// to the theme primary derived from the current environment.
struct HydroLogo: View {
    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.accentHex) private var accentHex

    /// Override the tint; defaults to `nil` which resolves to the theme primary.
    var tint: Color? = nil

    private var resolvedTint: Color {
        tint ?? HydroColors.from(colorScheme, accentHex: accentHex).primary
    }

    var body: some View {
        Canvas { context, size in
            let scale = size.width / 120.0
            let transform = CGAffineTransform(scaleX: scale, y: scale)

            // --- Teardrop droplet (filled) ---
            // SVG path (120-unit space):
            // M60 26 C60 26 84 54 84 74 C84 87.3 73.3 98 60 98
            //         C46.7 98 36 87.3 36 74 C36 54 60 26 60 26 Z
            let dropletPath = Path { p in
                p.move(to: CGPoint(x: 60, y: 26))
                p.addCurve(
                    to:         CGPoint(x: 84, y: 74),
                    control1:   CGPoint(x: 60, y: 26),
                    control2:   CGPoint(x: 84, y: 54)
                )
                p.addCurve(
                    to:         CGPoint(x: 60, y: 98),
                    control1:   CGPoint(x: 84, y: 87.3),
                    control2:   CGPoint(x: 73.3, y: 98)
                )
                p.addCurve(
                    to:         CGPoint(x: 36, y: 74),
                    control1:   CGPoint(x: 46.7, y: 98),
                    control2:   CGPoint(x: 36, y: 87.3)
                )
                p.addCurve(
                    to:         CGPoint(x: 60, y: 26),
                    control1:   CGPoint(x: 36, y: 54),
                    control2:   CGPoint(x: 60, y: 26)
                )
                p.closeSubpath()
            }.applying(transform)

            context.fill(dropletPath, with: .color(resolvedTint))

            // --- Open ring arc: 270° circle, gap at the top (~315°…45°) ---
            // Centre (60,60), radius 52, stroke 6 pt in the 120-unit space.
            let cx: CGFloat = 60
            let cy: CGFloat = 60
            let r:  CGFloat = 52
            let lineWidth: CGFloat = 6 * scale

            // Start at 135° (bottom-left), sweep 270° clockwise to 45° (top-right).
            // In CoreGraphics, 0° is 3 o'clock; positive angles are clockwise.
            let startAngle = Angle.degrees(135)
            let endAngle   = Angle.degrees(135 + 270)   // = 405° ≡ 45°

            let arcPath = Path { p in
                p.addArc(
                    center:     CGPoint(x: cx, y: cy),
                    radius:     r,
                    startAngle: startAngle,
                    endAngle:   endAngle,
                    clockwise:  false
                )
            }.applying(transform)

            context.stroke(
                arcPath,
                with: .color(resolvedTint.opacity(0.45)),
                style: StrokeStyle(lineWidth: lineWidth, lineCap: .round)
            )
        }
        .aspectRatio(1, contentMode: .fit)
    }
}

// MARK: - Preview
#if DEBUG
struct HydroLogo_Previews: PreviewProvider {
    static var previews: some View {
        HStack(spacing: 24) {
            HydroLogo()
                .frame(width: 44, height: 44)
            HydroLogo(tint: .purple)
                .frame(width: 44, height: 44)
        }
        .padding()
        .previewLayout(.sizeThatFits)
    }
}
#endif
