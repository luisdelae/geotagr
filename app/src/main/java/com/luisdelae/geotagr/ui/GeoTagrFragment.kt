package com.luisdelae.geotagr.ui

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
            val message = binding.editNotificationText.text.toString()
            val radius = binding.editRadius.text.toString().toFloat()

            viewModel.tagLocation(radius, message)
        }

        lifecycleScope.launch {
            viewModel.geofenceRequestCreated.collect { requestCreated ->
                requestCreated?.let {
                    val createdText = if (it) {
                        "successful"
                    } else {
                        "unsuccessful"
                    }

                    Toast.makeText(requireContext(), "GeoFence request $createdText", Toast.LENGTH_SHORT).show()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isInGeofenceFlow.collect { isWithinGeofence ->
                isWithinGeofence?.let {
                    if (it) {
                        Toast.makeText(requireContext(), "Entered geofence", Toast.LENGTH_SHORT).show()
                    }
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
                Toast.makeText(requireContext(), "Location permissions required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val singleLocationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(requireContext(), "All permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Background location permissions required", Toast.LENGTH_SHORT).show()
            }
        }
}