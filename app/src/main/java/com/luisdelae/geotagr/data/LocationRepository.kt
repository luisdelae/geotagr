package com.luisdelae.geotagr.data

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.luisdelae.geotagr.LocationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val locationManager: LocationManager
) {
    private var _geoFenceRequestCreated = locationManager.geoFenceRequestCreated

    val geoFenceRequestCreatedLiveData get() = _geoFenceRequestCreated

    val receivedGeofenceEvent = MutableLiveData<Boolean>()

    fun createGeoFenceOnCurrentLocation(key: String, radius: Float) {
        locationManager.createGeofenceAroundCurrentLocation(key, radius)
    }
}