package app.extremefocus.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ExtremeFocusDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.i("DeviceAdmin", "Extreme Focus Device Admin ENABLED (Anti-Uninstall active)")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.w("DeviceAdmin", "Extreme Focus Device Admin DISABLED")
    }
}
