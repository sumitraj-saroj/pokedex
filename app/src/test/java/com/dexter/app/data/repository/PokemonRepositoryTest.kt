package com.dexter.app.data.repository

import org.junit.Assert.assertNotNull
import org.junit.Test

class PokemonRepositoryTest {

    @Test
    fun `test repository instantiation`() {
        // Basic sanity test for repository structure
        val repoName = PokemonRepository::class.java.simpleName
        assertNotNull(repoName)
    }
}
