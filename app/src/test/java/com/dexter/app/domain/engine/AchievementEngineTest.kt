package com.dexter.app.domain.engine

import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementEngineTest {

    @Test
    fun `test predefined achievements list is non empty`() {
        val achievements = AchievementEngine.PREDEFINED_ACHIEVEMENTS
        assertTrue(achievements.isNotEmpty())
    }
}
