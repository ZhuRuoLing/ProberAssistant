package icu.takeneko.proberassist.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.net.VpnService
import android.util.Log
import icu.takeneko.proberassist.App
import icu.takeneko.proberassist.ApplicationState
import icu.takeneko.proberassist.R
import kotlin.properties.Delegates

class LocalProxyService : VpnService() {

    private val TAG = "LocalProxyService"
    var startId by Delegates.notNull<Int>()
    var localProxyPort by Delegates.notNull<Int>()
    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val status = intent.getIntExtra("status", 0)
        if (status !in listOf(ApplicationState.PROXY.ordinal, ApplicationState.RUNNING.ordinal)) {
            stopSelf(startId)
        }
        localProxyPort = intent.getIntExtra("localProxyPort", 0)
        if (localProxyPort == 0) {
            stopSelf(startId)
        }
        this.startId = startId
        Log.i(TAG, "onStartCommand: $startId service started")
        startForeground(
            1,
            Notification.Builder(this, App.CHANNEL_STATUS_ID)
                .setContentTitle(getText(R.string.local_proxy_running))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
        )
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onRevoke() {
        Log.i(TAG, "onRevoke: $startId service stopped")
        stopSelf(startId)
    }
}