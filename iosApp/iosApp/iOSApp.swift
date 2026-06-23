import SwiftUI
import SharedLogic

@main
struct iOSApp: App {
    init() {
        // Initialize the shared Koin graph once at launch.
        KoinIosKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
