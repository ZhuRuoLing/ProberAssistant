package icu.takeneko.proberassist.network

import android.os.Process
import libprob.Libprob
import kotlin.concurrent.thread

object LocalProxyAccess {
    var thread: Thread? = null

    fun start() {
        thread?.interrupt()
        thread?.join()
        thread = thread(name = "Proxy Thread", isDaemon = true) {
            Libprob.startProxy()
        }
    }

    fun stop() {
        thread?.interrupt()
        Libprob.stopProxy()
    }
}