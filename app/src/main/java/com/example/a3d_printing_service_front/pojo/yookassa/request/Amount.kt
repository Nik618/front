package com.example.a3d_printing_service_front.pojo.yookassa.request

import com.google.gson.annotations.SerializedName


data class Amount (

  @SerializedName("value"    ) var value    : String? = null,
  @SerializedName("currency" ) var currency : String? = null

)