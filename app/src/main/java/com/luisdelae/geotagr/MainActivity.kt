@file:OptIn(ExperimentalPermissionsApi::class)

package com.luisdelae.geotagr

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.luisdelae.geotagr.ui.theme.GeoTagrTheme
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalPermissionsApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoTagrTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val locationPermissionsState = rememberMultiplePermissionsState(
                        permissions = listOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) { permissions ->
                        // TODO: Handle only coarse granted
                        // TODO: Handle denials using showShowRationale
                        val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION]
                        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION]

                        if (coarseLocation == true && fineLocation == true) {
                            Toast.makeText(this, "Able to do stuff", Toast.LENGTH_SHORT).show()
                        }
                    }

                    GeoTagrScreen(locationPermissionsState)
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Preview(showBackground = true)
@Composable
fun GeoTagrPreview() {
    GeoTagrTheme {
        GeoTagrScreen(multiplePermissionsState = MultiplePermissionsStatePreview())
    }
}

/**
 * This is strictly to get the composable preview to work while using appcompanist permissions
 * https://github.com/google/accompanist/issues/1498
 */
@ExperimentalPermissionsApi
private class MultiplePermissionsStatePreview : MultiplePermissionsState {

    override val allPermissionsGranted: Boolean
        get() = false

    override val permissions: List<PermissionState>
        get() = emptyList()

    override val revokedPermissions: List<PermissionState>
        get() = emptyList()

    override val shouldShowRationale: Boolean
        get() = true

    override fun launchMultiplePermissionRequest() {
        // do nothing
    }
}