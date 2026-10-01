package com.erns.alertauni.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class RouteMainMenu(val route: String, val label: String, val icon: ImageVector) {
    object Posts : RouteMainMenu("posts", "Posts", Icons.Default.Email)
    object Courses : RouteMainMenu("courses", "Cursos", Icons.Default.AccountBox)
    object StudentContacts : RouteMainMenu("StudentContacts", "Contactos", Icons.Default.Person)
    object TeacherContacts : RouteMainMenu("TeacherContacts", "Contactos", Icons.Default.Person)
    object Notifications : RouteMainMenu("notifications", "Avisos", Icons.Default.Notifications)

}
