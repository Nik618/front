package com.example.a3d_printing_service_front.pojo

data class CreateOrderPojo (
        val description: String? = null,
        var photo: ByteArray? = null,
        var file: ByteArray? = null,
        var extension: String? = null,
        var mimeType: String? = null,
        var user: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CreateOrderPojo

        if (description != other.description) return false
        if (photo != null) {
            if (other.photo == null) return false
            if (!photo.contentEquals(other.photo)) return false
        } else if (other.photo != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = description?.hashCode() ?: 0
        result = 31 * result + (photo?.contentHashCode() ?: 0)
        return result
    }

}