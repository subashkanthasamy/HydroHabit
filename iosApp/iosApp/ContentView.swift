import SwiftUI
import SharedLogic

// MARK: - Accent hex environment key

private struct AccentHexKey: EnvironmentKey {
    static let defaultValue: String = "#6C5CE7"
}

extension EnvironmentValues {
    var accentHex: String {
        get { self[AccentHexKey.self] }
        set { self[AccentHexKey.self] = newValue }
    }
}

// MARK: - Glassmorphism Components & Helpers

/// Screen-wide container that draws glowing ambient blobs in the background.
struct GlassyBackground: View {
    @Environment(\.colorScheme) var colorScheme
    @Environment(\.accentHex) var accentHex

    var body: some View {
        let colors = HydroColors.from(colorScheme, accentHex: accentHex)
        let glow1 = colors.primary.opacity(colorScheme == .dark ? 0.10 : 0.14)
        let glow2 = colors.secondary.opacity(colorScheme == .dark ? 0.07 : 0.10)
        let glow3 = colors.primary.opacity(colorScheme == .dark ? 0.04 : 0.07)

        ZStack {
            colors.background.ignoresSafeArea()

            // Glowing mesh circles
            Circle()
                .fill(glow1)
                .frame(width: 400, height: 400)
                .blur(radius: 80)
                .offset(x: 180, y: -250)

            Circle()
                .fill(glow2)
                .frame(width: 350, height: 350)
                .blur(radius: 70)
                .offset(x: -180, y: 300)

            Circle()
                .fill(glow3)
                .frame(width: 250, height: 250)
                .blur(radius: 60)
                .offset(x: 0, y: 20)
        }
    }
}

/// Applies a semi-transparent, frosted glass filter with a glowing border overlay and drop shadow.
struct GlassCardModifier: ViewModifier {
    @Environment(\.colorScheme) var colorScheme
    @Environment(\.accentHex) var accentHex

    var cornerRadius: CGFloat
    var borderWidth: CGFloat

    func body(content: Content) -> some View {
        let isDark = colorScheme == .dark
        let colors = HydroColors.from(colorScheme, accentHex: accentHex)
        let opacity = isDark ? 0.12 : 0.65
        content
            .background(
                RoundedRectangle(cornerRadius: cornerRadius)
                    .fill(colors.surface.opacity(opacity))
            )
            .background(
                RoundedRectangle(cornerRadius: cornerRadius)
                    .fill(.thinMaterial)
            )
            .overlay(
                RoundedRectangle(cornerRadius: cornerRadius)
                    .stroke(
                        LinearGradient(
                            colors: [
                                .white.opacity(isDark ? 0.08 : 0.4),
                                .white.opacity(isDark ? 0.03 : 0.1)
                            ],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        ),
                        lineWidth: borderWidth
                    )
            )
            .shadow(color: Color.black.opacity(0.04), radius: 10, x: 0, y: 4)
    }
}

extension View {
    func glassCard(cornerRadius: CGFloat = 16, borderWidth: CGFloat = 1) -> some View {
        self.modifier(GlassCardModifier(cornerRadius: cornerRadius, borderWidth: borderWidth))
    }
}

// MARK: - Brand Color Extensions & Helpers
// These helpers are intentionally thin wrappers so call sites stay readable.
// All color values are sourced from HydroColors; no hardcoded aqua/cyan/indigo.
extension Color {
    static func brandPrimary(scheme: ColorScheme, accentHex: String) -> Color {
        HydroColors.from(scheme, accentHex: accentHex).primary
    }
    static func brandSecondary(scheme: ColorScheme, accentHex: String) -> Color {
        HydroColors.from(scheme, accentHex: accentHex).secondary
    }
    static func brandOnSurfaceVariant(scheme: ColorScheme, accentHex: String) -> Color {
        HydroColors.from(scheme, accentHex: accentHex).muted
    }
}

// MARK: - Core Custom UI Views

