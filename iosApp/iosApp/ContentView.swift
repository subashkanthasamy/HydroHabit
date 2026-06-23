import SwiftUI
import SharedLogic

// MARK: - Components

/// Animated circular hydration ring — the SwiftUI counterpart of the Compose `WaterRing`.
/// A track circle plus a trimmed, gradient progress arc that eases from 0 to `progress`.
struct WaterRing: View {
    let progress: Double      // 0...1
    let consumedMl: Int32
    let goalMl: Int32

    private var clamped: Double { min(max(progress, 0), 1) }

    var body: some View {
        ZStack {
            Circle()
                .stroke(Color(.systemGray5), style: StrokeStyle(lineWidth: 24, lineCap: .round))
            Circle()
                .trim(from: 0, to: clamped)
                .stroke(
                    AngularGradient(
                        gradient: Gradient(colors: [.cyan, .blue, .cyan]),
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
                    .foregroundStyle(.blue)
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
        .background(Color(.secondarySystemBackground))
        .cornerRadius(12)
    }
}

// MARK: - Root (bottom tabs, each in a NavigationStack for native large titles)

struct RootView: View {
    var body: some View {
        TabView {
            NavigationStack { HomeView().navigationTitle("HydroHabit") }
                .tabItem { Label("Home", systemImage: "drop.fill") }
            NavigationStack { HistoryView().navigationTitle("History") }
                .tabItem { Label("History", systemImage: "list.bullet") }
            NavigationStack { AnalyticsView().navigationTitle("Insights") }
                .tabItem { Label("Stats", systemImage: "chart.bar.fill") }
            NavigationStack { AchievementsView().navigationTitle("Achievements") }
                .tabItem { Label("Awards", systemImage: "trophy.fill") }
            NavigationStack { SettingsView().navigationTitle("Settings") }
                .tabItem { Label("Settings", systemImage: "gearshape.fill") }
        }
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
            VStack(alignment: .leading, spacing: 16) {
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
                                .background(Color.blue.opacity(0.08))
                                .cornerRadius(10)
                        }
                    }

                    Text("Recent activity").font(.headline)
                    if model.state.recentEntries.isEmpty {
                        Text("No water logged yet today.")
                            .font(.subheadline).foregroundStyle(.secondary)
                    } else {
                        ForEach(model.state.recentEntries, id: \.id) { entry in
                            HStack {
                                Text("\(entry.amountMl) ml").bold()
                                Spacer()
                                Text(entry.source.name.lowercased()).font(.caption).foregroundStyle(.secondary)
                            }
                            .padding(.vertical, 6)
                        }
                    }
                } else {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Welcome! Set up your goal").font(.headline)
                        TextField("Weight (kg)", text: $weight).keyboardType(.decimalPad).textFieldStyle(.roundedBorder)
                        TextField("Age", text: $age).keyboardType(.numberPad).textFieldStyle(.roundedBorder)
                        Button("Calculate my goal") {
                            if let w = Double(weight), let a = Int32(age) { model.createProfile(weight: w, age: a) }
                        }.buttonStyle(.borderedProminent)
                    }
                    .padding()
                    .background(Color.gray.opacity(0.1))
                    .cornerRadius(12)
                }
            }
            .padding()
        }
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
        VStack(spacing: 12) {
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
                List(model.state.entries, id: \.id) { entry in
                    HStack {
                        Text("\(entry.amountMl) ml").bold()
                        Text(entry.source.name.lowercased()).font(.caption).foregroundStyle(.secondary)
                        Spacer()
                        Button("Delete", role: .destructive) { model.delete(entry.id) }
                    }
                }
                .listStyle(.insetGrouped)
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
    @StateObject private var model = AnalyticsModel()
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Picker("Period", selection: Binding(
                    get: { model.state.period },
                    set: { model.select($0) }
                )) {
                    Text("Daily").tag(ReportPeriod.daily)
                    Text("Weekly").tag(ReportPeriod.weekly)
                    Text("Monthly").tag(ReportPeriod.monthly)
                }.pickerStyle(.segmented)

                if let report = model.state.report, !report.dailyBreakdown.isEmpty {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Average: \(report.averageMl) ml/day").bold()
                        Text("Goal completion: \(Int(report.goalCompletionRate * 100))%")
                        if let best = report.bestDay { Text("Best day: \(best.date.description) (\(best.consumedMl) ml)") }
                    }
                    .padding()
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color.gray.opacity(0.12))
                    .cornerRadius(12)

                    ForEach(report.insights, id: \.id) { insight in
                        Text(insight.message).padding().frame(maxWidth: .infinity, alignment: .leading)
                            .background(Color.blue.opacity(0.1)).cornerRadius(8)
                    }
                } else {
                    Text("Log some water to see your trends.").foregroundStyle(.secondary)
                }
            }
            .padding()
        }
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
    @StateObject private var model = AchievementsModel()
    private let columns = [GridItem(.flexible()), GridItem(.flexible())]
    var body: some View {
        ScrollView {
            VStack(alignment: .leading) {
                Text("\(model.state.unlockedCount) of \(model.state.achievements.count) unlocked")
                    .font(.subheadline).foregroundStyle(.secondary).padding(.bottom, 8)
                LazyVGrid(columns: columns, spacing: 12) {
                    ForEach(model.state.achievements, id: \.id) { a in
                        VStack(alignment: .leading, spacing: 6) {
                            Text(a.isUnlocked ? "🏆 \(a.title)" : "🔒 \(a.title)").bold()
                            Text(a.description_).font(.caption)
                            if !a.isUnlocked { ProgressView(value: Double(a.progress)) }
                        }
                        .padding()
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background((a.isUnlocked ? Color.yellow : Color.gray).opacity(0.15))
                        .cornerRadius(10)
                    }
                }
            }
            .padding()
        }
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
    deinit { token?.cancel(); store.close() }
}

struct SettingsView: View {
    @StateObject private var model = SettingsModel()
    @State private var weight = ""
    @State private var age = ""
    @State private var interval = ""
    @State private var seeded = false

    var body: some View {
        Form {
            Section("Profile") {
                TextField("Weight (kg)", text: $weight).keyboardType(.numberPad)
                TextField("Age", text: $age).keyboardType(.numberPad)
                Button("Save profile & recalculate goal") {
                    if let w = Double(weight), let a = Int32(age) { model.saveProfile(weight: w, age: a) }
                }
            }
            Section("Reminders") {
                Toggle("Enabled", isOn: Binding(
                    get: { model.state.reminderSettings.enabled },
                    set: { model.setEnabled($0) }
                ))
                TextField("Interval (minutes)", text: $interval).keyboardType(.numberPad)
                Button("Apply interval") {
                    if let m = Int32(interval), m > 0 { model.setInterval(m) }
                }
            }
        }
        .scrollDismissesKeyboard(.interactively)
        .onChange(of: model.state.isLoading) { _, loading in seedIfNeeded(loading: loading) }
        .onAppear { seedIfNeeded(loading: model.state.isLoading) }
    }

    // Seed editable fields once when data has loaded, so reactive updates never reset typing.
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
