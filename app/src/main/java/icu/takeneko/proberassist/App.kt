package icu.takeneko.proberassist

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import com.google.android.material.color.DynamicColors
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.sui.Sui

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        registerNotificationChannel(
            R.string.channel_status,
            R.string.channel_status_desc,
            CHANNEL_STATUS_ID
        )
        application = this
    }

    private fun registerNotificationChannel(
        channelName: Int,
        channelDescription: Int,
        channelId: String
    ) {
        val name = getString(channelName)
        val descriptionText = getString(channelDescription)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(channelId, name, importance)
        mChannel.description = descriptionText
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

    companion object {
        const val CHANNEL_STATUS_ID = "itpa.status"
        const val TAG = "ProberAssistant"
        const val PROXY_PORT = 8866
        lateinit var application: App
        val notification by lazy {
            application.getSystemService<NotificationManager>()!!
        }
        var isSui = Sui.init("icu.takeneko.proberassist")
            private set
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        HiddenApiBypass.addHiddenApiExemptions("L")
    }

}
