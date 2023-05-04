package com.example.a3d_printing_service_front.pojo.yookassa.request

import com.google.gson.annotations.SerializedName


data class PaymentMethodData (

  @SerializedName("type" ) var type : String? = null

)