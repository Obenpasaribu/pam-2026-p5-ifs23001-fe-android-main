package org.delcom.pam_p5_ifs23001.ui.screens.todos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.delcom.pam_p5_ifs23001.R
import org.delcom.pam_p5_ifs23001.helper.ConstHelper
import org.delcom.pam_p5_ifs23001.helper.RouteHelper
import org.delcom.pam_p5_ifs23001.helper.ToolsHelper
import org.delcom.pam_p5_ifs23001.network.todos.data.ResponseTodoData
import org.delcom.pam_p5_ifs23001.ui.components.BottomNavComponent
import org.delcom.pam_p5_ifs23001.ui.components.LoadingUI
import org.delcom.pam_p5_ifs23001.ui.components.TopAppBarComponent
import org.delcom.pam_p5_ifs23001.ui.components.TopAppBarMenuItem
import org.delcom.pam_p5_ifs23001.ui.viewmodels.AuthLogoutUIState
import org.delcom.pam_p5_ifs23001.ui.viewmodels.AuthUIState
import org.delcom.pam_p5_ifs23001.ui.viewmodels.AuthViewModel
import org.delcom.pam_p5_ifs23001.ui.viewmodels.TodoViewModel
import org.delcom.pam_p5_ifs23001.ui.viewmodels.TodosUIState

@Composable
fun TodosScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    todoViewModel: TodoViewModel
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateTodo by todoViewModel.uiState.collectAsState()

    var isInitialLoading by remember { mutableStateOf(false) }
    var isPaginationLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var selectedStatus by remember { mutableStateOf("Semua") }
    var selectedUrgency by remember { mutableStateOf("Semua Urgensi") }

    var todos by remember { mutableStateOf<List<ResponseTodoData>>(emptyList()) }
    var authToken by remember { mutableStateOf<String?>(null) }
    var endOfPaginationReached by remember { mutableStateOf(false) }

    fun fetchTodosData(reset: Boolean = false) {
        if (reset) {
            isInitialLoading = true
            endOfPaginationReached = false
        } else {
            isPaginationLoading = true
        }

        val token = (uiStateAuth.auth as? AuthUIState.Success)?.data?.authToken
        if (token != null) {
            authToken = token
            val isDoneFilter = when (selectedStatus) {
                "Selesai" -> true
                "Belum Selesai" -> false
                else -> null
            }
            val urgencyFilter = if (selectedUrgency == "Semua Urgensi") null else selectedUrgency
            todoViewModel.getAllTodos(token, searchQuery.text, isDoneFilter, urgencyFilter, reset)
        }
    }

    LaunchedEffect(Unit) {
        if (uiStateAuth.auth !is AuthUIState.Success) {
            RouteHelper.to(navController, ConstHelper.RouteNames.Home.path, true)
            return@LaunchedEffect
        }
        fetchTodosData(reset = true)
    }

    LaunchedEffect(selectedStatus, selectedUrgency) {
        if (uiStateAuth.auth is AuthUIState.Success) {
            fetchTodosData(reset = true)
        }
    }

    LaunchedEffect(uiStateTodo.todos) {
        when (val state = uiStateTodo.todos) {
            is TodosUIState.Success -> {
                isInitialLoading = false
                isPaginationLoading = false
                todos = state.data
                endOfPaginationReached = state.endOfPaginationReached
            }
            is TodosUIState.Error -> {
                isInitialLoading = false
                isPaginationLoading = false
            }
            else -> {}
        }
    }

    if (isInitialLoading) {
        LoadingUI()
        return
    }

    val menuItems = listOf(
        TopAppBarMenuItem(text = "Profile", icon = Icons.Filled.Person, route = ConstHelper.RouteNames.Profile.path),
        TopAppBarMenuItem(text = "Logout", icon = Icons.AutoMirrored.Filled.Logout, route = null, onClick = { authViewModel.logout(authToken ?: "") })
    )

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { RouteHelper.to(navController, ConstHelper.RouteNames.TodosAdd.path) },
                containerColor = Color(0xFF00E5FF),
                contentColor = Color(0xFF0F2027),
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp).border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        },
        bottomBar = { BottomNavComponent(navController = navController) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(gradientBrush).padding(paddingValues)) {
            Column {
                TopAppBarComponent(
                    navController = navController,
                    title = "MISSIONS",
                    showBackButton = false,
                    customMenuItems = menuItems,
                    withSearch = true,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onSearchAction = { fetchTodosData(reset = true) }
                )

                // Filters
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Semua", "Selesai", "Belum").forEach { filter ->
                            FuturisticChip(
                                text = filter,
                                selected = selectedStatus.startsWith(filter),
                                onClick = { selectedStatus = if(filter == "Belum") "Belum Selesai" else filter }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Low", "Medium", "High").forEach { urgency ->
                            FuturisticChip(
                                text = urgency,
                                selected = if(urgency == "All") selectedUrgency == "Semua Urgensi" else selectedUrgency == urgency,
                                onClick = { selectedUrgency = if(urgency == "All") "Semua Urgensi" else urgency }
                            )
                        }
                    }
                }

                TodosUI(
                    todos = todos,
                    onOpen = { todoId -> RouteHelper.to(navController, "todos/${todoId}") },
                    onLoadMore = {
                        if (!isPaginationLoading && !endOfPaginationReached) {
                            fetchTodosData(reset = false)
                        }
                    },
                    isPaginationLoading = isPaginationLoading
                )
            }
        }
    }
}

@Composable
fun FuturisticChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (selected) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (selected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.2f)),
        modifier = Modifier.height(32.dp)
    ) {
        Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun TodosUI(
    todos: List<ResponseTodoData>,
    onOpen: (String) -> Unit,
    onLoadMore: () -> Unit,
    isPaginationLoading: Boolean
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItemIndex >= todos.size - 1 && todos.isNotEmpty()
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        itemsIndexed(todos) { _, todo -> TodoItemUI(todo, onOpen) }
        if (isPaginationLoading) {
            item { CircularProgressIndicator(modifier = Modifier.padding(16.dp).size(32.dp), strokeWidth = 2.dp, color = Color(0xFF00E5FF)) }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    if (todos.isEmpty() && !isPaginationLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "NO MISSIONS FOUND", color = Color.White.copy(alpha = 0.4f), letterSpacing = 2.sp)
        }
    }
}

@Composable
fun TodoItemUI(todo: ResponseTodoData, onOpen: (String) -> Unit) {
    val urgency = todo.urgency ?: "Low"
    val urgencyColor = when (urgency) {
        "High" -> Color(0xFFFF5252)
        "Medium" -> Color(0xFFFFD600)
        else -> Color(0xFF00E676)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .clickable { onOpen(todo.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))) {
                AsyncImage(
                    model = ToolsHelper.getTodoImage(todo.id, todo.updatedAt),
                    contentDescription = null,
                    placeholder = painterResource(R.drawable.img_placeholder),
                    error = painterResource(R.drawable.img_placeholder),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todo.title.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = todo.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(urgencyColor.copy(alpha = 0.1f))
                            .border(1.dp, urgencyColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = urgency, style = MaterialTheme.typography.labelSmall, color = urgencyColor, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (todo.isDone) "RESOLVED" else "PENDING",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (todo.isDone) Color(0xFF00E676) else Color(0xFFFFD600),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