/// Animated circular hydration ring — the SwiftUI counterpart of the Compose `WaterRing`.
struct WaterRing: View {
    @Environment(\.colorScheme) var colorScheme
    @Environment(\.accentHex) var accentHex
    let progress: Double      // 0...1
    let consumedMl: Int32
    let goalMl: Int32

    private var clamped: Double { min(max(progress, 0), 1) }

    var body: some View {
        let colors = HydroColors.from(colorScheme, accentHex: accentHex)

        ZStack {
            Circle()
                .stroke(colors.surfaceVariant, style: StrokeStyle(lineWidth: 24, lineCap: .round))
            Circle()
                .trim(from: 0, to: clamped)
                .stroke(
                    AngularGradient(
                        gradient: Gradient(colors: [colors.secondary, colors.primary, colors.secondary]),
                        center: .center,
                        startAngle: .degrees(-90),
                        endAngle: .degrees(270)
                    ),
                    style: StrokeStyle(lineWidth: 24, lineCap: .round)
                )
                .rotationEffect(.degrees(-90))
            VStack(spacing: 4) {
                Text("\(Int(clamped * 100))%")
                    .font(.system(size: 44, weight: .bold, design: .rounded))
                    .foregroundStyle(colors.primary)
                    .contentTransition(.numericText())
                Text("\(consumedMl) / \(goalMl) ml")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
        }
        .frame(width: 232, height: 232)
        .padding(12)
        .animation(.easeInOut(duration: 0.9), value: clamped)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("Hydration progress \(Int(clamped * 100)) percent, \(consumedMl) of \(goalMl) milliliters")
    }
}

/// Compact dashboard stat (Goal / Remaining / Streak), matching the Compose stat tiles.
struct StatTile: View {
    let label: String
    let value: String
    var body: some View {
        VStack(spacing: 4) {
            Text(value).font(.headline).fontWeight(.bold)
            Text(label).font(.caption).foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 16)
        .glassCard(cornerRadius: 16)
    }
}

// MARK: - Root (bottom tabs, each in a NavigationStack with GlassyBackground)

extension Color {
    static func fromHex(_ hex: String) -> Color {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 102, 144) // Default brand primary
        }
        return Color(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

@MainActor
final class RootModel: ObservableObject {
    @Published var settings: ReminderSettings = ReminderSettings.companion.DEFAULT
    private let store: SettingsStoreNative
    private var token: CancellationToken?

    init() {
        let store = KoinHelper().createSettingsStore()
        self.store = store
        self.settings = store.currentState.reminderSettings
        self.token = store.watch { [weak self] newState in
            self?.settings = newState.reminderSettings
        }
    }
    
    func updateSettings(_ settings: ReminderSettings) {
        store.dispatch(intent: SettingsIntentUpdateReminders(settings: settings))
    }

    deinit { token?.cancel(); store.close() }
}

struct RootView: View {
    @StateObject private var rootModel = RootModel()
    
    var body: some View {
        let preferredScheme: ColorScheme? = {
            switch rootModel.settings.themeMode.uppercased() {
            case "LIGHT": return .light
            case "DARK": return .dark
            default: return nil
            }
        }()
        let brandColor = Color.fromHex(rootModel.settings.accentColor)

        TabView {
            NavigationStack {
                ZStack {
                    GlassyBackground()
                    HomeView().navigationTitle("HydroHabit")
                }
            }
            .tabItem { Label("Home", systemImage: "drop.fill") }

            NavigationStack {
                ZStack {
                    GlassyBackground()
                    HistoryView().navigationTitle("History")
                }
            }
            .tabItem { Label("History", systemImage: "list.bullet") }

            NavigationStack {
                ZStack {
                    GlassyBackground()
                    AnalyticsView().navigationTitle("Insights")
                }
            }
            .tabItem { Label("Stats", systemImage: "chart.bar.fill") }

            NavigationStack {
                ZStack {
                    GlassyBackground()
                    AchievementsView().navigationTitle("Achievements")
                }
            }
            .tabItem { Label("Awards", systemImage: "trophy.fill") }

            NavigationStack {
                ZStack {
                    GlassyBackground()
                    SettingsView().navigationTitle("Settings")
                }
            }
            .tabItem { Label("Settings", systemImage: "gearshape.fill") }
        }
        .environment(\.accentHex, rootModel.settings.accentColor.isEmpty ? "#6C5CE7" : rootModel.settings.accentColor)
        .preferredColorScheme(preferredScheme)
        .tint(brandColor)
    }
}

// Kept so existing project references to `ContentView` still resolve.
struct ContentView: View {
    var body: some View { RootView() }
}

// MARK: - Home

@MainActor
final class HomeModel: ObservableObject {
    @Published var state: HomeState
    private let store: HomeStoreNative
    private var token: CancellationToken?

    init() {
        let store = KoinHelper().createHomeStore()
        self.store = store
        self.state = store.currentState
        self.token = store.watch { [weak self] newState in self?.state = newState }
    }
    func quickAdd(_ option: QuickAddOption) { store.addQuickAdd(option: option) }
    func addCustom(_ ml: Int32) { store.addWater(amountMl: ml) }
    func createProfile(weight: Double, age: Int32) { store.createProfile(weightKg: weight, age: age) }
    deinit { token?.cancel(); store.close() }
}

struct HomeView: View {
    @StateObject private var model = HomeModel()
    @State private var weight = "70"
    @State private var age = "30"
    @State private var custom = ""

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                if let progress = model.state.progress, progress.goalMl > 0 {
                    WaterRing(
                        progress: Double(progress.completionPercent),
                        consumedMl: progress.consumedMl,
                        goalMl: progress.goalMl
                    )
                    .frame(maxWidth: .infinity)

                    Text(progress.isCompleted ? "Goal reached! 🎉" : "\(progress.remainingMl) ml to go")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .frame(maxWidth: .infinity, alignment: .center)

                    HStack(spacing: 12) {
                        StatTile(label: "Goal", value: "\(progress.goalMl) ml")
                        StatTile(label: "Remaining", value: "\(progress.remainingMl) ml")
                        StatTile(label: "Streak", value: "🔥 \(model.state.streak.currentDailyStreak)")
                    }

                    Text("Quick add").font(.headline)
                    HStack {
                        ForEach(model.state.quickAddOptions, id: \.amountMl) { option in
                            Button(option.label) { model.quickAdd(option) }
                                .buttonStyle(.bordered).frame(maxWidth: .infinity)
                        }
                    }
                    
                    HStack {
                        TextField("Custom ml", text: $custom).keyboardType(.numberPad).textFieldStyle(.roundedBorder)
                        Button("Add") {
                            if let ml = Int32(custom), ml > 0 { model.addCustom(ml); custom = "" }
                        }.buttonStyle(.borderedProminent)
                    }

                    if !model.state.insights.isEmpty {
                        Text("Today's insights").font(.headline)
                        ForEach(model.state.insights, id: \.id) { insight in
                            Text(insight.message)
                                .font(.subheadline)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding()
                                .glassCard(cornerRadius: 12)
                        }
                    }

                    Text("Recent activity").font(.headline)
                    if model.state.recentEntries.isEmpty {
                        Text("No water logged yet today.")
                            .font(.subheadline).foregroundStyle(.secondary)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding()
                            .glassCard(cornerRadius: 12)
                    } else {
                        VStack(alignment: .leading, spacing: 0) {
                            ForEach(model.state.recentEntries, id: \.id) { entry in
                                HStack {
                                    Text("\(entry.amountMl) ml").bold()
                                    Spacer()
                                    Text(entry.source.name.lowercased()).font(.caption).foregroundStyle(.secondary)
                                }
                                .padding(.vertical, 10)
                                .padding(.horizontal)
                                if entry.id != model.state.recentEntries.last?.id {
                                    Divider().padding(.horizontal)
                                }
                            }
                        }
                        .glassCard(cornerRadius: 16)
                    }
                } else {
                    VStack(alignment: .leading, spacing: 14) {
                        Text("Welcome! Set up your goal").font(.headline)
                        TextField("Weight (kg)", text: $weight).keyboardType(.decimalPad).textFieldStyle(.roundedBorder)
                        TextField("Age", text: $age).keyboardType(.numberPad).textFieldStyle(.roundedBorder)
                        Button("Calculate my goal") {
                            if let w = Double(weight), let a = Int32(age) { model.createProfile(weight: w, age: a) }
                        }.buttonStyle(.borderedProminent)
                    }
                    .padding()
                    .glassCard(cornerRadius: 20)
                }
            }
            .padding()
        }
        .scrollContentBackground(.hidden)
        .scrollDismissesKeyboard(.interactively)
    }
}

