package icu.takeneko.proberassist.interop

import android.util.Log
import icu.takeneko.proberassist.App
import icu.takeneko.proberassist.App.Companion.TAG
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import libprob.Platform

class Interop(private val localProxyPort: Int, private val messageActor: SendChannel<String>): Platform {

    override fun logI(content: String) {
        Log.i(TAG, content)
    }

    override fun logW(content: String) {
        Log.w(TAG, content)
    }

    override fun maiResultCallback(index: Long, encoded: ByteArray) {
        Log.i(TAG, "maiResultCallback: $index ${encoded.decodeToString()}")
    }

    override fun chuniResultCallback(index: Long, encoded: ByteArray) {
        Log.i(TAG, "chuniResultCallback: $index ${encoded.decodeToString()}")
    }

    override fun getCertificateContent(): ByteArray {
        return App.assetManager.open("cert.crt").readBytes()
    }

    override fun getPrivateKeyContent(): ByteArray {
        return App.assetManager.open("key.pem").readBytes()
    }

    override fun getLocalProxyPort(): Long = localProxyPort.toLong()

    override fun logE(content: String) {
        Log.e(TAG, content)
    }

    override fun notifyStatus(content: String) {
        GlobalScope.launch {
            messageActor.send(content)
        }
    }

}