package com.example.a3d_printing_service_front.pojo.yookassa.response

import com.google.gson.annotations.SerializedName


data class Recipient (

  @SerializedName("account_id" ) var accountId : String? = null,
  @SerializedName("gateway_id" ) var gatewayId : String? = null

)