package com.luisdelae.geotagr

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GeoTagrScreen(
    multiplePermissionsState: MultiplePermissionsState,
    modifier: Modifier = Modifier
) {
    val inputValue = remember { mutableStateOf(TextFieldValue()) }

    val context = LocalContext.current.applicationContext

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {

        Text(
            text = stringResource(R.string.app_name),
            style = typography.titleLarge,
            modifier = modifier
        )

        Spacer(
            modifier = modifier
                .size(50.dp)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = inputValue.value ,
                onValueChange = {
                    if (it.text.length <= 2) {
                        inputValue.value = it
                    }
                },
                placeholder = { Text(text = stringResource(R.string.enter_radius_in_meters)) },
                keyboardOptions = KeyboardOptions(
                    // below line is used to specify our
                    // type of keyboard such as text, number, phone.
                    keyboardType = KeyboardType.Number,
                ),
                modifier = modifier
                    .widthIn(100.dp)
            )

            Button(onClick = { invokePermissions(multiplePermissionsState, context) }) {
                Text(text = stringResource(R.string.tag))
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
private fun invokePermissions(
    multiplePermissionsState: MultiplePermissionsState,
//    viewModel: GeoTagrViewModel,
    context: Context) {
    if (multiplePermissionsState.allPermissionsGranted) {
        // Do the thing
        Toast.makeText(context, "Permissions already granted", Toast.LENGTH_SHORT).show()

//        viewModel.tagLocation()
    } else {
        multiplePermissionsState.launchMultiplePermissionRequest()
    }
}