package com.erns.alertauni

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.erns.alertauni.data.model.PostRequest
import com.erns.alertauni.navigation.AppNavigation
import com.erns.alertauni.navigation.MainAppNavigation
import com.erns.alertauni.ui.theme.AlertaUNITheme
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            AlertaUNITheme {
                MainAppNavigation()
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .padding(top = 24.dp)
                ) { padding ->
                        MainAppNavigation()
                }
            }
        }

//        lifecycleScope.launch {
//            test()
//        }

    }

    private suspend fun test() {
        // 1. Inicializar el cliente
        var supabaseKey = "supabase_key"
        var supabaseUrl = "https://myurl.supabase.co"
        val supabase = createSupabaseClient(supabaseUrl, supabaseKey) {
            httpEngine = io.ktor.client.engine.okhttp.OkHttp.create()
            install(Postgrest)
            install(Realtime)
            install(Auth)
        }

        val channel = supabase.realtime.channel("chat-channel")

        val updates = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "course"
        }
        channel.subscribe()
        updates.collect { update ->
            val mess = update.decodeRecord<PostRequest>()
            Log.d("Supabase", "Registro actualizado: $mess")

        }
    }

}