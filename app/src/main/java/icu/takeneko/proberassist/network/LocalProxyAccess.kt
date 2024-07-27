package icu.takeneko.proberassist.network

import libprob.Libprob
import kotlin.concurrent.thread

object LocalProxyAccess {
    var thread: Thread? = null

    fun start() {
        thread?.interrupt()
        thread?.join()
        thread = thread(name = "Proxy Thread") {
            Libprob.startProxy()
        }
    }

    fun stop() {
        thread?.interrupt()
        thread?.join()
    }
}