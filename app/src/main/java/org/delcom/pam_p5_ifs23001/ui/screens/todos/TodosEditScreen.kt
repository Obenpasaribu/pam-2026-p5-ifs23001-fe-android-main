package org.delcom.pam_p5_ifs23001.ui.screens.todos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.delcom.pam_p5_ifs23001.helper.AlertHelper
import org.delcom.pam_p5_ifs23001.helper.AlertState
import org.delcom.pam_p5_ifs23001.helper.AlertType
import org.delcom.pam_p5_ifs23001.helper.ConstHelper
import org.delcom.pam_p5_ifs23001.helper.RouteHelper
import org.delcom.pam_p5_ifs23001.helper.SuspendHelper
import org.delcom.pam_p5_ifs23001.helper.SuspendHelper.SnackBarType
import org.delcom.pam_p5_ifs23001.network.todos.data.ResponseTodoData
import org.delcom.pam_p5_ifs23001.ui.components.BottomNavComponent
import org.delcom.pam_p5_ifs23001.ui.components.LoadingUI
import org.delcom.pam_p5_ifs23001.ui.components.TopAppBarComponent
import org.delcom.pam_p5_ifs23001.ui.viewmodels.AuthUIState
import org.delcom.pam_p5_ifs23001.ui.viewmodels.AuthViewModel
import org.delcom.pam_p5_ifs23001.ui.viewmodels.TodoActionUIState
import org.delcom.pam_p5_ifs23001.ui.viewmodels.TodoUIState
import org.delcom.pam_p5_ifs23001.ui.viewmodels.TodoViewModel

@Composable
fun TodosEditScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    authViewModel: AuthViewModel,
    todoViewModel: TodoViewModel,
    todoId: String
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateTodo by todoViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var todo by remember { mutableStateOf<ResponseTodoData?>(null) }
    var authToken by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        if(uiStateAuth.auth !is AuthUIState.Success){
            RouteHelper.to(navController, ConstHelper.RouteNames.Home.path, true)
            return@LaunchedEffect
        }
        authToken = (uiStateAuth.auth as AuthUIState.Success).data.authToken
        uiStateTodo.todo = TodoUIState.Loading
        uiStateTodo.todoChange = TodoActionUIState.Loading
        todoViewModel.getTodoById(authToken!!, todoId)
    }

    LaunchedEffect(uiStateTodo.todo) {
        if (uiStateTodo.todo !is TodoUIState.Loading) {
            if (uiStateTodo.todo is TodoUIState.Success) {
                todo = (uiStateTodo.todo as TodoUIState.Success).data
                isLoading = false
            } else {
                RouteHelper.back(navController)
                isLoading = false
            }
        }
    }

    fun onSave(title: String, description: String, isDone: Boolean, urgency: String) {
        isLoading = true
        todoViewModel.putTodo(authToken!!, todoId, title, description, isDone, urgency)
    }

    LaunchedEffect(uiStateTodo.todoChange) {
        when (val state = uiStateTodo.todoChange) {
            is TodoActionUIState.Success -> {
                SuspendHelper.showSnackBar(snackbarHost, SnackBarType.SUCCESS, state.message)
                RouteHelper.to(navController, ConstHelper.RouteNames.TodosDetail.path.replace("{todoId}", todoId), true)
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

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
    )

    Scaffold(
        bottomBar = { BottomNavComponent(navController = navController) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(gradientBrush).padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBarComponent(
                    navController = navController,
                    title = "MODIFY MISSION",
                    showBackButton = true,
                )
                Box(modifier = Modifier.weight(1f)) {
                    TodosEditUI(todo = todo!!, onSave = ::onSave)
                }
            }
        }
    }
}

@Composable
fun TodosEditUI(todo: ResponseTodoData, onSave: (String, String, Boolean, String) -> Unit) {
    val alertState = remember { mutableStateOf(AlertState()) }
    var dataTitle by remember { mutableStateOf(todo.title) }
    var dataDescription by remember { mutableStateOf(todo.description) }
    var dataIsDone by remember { mutableStateOf(todo.isDone) }
    var dataUrgency by remember { mutableStateOf(todo.urgency ?: "Low") }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "MISSION PARAMETERS",
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF00E5FF),
            letterSpacing = 2.sp
        )

        FuturisticEditField(value = dataTitle, onValueChange = { dataTitle = it }, label = "MISSION TITLE")

        Column {
            Text(text = "PRIORITY LEVEL", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f), letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Low", "Medium", "High").forEach { level ->
                    val color = when(level) {
                        "High" -> Color(0xFFFF5252)
                        "Medium" -> Color(0xFFFFD600)
                        else -> Color(0xFF00E676)
                    }
                    Surface(
                        onClick = { dataUrgency = level },
                        color = if (dataUrgency == level) color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (dataUrgency == level) color else Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = level.uppercase(), style = MaterialTheme.typography.labelMedium, color = if (dataUrgency == level) color else Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Column {
            Text(text = "MISSION STATUS", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f), letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(true, false).forEach { done ->
                    val color = if (done) Color(0xFF00E676) else Color(0xFFFFD600)
                    val label = if (done) "RESOLVED" else "PENDING"
                    Surface(
                        onClick = { dataIsDone = done },
                        color = if (dataIsDone == done) color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (dataIsDone == done) color else Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = label, style = MaterialTheme.typography.labelMedium, color = if (dataIsDone == done) color else Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        FuturisticEditField(value = dataDescription, onValueChange = { dataDescription = it }, label = "MISSION BRIEFING", minLines = 5)

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if(dataTitle.isEmpty()) { AlertHelper.show(alertState, AlertType.ERROR, "TITLE REQUIRED"); return@Button }
                if(dataDescription.isEmpty()) { AlertHelper.show(alertState, AlertType.ERROR, "DESCRIPTION REQUIRED"); return@Button }
                onSave(dataTitle, dataDescription, dataIsDone, dataUrgency)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp).border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Save, null, tint = Color(0xFF00E5FF))
            Spacer(modifier = Modifier.width(8.dp))
            Text("UPDATE MISSION", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        }
    }

    if (alertState.value.isVisible) {
        AlertDialog(
            onDismissRequest = { AlertHelper.dismiss(alertState) },
            containerColor = Color(0xFF203A43),
            title = { Text(alertState.value.type.title, color = Color.White) },
            text = { Text(alertState.value.message, color = Color.White.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(onClick = { AlertHelper.dismiss(alertState) }) {
                    Text("UNDERSTOOD", color = Color(0xFF00E5FF))
                }
            }
        )
    }
}

@Composable
fun FuturisticEditField(value: String, onValueChange: (String) -> Unit, label: String, minLines: Int = 1) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f), letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                cursorColor = Color(0xFF00E5FF),
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
            ),
            shape = RoundedCornerShape(12.dp),
            minLines = minLines
        )
    }
}