// MARK: - History

@MainActor
final class HistoryModel: ObservableObject {
    @Published var state: HistoryState
    private let store: HistoryStoreNative
    private var token: CancellationToken?
    init() {
        let store = KoinHelper().createHistoryStore()
        self.store = store
        self.state = store.currentState
        self.token = store.watch { [weak self] s in self?.state = s }
    }
    func previous() { store.previousDay() }
    func next() { store.nextDay() }
    func delete(_ id: String) { store.delete(id: id) }
    deinit { token?.cancel(); store.close() }
}

struct HistoryView: View {
    @StateObject private var model = HistoryModel()
    var body: some View {
        VStack(spacing: 14) {
            HStack {
                Button("‹") { model.previous() }
                Spacer()
                VStack {
                    Text(model.state.date?.description ?? "").bold()
                    Text("\(model.state.dailyTotalMl) ml").font(.caption).foregroundStyle(.secondary)
                }
                Spacer()
                Button("›") { model.next() }
            }
            .font(.title3)
            .padding(.horizontal)

            if model.state.entries.isEmpty {
                Spacer()
                Text("No entries logged this day.").foregroundStyle(.secondary)
                Spacer()
            } else {
                ScrollView {
                    VStack(spacing: 10) {
                        ForEach(model.state.entries, id: \.id) { entry in
                            HStack {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text("\(entry.amountMl) ml").bold()
                                    Text(entry.source.name.lowercased()).font(.caption).foregroundStyle(.secondary)
                                }
                                Spacer()
                                Button("Delete", role: .destructive) { model.delete(entry.id) }
                                    .buttonStyle(.bordered)
                            }
                            .padding()
                            .glassCard(cornerRadius: 14)
                        }
                    }
                    .padding(.horizontal)
                }
            }
        }
        .padding(.top)
    }
}

