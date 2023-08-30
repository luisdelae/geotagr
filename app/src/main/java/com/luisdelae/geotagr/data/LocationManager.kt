@file:Suppress("PrivatePropertyName")

package com.luisdelae.geotagr.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class LocationManager(context: Context, val externalScope: CoroutineScope) {
    private val TAG = "LocationManager"

    private val locationClient = LocationServices.getFusedLocationProviderClient(context)

    private var _originalLocation: Location? = null
    private var _radiusInMeters = 10F
    private var _geofenceNotificationMessage = ""

    private var _geoFenceRequestCreated = MutableStateFlow<Boolean?>(null)
    val geoFenceRequestCreated get() = _geoFenceRequestCreated


    private var _isInGeofenceFlow = MutableStateFlow<Boolean?>(null)
    val isInGeofenceFlow get() = _isInGeofenceFlow

    private val locationUpdateCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            locationResult.lastLocation?.let { lastLocation ->
                Log.d(TAG, "$lastLocation")

                _originalLocation?.let { origLocation ->
                    val isWithinGeofence = isWithinGeoFence(origLocation, lastLocation, _radiusInMeters)
                    Log.d(TAG, "isWithinGeofence: $isWithinGeofence")
                    externalScope.launch {
                        _isInGeofenceFlow.emit(isWithinGeofence)
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun createGeofenceAroundCurrentLocation(
        radiusInMeters: Float = 10.0f,
        geofenceNotificationMessage: String = ""
    ) {
        resetFlows()

        _radiusInMeters = radiusInMeters
        _geofenceNotificationMessage = geofenceNotificationMessage

        locationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener {
            Log.d(TAG, "Current location acquired.")

            _originalLocation = it

            createLocationUpdateRequest()
        }
    }

    private fun resetFlows() {
        externalScope.launch {
            _geoFenceRequestCreated.emit(null)
            _isInGeofenceFlow.emit(null)
        }
    }

    @SuppressLint("MissingPermission")
    private fun createLocationUpdateRequest() {
        val locationRequest = LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000).build()

        locationClient.requestLocationUpdates(
            locationRequest, locationUpdateCallback, Looper.getMainLooper())
            .addOnSuccessListener {
                Log.d(TAG, "Location update request created.")
                externalScope.launch {
                    _geoFenceRequestCreated.emit(true)
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Location update request failed to create.")

                externalScope.launch {
                    _geoFenceRequestCreated.emit(false)
                }
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
}