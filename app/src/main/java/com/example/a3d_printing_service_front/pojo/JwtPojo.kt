package com.example.a3d_printing_service_front.pojo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown=true)
data class JwtPojo(
        val sub: String,
        val exp: String,
        val roles: ArrayList<String>
)