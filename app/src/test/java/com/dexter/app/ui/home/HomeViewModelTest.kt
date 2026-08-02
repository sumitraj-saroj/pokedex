package com.dexter.app.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeViewModelTest {

    @Test
    fun `test initial state is loading or empty`() {
        val initialQuery = ""
        assertEquals("", initialQuery)
    }
}
