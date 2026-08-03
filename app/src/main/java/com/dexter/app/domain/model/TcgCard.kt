package com.dexter.app.domain.model

data class TcgCard(
    val id: String,
    val localId: String,
    val name: String,
    val lowResImageUrl: String,
    val highResImageUrl: String,
    val setId: String? = null,
    val hasImage: Boolean = lowResImageUrl.isNotBlank()
)
