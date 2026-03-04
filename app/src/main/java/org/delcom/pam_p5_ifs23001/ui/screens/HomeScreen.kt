package org.delcom.pam_p5_ifs23001.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.delcom.pam_p5_ifs23001.helper.ConstHelper
import org.delcom.pam_p5_ifs23001.helper.RouteHelper
import org.delcom.pam_p5_ifs23001.ui.components.BottomNavComponent
import org.delcom.pam_p5_ifs23001.ui.components.LoadingUI
import org.delcom.pam_p5_ifs23001.ui.components.TopAppBarComponent
import org.delcom.pam_p5_ifs23001.ui.components.TopAppBarMenuItem
import org.delcom.pam_p5_ifs23001.ui.viewmodels.AuthActionUIState
import org.delcom.pam_p5_ifs23001.ui.viewmodels.AuthLogoutUIState
import org.delcom.pam_p5_ifs23001.ui.viewmodels.AuthUIState
import org.delcom.pam_p5_ifs23001.ui.viewmodels.AuthViewModel
import org.delcom.pam_p5_ifs23001.ui.viewmodels.TodoViewModel
import org.delcom.pam_p5_ifs23001.ui.viewmodels.TodosUIState

@Composable
fun HomeScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    todoViewModel: TodoViewModel
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateTodo by todoViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var isFreshToken by remember { mutableStateOf(false) }
    var authToken by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (isLoading) return@LaunchedEffect
        isLoading = true
        isFreshToken = true
        uiStateAuth.authLogout = AuthLogoutUIState.Loading
        authViewModel.loadTokenFromPreferences()
    }

    LaunchedEffect(authToken) {
        if (authToken != null) {
            todoViewModel.getAllTodos(authToken!!)
        }
    }

    fun onLogout(token: String){
        isLoading = true
        authViewModel.logout(token)
    }

    LaunchedEffect(uiStateAuth.auth) {
        if (!isLoading) return@LaunchedEffect
        if (uiStateAuth.auth !is AuthUIState.Loading) {
            if (uiStateAuth.auth is AuthUIState.Success) {
                if (isFreshToken) {
                    val dataToken = (uiStateAuth.auth as AuthUIState.Success).data
                    authViewModel.refreshToken(dataToken.authToken, dataToken.refreshToken)
                    isFreshToken = false
                } else if(uiStateAuth.authRefreshToken is AuthActionUIState.Success) {
                    val newToken = (uiStateAuth.auth as AuthUIState.Success).data.authToken
                    if (authToken != newToken) {
                        authToken = (uiStateAuth.auth as AuthUIState.Success).data.authToken
                    }
                    isLoading = false
                }
            } else {
                onLogout("")
            }
        }
    }

    LaunchedEffect(uiStateAuth.authLogout) {
        if (uiStateAuth.authLogout !is AuthLogoutUIState.Loading && uiStateAuth.authLogout !is AuthLogoutUIState.Success && uiStateAuth.authLogout !is AuthLogoutUIState.Error) return@LaunchedEffect
        if (uiStateAuth.authLogout !is AuthLogoutUIState.Loading) {
            RouteHelper.to(navController, ConstHelper.RouteNames.AuthLogin.path, true)
        }
    }

    if (isLoading || authToken == null || isFreshToken) {
        LoadingUI()
        return
    }

    val menuItems = listOf(
        TopAppBarMenuItem(text = "Profile", icon = Icons.Filled.Person, route = ConstHelper.RouteNames.Profile.path),
        TopAppBarMenuItem(text = "Logout", icon = Icons.AutoMirrored.Filled.Logout, route = null, onClick = { onLogout(authToken ?: "") })
    )

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
    )

    Scaffold(
        bottomBar = { BottomNavComponent(navController = navController) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBarComponent(
                    navController = navController,
                    title = "DASHBOARD",
                    showBackButton = false,
                    customMenuItems = menuItems
                )

                HomeUI(uiStateTodo.todos)
            }
        }
    }
}

@Composable
fun HomeUI(todosState: TodosUIState) {
    val todos = if (todosState is TodosUIState.Success) todosState.data else emptyList()
    val totalTodos = todos.size
    val doneTodos = todos.count { it.isDone }
    val pendingTodos = totalTodos - doneTodos

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Welcome Header
        FuturisticHeader("CENTRAL COMMAND")

        Spacer(modifier = Modifier.height(24.dp))

        // Hero Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MISSION STATUS",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF00E5FF),
                        letterSpacing = 4.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (totalTodos > 0) "${(doneTodos * 100 / totalTodos)}%" else "0%",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "COMPLETED",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FuturisticStatCard(
                modifier = Modifier.weight(1f),
                title = "TOTAL",
                value = totalTodos.toString(),
                icon = Icons.AutoMirrored.Filled.List,
                accentColor = Color(0xFF00E5FF)
            )
            FuturisticStatCard(
                modifier = Modifier.weight(1f),
                title = "ACTIVE",
                value = pendingTodos.toString(),
                icon = Icons.Default.Bolt,
                accentColor = Color(0xFFFFD600)
            )
            FuturisticStatCard(
                modifier = Modifier.weight(1f),
                title = "DONE",
                value = doneTodos.toString(),
                icon = Icons.Default.CheckCircle,
                accentColor = Color(0xFF00E676)
            )
        }
    }
}

@Composable
fun FuturisticHeader(text: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "header")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00E5FF).copy(alpha = alpha),
            letterSpacing = 6.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(2.dp)
                .background(Color(0xFF00E5FF))
        )
    }
}

@Composable
fun FuturisticStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color
) {
    Card(
        modifier = modifier
            .aspectRatio(0.9f)
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
        }
    }
}
