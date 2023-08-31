package com.luisdelae.geotagr.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.luisdelae.geotagr.R
import com.luisdelae.geotagr.data.model.GeofenceEvent
import com.luisdelae.geotagr.data.model.GeofenceRequest
import com.luisdelae.geotagr.data.model.GeofenceRequestStatus
import com.luisdelae.geotagr.databinding.FragmentGeotagrBinding
import com.luisdelae.geotagr.service.GeoTagrLocationForegroundService
import com.luisdelae.geotagr.ui.viewmodel.GeoTagrViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class GeoTagrFragment : Fragment() {

    private var _binding: FragmentGeotagrBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GeoTagrViewModel by viewModels()

    private var foregroundOnlyLocationServiceBound = false

    private var foregroundOnlyLocationService: GeoTagrLocationForegroundService? = null

    var uiInForeground = true

    private val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as GeoTagrLocationForegroundService.LocalBinder
            foregroundOnlyLocationService = binder.service
            foregroundOnlyLocationServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyLocationService = null
            foregroundOnlyLocationServiceBound = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeotagrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val permissionsList = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsList.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        locationPermissionRequest.launch(permissionsList.toTypedArray())

        binding.buttonTag.setOnClickListener {
            if (hasPermissions()) {
                it.isClickable = false
                foregroundOnlyLocationService?.subscribeToLocationUpdates()

                val message = binding.editNotificationText.text.toString()
                    .ifEmpty { getString(R.string.we_are_here) }
                val radius = binding.editRadius.text.toString()
                    .toFloatOrNull() ?: 10F

                viewModel.tagLocation(GeofenceRequest(radius, message))

            } else {
                foregroundOnlyLocationService?.unsubscribeToLocationUpdates()

                locationPermissionRequest.launch(permissionsList.toTypedArray())
            }
        }

        binding.buttonCancel.setOnClickListener {
            viewModel.cancelGeotag()
        }

        lifecycleScope.launch {
            viewModel.geoFenceRequestStatusFlow.collect { requestCreated ->
                when (requestCreated) {
                    GeofenceRequestStatus.INITIAL -> {
                        binding.buttonCancel.visibility = GONE
                    }
                    GeofenceRequestStatus.CANCELLED -> {
                        if (uiInForeground) Toast.makeText(requireContext(),
                            getString(R.string.geofence_request_cancelled), Toast.LENGTH_SHORT).show()
                        binding.buttonTag.isClickable = true
                        binding.buttonCancel.visibility = GONE
                    }
                    GeofenceRequestStatus.SUCCESS -> {
                        if (uiInForeground) Toast.makeText(requireContext(),
                            getString(R.string.geofence_request_created), Toast.LENGTH_SHORT).show()
                        binding.buttonTag.isClickable = true
                        binding.buttonCancel.visibility = VISIBLE
                    }
                    GeofenceRequestStatus.FAIL -> {
                        if (uiInForeground) Toast.makeText(requireContext(),
                            getString(R.string.geofence_request_failed), Toast.LENGTH_SHORT).show()
                        binding.buttonTag.isClickable = true
                        binding.buttonCancel.visibility = GONE
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.geofenceEventFlow.collect { event ->
                when (event.geofenceEvent) {
                    GeofenceEvent.INITIAL -> { }
                    GeofenceEvent.ENTER ->
                        if (uiInForeground) Toast.makeText(
                            requireContext(),
                            getString(R.string.entered_area, event.messageText),
                            Toast.LENGTH_SHORT).show()
                    GeofenceEvent.EXIT -> { }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        uiInForeground = true

        val serviceIntent = Intent(requireContext(), GeoTagrLocationForegroundService::class.java)
        requireActivity().bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()

        uiInForeground = false

        if (foregroundOnlyLocationServiceBound) {
            requireActivity().unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyLocationServiceBound = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                singleLocationPermissionRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                singleLocationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else -> {
            if (uiInForeground) Toast.makeText(requireContext(),
                    getString(R.string.location_permissions_required), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val singleLocationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                if (uiInForeground)  Toast.makeText(requireContext(),
                    getString(R.string.background_location_permissions_required), Toast.LENGTH_SHORT).show()
            }
    }

    private fun hasPermissions(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}