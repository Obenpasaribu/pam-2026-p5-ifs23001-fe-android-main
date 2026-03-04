package org.delcom.pam_p5_ifs23001.network.todos.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseTodos (
    val todos: List<ResponseTodoData>
)

@Serializable
data class ResponseTodo (
    val todo: ResponseTodoData
)

@Serializable
data class ResponseTodoData(
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    val title: String,
    val description: String,
    @SerialName("is_done")
    val isDone: Boolean = false,
    val urgency: String? = "Low",
    val cover: String? = null,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    var updatedAt: String = ""
)

@Serializable
data class ResponseTodoAdd (
    val todoId: String
)