package org.delcom.pam_p5_ifs23001.network.todos.service

import okhttp3.MultipartBody
import org.delcom.pam_p5_ifs23001.helper.SuspendHelper
import org.delcom.pam_p5_ifs23001.network.data.ResponseMessage
import org.delcom.pam_p5_ifs23001.network.todos.data.RequestAuthLogin
import org.delcom.pam_p5_ifs23001.network.todos.data.RequestAuthLogout
import org.delcom.pam_p5_ifs23001.network.todos.data.RequestAuthRefreshToken
import org.delcom.pam_p5_ifs23001.network.todos.data.RequestAuthRegister
import org.delcom.pam_p5_ifs23001.network.todos.data.RequestTodo
import org.delcom.pam_p5_ifs23001.network.todos.data.RequestUserChange
import org.delcom.pam_p5_ifs23001.network.todos.data.RequestUserChangePassword
import org.delcom.pam_p5_ifs23001.network.todos.data.ResponseAuthLogin
import org.delcom.pam_p5_ifs23001.network.todos.data.ResponseAuthRegister
import org.delcom.pam_p5_ifs23001.network.todos.data.ResponseTodo
import org.delcom.pam_p5_ifs23001.network.todos.data.ResponseTodoAdd
import org.delcom.pam_p5_ifs23001.network.todos.data.ResponseTodos
import org.delcom.pam_p5_ifs23001.network.todos.data.ResponseUser

class TodoRepository(
    private val apiService: TodoApiService
) : ITodoRepository {

    override suspend fun postRegister(request: RequestAuthRegister) = SuspendHelper.safeApiCall { apiService.postRegister(request) }
    override suspend fun postLogin(request: RequestAuthLogin) = SuspendHelper.safeApiCall { apiService.postLogin(request) }
    override suspend fun postLogout(request: RequestAuthLogout) = SuspendHelper.safeApiCall { apiService.postLogout(request) }
    override suspend fun postRefreshToken(request: RequestAuthRefreshToken) = SuspendHelper.safeApiCall { apiService.postRefreshToken(request) }

    override suspend fun getUserMe(authToken: String) = SuspendHelper.safeApiCall { apiService.getUserMe("Bearer $authToken") }
    override suspend fun putUserMe(authToken: String, request: RequestUserChange) = SuspendHelper.safeApiCall { apiService.putUserMe("Bearer $authToken", request) }
    override suspend fun putUserMePassword(authToken: String, request: RequestUserChangePassword) = SuspendHelper.safeApiCall { apiService.putUserMePassword("Bearer $authToken", request) }
    override suspend fun putUserMePhoto(authToken: String, file: MultipartBody.Part) = SuspendHelper.safeApiCall { apiService.putUserMePhoto("Bearer $authToken", file) }

    override suspend fun getTodos(
        authToken: String,
        search: String?,
        isDone: Boolean?,
        urgency: String?,
        page: Int?,
        perPage: Int?
    ): ResponseMessage<ResponseTodos?> {
        return SuspendHelper.safeApiCall {
            apiService.getTodos("Bearer $authToken", search, isDone, urgency, page, perPage)
        }
    }

    override suspend fun postTodo(authToken: String, request: RequestTodo) = SuspendHelper.safeApiCall { apiService.postTodo("Bearer $authToken", request) }
    override suspend fun getTodoById(authToken: String, todoId: String) = SuspendHelper.safeApiCall { apiService.getTodoById("Bearer $authToken", todoId) }
    override suspend fun putTodo(authToken: String, todoId: String, request: RequestTodo) = SuspendHelper.safeApiCall { apiService.putTodo("Bearer $authToken", todoId, request) }
    override suspend fun putTodoCover(authToken: String, todoId: String, file: MultipartBody.Part) = SuspendHelper.safeApiCall { apiService.putTodoCover("Bearer $authToken", todoId, file) }
    override suspend fun deleteTodo(authToken: String, todoId: String) = SuspendHelper.safeApiCall { apiService.deleteTodo("Bearer $authToken", todoId) }
}