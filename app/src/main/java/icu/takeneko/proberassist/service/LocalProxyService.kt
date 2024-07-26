package icu.takeneko.proberassist.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.net.ProxyInfo
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import icu.takeneko.proberassist.App
import icu.takeneko.proberassist.App.Companion.TAG
import icu.takeneko.proberassist.ApplicationState
import icu.takeneko.proberassist.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.IllegalStateException
import kotlin.properties.Delegates

class LocalProxyService : VpnService() {
    private var startId by Delegates.notNull<Int>()
    private var localProxyPort by Delegates.notNull<Int>()
    private var shouldKeepRunning = true
    private lateinit var fd: ParcelFileDescriptor


    override fun onCreate() {
        super.onCreate()
        instance = this
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

        GlobalScope.launch {
            val builder = Builder()
                .setSession(getString(R.string.app_name))
                .setMtu(8192)
                .addAddress("172.0.72.1", 30)
                .addAddress("fdfe:0000:0721::1", 126)
                .addDnsServer("172.0.72.2")
                .addRoute("0.0.0.0", 0)
                .addRoute("::", 0)
                .addDisallowedApplication("icu.takeneko.proberassist")
                .setHttpProxy(ProxyInfo.buildDirectProxy("localhost", localProxyPort))
            App.underlyingNetwork?.let {
                builder.setUnderlyingNetworks(arrayOf(it))
            }
            fd = builder.establish()
                ?: throw IllegalStateException("Could not establish local vpn connection")
            launch(Dispatchers.IO) {
                val input = FileInputStream(fd.fileDescriptor)
                val arr = ByteArray(8192) { 0 }
                while (shouldKeepRunning) {
                    val len = input.read(arr)
                }
            }
            launch(Dispatchers.IO) {
                val output = FileOutputStream(fd.fileDescriptor)
                while (shouldKeepRunning) {

                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        instance = null
    }

    override fun onRevoke() {
        shouldKeepRunning = false
        fd.close()
        Log.i(TAG, "onRevoke: $startId service stopped")
        stopSelf()
    }

    fun stop() {
        onRevoke()
    }


    companion object {
        var instance: LocalProxyService? = null
    }
}