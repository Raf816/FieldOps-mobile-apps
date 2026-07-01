package com.raf.fieldops.ui.engineer.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.raf.fieldops.data.model.DatabaseState
import com.raf.fieldops.data.model.Job
import com.raf.fieldops.ui.theme.FieldOpsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EngineerHomeScreenTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `offline banner has correct content description for accessibility`() {
        rule.setContent {
            FieldOpsTheme {
                com.raf.fieldops.ui.components.OfflineBanner(isOffline = true)
            }
        }

        rule.onNode(hasContentDescription("You are offline. Showing cached data."))
            .assertIsDisplayed()
    }

    @Test
    fun `offline banner is hidden when online`() {
        rule.setContent {
            FieldOpsTheme {
                com.raf.fieldops.ui.components.OfflineBanner(isOffline = false)
            }
        }

        rule.onNode(hasContentDescription("You are offline. Showing cached data."))
            .assertDoesNotExist()
    }

    @Test
    fun `offline banner shows correct message text`() {
        rule.setContent {
            FieldOpsTheme {
                com.raf.fieldops.ui.components.OfflineBanner(isOffline = true)
            }
        }

        rule.onNodeWithText("You're offline — showing cached data").assertIsDisplayed()
    }
}
