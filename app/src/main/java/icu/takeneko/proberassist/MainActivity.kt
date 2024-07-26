package icu.takeneko.proberassist

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import icu.takeneko.proberassist.databinding.ActivityMainBinding
import icu.takeneko.proberassist.network.ProberAccess
import icu.takeneko.proberassist.service.LocalProxyService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var state: ApplicationState by Delegates.observable(ApplicationState.STOP) { _, before, after ->
        lifecycleScope.launch(Dispatchers.Main) {
            updateStateDisplay(before, after)
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
        lifecycleScope.launch(Dispatchers.Main) {
            val intent = VpnService.prepare(this@MainActivity)
            if (intent == null) {
                onActivityResult(0, RESULT_OK, null);
            } else {
                startActivityForResult(intent, 0)
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            info(R.string.permission_disallowed_proxy)
            state = ApplicationState.STOP
            return
        }
        val intent = Intent(this, LocalProxyService::class.java)
        intent.putExtra("status", state.ordinal)
        intent.putExtra("localProxyPort", 8282)
        startService(intent)
        state = ApplicationState.RUNNING
    }

    private fun stop(): Boolean {
        state = ApplicationState.STOP
        LocalProxyService.instance?.stop()
        stopService(Intent(this, LocalProxyService::class.java))
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