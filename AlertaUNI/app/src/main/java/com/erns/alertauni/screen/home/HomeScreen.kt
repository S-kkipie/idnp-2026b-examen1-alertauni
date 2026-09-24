package com.erns.alertauni.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.erns.alertauni.navigation.RouteScreen
import com.erns.alertauni.screen.utils.AssetImageLoader
import com.erns.alertauni.ui.theme.ColorLogin

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val homeViewModel: HomeViewModel = viewModel();
    val menuItems = homeViewModel.getMenu()
    val nextPageOnClick: (String) -> Unit = { screen ->
        navController.navigate(screen)
    }

    HomeLayout(modifier, menuItems, nextPageOnClick)


}

@Composable
fun HomeLayout(
    modifier: Modifier,
    menuItems: Map<String, MenuItemEntity>,
    nextPageOnClick: (String) -> Unit
) {
    Column(modifier = modifier) {
        HomeLayoutHeader()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            menuItems.get("anuncios")?.let {
                HomeLayoutMenuItem(
                    it,
                    menuItemOnClick = { nextPageOnClick(RouteScreen.Posts.route) })
            }
            menuItems.get("asistencia")?.let {
                HomeLayoutMenuItem(
                    it,
                    menuItemOnClick = { nextPageOnClick(RouteScreen.Attendance.route) })
            }
            menuItems.get("participacion")?.let {
                HomeLayoutMenuItem(
                    it,
                    menuItemOnClick = { nextPageOnClick(RouteScreen.Participation.route) })
            }
            menuItems.get("chat")?.let {
                HomeLayoutMenuItem(
                    it,
                    menuItemOnClick = { nextPageOnClick(RouteScreen.Chat.route) })
            }
        }
    }
}

@Composable
fun HomeLayoutHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(ColorLogin),
        contentAlignment = Alignment.Center
    ) {
        Text("Alerta UNI", fontSize = 32.sp)
    }
}

@Composable
fun HomeLayoutMenuItem(menu: MenuItemEntity, menuItemOnClick: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.height(160.dp),
        shape = RoundedCornerShape(1.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        onClick = menuItemOnClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = AssetImageLoader(context).loadPainter(menu.image),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = menu.title,
                fontSize = 20.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(20.dp)
            )

        }
    }
}

