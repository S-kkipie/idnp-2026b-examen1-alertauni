package com.erns.alertauni.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.SessionStatus

// En tu AuthViewModel o en tu manejador de navegación
sealed class RootState {
    object Checking : RootState()
    object LoginRequired : RootState()
    object ProfileIncomplete : RootState()
    object AccessGranted : RootState()
}

class MainViewModel(private val supabaseClient: SupabaseClient) : ViewModel() {



}