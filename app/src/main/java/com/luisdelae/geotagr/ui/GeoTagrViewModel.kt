package com.luisdelae.geotagr.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luisdelae.geotagr.data.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeoTagrViewModel @Inject internal constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {
    val geofenceRequestCreated = locationRepository.geoFenceRequestCreatedLiveData

    val receivedGeofenceEvent = locationRepository.receivedGeofenceEvent

    fun tagLocation(radius: Float) {
        viewModelScope.launch {
            locationRepository.createGeoFenceOnCurrentLocation("GEO_TAG_KEY", radius)
        }
    }
}