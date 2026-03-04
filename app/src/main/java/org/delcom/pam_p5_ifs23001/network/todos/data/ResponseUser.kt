package org.delcom.pam_p5_ifs23001.network.todos.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseUser (
    val user: ResponseUserData
)

@Serializable
data class ResponseUserData(
    val id: String,
    val name: String,
    val username: String,
    val bio: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)