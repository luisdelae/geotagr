package com.luisdelae.geotagr

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.luisdelae.geotagr.databinding.ActivityGeoTagrBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class GeoTagrActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityGeoTagrBinding

    private var foregroundOnlyLocationServiceBound = false

    // Provides location updates for while-in-use feature.
    private var foregroundOnlyLocationService: GeoTagrLocationService? = null

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

        val serviceIntent = Intent(this, GeoTagrLocationService::class.java)
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