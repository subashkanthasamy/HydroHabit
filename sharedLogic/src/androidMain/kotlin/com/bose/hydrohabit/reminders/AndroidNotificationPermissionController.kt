package com.bose.hydrohabit.reminders

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.bose.hydrohabit.domain.reminders.NotificationPermissionController

/**
 * Reports POST_NOTIFICATIONS permission state on Android 13+. The actual runtime request must be
 * launched from an Activity/Compose permission launcher; [requestPermission] therefore just
 * reflects the current grant state.
 */
class AndroidNotificationPermissionController(private val context: Context) : NotificationPermissionController {

    override suspend fun hasPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    override suspend fun requestPermission(): Boolean = hasPermission()
}
