package com.ichaiwizm.metaraybanassistant.data.model

import java.util.Date

/**
 * Represents a photo captured from Meta Ray-Ban glasses
 */
data class Photo(
    val id: String,
    val timestamp: Date,
    val imageData: ByteArray,
    val metadata: PhotoMetadata? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Photo

        if (id != other.id) return false
        if (timestamp != other.timestamp) return false
        if (!imageData.contentEquals(other.imageData)) return false
        if (metadata != other.metadata) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + imageData.contentHashCode()
        result = 31 * result + (metadata?.hashCode() ?: 0)
        return result
    }
}

data class PhotoMetadata(
    val width: Int,
    val height: Int,
    val format: String,
    val size: Long
)
