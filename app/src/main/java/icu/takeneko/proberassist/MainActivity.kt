package icu.takeneko.proberassist

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.ProxyInfo
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import icu.takeneko.proberassist.App.Companion.TAG
import icu.takeneko.proberassist.databinding.ActivityMainBinding
import icu.takeneko.proberassist.network.IConnectivityManagerAccess
import icu.takeneko.proberassist.network.ProberAccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.OnBinderDeadListener
import rikka.shizuku.Shizuku.OnBinderReceivedListener
import rikka.shizuku.Shizuku.OnRequestPermissionResultListener
import rikka.shizuku.ShizukuSystemProperties
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var shizukuBinderState = false
    private var state: ApplicationState by Delegates.observable(ApplicationState.STOP) { _, before, after ->
        lifecycleScope.launch(Dispatchers.Main) {
            updateStateDisplay(before, after)
        }
    }
    private val onBinderReceivedListener = OnBinderReceivedListener {
        if (Shizuku.isPreV11()) {
            info(R.string.not_supported_shuzuku_version)
            return@OnBinderReceivedListener
        }
        shizukuBinderState = true
    }

    private val onBinderDeadListener = OnBinderDeadListener {
        shizukuBinderState = false
    }

    private val onRequestPermissionResultListener =
        OnRequestPermissionResultListener { requestCode, grantResult ->
            Log.i(TAG, "permission: $grantResult")
            if (grantResult == PERMISSION_GRANTED) {
                GlobalScope.launch(Dispatchers.IO) {
                    while (!shizukuBinderState) delay(1)
                    IConnectivityManagerAccess.connectivityManager.globalProxy =
                        ProxyInfo.buildDirectProxy("localhost", App.PROXY_PORT)
                    for (declaredMethod in IConnectivityManagerAccess.connectivityManager.javaClass.methods) {
                        println(declaredMethod)
                    }
                    state = ApplicationState.RUNNING
                }
            } else {
                info(R.string.permission_denied)
                state = ApplicationState.STOP
            }
        }

    private fun checkPermission(code: Int = 0): Boolean {
        if (Shizuku.isPreV11()) {
            info(R.string.not_supported_shuzuku_version)
            return false
        }
        return try {
            if (Shizuku.checkSelfPermission() == PERMISSION_GRANTED) {
                true
            } else {
                if (Shizuku.shouldShowRequestPermissionRationale()) {
                    info(R.string.permission_denied)
                    false
                } else {
                    Shizuku.requestPermission(code)
                    false
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            info(e.stackTraceToString())
            state = ApplicationState.STOP
            false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.buttonSwitch.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                if (state == ApplicationState.STOP) {
                    start()
                } else {
                    stop()
                }
            }
        }
        state = ApplicationState.STOP
        ProberAccess.load(this)
        binding.textUsername.setText(ProberAccess.username)
        binding.textPassword.setText(ProberAccess.password)
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                "android.permission.POST_NOTIFICATIONS"
            )
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    "android.permission.POST_NOTIFICATIONS"
                ),
                0
            )
        }
        Shizuku.addBinderReceivedListenerSticky(onBinderReceivedListener)
        Shizuku.addBinderDeadListener(onBinderDeadListener)
        Shizuku.addRequestPermissionResultListener(onRequestPermissionResultListener)
    }

    override fun onDestroy() {
        Shizuku.removeBinderReceivedListener(onBinderReceivedListener)
        Shizuku.removeBinderDeadListener(onBinderDeadListener)
        Shizuku.removeRequestPermissionResultListener(onRequestPermissionResultListener)
        IConnectivityManagerAccess.connectivityManager.globalProxy = null
        super.onDestroy()
    }

    private fun info(infoId: Int) {
        lifecycleScope.launch(Dispatchers.Main) {
            Snackbar.make(
                binding.buttonSwitch,
                infoId,
                Snackbar.LENGTH_LONG
            ).setAction(R.string.ok) {}.setAnchorView(binding.buttonSwitch).show()
        }
    }

    private fun info(info: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Snackbar.make(
                binding.buttonSwitch,
                info,
                Snackbar.LENGTH_LONG
            ).setAction(R.string.ok) {}.setAnchorView(binding.buttonSwitch).show()
        }
    }

    private suspend fun start(): Boolean {
        val password = binding.textPassword.text?.toString() ?: ""
        val username = binding.textUsername.text?.toString() ?: ""
        if (password.isEmpty() || username.isEmpty()) {
            info(R.string.no_password_no_username)
            state = ApplicationState.STOP
            return false
        }
        state = ApplicationState.LOGIN
        ProberAccess.updateAccount(username, password, this)
        try {
            if (!ProberAccess.login()) {
                info(R.string.invalid_login_credentals)
                state = ApplicationState.STOP
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            info(R.string.login_failed)
            state = ApplicationState.STOP
            return false
        }
        state = ApplicationState.PROXY
        if (checkPermission()) {
            onRequestPermissionResultListener.onRequestPermissionResult(0, PERMISSION_GRANTED)
        }
        return true
    }

    private fun stop(): Boolean {
        IConnectivityManagerAccess.connectivityManager.globalProxy = null
        state = ApplicationState.STOP
        return true
    }

    private fun updateStateDisplay(before: ApplicationState, after: ApplicationState) {
        if (before == after) {
            binding.statusTitle.setText(after.textDescription)
            binding.statusCard.setCardBackgroundColor(getColor(after.color))
            binding.textUsername.alpha = if (after.enableTextBox) 1f else 0f
            binding.textPassword.alpha = if (after.enableTextBox) 1f else 0f
            binding.textUsernameFrame.alpha = if (after.enableTextBox) 1f else 0f
            binding.textPasswordFrame.alpha = if (after.enableTextBox) 1f else 0f
            binding.textUsername.isEnabled = after.enableTextBox
            binding.textPassword.isEnabled = after.enableButton
            binding.buttonSwitch.isEnabled = after.enableButton
            return
        }

        binding.statusTitle.setText(after.textDescription)
        val colorAnim = ObjectAnimator.ofInt(
            binding.statusCard,
            "cardBackgroundColor",
            getColor(before.color),
            getColor(after.color)
        )
        colorAnim.setDuration(250)
        colorAnim.setEvaluator(ArgbEvaluator())
        colorAnim.start()
        binding.statusIcon.setImageResource(after.icon)
        if (after.enableTextBox) {
            binding.textUsername.animate().alpha(1f).setDuration(250).start()
            binding.textPassword.animate().alpha(1f).setDuration(250).start()
            binding.textUsernameFrame.animate().alpha(1f).setDuration(250).start()
            binding.textPasswordFrame.animate().alpha(1f).setDuration(250).start()
        } else {
            binding.textUsername.animate().alpha(0f).setDuration(250).start()
            binding.textPassword.animate().alpha(0f).setDuration(250).start()
            binding.textUsernameFrame.animate().alpha(0f).setDuration(250).start()
            binding.textPasswordFrame.animate().alpha(0f).setDuration(250).start()
        }
        binding.textUsername.isEnabled = after.enableTextBox
        binding.textPassword.isEnabled = after.enableTextBox
        binding.buttonSwitch.isEnabled = after.enableButton
        binding.buttonSwitch.setText(after.buttonText)
    }
}