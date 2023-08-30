package com.luisdelae.geotagr.data.repository

import com.luisdelae.geotagr.data.LocationManager
import com.luisdelae.geotagr.data.model.GeoTagrEvent
import com.luisdelae.geotagr.data.model.GeofenceRequest
import com.luisdelae.geotagr.data.model.GeofenceRequestStatus
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val locationManager: LocationManager
) {
    val geoFenceRequestStatusFlow: StateFlow<GeofenceRequestStatus> = locationManager.geoFenceRequestStatusFlow

    val geofenceEventFlow: StateFlow<GeoTagrEvent> = locationManager.geoFenceEventFlow

    fun createGeoFenceOnCurrentLocation(geofenceRequest: GeofenceRequest) {
        locationManager.createGeofenceAroundCurrentLocation(geofenceRequest)
    }

    fun cancelGeofence() {
        locationManager.cancelGeofence()
    }
}