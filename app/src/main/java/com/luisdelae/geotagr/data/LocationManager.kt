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
import com.luisdelae.geotagr.data.model.GeoTagrEvent
import com.luisdelae.geotagr.data.model.GeofenceEvent
import com.luisdelae.geotagr.data.model.GeofenceRequest
import com.luisdelae.geotagr.data.model.GeofenceRequestStatus
import com.luisdelae.geotagr.utils.Constants.EARTH_RADIUS_METERS
import com.luisdelae.geotagr.utils.Constants.LOCATION_UPDATE_INTERVAL
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

    private var _geoFenceRequestStatusFlow = MutableStateFlow(GeofenceRequestStatus.INITIAL)
    val geoFenceRequestStatusFlow get() = _geoFenceRequestStatusFlow


    private var _geoFenceEventFlow = MutableStateFlow(GeoTagrEvent(GeofenceEvent.INITIAL, ""))
    val geoFenceEventFlow get() = _geoFenceEventFlow

    private val locationUpdateCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            locationResult.lastLocation?.let { lastLocation ->
                Log.d(TAG, "$lastLocation")

                _originalLocation?.let { origLocation ->
                    val isWithinGeofence = isWithinGeoFence(origLocation, lastLocation, _radiusInMeters)
                    Log.d(TAG, "isWithinGeofence: $isWithinGeofence")

                    externalScope.launch {
                        if (isWithinGeofence) {
                            _geoFenceEventFlow.emit(
                                GeoTagrEvent(GeofenceEvent.ENTER, _geofenceNotificationMessage)
                            )
                        } else {
                            _geoFenceEventFlow.emit(
                                GeoTagrEvent(GeofenceEvent.EXIT, _geofenceNotificationMessage)
                            )
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun createGeofenceAroundCurrentLocation(geofenceRequest: GeofenceRequest) {
        resetEvents()

        _radiusInMeters = geofenceRequest.radius
        _geofenceNotificationMessage = geofenceRequest.messageText

        locationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener {
            Log.d(TAG, "Current location acquired.")

            _originalLocation = it

            createLocationUpdateRequest()
        }
    }

    fun cancelGeofence() {
        locationClient.removeLocationUpdates(locationUpdateCallback).addOnSuccessListener {
            externalScope.launch {
                _geoFenceRequestStatusFlow.emit(GeofenceRequestStatus.CANCELLED)
            }
        }
    }

    private fun resetEvents() {
        externalScope.launch {
            _geoFenceRequestStatusFlow.emit(GeofenceRequestStatus.INITIAL)
            _geoFenceEventFlow.emit(GeoTagrEvent(GeofenceEvent.INITIAL, ""))
        }
    }

    @SuppressLint("MissingPermission")
    private fun createLocationUpdateRequest() {
        val locationRequest = LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL).build()

        locationClient.requestLocationUpdates(
            locationRequest, locationUpdateCallback, Looper.getMainLooper())
            .addOnSuccessListener {
                Log.d(TAG, "Location update request created.")
                externalScope.launch {
                    _geoFenceRequestStatusFlow.emit(GeofenceRequestStatus.SUCCESS)
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Location update request failed to create.")

                externalScope.launch {
                    _geoFenceRequestStatusFlow.emit(GeofenceRequestStatus.FAIL)
                }
            }
    }

    private fun isWithinGeoFence(originalLocation: Location, currentLocation: Location, radiusInMeters: Float): Boolean {
        val longitude = currentLocation.longitude - originalLocation.longitude
        val latitude = currentLocation.latitude - originalLocation.latitude

        val a = sin(latitude / 2).pow(2.0) + (cos(originalLocation.latitude)
                * cos(currentLocation.latitude) * sin(longitude / 2).pow(2.0))
        val circuit = (2 * asin(sqrt(a))).toFloat()

        return (circuit * EARTH_RADIUS_METERS) < radiusInMeters
    }
}