package icu.takeneko.proberassist.util

import android.app.Notification
import android.content.Context
import icu.takeneko.proberassist.App

private var notificationId = 0

fun Context.sendNotification(titleId: Int, messageId: Int, priority: Int) {
    App.notification.notify(
        notificationId++,
        Notification.Builder(this, App.CHANNEL_STATUS_ID)
            .setContentTitle(getText(titleId))
            .setContentText(getText(messageId))
            .setChannelId(App.CHANNEL_STATUS_ID)
            .build()
    )
}