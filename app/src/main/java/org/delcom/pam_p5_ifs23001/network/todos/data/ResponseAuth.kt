package org.delcom.pam_p5_ifs23001.network.todos.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseAuthRegister (
    @SerialName("user_id")
    val userId: String
)

@Serializable
data class ResponseAuthLogin (
    @SerialName("auth_token")
    val authToken: String,
    @SerialName("refresh_token")
    val refreshToken: String
)