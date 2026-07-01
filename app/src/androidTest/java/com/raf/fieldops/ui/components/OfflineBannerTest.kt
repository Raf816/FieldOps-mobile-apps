package com.raf.fieldops.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class OfflineBannerTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `banner is visible when offline`() {

        rule.setContent {
            OfflineBanner(isOffline = true)
        }

        rule.onNode(hasText("offline", substring = true, ignoreCase = true))
            .assertIsDisplayed()
    }

    @Test
    fun `banner is hidden when online`() {

        rule.setContent {
            OfflineBanner(isOffline = false)
        }

        rule.onNode(hasText("offline", substring = true, ignoreCase = true))
            .assertDoesNotExist()
    }

    @Test
    fun `banner has correct accessibility description when offline`() {

        rule.setContent {
            OfflineBanner(isOffline = true)
        }

        rule.onNode(hasContentDescription("You are offline. Showing cached data."))
            .assertIsDisplayed()
    }
}
