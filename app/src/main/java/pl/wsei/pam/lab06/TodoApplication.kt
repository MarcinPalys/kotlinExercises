package pl.wsei.pam.lab06

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import pl.wsei.pam.lab06.data.AppContainer
import pl.wsei.pam.lab06.data.AppDataContainer

class TodoApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this.applicationContext)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelID,
            "Lab06 channel",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Lab06 is channel for notifications for approaching tasks."
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
