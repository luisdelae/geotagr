package com.luisdelae.geotagr.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luisdelae.geotagr.data.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeoTagrViewModel @Inject internal constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {
    val geofenceRequestCreated: MutableStateFlow<Boolean?> = locationRepository.geoFenceRequestCreatedLiveData

    val isInGeofenceFlow: MutableStateFlow<Boolean?>  = locationRepository.isInGeofenceFlow

    fun tagLocation(radius: Float, geofenceNotificationMessage: String) {
        viewModelScope.launch {
            locationRepository.createGeoFenceOnCurrentLocation(radius, geofenceNotificationMessage)
        }
    }
}