package org.delcom.pam_p5_ifs23001.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.delcom.pam_p5_ifs23001.R
import org.delcom.pam_p5_ifs23001.helper.ConstHelper
import org.delcom.pam_p5_ifs23001.helper.RouteHelper
import org.delcom.pam_p5_ifs23001.helper.SuspendHelper
import org.delcom.pam_p5_ifs23001.helper.ToolsHelper
import org.delcom.pam_p5_ifs23001.network.todos.data.ResponseUserData
import org.delcom.pam_p5_ifs23001.ui.components.BottomNavComponent
import org.delcom.pam_p5_ifs23001.ui.components.LoadingUI
import org.delcom.pam_p5_ifs23001.ui.components.TopAppBarComponent
import org.delcom.pam_p5_ifs23001.ui.viewmodels.AuthUIState
import org.delcom.pam_p5_ifs23001.ui.viewmodels.AuthViewModel
import org.delcom.pam_p5_ifs23001.ui.viewmodels.ProfileUIState
import org.delcom.pam_p5_ifs23001.ui.viewmodels.TodoActionUIState
import org.delcom.pam_p5_ifs23001.ui.viewmodels.TodoViewModel
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProfileEditScreen(
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
    
    val context = LocalContext.current

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

    LaunchedEffect(uiStateTodo.profileUpdate, uiStateTodo.passwordUpdate, uiStateTodo.photoUpdate) {
        val states = listOf(uiStateTodo.profileUpdate, uiStateTodo.passwordUpdate, uiStateTodo.photoUpdate)
        states.forEach { state ->
            if (state is TodoActionUIState.Success) {
                SuspendHelper.showSnackBar(snackbarHost, SuspendHelper.SnackBarType.SUCCESS, state.message)
                todoViewModel.getProfile(authToken ?: "")
            } else if (state is TodoActionUIState.Error) {
                SuspendHelper.showSnackBar(snackbarHost, SuspendHelper.SnackBarType.ERROR, state.message)
            }
        }
    }

    if(isLoading || profile == null){
        LoadingUI()
        return
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
                    title = "EDIT PROFILE",
                    showBackButton = true
                )

                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ProfileEditUI(
                        profile = profile!!,
                        onUpdateProfile = { name, username, bio -> todoViewModel.updateProfile(authToken!!, name, username, bio) },
                        onUpdatePassword = { current, new -> todoViewModel.updatePassword(authToken!!, current, new) },
                        onUpdatePhoto = { uri ->
                            val file = uriToFile(uri, context)
                            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                            todoViewModel.updatePhoto(authToken!!, body)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileEditUI(
    profile: ResponseUserData,
    onUpdateProfile: (String, String, String?) -> Unit,
    onUpdatePassword: (String, String) -> Unit,
    onUpdatePhoto: (Uri) -> Unit
){
    var name by remember { mutableStateOf(profile.name) }
    var username by remember { mutableStateOf(profile.username) }
    var bio by remember { mutableStateOf(profile.bio ?: "") }
    
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onUpdatePhoto(it) }
    }

    // Avatar Section
    Box(contentAlignment = Alignment.BottomEnd) {
        AsyncImage(
            model = ToolsHelper.getUserImage(profile.id),
            contentDescription = "Photo Profil",
            placeholder = painterResource(R.drawable.img_placeholder),
            error = painterResource(R.drawable.img_placeholder),
            modifier = Modifier.size(120.dp).clip(CircleShape).border(2.dp, Color(0xFF00E5FF), CircleShape),
            contentScale = ContentScale.Crop
        )
        IconButton(
            onClick = { photoLauncher.launch("image/*") },
            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF00E5FF))
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = "Change Photo", tint = Color(0xFF0F2027), modifier = Modifier.size(20.dp))
        }
    }

    // Info Account Card
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("ACCOUNT INFO", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, letterSpacing = 2.sp, style = MaterialTheme.typography.labelLarge)
            }

            FuturisticEditField(value = name, onValueChange = { name = it }, label = "FULL NAME")
            FuturisticEditField(value = username, onValueChange = { username = it }, label = "USERNAME")
            FuturisticEditField(value = bio, onValueChange = { bio = it }, label = "BIO", minLines = 3)

            Button(
                onClick = { onUpdateProfile(name, username, bio) },
                modifier = Modifier.fillMaxWidth().height(48.dp).border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF00E5FF))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SAVE PROFILE", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
            }
        }
    }

    // Password Card
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("SECURITY", color = Color(0xFFFFD600), fontWeight = FontWeight.Black, letterSpacing = 2.sp, style = MaterialTheme.typography.labelLarge)
            }

            FuturisticEditField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = "CURRENT PASSWORD",
                isPassword = true,
                passwordVisible = currentPasswordVisible,
                onPasswordToggle = { currentPasswordVisible = !currentPasswordVisible }
            )

            FuturisticEditField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "NEW PASSWORD",
                isPassword = true,
                passwordVisible = newPasswordVisible,
                onPasswordToggle = { newPasswordVisible = !newPasswordVisible }
            )

            Button(
                onClick = {
                    onUpdatePassword(currentPassword, newPassword)
                    currentPassword = ""
                    newPassword = ""
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).border(1.dp, Color(0xFFFFD600).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600).copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("UPDATE PASSWORD", color = Color(0xFFFFD600), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FuturisticEditField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    minLines: Int = 1,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: (() -> Unit)? = null
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text),
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { onPasswordToggle?.invoke() }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                cursorColor = Color(0xFF00E5FF),
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
            ),
            shape = RoundedCornerShape(12.dp),
            minLines = minLines,
            singleLine = minLines == 1
        )
    }
}

private fun uriToFile(uri: Uri, context: android.content.Context): File {
    val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
    }
    return file
}
