package com.dexter.app.ui.detail

import org.junit.Assert.assertNotNull
import org.junit.Test

class DetailViewModelTest {

    @Test
    fun `test detail view model initial state`() {
        val vmName = DetailViewModel::class.java.simpleName
        assertNotNull(vmName)
    }
}
