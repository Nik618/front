package com.example.a3d_printing_service_front.pojo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown=true)
data class OrdersPojo(
        val orders: MutableList<OrderPojo>? = mutableListOf()
)