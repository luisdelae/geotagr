package com.luisdelae.geotagr.data.repository

import com.luisdelae.geotagr.data.LocationManager
import com.luisdelae.geotagr.data.model.GeoTagrEvent
import com.luisdelae.geotagr.data.model.GeofenceEvent
import com.luisdelae.geotagr.data.model.GeofenceRequest
import com.luisdelae.geotagr.data.model.GeofenceRequestStatus
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val locationManager: LocationManager
) {
    val geoFenceRequestCreatedFlow: StateFlow<GeofenceRequestStatus> = locationManager.geoFenceRequestCreated

    val geofenceEventFlow: StateFlow<GeoTagrEvent> = locationManager.geoFenceEventFlow

    fun createGeoFenceOnCurrentLocation(geofenceRequest: GeofenceRequest) {
        locationManager.createGeofenceAroundCurrentLocation(geofenceRequest)
    }
}