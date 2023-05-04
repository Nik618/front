package com.example.a3d_printing_service_front.pojo.yookassa.response

import com.google.gson.annotations.SerializedName


data class PaymentMethod (

  @SerializedName("type"  ) var type  : String?  = null,
  @SerializedName("id"    ) var id    : String?  = null,
  @SerializedName("saved" ) var saved : Boolean? = null

)