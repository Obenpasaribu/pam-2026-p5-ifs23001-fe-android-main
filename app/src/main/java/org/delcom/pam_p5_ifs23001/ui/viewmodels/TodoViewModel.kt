package org.delcom.pam_p5_ifs23001.ui.viewmodels

import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.delcom.pam_p5_ifs23001.network.todos.data.RequestTodo
import org.delcom.pam_p5_ifs23001.network.todos.data.RequestUserChange
import org.delcom.pam_p5_ifs23001.network.todos.data.RequestUserChangePassword
import org.delcom.pam_p5_ifs23001.network.todos.data.ResponseTodoData
import org.delcom.pam_p5_ifs23001.network.todos.data.ResponseUserData
import org.delcom.pam_p5_ifs23001.network.todos.service.ITodoRepository
import javax.inject.Inject

sealed interface ProfileUIState {
    data class Success(val data: ResponseUserData) : ProfileUIState
    data class Error(val message: String) : ProfileUIState
    object Loading : ProfileUIState
}

sealed interface TodosUIState {
    data class Success(val data: List<ResponseTodoData>, val endOfPaginationReached: Boolean = false) : TodosUIState
    data class Error(val message: String) : TodosUIState
    object Loading : TodosUIState
}

sealed interface TodoUIState {
    data class Success(val data: ResponseTodoData) : TodoUIState
    data class Error(val message: String) : TodoUIState
    object Loading : TodoUIState
}

sealed interface TodoActionUIState {
    data class Success(val message: String) : TodoActionUIState
    data class Error(val message: String) : TodoActionUIState
    object Loading : TodoActionUIState
}

data class UIStateTodo(
    val profile: ProfileUIState = ProfileUIState.Loading,
    val todos: TodosUIState = TodosUIState.Loading,
    var todo: TodoUIState = TodoUIState.Loading,
    var todoAdd: TodoActionUIState = TodoActionUIState.Loading,
    var todoChange: TodoActionUIState = TodoActionUIState.Loading,
    var todoDelete: TodoActionUIState = TodoActionUIState.Loading,
    var todoChangeCover: TodoActionUIState = TodoActionUIState.Loading,
    var profileUpdate: TodoActionUIState = TodoActionUIState.Loading,
    var passwordUpdate: TodoActionUIState = TodoActionUIState.Loading,
    var photoUpdate: TodoActionUIState = TodoActionUIState.Loading
)

