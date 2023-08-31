@file:Suppress("PrivatePropertyName")

package com.luisdelae.geotagr.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.luisdelae.geotagr.R
import com.luisdelae.geotagr.data.model.GeofenceEvent
import com.luisdelae.geotagr.data.repository.LocationRepository
import com.luisdelae.geotagr.utils.Constants.FOREGROUND_SERVICE_NOTIFICATION_ID
import com.luisdelae.geotagr.utils.Constants.GEOFENCE_NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GeoTagrLocationForegroundService : LifecycleService() {
    private val TAG = "GeoTagrLocationService"

    private val EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION =
        "extra.CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"

    private val NOTIFICATION_CHANNEL_ID = "while_in_use_channel_01"

    @Inject lateinit var locationRepository: LocationRepository

    private var configurationChange = false

    private var serviceRunningInForeground = false

    private val localBinder = LocalBinder()

    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)

        Log.d(TAG, "onBind()")

        stopForeground(STOP_FOREGROUND_REMOVE)
        serviceRunningInForeground = false
        configurationChange = false

        return localBinder
    }

    override fun onRebind(intent: Intent) {
        Log.d(TAG, "onRebind()")

        stopForeground(STOP_FOREGROUND_REMOVE)
        serviceRunningInForeground = false
        configurationChange = false

        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "onUnbind()")

        if (!configurationChange) {
            Log.d(TAG, "Start foreground service")

            val notification = generateGeofenceServiceNotification()
            startForeground(FOREGROUND_SERVICE_NOTIFICATION_ID, notification)

            serviceRunningInForeground = true
        }

        return true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        Log.d(TAG, "onStartCommand()")

        val cancelLocationTrackingFromNotification =
            intent?.getBooleanExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, false)

        if (cancelLocationTrackingFromNotification == true) {
            unsubscribeToLocationUpdates()
            stopSelf()
        }

        return START_NOT_STICKY
    }

    fun subscribeToLocationUpdates() {
        Log.d(TAG, "subscribeToLocationUpdates()")

        startService(Intent(applicationContext, GeoTagrLocationForegroundService::class.java))

        try {
            // TODO: Step 1.5, Subscribe to location changes.
            lifecycleScope.launch {
                locationRepository.geofenceEventFlow.collect { event ->
                    when (event.geofenceEvent) {
                        GeofenceEvent.INITIAL -> { }
                        GeofenceEvent.ENTER -> {
                            notificationManager.notify(
                                GEOFENCE_NOTIFICATION_ID,
                                generateGeofenceNotification(event.messageText)
                            )
                        }
                        GeofenceEvent.EXIT -> { }
                    }
                }
            }
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    fun unsubscribeToLocationUpdates() {
        Log.d(TAG, "unsubscribeToLocationUpdates()")

        try {
            // TODO: Step 1.6, Unsubscribe to location changes.
            cancelGeofence()
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    private fun cancelGeofence() {
        locationRepository.cancelGeofence()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy()")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }

    private fun generateGeofenceNotification(message: String): Notification {
        val title = getString(R.string.geotagr_event)

        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID, title, NotificationManager.IMPORTANCE_DEFAULT)

        notificationManager.createNotificationChannel(notificationChannel)

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(message)
            .setBigContentTitle(title)

        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)

        return notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun generateGeofenceServiceNotification(): Notification {
        val title = getString(R.string.app_name)
        val body = getString(R.string.geotagr_is_tracking_you_in_the_background)

        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID, title, NotificationManager.IMPORTANCE_DEFAULT)

        notificationManager.createNotificationChannel(notificationChannel)

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(body)
            .setBigContentTitle(title)

        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)

        val cancelIntent = Intent(this, GeoTagrLocationForegroundService::class.java)
        cancelIntent.putExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, true)

        val servicePendingIntent = PendingIntent.getService(
            this, 0, cancelIntent, PendingIntent.FLAG_IMMUTABLE)

        return notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.ic_cancel,
                getString(R.string.stop_location_updates_button_text),
                servicePendingIntent)
            .build()
    }

    inner class LocalBinder : Binder() {
        internal val service: GeoTagrLocationForegroundService
            get() = this@GeoTagrLocationForegroundService
    }
}