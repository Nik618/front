package com.example.a3d_printing_service_front.pojo.yookassa.response

import com.google.gson.annotations.SerializedName


data class Confirmation (

  @SerializedName("type"             ) var type            : String? = null,
  @SerializedName("return_url"       ) var returnUrl       : String? = null,
  @SerializedName("confirmation_url" ) var confirmationUrl : String? = null

)