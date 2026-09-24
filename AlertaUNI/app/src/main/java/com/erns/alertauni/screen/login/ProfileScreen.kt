package com.erns.alertauni.screen.login

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.erns.alertauni.data.model.GoogleAccountRequest


@Composable
fun ProfileScreen(
    onCompleteProfile: (String) -> Unit,
) {
    val context = LocalContext.current
    val viewModel: LoginViewModel = hiltViewModel()
    val authState = viewModel.authState.collectAsState()
    val googleAccountRequest = remember { mutableStateOf<GoogleAccountRequest?>(null) }
    val userTypeSelected = remember { mutableStateOf("") }
    val userTypeMenu = viewModel.userTypeMenu


    val onClickCreateAccount: (String?) -> Unit = { userType ->
        if (userType != null) {
            userTypeSelected.value = userType
            viewModel.updateUserProfile(
                GoogleAccountRequest(
                    email = googleAccountRequest.value?.email ?: "",
                    firstname = googleAccountRequest.value?.firstname ?: "",
                    surname = googleAccountRequest.value?.surname ?: "",
                    user_type = userType
                )
            )
        } else {
            Toast.makeText(context, "Seleccione Docente o Estudiante", Toast.LENGTH_SHORT).show()
        }

    }

    LaunchedEffect(authState.value) {
        if (authState.value == LoginViewModel.AuthState.CompleteProfile) {
            onCompleteProfile(userTypeSelected.value)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getUserInfo()
    }

    LaunchedEffect(Unit) {
        viewModel.googleAccountRequest.collect {
            googleAccountRequest.value = it
        }
    }

    if (googleAccountRequest.value != null) {
        AccountScreenLayoutPage2(
            googleAccountRequest.value!!,
            userTypeMenu,
            onClickCreateAccount
        )
    } else {
        Text(text = "No se puede mostrar información")
    }

}

@Composable
fun AccountScreenLayoutPage2(
    googleAccountRequest: GoogleAccountRequest,
    userTypeMenu: Map<String, String>,
    onClickCreateAccount: (String?) -> Unit
) {
    val selectedIndex = remember { mutableStateOf(googleAccountRequest.user_type) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Información obtenida de Google",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = googleAccountRequest.email,
            onValueChange = {},
            label = { Text("Email") },
            enabled = false
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = googleAccountRequest.firstname,
            onValueChange = {},
            label = { Text("Nombres") },
            enabled = false
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = googleAccountRequest.surname,
            onValueChange = {},
            label = { Text("Apellidos") },
            enabled = false
        )

        Spacer(modifier = Modifier.height(18.dp))
        SegmentedSelector(selectedIndex, userTypeMenu)

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 42.dp),
            onClick = { onClickCreateAccount(selectedIndex.value) }
        ) {
            Text("Actualizar Información")
        }

    }

}

@Composable
fun SegmentedSelector(selectedIndex: MutableState<String?>, userTypeMenu: Map<String, String>) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 42.dp)
    ) {
        userTypeMenu.entries.forEachIndexed { index, entry ->
            SegmentedButton(
                selected = selectedIndex.value == entry.key,
                onClick = { selectedIndex.value = entry.key },
                shape = SegmentedButtonDefaults.itemShape(
                    index,
                    userTypeMenu.size
                )
            ) {
                Text(entry.value)
            }
        }
    }

    val selected = if (selectedIndex.value != null) userTypeMenu[selectedIndex.value!!] else "---"
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "Selección actual: ${selected}",
        fontSize = 18.sp
    )


}