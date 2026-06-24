import SwiftUI
import SharedLogic
import UserNotifications

/// Presents reminder notifications as banners even while the app is in the foreground,
/// so timer-based water reminders are visible whether or not the app is open.
final class NotificationDelegate: NSObject, UNUserNotificationCenterDelegate {
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound, .list])
    }
}

@main
struct iOSApp: App {
    private let notificationDelegate = NotificationDelegate()

    init() {
        // Initialize the shared Koin graph once at launch.
        KoinIosKt.doInitKoin()

        // Request local-notification authorization. Without this, iOS silently drops every
        // UNNotificationRequest the reminder scheduler adds, so no water reminder ever fires.
        let center = UNUserNotificationCenter.current()
        center.delegate = notificationDelegate
        center.requestAuthorization(options: [.alert, .sound, .badge]) { _, _ in }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
