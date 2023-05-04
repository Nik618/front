package com.example.a3d_printing_service_front.pojo.yookassa.request

import com.google.gson.annotations.SerializedName


data class YooKassaRequest (

  @SerializedName("amount"              ) var amount            : Amount?            = Amount(),
  @SerializedName("payment_method_data" ) var paymentMethodData : PaymentMethodData? = PaymentMethodData(),
  @SerializedName("confirmation"        ) var confirmation      : Confirmation?      = Confirmation(),
  @SerializedName("description"         ) var description       : String?            = null

)