// MARK: - Analytics

@MainActor
final class AnalyticsModel: ObservableObject {
    @Published var state: AnalyticsState
    private let store: AnalyticsStoreNative
    private var token: CancellationToken?
    init() {
        let store = KoinHelper().createAnalyticsStore()
        self.store = store
        self.state = store.currentState
        self.token = store.watch { [weak self] s in self?.state = s }
    }
    func select(_ period: ReportPeriod) { store.selectPeriod(period: period) }
    deinit { token?.cancel(); store.close() }
}

struct AnalyticsView: View {
    @Environment(\.colorScheme) var colorScheme
    @Environment(\.accentHex) var accentHex
    @StateObject private var model = AnalyticsModel()
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                Picker("Period", selection: Binding(
                    get: { model.state.period },
                    set: { model.select($0) }
                )) {
                    Text("Daily").tag(ReportPeriod.daily)
                    Text("Weekly").tag(ReportPeriod.weekly)
                    Text("Monthly").tag(ReportPeriod.monthly)
                }.pickerStyle(.segmented)

                if let report = model.state.report, !report.dailyBreakdown.isEmpty {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Average: \(report.averageMl) ml/day").bold()
                        Text("Goal completion: \(Int(report.goalCompletionRate * 100))%")
                        if let best = report.bestDay { Text("Best day: \(best.date.description) (\(best.consumedMl) ml)") }
                    }
                    .padding()
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .glassCard(cornerRadius: 16)

