package com.luisdelae.geotagr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.luisdelae.geotagr.ui.theme.GeoTagrTheme

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissions(text: String, rationale: String, locationState: PermissionState) {
    LocationPermissions(
        text = text,
        rationale = rationale,
        locationState = rememberMultiplePermissionsState(
            permissions = listOf(
                locationState.permission
            )
        )
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissions(text: String, rationale: String, locationState: MultiplePermissionsState) {
    var showRationale by remember(locationState) {
        mutableStateOf(false)
    }

    if (showRationale) {
        PermissionRationaleDialog(rationaleState = RationaleState(
            title = text,
            rationale = rationale,
            onRationaleReply = { proceed ->
                if (proceed) {
                    locationState.launchMultiplePermissionRequest()
                    showRationale = false
                }
            }
        ))
//        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//            PermissionRequestButton(isGranted = false, title = "This is the title of the permission request button") {
//                if (permissionsState.shouldShowRationale) {
//                    showRationale = true
//                } else {
//                    permissionsState.launchMultiplePermissionRequest()
//                }
//            }
//        }
    }
}

// TODO: This is not needed. This is only for triggering the permission request dialog.
//  This will be replaced by the GO button in the main screen
@Composable
fun PermissionRequestButton(isGranted: Boolean, title: String, onClick: () -> Unit) {
    if (isGranted) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.CheckCircle, title, modifier = Modifier.size(48.dp))
            Spacer(Modifier.size(10.dp))
            Text(text = title, modifier = Modifier.background(Color.Transparent))
        }
    } else {
        Button(onClick = onClick) {
            Text("Request $title")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionRequestButtonPreview() {
    GeoTagrTheme {
        PermissionRequestButton(false, "Title") {}
    }
}

@Composable
fun PermissionRationaleDialog(rationaleState: RationaleState) {
    AlertDialog(onDismissRequest = { rationaleState.onRationaleReply(false) }, title = {
        Text(text = rationaleState.title)
    }, text = {
        Text(text = rationaleState.rationale)
    }, confirmButton = {
        TextButton(onClick = {
            rationaleState.onRationaleReply(true)
        }) {
            Text("Continue")
        }
    }, dismissButton = {
        TextButton(onClick = {
            rationaleState.onRationaleReply(false)
        }) {
            Text("Dismiss")
        }
    })
}

@Preview(showBackground = true)
@Composable
fun PermissionRationaleDialogPreview() {
    GeoTagrTheme {
        PermissionRationaleDialog(RationaleState("Title", "Rationale") {})
    }
}

data class RationaleState(
    val title: String,
    val rationale: String,
    val onRationaleReply: (proceed: Boolean) -> Unit,
)