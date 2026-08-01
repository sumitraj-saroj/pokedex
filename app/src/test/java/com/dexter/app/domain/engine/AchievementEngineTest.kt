package com.dexter.app.domain.engine

import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementEngineTest {

    @Test
    fun `test achievement engine evaluation`() {
        val engine = AchievementEngine()
        val achievements = engine.evaluateProgress(caughtCount = 150)
        assertTrue(achievements.isNotEmpty())
    }
}