                    // Daily breakdown bar chart
                    let analyticsColors = HydroColors.from(colorScheme, accentHex: accentHex)
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Daily breakdown").font(.headline).fontWeight(.bold)
                        ForEach(report.dailyBreakdown, id: \.date.description) { day in
                            VStack(alignment: .leading, spacing: 6) {
                                Text("\(day.date.description) — \(day.consumedMl) ml")
                                    .font(.caption)
                                GeometryReader { geo in
                                    let maxVal = CGFloat(report.dailyBreakdown.map { $0.consumedMl }.max() ?? 1)
                                    let fraction = maxVal > 0 ? CGFloat(day.consumedMl) / maxVal : 0
                                    RoundedRectangle(cornerRadius: 4)
                                        .fill(
                                            LinearGradient(
                                                colors: [analyticsColors.secondary, analyticsColors.primary],
                                                startPoint: .leading,
                                                endPoint: .trailing
                                            )
                                        )
                                        .frame(width: geo.size.width * fraction)
                                }
                                .frame(height: 8)
                            }
                        }
                    }
                    .padding()
                    .glassCard(cornerRadius: 16)

                    ForEach(report.insights, id: \.id) { insight in
                        Text(insight.message)
                            .padding()
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .glassCard(cornerRadius: 12)
                    }
                } else {
                    Text("Log some water to see your trends.").foregroundStyle(.secondary)
                }
            }
            .padding()
        }
        .scrollContentBackground(.hidden)
    }
}

// MARK: - Achievements

@MainActor
final class AchievementsModel: ObservableObject {
    @Published var state: AchievementsState
    private let store: AchievementsStoreNative
    private var token: CancellationToken?
    init() {
        let store = KoinHelper().createAchievementsStore()
        self.store = store
        self.state = store.currentState
        self.token = store.watch { [weak self] s in self?.state = s }
    }
    deinit { token?.cancel(); store.close() }
}

struct AchievementsView: View {
    @Environment(\.colorScheme) var colorScheme
    @Environment(\.accentHex) var accentHex
    @StateObject private var model = AchievementsModel()
    private let columns = [GridItem(.flexible()), GridItem(.flexible())]
    var body: some View {
        let unlockColor = HydroColors.from(colorScheme, accentHex: accentHex).primary
        
        ScrollView {
            VStack(alignment: .leading) {
                Text("\(model.state.unlockedCount) of \(model.state.achievements.count) unlocked")
                    .font(.subheadline).foregroundStyle(.secondary).padding(.bottom, 8)
                LazyVGrid(columns: columns, spacing: 12) {
                    ForEach(model.state.achievements, id: \.id) { a in
                        VStack(alignment: .leading, spacing: 6) {
                            Text(a.isUnlocked ? "🏆 \(a.title)" : "🔒 \(a.title)").bold()
                                .foregroundStyle(a.isUnlocked ? unlockColor : .primary)
                            Text(a.description_).font(.caption)
                            if !a.isUnlocked { ProgressView(value: Double(a.progress)) }
                        }
                        .padding()
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(a.isUnlocked ? unlockColor.opacity(0.08) : Color.primary.opacity(0.02))
                        .glassCard(cornerRadius: 14, borderWidth: a.isUnlocked ? 1.5 : 1)
                    }
                }
            }
            .padding()
        }
        .scrollContentBackground(.hidden)
    }
}

// MARK: - Settings

@MainActor
final class SettingsModel: ObservableObject {
    @Published var state: SettingsState
    private let store: SettingsStoreNative
    private var token: CancellationToken?
    init() {
        let store = KoinHelper().createSettingsStore()
        self.store = store
        self.state = store.currentState
        self.token = store.watch { [weak self] s in self?.state = s }
    }
    func saveProfile(weight: Double, age: Int32) { store.saveProfile(weightKg: weight, age: age) }
    func setEnabled(_ on: Bool) { store.setRemindersEnabled(enabled: on) }
    func setInterval(_ minutes: Int32) { store.setReminderInterval(minutes: minutes) }
    
