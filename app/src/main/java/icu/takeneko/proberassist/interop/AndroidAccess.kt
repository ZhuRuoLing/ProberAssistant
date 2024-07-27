package icu.takeneko.proberassist.interop

import android.util.Log
import icu.takeneko.proberassist.App.Companion.TAG

class AndroidAccess {
    fun logI(content: String) {
        Log.i(TAG, content)
    }

    fun logW(content: String) {
        Log.w(TAG, content)
    }

    fun logE(content: String) {
        Log.e(TAG, content)
    }
}