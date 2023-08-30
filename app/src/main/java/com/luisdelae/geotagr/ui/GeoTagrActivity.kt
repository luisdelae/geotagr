package com.luisdelae.geotagr.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.luisdelae.geotagr.R
import com.luisdelae.geotagr.databinding.ActivityGeoTagrBinding
import com.luisdelae.geotagr.services.GeoTagrLocationForegroundService
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class GeoTagrActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityGeoTagrBinding

    private var foregroundOnlyLocationServiceBound = false

    // Provides location updates for while-in-use feature.
    private var foregroundOnlyLocationService: GeoTagrLocationForegroundService? = null

//    private val foregroundOnlyServiceConnection = object : ServiceConnection {
//
//        override fun onServiceConnected(name: ComponentName, service: IBinder) {
//            val binder = service as GeoTagrLocationService.LocalBinder
//            foregroundOnlyLocationService = binder.service
//            foregroundOnlyLocationServiceBound = true
//        }
//
//        override fun onServiceDisconnected(name: ComponentName) {
//            foregroundOnlyLocationService = null
//            foregroundOnlyLocationServiceBound = false
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGeoTagrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_geo_tagr)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

//        foregroundOnlyLocationService?.startGeoTagrForegroundService()
    }

    override fun onStart() {
        super.onStart()

        val serviceIntent = Intent(this, GeoTagrLocationForegroundService::class.java)
        startForegroundService(serviceIntent)
//        bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()

//        if (foregroundOnlyLocationServiceBound) {
//            unbindService(foregroundOnlyServiceConnection)
//            foregroundOnlyLocationServiceBound = false
//        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_geo_tagr)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}