    func updateThemeMode(_ mode: String) {
        let s = state.reminderSettings
        let newSettings = ReminderSettings(
            enabled: s.enabled,
            intervalMinutes: s.intervalMinutes,
            wakeTime: s.wakeTime,
            sleepTime: s.sleepTime,
            strategy: s.strategy,
            quietWindows: s.quietWindows,
            skipIfRecentlyLoggedMinutes: s.skipIfRecentlyLoggedMinutes,
            soundEnabled: s.soundEnabled,
            vibrationEnabled: s.vibrationEnabled,
            notificationSound: s.notificationSound,
            themeMode: mode,
            accentColor: s.accentColor
        )
        store.dispatch(intent: SettingsIntentUpdateReminders(settings: newSettings))
    }
    
    func updateNotificationSound(_ sound: String) {
        let s = state.reminderSettings
        let newSettings = ReminderSettings(
            enabled: s.enabled,
            intervalMinutes: s.intervalMinutes,
            wakeTime: s.wakeTime,
            sleepTime: s.sleepTime,
            strategy: s.strategy,
            quietWindows: s.quietWindows,
            skipIfRecentlyLoggedMinutes: s.skipIfRecentlyLoggedMinutes,
            soundEnabled: s.soundEnabled,
            vibrationEnabled: s.vibrationEnabled,
            notificationSound: sound,
            themeMode: s.themeMode,
            accentColor: s.accentColor
        )
        store.dispatch(intent: SettingsIntentUpdateReminders(settings: newSettings))
    }
    
    func updateAccentColor(_ hex: String) {
        let s = state.reminderSettings
        let newSettings = ReminderSettings(
            enabled: s.enabled,
            intervalMinutes: s.intervalMinutes,
            wakeTime: s.wakeTime,
            sleepTime: s.sleepTime,
            strategy: s.strategy,
            quietWindows: s.quietWindows,
            skipIfRecentlyLoggedMinutes: s.skipIfRecentlyLoggedMinutes,
            soundEnabled: s.soundEnabled,
            vibrationEnabled: s.vibrationEnabled,
            notificationSound: s.notificationSound,
            themeMode: s.themeMode,
            accentColor: hex
        )
        store.dispatch(intent: SettingsIntentUpdateReminders(settings: newSettings))
    }

    deinit { token?.cancel(); store.close() }
}

