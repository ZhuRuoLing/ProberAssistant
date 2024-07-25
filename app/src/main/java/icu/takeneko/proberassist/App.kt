package icu.takeneko.proberassist

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.google.android.material.color.DynamicColors

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        registerNotificationChannel(R.string.channel_status, R.string.channel_status_desc, CHANNEL_STATUS_ID)
    }

    private fun registerNotificationChannel(
        channelName: Int,
        channelDescription: Int,
        channelId: String
    ){
        val name = getString(channelName)
        val descriptionText = getString(channelDescription)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(channelId, name, importance)
        mChannel.description = descriptionText
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

    companion object{
        const val CHANNEL_STATUS_ID = "itpa.status"
    }
}
