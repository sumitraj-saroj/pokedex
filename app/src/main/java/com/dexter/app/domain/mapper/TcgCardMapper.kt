package com.dexter.app.domain.mapper

import com.dexter.app.data.remote.TcgdexCardDto
import com.dexter.app.domain.model.TcgCard

fun TcgdexCardDto.toDomain(): TcgCard {
    val baseImg = image ?: ""
    val lowRes = if (baseImg.isNotBlank()) "$baseImg/low.webp" else ""
    val highRes = if (baseImg.isNotBlank()) "$baseImg/high.png" else ""
    val computedLocalId = localId ?: if (id.contains("-")) id.substringAfterLast("-") else id
    val computedSetId = if (id.contains("-")) id.substringBeforeLast("-") else null

    return TcgCard(
        id = id,
        localId = computedLocalId,
        name = name,
        lowResImageUrl = lowRes,
        highResImageUrl = highRes,
        setId = computedSetId,
        hasImage = baseImg.isNotBlank()
    )
}

fun List<TcgdexCardDto>.toDomain(): List<TcgCard> = map { it.toDomain() }
