package com.example.a3d_printing_service_front.pojo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown=true)
data class OrderPojo(
        val id: Int? = null,
        val description: String? = null,
        val photo: ByteArray? = null,
        var file: ByteArray? = null,
        var extension: String? = null,
        var mimeType: String? = null,
        val status: String? = null,
        val price: String? = null,
        val track: String? = null,
        val paymentAddress: String? = null
) {

        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as OrderPojo

                if (id != other.id) return false
                if (description != other.description) return false
                if (!photo.contentEquals(other.photo)) return false

                return true
        }

        override fun hashCode(): Int {
                var result = id ?: 0
                result = 31 * result + description.hashCode()
                result = 31 * result + photo.contentHashCode()
                return result
        }
}