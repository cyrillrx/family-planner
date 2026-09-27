package com.cyrillrx.family

import kotlin.test.Test
import kotlin.test.assertTrue

class PlatformTest {

    @Test
    fun `names the platform it runs on`() {
        assertTrue(getPlatform().name.isNotBlank())
    }
}
