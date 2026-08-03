package com.dexter.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TcgdexCardDto(
    @SerialName("id") val id: String,
    @SerialName("localId") val localId: String? = null,
    @SerialName("name") val name: String,
    @SerialName("image") val image: String? = null
)
