package com.raf.fieldops.ui.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class RegisterUiStateInstrumentedTest {

    @Test
    fun emailIsInvalid_emptyString_returnsTrue() {
        val state = RegisterUiState(email = "")
        assertTrue(state.emailIsInvalid())
    }

    @Test
    fun emailIsInvalid_noAtSymbol_returnsTrue() {
        val state = RegisterUiState(email = "rafbt.com")
        assertTrue(state.emailIsInvalid())
    }

    @Test
    fun emailIsInvalid_validEmail_returnsFalse() {
        val state = RegisterUiState(email = "raf@bt.com")
        assertFalse(state.emailIsInvalid())
    }

    @Test
    fun emailIsInvalid_missingDomain_returnsTrue() {
        val state = RegisterUiState(email = "raf@")
        assertTrue(state.emailIsInvalid())
    }

    @Test
    fun isValid_allFieldsValid_returnsTrue() {
        val state = RegisterUiState(
            displayName = "Raf",
            email = "raf@bt.com",
            password = "Passw0rd",
            confirmPassword = "Passw0rd",
            role = "engineer"
        )
        assertTrue(state.isValid())
    }

    @Test
    fun isValid_invalidEmail_returnsFalse() {
        val state = RegisterUiState(
            displayName = "Raf",
            email = "invalid",
            password = "Passw0rd",
            confirmPassword = "Passw0rd"
        )
        assertFalse(state.isValid())
    }

    @Test
    fun isValid_allFieldsInvalid_returnsFalse() {
        val state = RegisterUiState(
            displayName = "R",
            email = "bad",
            password = "12",
            confirmPassword = "34"
        )
        assertFalse(state.isValid())
    }
}
