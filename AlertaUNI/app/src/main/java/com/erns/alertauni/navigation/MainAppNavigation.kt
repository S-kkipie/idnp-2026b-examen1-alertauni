package com.erns.alertauni.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.erns.alertauni.screen.contact.StudentContactScreen
import com.erns.alertauni.screen.contact.TeacherContactScreen
import com.erns.alertauni.screen.course.StudentCourseScreen
import com.erns.alertauni.screen.login.LoginScreen
import com.erns.alertauni.screen.login.ProfileScreen
import com.erns.alertauni.screen.post.PostCommentScreen
import com.erns.alertauni.screen.post.PostScreen


@Composable
fun MainAppNavigation(navController: NavHostController = rememberNavController()) {
    val userTypeSelected = remember { mutableStateOf("") }
    NavHost(
        navController = navController,
        startDestination = RouteScreen.Login.route
    ) {
        composable(RouteScreen.Login.route) {
            LoginScreen(
                onIncompleteProfile = {
                    navController.navigate(RouteScreen.Profile.route) {
                        popUpTo(RouteScreen.Login.route) {
                            inclusive = false
                        }
                    }
                },
                onLoginSuccess = {userType ->
                    userTypeSelected.value = userType
                }
            )
        }

        composable(RouteScreen.Profile.route) {
            ProfileScreen(
                onCompleteProfile = { userType ->
                    userTypeSelected.value = userType
                }
            )
        }

    }

    if(userTypeSelected.value.isNotEmpty()){
        MainMenuScreen(userTypeSelected.value)
    }
}

@Composable
fun MainMenuScreen(userType: String) {
    val items = mutableListOf<RouteMainMenu>()
    val fabAction = remember { mutableStateOf<() -> Unit>({}) }
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry.value?.destination?.route

    if (userType == "PROFESSOR") {
        items.addAll(
            listOf(
                RouteMainMenu.Posts,
                RouteMainMenu.TeacherContacts,
                RouteMainMenu.Courses
            )
        )
    } else if (userType == "STUDENT") {
        items.addAll(
            listOf(
                RouteMainMenu.Posts,
                RouteMainMenu.StudentContacts,
                RouteMainMenu.Courses
            )
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (currentDestination == RouteScreen.Posts.route ||
                currentDestination == RouteMainMenu.Courses.route
            ) {
                FloatingActionButton(
                    shape = CircleShape,
                    onClick = fabAction.value
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar {
                val currentDestination =
                    navController.currentBackStackEntryAsState().value?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        selected = currentDestination?.route == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = RouteMainMenu.Posts.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(RouteMainMenu.Posts.route) {
                PostScreen(
                    onClickComment = { postId ->
                        navController.navigate(RouteScreen.Comment.route + "/$postId")
                    },
                    snackbarHostState = snackbarHostState,
                    onFabActionReady = { fabAction.value = it }
                )
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

            composable(RouteMainMenu.Courses.route) {
                StudentCourseScreen(
                    snackbarHostState = snackbarHostState,
                    onFabActionReady = { fabAction.value = it }
                )
            }

            composable(RouteMainMenu.TeacherContacts.route) {
                TeacherContactScreen()
            }
            composable(RouteMainMenu.StudentContacts.route) {
                StudentContactScreen()
            }

        }
    }
}

