package com.luisdelae.geotagr

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
class GeoTagrApplication : Application() {
    val applicationScope = CoroutineScope(Dispatchers.IO)
}