@HiltViewModel
@Keep
class TodoViewModel @Inject constructor(
    private val repository: ITodoRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UIStateTodo())
    val uiState = _uiState.asStateFlow()

    private var currentTodosList = mutableListOf<ResponseTodoData>()
    private var currentPage = 1 // Sesuai Backend (Page 1)
    private val perPage = 10

    fun getAllTodos(
        authToken: String, 
        search: String? = null, 
        isDone: Boolean? = null,
        urgency: String? = null,
        reset: Boolean = false
    ) {
        if (reset) {
            currentPage = 1
            currentTodosList.clear()
        }

        viewModelScope.launch {
            if (reset) {
                _uiState.update { it.copy(todos = TodosUIState.Loading) }
            }
            
            val result = runCatching {
                repository.getTodos(authToken, search, isDone, urgency, currentPage, perPage)
            }.fold(
                onSuccess = {
                    if (it.status == "success") {
                        val newTodos = it.data?.todos ?: emptyList()
                        if (reset) {
                            currentTodosList = newTodos.toMutableList()
                        } else {
                            currentTodosList.addAll(newTodos)
                        }
                        
                        if (newTodos.isNotEmpty()) {
                            currentPage++
                        }

                        TodosUIState.Success(
                            data = currentTodosList.toList(),
                            endOfPaginationReached = newTodos.size < perPage
                        )
                    } else {
                        TodosUIState.Error(it.message)
                    }
                },
                onFailure = {
                    TodosUIState.Error(it.message ?: "Unknown error")
                }
            )

            _uiState.update { it.copy(todos = result) }
        }
    }

    fun getProfile(authToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(profile = ProfileUIState.Loading) }
            val tmpState = runCatching { repository.getUserMe(authToken) }.fold(
                onSuccess = { if (it.status == "success") ProfileUIState.Success(it.data!!.user) else ProfileUIState.Error(it.message) },
                onFailure = { ProfileUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(profile = tmpState) }
        }
    }

    fun updateProfile(authToken: String, name: String, username: String, bio: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(profileUpdate = TodoActionUIState.Loading) }
            val tmpState = runCatching {
                repository.putUserMe(authToken, RequestUserChange(name, username, bio))
            }.fold(
                onSuccess = { if (it.status == "success") TodoActionUIState.Success(it.message) else TodoActionUIState.Error(it.message) },
                onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(profileUpdate = tmpState) }
        }
    }

    fun updatePassword(authToken: String, currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(passwordUpdate = TodoActionUIState.Loading) }
            val tmpState = runCatching {
                repository.putUserMePassword(authToken, RequestUserChangePassword(newPassword, currentPassword))
            }.fold(
                onSuccess = { if (it.status == "success") TodoActionUIState.Success(it.message) else TodoActionUIState.Error(it.message) },
                onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(passwordUpdate = tmpState) }
        }
    }

    fun updatePhoto(authToken: String, file: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.update { it.copy(photoUpdate = TodoActionUIState.Loading) }
            val tmpState = runCatching {
                repository.putUserMePhoto(authToken, file)
            }.fold(
                onSuccess = { if (it.status == "success") TodoActionUIState.Success(it.message) else TodoActionUIState.Error(it.message) },
                onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(photoUpdate = tmpState) }
        }
    }

    fun postTodo(authToken: String, title: String, description: String, urgency: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoAdd = TodoActionUIState.Loading) }
            val tmpState = runCatching { repository.postTodo(authToken, RequestTodo(title, description, urgency = urgency)) }.fold(
                onSuccess = { if (it.status == "success") TodoActionUIState.Success(it.message) else TodoActionUIState.Error(it.message) },
                onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(todoAdd = tmpState) }
        }
    }

    fun getTodoById(authToken: String, todoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(todo = TodoUIState.Loading) }
            val tmpState = runCatching { repository.getTodoById(authToken, todoId) }.fold(
                onSuccess = { if (it.status == "success") TodoUIState.Success(it.data!!.todo) else TodoUIState.Error(it.message) },
                onFailure = { TodoUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(todo = tmpState) }
        }
    }

    fun putTodo(authToken: String, todoId: String, title: String, description: String, isDone: Boolean, urgency: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoChange = TodoActionUIState.Loading) }
            val tmpState = runCatching { repository.putTodo(authToken, todoId, RequestTodo(title, description, isDone, urgency)) }.fold(
                onSuccess = { if (it.status == "success") TodoActionUIState.Success(it.message) else TodoActionUIState.Error(it.message) },
                onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(todoChange = tmpState) }
        }
    }

    fun putTodoCover(authToken: String, todoId: String, file: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoChangeCover = TodoActionUIState.Loading) }
            val tmpState = runCatching { repository.putTodoCover(authToken, todoId, file) }.fold(
                onSuccess = { if (it.status == "success") TodoActionUIState.Success(it.message) else TodoActionUIState.Error(it.message) },
                onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(todoChangeCover = tmpState) }
        }
    }

    fun deleteTodo(authToken: String, todoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoDelete = TodoActionUIState.Loading) }
            val tmpState = runCatching { repository.deleteTodo(authToken, todoId) }.fold(
                onSuccess = { if (it.status == "success") TodoActionUIState.Success(it.message) else TodoActionUIState.Error(it.message) },
                onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(todoDelete = tmpState) }
        }
    }
}