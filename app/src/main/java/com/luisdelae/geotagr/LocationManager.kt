@file:Suppress("PrivatePropertyName")

package com.luisdelae.geotagr

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class LocationManager(context: Context) {
    private val TAG = "LocationManager"

    private val locationClient = LocationServices.getFusedLocationProviderClient(context)
    private val geofenceClient = LocationServices.getGeofencingClient(context)

    private var _originalLocation: Location? = null
    private var _radiusInMeters = 10F

    private val locationUpdateCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)


            locationResult.lastLocation?.let { lastLocation ->
                Log.d(TAG, "$lastLocation")

                _originalLocation?.let { origLocation ->
                    Log.d(TAG, "${isWithinGeoFence(origLocation, lastLocation, _radiusInMeters)}")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun createGeofenceAroundCurrentLocation(key: String, radiusInMeters: Float = 10.0f) {
        _radiusInMeters = radiusInMeters

        locationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener {
            _originalLocation = it
            val locationRequest = LocationRequest
                .Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()

            locationClient.requestLocationUpdates(locationRequest, locationUpdateCallback, Looper.getMainLooper())
        }
    }

    private fun isWithinGeoFence(originalLocation: Location, currentLocation: Location, radiusInMeters: Float): Boolean {
        val longitude = currentLocation.longitude - originalLocation.longitude
        val latitude = currentLocation.latitude - originalLocation.latitude

        val a = sin(latitude / 2).pow(2.0) + (cos(originalLocation.latitude)
                * cos(currentLocation.latitude) * sin(longitude / 2).pow(2.0))
        val circuit = (2 * asin(sqrt(a))).toFloat()
        val earthRadiusMeters = 6371000.0F

        return (circuit * earthRadiusMeters) < radiusInMeters
    }







    private var _geoFenceRequestCreated = MutableLiveData<Boolean>()

    val geoFenceRequestCreated get() = _geoFenceRequestCreated

    private val geofencingPendingIntent by lazy {
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, GeoTagrBroadcastReceiver::class.java),
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_IMMUTABLE
            }
        )
    }

    @SuppressLint("MissingPermission")
    fun createGeofenceAroundCurrentLocation2(key: String, radiusInMeters: Float = 10.0f) {
        locationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener {
            val geoFence = createGeofence(key, it, radiusInMeters)
            registerGeofence(geoFence)
        }
    }

    private fun createGeofence(
        key: String,
        location: Location,
        radiusInMeters: Float
    ): Geofence {
        return Geofence.Builder()
            .setRequestId(key)
            .setCircularRegion(location.latitude, location.longitude, radiusInMeters)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setNotificationResponsiveness(5000)
            .setTransitionTypes(GEOFENCE_TRANSITION_ENTER or GEOFENCE_TRANSITION_EXIT)
            .build()
    }

    private fun createGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
        }.build()
    }

    @SuppressLint("MissingPermission")
    private fun registerGeofence(geofence: Geofence) {
        geofenceClient.addGeofences(createGeofencingRequest(geofence), geofencingPendingIntent)
            .addOnSuccessListener {
                Log.d(TAG, "registerGeofence: SUCCESS")
                _geoFenceRequestCreated.value = true
            }.addOnFailureListener { exception ->
                _geoFenceRequestCreated.value = false
                Log.d(TAG, "registerGeofence: Failure\n$exception")
            }
    }
}