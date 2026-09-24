package com.erns.alertauni.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.erns.alertauni.common.AppEvent
import com.erns.alertauni.common.AppEventManager
import com.erns.alertauni.screen.home.HomeScreen
import com.erns.alertauni.screen.login.LoginScreen
import com.erns.alertauni.screen.login.ProfileScreen

import com.erns.alertauni.screen.post.PostCommentScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        AppEventManager.globalEvents.collect { event ->
            when (event) {
                is AppEvent.NavigateToCompleteProfile -> {
                    // Redirige al usuario a la pantalla de registro/perfil desde cualquier lugar
                    navController.navigate(RouteScreen.Home.route) {
                        // Limpia el stack para que no pueda volver atrás con el botón físico
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
                is AppEvent.ShowToast -> { /* Mostrar alerta */ }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = RouteScreen.Login.route
    ) {
        composable(RouteScreen.Login.route) {
//            LoginScreen(
//                onCompleteProfile = {
//                    navController.navigate(RouteScreen.Profile.route) {
//                        popUpTo(RouteScreen.Login.route) {
//                            inclusive = false
//                        }
//                    }
//                },
//                onLoginSuccess = {
//                    navController.navigate(RouteScreen.Posts.route) {
//                        popUpTo(RouteScreen.Login.route) {
//                            inclusive = true
//                        }
//                    }
//                }
//
//            )
        }
        composable(RouteScreen.Home.route) {
            HomeScreen(modifier, navController)
        }
        composable(RouteScreen.Posts.route) {
//            PostScreen(onClickComment = { postId ->
//                navController.navigate(RouteScreen.Comment.route + "/$postId")
//            })
        }
        composable(
            route = RouteScreen.Comment.route + "/{postId}",
            arguments = listOf(
                navArgument("postId") {
                    type = NavType.StringType
                }
            )
        ) {
            PostCommentScreen()
        }

        composable(RouteScreen.Profile.route) {
            ProfileScreen(
                onCompleteProfile = {
                    navController.navigate(RouteScreen.Posts.route)
                }
            )
        }
    }
}
