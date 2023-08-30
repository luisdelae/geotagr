package com.luisdelae.geotagr.ui

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.luisdelae.geotagr.R
import com.luisdelae.geotagr.data.model.GeofenceEvent
import com.luisdelae.geotagr.data.model.GeofenceRequest
import com.luisdelae.geotagr.data.model.GeofenceRequestStatus
import com.luisdelae.geotagr.databinding.FragmentGeotagrBinding
import com.luisdelae.geotagr.ui.viewmodel.GeoTagrViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class GeoTagrFragment : Fragment() {

    private var _binding: FragmentGeotagrBinding? = null

    private val binding get() = _binding!!

    private val viewModel: GeoTagrViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeotagrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationPermissionRequest.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS)
        )

        binding.buttonFirst.setOnClickListener {
            it.isClickable = false

            val message = binding.editNotificationText.text.toString()
                .ifEmpty { getString(R.string.we_re_here) }
            val radius = binding.editRadius.text.toString()
                .toFloatOrNull() ?: 10F

            viewModel.tagLocation(GeofenceRequest(radius, message))
        }

        lifecycleScope.launch {
            viewModel.geoFenceRequestStatusFlow.collect { requestCreated ->
                when (requestCreated) {
                    GeofenceRequestStatus.INITIAL -> { }
                    GeofenceRequestStatus.CANCELLED -> {
                        Toast.makeText(requireContext(),
                            getString(R.string.geofence_request_cancelled), Toast.LENGTH_SHORT).show()
                    }
                    GeofenceRequestStatus.SUCCESS -> {
                        Toast.makeText(requireContext(),
                            getString(R.string.geofence_request_created), Toast.LENGTH_SHORT).show()
                        binding.buttonFirst.isClickable = true
                    }
                    GeofenceRequestStatus.FAIL -> {
                        Toast.makeText(requireContext(),
                            getString(R.string.geofence_request_failed), Toast.LENGTH_SHORT).show()
                        binding.buttonFirst.isClickable = true
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.geofenceEventFlow.collect { event ->
                when (event.geofenceEvent) {
                    GeofenceEvent.INITIAL -> { }
                    GeofenceEvent.ENTER -> Toast.makeText(
                        requireContext(),
                        getString(R.string.entered_area, event.messageText),
                        Toast.LENGTH_SHORT).show()
                    GeofenceEvent.EXIT -> { }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // TODO: This works happy path. Test sad path.
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
                Toast.makeText(requireContext(),
                    getString(R.string.location_permissions_required), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val singleLocationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("GeoTagrFragment", "All permissions granted")
            } else {
                Toast.makeText(requireContext(),
                    getString(R.string.background_location_permissions_required), Toast.LENGTH_SHORT).show()
            }
        }
}