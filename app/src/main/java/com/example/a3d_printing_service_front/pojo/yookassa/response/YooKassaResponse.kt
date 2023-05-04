package com.example.a3d_printing_service_front.pojo.yookassa.response

import com.google.gson.annotations.SerializedName


data class YooKassaResponse (

  @SerializedName("id"             ) var id            : String?        = null,
  @SerializedName("status"         ) var status        : String?        = null,
  @SerializedName("amount"         ) var amount        : Amount?        = Amount(),
  @SerializedName("description"    ) var description   : String?        = null,
  @SerializedName("recipient"      ) var recipient     : Recipient?     = Recipient(),
  @SerializedName("payment_method" ) var paymentMethod : PaymentMethod? = PaymentMethod(),
  @SerializedName("created_at"     ) var createdAt     : String?        = null,
  @SerializedName("confirmation"   ) var confirmation  : Confirmation?  = Confirmation(),
  @SerializedName("test"           ) var test          : Boolean?       = null,
  @SerializedName("paid"           ) var paid          : Boolean?       = null,
  @SerializedName("refundable"     ) var refundable    : Boolean?       = null,
  @SerializedName("metadata"       ) var metadata      : Metadata?      = Metadata()

)