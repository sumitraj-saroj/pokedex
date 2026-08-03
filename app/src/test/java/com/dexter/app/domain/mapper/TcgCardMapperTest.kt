package com.dexter.app.domain.mapper

import com.dexter.app.data.remote.TcgdexCardDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TcgCardMapperTest {

    @Test
    fun `toDomain maps TcgdexCardDto correctly to TcgCard`() {
        val dto = TcgdexCardDto(
            id = "swsh3-136",
            localId = "136",
            name = "Pikachu",
            image = "https://assets.tcgdex.net/en/swsh/swsh3/136"
        )

        val domainModel = dto.toDomain()

        assertEquals("swsh3-136", domainModel.id)
        assertEquals("136", domainModel.localId)
        assertEquals("Pikachu", domainModel.name)
        assertEquals("https://assets.tcgdex.net/en/swsh/swsh3/136/low.webp", domainModel.lowResImageUrl)
        assertEquals("https://assets.tcgdex.net/en/swsh/swsh3/136/high.png", domainModel.highResImageUrl)
        assertEquals("swsh3", domainModel.setId)
    }

    @Test
    fun `toDomain handles null image gracefully`() {
        val dto = TcgdexCardDto(
            id = "base1-4",
            localId = "4",
            name = "Charizard",
            image = null
        )

        val domainModel = dto.toDomain()

        assertEquals("base1-4", domainModel.id)
        assertEquals("4", domainModel.localId)
        assertEquals("Charizard", domainModel.name)
        assertEquals("", domainModel.lowResImageUrl)
        assertEquals("", domainModel.highResImageUrl)
        assertEquals("base1", domainModel.setId)
    }
}
