@file:Suppress("PrivatePropertyName")

package com.luisdelae.geotagr

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.luisdelae.geotagr.data.LocationRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GeoTagrBroadcastReceiver : BroadcastReceiver() {
    private val TAG = "GeoTagrBroadcastReceiver"

    @Inject lateinit var locationRepository: LocationRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e(TAG, "onReceive")

        val geofencingEvent = GeofencingEvent.fromIntent(intent!!)

        if (geofencingEvent?.hasError() == true) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent?.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
        geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            locationRepository.receivedGeofenceEvent.value = true

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            if (triggeringGeofences?.any() == true) {
                val geofenceTransitionDetails = getGeofenceTransitionDetails(
                    geofenceTransition,
                    triggeringGeofences.toList()
                )

                Log.i(TAG, geofenceTransitionDetails)

//                val serviceIntent = Intent(context, LocationService::class.java)
//                context!!.startForegroundService(serviceIntent)
            }

            // Send notification and log the transition details.
//            sendNotification(geofenceTransitionDetails)
        } else {
            // Log the error.
            Log.e(TAG, "Not a valid transition: $geofenceTransition")
        }
    }

    private fun getGeofenceTransitionDetails(
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>
    ): String {
        val geofenceTransitionString: String = getTransitionString(geofenceTransition)

        // Get the Ids of each geofence that was triggered.
        val triggeringGeofencesIdsList = mutableListOf<String>()
        for (geofence in triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.requestId)
        }
        val triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList)
        return "$geofenceTransitionString: $triggeringGeofencesIdsString"
    }

    private fun getTransitionString(transitionType: Int): String {
        return when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "transition - enter"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "transition - exit"
            else -> "unknown transition"
        }
    }
}