package org.delcom.pam_p5_ifs23001.ui.screens.todos

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.delcom.pam_p5_ifs23001.R
import org.delcom.pam_p5_ifs23001.helper.ConstHelper
import org.delcom.pam_p5_ifs23001.helper.RouteHelper
import org.delcom.pam_p5_ifs23001.helper.SuspendHelper
import org.delcom.pam_p5_ifs23001.helper.SuspendHelper.SnackBarType
import org.delcom.pam_p5_ifs23001.helper.ToolsHelper
import org.delcom.pam_p5_ifs23001.helper.ToolsHelper.uriToMultipart
import org.delcom.pam_p5_ifs23001.network.todos.data.ResponseTodoData
import org.delcom.pam_p5_ifs23001.ui.components.BottomDialog
import org.delcom.pam_p5_ifs23001.ui.components.BottomDialogType
import org.delcom.pam_p5_ifs23001.ui.components.BottomNavComponent
import org.delcom.pam_p5_ifs23001.ui.components.LoadingUI
import org.delcom.pam_p5_ifs23001.ui.components.TopAppBarComponent
import org.delcom.pam_p5_ifs23001.ui.components.TopAppBarMenuItem
import org.delcom.pam_p5_ifs23001.ui.viewmodels.AuthUIState
import org.delcom.pam_p5_ifs23001.ui.viewmodels.AuthViewModel
import org.delcom.pam_p5_ifs23001.ui.viewmodels.TodoActionUIState
import org.delcom.pam_p5_ifs23001.ui.viewmodels.TodoUIState
import org.delcom.pam_p5_ifs23001.ui.viewmodels.TodoViewModel
import java.time.Instant

