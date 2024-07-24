package icu.takeneko.proberassist

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import icu.takeneko.proberassist.databinding.ActivityMainBinding
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var state: ApplicationState by Delegates.observable(ApplicationState.STOP) { _, before, after ->
        updateStateDisplay(before, after)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.buttonSwitch.setOnClickListener {
            if (state == ApplicationState.STOP) {
                if (!start()) {
                    state = ApplicationState.STOP
                    return@setOnClickListener
                }
            } else {
                if (!stop()) {
                    state = ApplicationState.RUNNING
                    return@setOnClickListener
                }
            }
        }
        state = ApplicationState.STOP
    }

    fun start(): Boolean {
        state = ApplicationState.LOGIN
        val password = binding.textPassword.text ?: ""
        val username = binding.textUsername.text ?: ""
        if (password.isEmpty() || username.isEmpty()) {
            Snackbar.make(
                binding.buttonSwitch,
                R.string.no_password_no_username,
                Snackbar.LENGTH_LONG
            ).setAction(R.string.ok) {}.setAnchorView(binding.buttonSwitch).show()
            return false
        }
        state = ApplicationState.RUNNING
        return true
    }

    fun stop(): Boolean {
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