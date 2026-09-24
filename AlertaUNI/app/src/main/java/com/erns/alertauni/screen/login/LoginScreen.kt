package com.erns.alertauni.screen.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.erns.alertauni.domain.manager.DataStoreHelper
import com.erns.alertauni.screen.login.LoginViewModel.AuthState
import com.erns.alertauni.screen.utils.AssetImageLoader


@Composable
fun LoginScreen(
    onIncompleteProfile: () -> Unit,
    onLoginSuccess: (String) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val authState = viewModel.authState.collectAsState(initial = false)
    val rememberMe = viewModel.rememberMe.collectAsState()

    val btnAutenticar: () -> Unit = {
        viewModel.startSessionWithGoogle(context)
    }

    LaunchedEffect(authState.value) {
        if (authState.value == AuthState.Authenticated) {
            if (DataStoreHelper(context).getUserType().isNullOrEmpty()) {
                Toast.makeText(context, "No se encontro el tipo de usuario", Toast.LENGTH_SHORT)
                    .show()
            } else {
                onLoginSuccess(DataStoreHelper(context).getUserType() ?: "")
            }
        } else if (authState.value == AuthState.IncompleteProfile) {
            onIncompleteProfile()
        }
    }

    LoginLayout(
        rememberMe = rememberMe.value,
        onRememberMeChange = { viewModel.setRememberMe(it) },
        btnAutenticar = btnAutenticar
    )

}

@Composable
fun LoginLayout(
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    btnAutenticar: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Row(
            modifier = Modifier
                .height(56.dp)
                .padding(horizontal = 26.dp)
                .border(width = 1.dp, color = Color(0xFF5E86EC), shape = RoundedCornerShape(0.dp))
                .clickable(onClick = { btnAutenticar() }),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = AssetImageLoader(LocalContext.current).loadPainter("google_signin_bg.png"),
                contentDescription = "",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(56.dp)
                    .padding(8.dp),
                alignment = Alignment.CenterStart
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF5E86EC)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Autenticación con Google",
                    fontSize = 20.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(start = 8.dp)
                )
            }

        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = onRememberMeChange
            )
            Text("Recordar cuenta", modifier = Modifier.padding(start = 2.dp))
        }

    }
}