@Composable
fun TodosDetailScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    authViewModel: AuthViewModel,
    todoViewModel: TodoViewModel,
    todoId: String
) {
    val uiStateTodo by todoViewModel.uiState.collectAsState()
    val uiStateAuth by authViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var isConfirmDelete by remember { mutableStateOf(false) }

    var todo by remember { mutableStateOf<ResponseTodoData?>(null) }
    var authToken by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        if (uiStateAuth.auth !is AuthUIState.Success) {
            RouteHelper.to(navController, ConstHelper.RouteNames.Home.path, true)
            return@LaunchedEffect
        }
        authToken = (uiStateAuth.auth as AuthUIState.Success).data.authToken
        uiStateTodo.todoDelete = TodoActionUIState.Loading
        uiStateTodo.todoChangeCover = TodoActionUIState.Loading
        uiStateTodo.todo = TodoUIState.Loading
        todoViewModel.getTodoById(authToken!!, todoId)
    }

    LaunchedEffect(uiStateTodo.todo) {
        if (uiStateTodo.todo !is TodoUIState.Loading) {
            if (uiStateTodo.todo is TodoUIState.Success) {
                todo = (uiStateTodo.todo as TodoUIState.Success).data
                isLoading = false
            } else {
                RouteHelper.back(navController)
            }
        }
    }

    fun onDelete() {
        if (authToken == null) return
        uiStateTodo.todoDelete = TodoActionUIState.Loading
        isLoading = true
        todoViewModel.deleteTodo(authToken!!, todoId)
    }

    LaunchedEffect(uiStateTodo.todoDelete) {
        when (val state = uiStateTodo.todoDelete) {
            is TodoActionUIState.Success -> {
                SuspendHelper.showSnackBar(snackbarHost, SnackBarType.SUCCESS, state.message)
                RouteHelper.to(navController, ConstHelper.RouteNames.Todos.path, true)
                uiStateTodo.todo = TodoUIState.Loading
                isLoading = false
            }
            is TodoActionUIState.Error -> {
                SuspendHelper.showSnackBar(snackbarHost, SnackBarType.ERROR, state.message)
                isLoading = false
            }
            else -> {}
        }
    }

    fun onChangeCover(context: Context, file: Uri) {
        if (authToken == null) return
        uiStateTodo.todoChangeCover = TodoActionUIState.Loading
        isLoading = true
        val filePart = uriToMultipart(context, file, "file")
        todoViewModel.putTodoCover(authToken!!, todoId, filePart)
    }

    LaunchedEffect(uiStateTodo.todoChangeCover) {
        when (val state = uiStateTodo.todoChangeCover) {
            is TodoActionUIState.Success -> {
                if(todo != null){
                    todo = todo!!.copy(updatedAt = Instant.now().toString())
                }
                SuspendHelper.showSnackBar(snackbarHost, SnackBarType.SUCCESS, state.message)
                isLoading = false
            }
            is TodoActionUIState.Error -> {
                SuspendHelper.showSnackBar(snackbarHost, SnackBarType.ERROR, state.message)
                isLoading = false
            }
            else -> {}
        }
    }

    if (isLoading || todo == null) {
        LoadingUI()
        return
    }

    val detailMenuItems = listOf(
        TopAppBarMenuItem(
            text = "Edit Mission",
            icon = Icons.Filled.Edit,
            route = null,
            onClick = { RouteHelper.to(navController, ConstHelper.RouteNames.TodosEdit.path.replace("{todoId}", todo!!.id)) }
        ),
        TopAppBarMenuItem(
            text = "Abort Mission",
            icon = Icons.Filled.Delete,
            route = null,
            onClick = { isConfirmDelete = true }
        ),
    )

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
                    title = "MISSION DETAIL",
                    showBackButton = true,
                    customMenuItems = detailMenuItems
                )

                Box(modifier = Modifier.weight(1f)) {
                    TodosDetailUI(todo = todo!!, onChangeCover = ::onChangeCover)

                    BottomDialog(
                        type = BottomDialogType.ERROR,
                        show = isConfirmDelete,
                        onDismiss = { isConfirmDelete = false },
                        title = "ABORT MISSION",
                        message = "Are you sure you want to delete this mission data permanently?",
                        confirmText = "YES, DELETE",
                        onConfirm = { onDelete() },
                        cancelText = "CANCEL",
                        destructiveAction = true
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun TodosDetailUI(
    todo: ResponseTodoData,
    onChangeCover: (context: Context, file: Uri) -> Unit,
) {
    var dataFile by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> dataFile = uri }

    val urgencyColor = when (todo.urgency) {
        "High" -> Color(0xFFFF5252)
        "Medium" -> Color(0xFFFFD600)
        else -> Color(0xFF00E676)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Image Section
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, Color(0xFF00E5FF).copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = dataFile ?: ToolsHelper.getTodoImage(todo.id, todo.updatedAt),
                    contentDescription = "Cover Todo",
                    placeholder = painterResource(R.drawable.img_placeholder),
                    error = painterResource(R.drawable.img_placeholder),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (dataFile == null) "TAP TO MODIFY COVER" else "NEW COVER READY",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f),
                letterSpacing = 1.sp
            )

            if (dataFile != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onChangeCover(context, dataFile!!) },
                    modifier = Modifier.fillMaxWidth(0.6f).height(40.dp).border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(10.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("UPLOAD COVER", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Title and Status
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = todo.title.uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(
                    text = if (todo.isDone) "RESOLVED" else "PENDING",
                    color = if (todo.isDone) Color(0xFF00E676) else Color(0xFFFFD600)
                )
                StatusBadge(text = todo.urgency?.uppercase() ?: "LOW", color = urgencyColor)
            }
        }

        // Description
        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "MISSION BRIEFING",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = todo.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 24.sp
                )
            }
        }

        // Metadata
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoRow(label = "MISSION ID", value = "#${todo.id.take(8).uppercase()}")
            InfoRow(label = "LAST UPDATED", value = todo.updatedAt.split("T")[0])
            InfoRow(label = "CREATED AT", value = todo.createdAt.split("T")[0])
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.4f),
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}
