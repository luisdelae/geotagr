package com.luisdelae.geotagr.data

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val locationManager: LocationManager
) {
    private var _geoFenceRequestCreated: MutableStateFlow<Boolean?> = locationManager.geoFenceRequestCreated
    val geoFenceRequestCreatedLiveData get(): MutableStateFlow<Boolean?> = _geoFenceRequestCreated


    val receivedGeofenceEvent = MutableLiveData<Boolean>()


    private var _isInGeofenceFlow: MutableStateFlow<Boolean?> = locationManager.isInGeofenceFlow
    val isInGeofenceFlow get(): MutableStateFlow<Boolean?> = _isInGeofenceFlow

    fun createGeoFenceOnCurrentLocation(radius: Float, geofenceNotificationMessage: String) {
        locationManager.createGeofenceAroundCurrentLocation(radius, geofenceNotificationMessage)
    }
}