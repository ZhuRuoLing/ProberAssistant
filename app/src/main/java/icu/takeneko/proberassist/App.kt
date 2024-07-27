package icu.takeneko.proberassist

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.AssetManager
import androidx.core.content.getSystemService
import com.google.android.material.color.DynamicColors
import icu.takeneko.proberassist.interop.Interop
import icu.takeneko.proberassist.util.sendNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.actor
import libprob.Libprob
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
        Libprob.updatePlatform(Interop(PROXY_PORT, notificationSender))
        assetManager = assets
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
        lateinit var assetManager:AssetManager
        var isSui = Sui.init("icu.takeneko.proberassist")
            private set

        private val notificationSender = GlobalScope.actor<String>(Dispatchers.Main){
            for (m in channel){
                application.sendNotification(R.string.status, m)
            }
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        HiddenApiBypass.addHiddenApiExemptions("L")
    }

}
