package icu.takeneko.proberassist

enum class ApplicationState(
    val color: Int,
    val textDescription: Int,
    val buttonText: Int,
    val icon: Int,
    val enableTextBox: Boolean,
    val enableButton: Boolean
) {
    STOP(
        R.color.grey,
        R.string.status_not_running,
        R.string.start,
        R.drawable.ic_baseline_cross_circle_24,
        true,
        true
    ),
    LOGIN(
        R.color.yellow,
        R.string.status_login,
        R.string.start,
        R.drawable.ic_baseline_circle_24,
        false,
        false
    ),
    PROXY(
        R.color.orange,
        R.string.status_proxy,
        R.string.start,
        R.drawable.ic_baseline_circle_24,
        false,
        false
    ),
    RUNNING(
        R.color.green,
        R.string.status_running,
        R.string.stop,
        R.drawable.ic_baseline_check_circle_24,
        false,
        true
    ),
    STOPPING(
        R.color.yellow,
        R.string.status_stopping,
        R.string.start,
        R.drawable.ic_baseline_circle_24,
        false, false
    )
}