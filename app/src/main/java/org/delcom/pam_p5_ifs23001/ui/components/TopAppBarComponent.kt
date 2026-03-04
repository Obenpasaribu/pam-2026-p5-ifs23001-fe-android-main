package org.delcom.pam_p5_ifs23001.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.delcom.pam_p5_ifs23001.helper.ConstHelper
import org.delcom.pam_p5_ifs23001.helper.RouteHelper
import org.delcom.pam_p5_ifs23001.ui.theme.DelcomTheme

data class TopAppBarMenuItem(
    val text: String,
    val icon: ImageVector,
    val route: String? = null,
    val onClick: (() -> Unit)? = null,
    val isDestructive: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarComponent(
    navController: NavHostController,
    title: String = "Home",
    showBackButton: Boolean = true,
    showMenu: Boolean = true,
    customMenuItems: List<TopAppBarMenuItem>? = null,
    onBackClick: (() -> Unit)? = null,
    elevation: Int = 0,
    withSearch: Boolean = false,
    searchQuery: TextFieldValue = TextFieldValue(""),
    onSearchQueryChange: (TextFieldValue) -> Unit = {},
    onSearchAction: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    val queryFocusRequester = remember { FocusRequester() }

    val menuItems = customMenuItems ?: listOf(
        TopAppBarMenuItem(text = "Profile", icon = Icons.Filled.Person, route = ConstHelper.RouteNames.Profile.path),
    )

    Column {
        TopAppBar(
            title = {
                if (isSearching) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { onSearchQueryChange(it) },
                        placeholder = { Text("SEARCH MISSIONS...", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().focusRequester(queryFocusRequester),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearchAction() }),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = Color(0xFF00E5FF),
                            focusedIndicatorColor = Color(0xFF00E5FF),
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                    LaunchedEffect(Unit) { queryFocusRequester.requestFocus() }
                } else {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                }
            },
            navigationIcon = {
                if (showBackButton && !isSearching) {
                    IconButton(onClick = { onBackClick?.invoke() ?: RouteHelper.back(navController) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                }
            },
            actions = {
                if (isSearching) {
                    IconButton(onClick = { 
                        isSearching = false
                        onSearchQueryChange(TextFieldValue(""))
                        onSearchAction()
                    }) {
                        Icon(Icons.Filled.Close, "Close", tint = Color.White)
                    }
                } else {
                    if (withSearch) {
                        IconButton(onClick = { isSearching = true }) {
                            Icon(Icons.Filled.Search, "Search", tint = Color.White)
                        }
                    }
                    if (showMenu) {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Filled.MoreVert, "Menu", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            containerColor = Color(0xFF203A43),
                            modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        ) {
                            menuItems.forEach { item ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(item.icon, null, modifier = Modifier.size(18.dp), tint = if(item.isDestructive) Color.Red else Color(0xFF00E5FF))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(item.text, color = Color.White, fontSize = 14.sp)
                                        }
                                    },
                                    onClick = {
                                        expanded = false
                                        item.route?.let { RouteHelper.to(navController, it) }
                                        item.onClick?.invoke()
                                    }
                                )
                            }
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White
            )
        )
        // Futuristic separator line
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(
            Brush.horizontalGradient(listOf(Color.Transparent, Color(0xFF00E5FF), Color.Transparent))
        ))
    }
}
