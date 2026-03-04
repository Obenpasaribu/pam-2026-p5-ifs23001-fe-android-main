package org.delcom.pam_p5_ifs23001.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.delcom.pam_p5_ifs23001.R
import org.delcom.pam_p5_ifs23001.helper.ConstHelper
import org.delcom.pam_p5_ifs23001.helper.RouteHelper
import org.delcom.pam_p5_ifs23001.helper.ToolsHelper
import org.delcom.pam_p5_ifs23001.network.todos.data.ResponseUserData
import org.delcom.pam_p5_ifs23001.ui.components.BottomNavComponent
import org.delcom.pam_p5_ifs23001.ui.components.LoadingUI
import org.delcom.pam_p5_ifs23001.ui.components.TopAppBarComponent
import org.delcom.pam_p5_ifs23001.ui.components.TopAppBarMenuItem
import org.delcom.pam_p5_ifs23001.ui.viewmodels.AuthLogoutUIState
import org.delcom.pam_p5_ifs23001.ui.viewmodels.AuthUIState
import org.delcom.pam_p5_ifs23001.ui.viewmodels.AuthViewModel
import org.delcom.pam_p5_ifs23001.ui.viewmodels.ProfileUIState
import org.delcom.pam_p5_ifs23001.ui.viewmodels.TodoViewModel

@Composable
fun ProfileScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    authViewModel: AuthViewModel,
    todoViewModel: TodoViewModel
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateTodo by todoViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var profile by remember { mutableStateOf<ResponseUserData?>(null) }
    var authToken by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        if(uiStateAuth.auth !is AuthUIState.Success){
            RouteHelper.to(navController, ConstHelper.RouteNames.Home.path, true)
            return@LaunchedEffect
        }
        authToken = (uiStateAuth.auth as AuthUIState.Success).data.authToken
        todoViewModel.getProfile(authToken ?: "")
    }

    LaunchedEffect(uiStateTodo.profile) {
        if(uiStateTodo.profile !is ProfileUIState.Loading){
            isLoading = false
            if(uiStateTodo.profile is ProfileUIState.Success){
                profile = (uiStateTodo.profile as ProfileUIState.Success).data
            }
        }
    }

    if(isLoading || profile == null){
        LoadingUI()
        return
    }

    val menuItems = listOf(
        TopAppBarMenuItem(text = "Edit Profile", icon = Icons.Default.Edit, route = ConstHelper.RouteNames.ProfileEdit.path),
        TopAppBarMenuItem(text = "Logout", icon = Icons.AutoMirrored.Filled.Logout, route = null, onClick = { authViewModel.logout(authToken ?: "") })
    )

    LaunchedEffect(uiStateAuth.authLogout) {
        if (uiStateAuth.authLogout is AuthLogoutUIState.Success || uiStateAuth.authLogout is AuthLogoutUIState.Error) {
             RouteHelper.to(navController, ConstHelper.RouteNames.AuthLogin.path, true)
        }
    }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
    )

    Scaffold(
        bottomBar = { BottomNavComponent(navController = navController) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(gradientBrush).padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBarComponent(
                    navController = navController,
                    title = "USER PROFILE",
                    showBackButton = false,
                    customMenuItems = menuItems
                )

                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar Section with glowing border
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(130.dp)
                            .border(2.dp, Color(0xFF00E5FF), CircleShape)
                            .padding(4.dp)
                    ) {
                        AsyncImage(
                            model = ToolsHelper.getUserImage(profile!!.id),
                            contentDescription = "Photo Profil",
                            placeholder = painterResource(R.drawable.img_placeholder),
                            error = painterResource(R.drawable.img_placeholder),
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = profile!!.name.uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "@${profile!!.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF00E5FF)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Identity Card
                    ProfileFuturisticCard(
                        title = "IDENTIFICATION",
                        icon = Icons.Default.Fingerprint
                    ) {
                        Column {
                            InfoRow("BIO", profile!!.bio ?: "NO BIO DETECTED")
                            Spacer(modifier = Modifier.height(16.dp))
                            InfoRow("SYSTEM ID", profile!!.id)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Stats / Other info could go here
                }
            }
        }
    }
}

@Composable
fun ProfileFuturisticCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF00E5FF),
                    letterSpacing = 2.sp
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.1f))
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = Color.White)
    }
}
