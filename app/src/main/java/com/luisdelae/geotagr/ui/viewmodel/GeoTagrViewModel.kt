package com.luisdelae.geotagr.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luisdelae.geotagr.data.model.GeofenceRequest
import com.luisdelae.geotagr.data.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeoTagrViewModel @Inject internal constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {
    val geoFenceRequestCreatedFlow = locationRepository.geoFenceRequestCreatedFlow

    val geofenceEventFlow = locationRepository.geofenceEventFlow

    fun tagLocation(geofenceRequest: GeofenceRequest) {
        viewModelScope.launch {
            locationRepository.createGeoFenceOnCurrentLocation(geofenceRequest)
        }
    }
}