struct SettingsView: View {
    @StateObject private var model = SettingsModel()
    @State private var weight = ""
    @State private var age = ""
    @State private var interval = ""
    @State private var seeded = false

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                // Profile Section
                VStack(alignment: .leading, spacing: 14) {
                    Text("Profile").font(.headline).fontWeight(.bold)
                    
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Weight (kg)").font(.caption).foregroundStyle(.secondary)
                        TextField("Weight (kg)", text: $weight)
                            .keyboardType(.numberPad)
                            .textFieldStyle(.roundedBorder)
                    }
                    
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Age").font(.caption).foregroundStyle(.secondary)
                        TextField("Age", text: $age)
                            .keyboardType(.numberPad)
                            .textFieldStyle(.roundedBorder)
                    }
                    
                    Button("Save profile & recalculate goal") {
                        if let w = Double(weight), let a = Int32(age) { model.saveProfile(weight: w, age: a) }
                    }
                    .buttonStyle(.borderedProminent)
                    .frame(maxWidth: .infinity)
                }
                .padding()
                .glassCard(cornerRadius: 20)

                // Reminders Section
                VStack(alignment: .leading, spacing: 14) {
                    Text("Reminders").font(.headline).fontWeight(.bold)
                    
                    Toggle("Enabled", isOn: Binding(
                        get: { model.state.reminderSettings.enabled },
                        set: { model.setEnabled($0) }
                    ))
                    
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Interval (minutes)").font(.caption).foregroundStyle(.secondary)
                        TextField("Interval (minutes)", text: $interval)
                            .keyboardType(.numberPad)
                            .textFieldStyle(.roundedBorder)
                    }
                    
                    Button("Apply interval") {
                        if let m = Int32(interval), m > 0 { model.setInterval(m) }
                    }
                    .buttonStyle(.borderedProminent)
                    .frame(maxWidth: .infinity)
                }
                .padding()
                .glassCard(cornerRadius: 20)

                // Customization Section
                VStack(alignment: .leading, spacing: 14) {
                    Text("Customization").font(.headline).fontWeight(.bold)
                    
                    // 1. Theme Selection
                    Text("Theme").font(.subheadline).foregroundStyle(.secondary)
                    Picker("Theme Mode", selection: Binding(
                        get: { model.state.reminderSettings.themeMode },
                        set: { model.updateThemeMode($0) }
                    )) {
                        Text("System").tag("SYSTEM")
                        Text("Light").tag("LIGHT")
                        Text("Dark").tag("DARK")
                    }
                    .pickerStyle(.segmented)
                    
                    Divider()
                    
                    // 2. Notification Sound Selection
                    Text("Notification Sound").font(.subheadline).foregroundStyle(.secondary)
                    let sounds = ["default", "chime", "glass", "droplet", "ping"]
                    ForEach(sounds, id: \.self) { sound in
                        HStack {
                            Text(sound.capitalized)
                                .fontWeight(model.state.reminderSettings.notificationSound == sound ? .bold : .regular)
                            Spacer()
                            if model.state.reminderSettings.notificationSound == sound {
                                Image(systemName: "checkmark").foregroundColor(.accentColor)
                            }
                            Button(action: {
                                KoinHelper().playSoundPreview(soundName: sound)
                            }) {
                                Label("Preview", systemImage: "play.circle.fill")
                            }
                            .buttonStyle(.borderless)
                        }
                        .padding(.vertical, 4)
                        .contentShape(Rectangle())
                        .onTapGesture {
                            model.updateNotificationSound(sound)
                        }
                    }
                    
                    Divider()
                    
                    // 3. Accent Color Picker
                    Text("Accent Color").font(.subheadline).foregroundStyle(.secondary)
                    
                    let presets = [
                        ("Ocean Blue", "#006690"),
                        ("Teal Breeze", "#006A75"),
                        ("Sunset Orange", "#E65100"),
                        ("Emerald Green", "#1B5E20"),
                        ("Purple Rain", "#6A1B9A")
                    ]
                    
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 12) {
                            ForEach(presets, id: \.1) { name, hex in
                                Button(action: {
                                    model.updateAccentColor(hex)
                                }) {
                                    VStack {
                                        Circle()
                                            .fill(Color.fromHex(hex))
                                            .frame(width: 32, height: 32)
                                            .overlay(
                                                Circle()
                                                    .stroke(Color.primary, lineWidth: model.state.reminderSettings.accentColor.uppercased() == hex.uppercased() ? 2 : 0)
                                            )
                                        Text(name)
                                            .font(.caption2)
                                            .foregroundStyle(.primary)
                                    }
                                }
                            }
                        }
                    }
                    
                    // Custom Hex Picker
                    HStack {
                        Text("Custom Hex:")
                            .font(.caption)
                        TextField("#6C5CE7", text: Binding(
                            get: { model.state.reminderSettings.accentColor },
                            set: { val in
                                if val.count == 7 && val.hasPrefix("#") {
                                    model.updateAccentColor(val)
                                }
                            }
                        ))
                        .textFieldStyle(.roundedBorder)
                        .frame(width: 120)
                    }
                }
                .padding()
                .glassCard(cornerRadius: 20)
            }
            .padding()
        }
        .scrollContentBackground(.hidden)
        .scrollDismissesKeyboard(.interactively)
        .onChange(of: model.state.isLoading) { _, loading in seedIfNeeded(loading: loading) }
        .onAppear { seedIfNeeded(loading: model.state.isLoading) }
    }

    private func seedIfNeeded(loading: Bool) {
        guard !loading, !seeded else { return }
        weight = model.state.profile.map { String(Int($0.weightKg)) } ?? "70"
        age = model.state.profile.map { String($0.age) } ?? "30"
        interval = String(model.state.reminderSettings.intervalMinutes)
        seeded = true
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View { RootView() }
}
