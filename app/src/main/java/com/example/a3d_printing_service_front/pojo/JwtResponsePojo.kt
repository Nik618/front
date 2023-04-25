package com.example.a3d_printing_service_front.pojo

data class JwtResponsePojo(
    val type: String? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null
)