package org.delcom.pam_p5_ifs23001.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Task
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.delcom.pam_p5_ifs23001.helper.ConstHelper
import org.delcom.pam_p5_ifs23001.helper.RouteHelper

sealed class MenuBottomNav(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val iconActive: ImageVector,
) {
    object Home : MenuBottomNav(ConstHelper.RouteNames.Home.path, "HOME", Icons.Outlined.Home, Icons.Filled.Home)
    object Todos : MenuBottomNav(ConstHelper.RouteNames.Todos.path, "MISSIONS", Icons.Outlined.Task, Icons.Filled.Task)
    object Profile : MenuBottomNav(ConstHelper.RouteNames.Profile.path, "USER", Icons.Outlined.Person, Icons.Filled.Person)
}

@Composable
fun BottomNavComponent(navController: NavHostController) {
    val items = listOf(
        MenuBottomNav.Home,
        MenuBottomNav.Todos,
        MenuBottomNav.Profile,
    )

    val currentRoute = navController.currentDestination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // Menambahkan padding untuk navigation bar sistem
            .padding(horizontal = 16.dp, vertical = 12.dp) // Mengatur padding luar
            .background(Color.Transparent)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F2027).copy(alpha = 0.8f),
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { screen ->
                    val selected = currentRoute?.contains(screen.route) == true
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { RouteHelper.to(navController, screen.route, true) }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (selected) screen.iconActive else screen.icon,
                            contentDescription = screen.title,
                            tint = if (selected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                        if (selected) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = screen.title,
                                color = Color(0xFF00E5FF),
                                fontSize = 10.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
