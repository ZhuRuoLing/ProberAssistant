package icu.takeneko.proberassist

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.net.ConnectivityManager
import android.net.Network
import androidx.core.content.getSystemService
import com.google.android.material.color.DynamicColors
import icu.takeneko.proberassist.network.DefaultNetworkListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
        GlobalScope.launch(Dispatchers.Default) {
            DefaultNetworkListener.start(this){
                underlyingNetwork = it
            }
        }
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
        lateinit var application: App
        val connectivity by lazy {
            application.getSystemService<ConnectivityManager>()!!
        }
        val notification by lazy {
            application.getSystemService<NotificationManager>()!!
        }
        var underlyingNetwork: Network? = null
    }


}
