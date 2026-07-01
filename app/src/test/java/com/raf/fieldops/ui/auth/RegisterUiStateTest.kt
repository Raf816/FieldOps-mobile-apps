package com.raf.fieldops.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterUiStateTest {

    @Test
    fun nameIsInvalid_emptyString_returnsTrue() {
        val state = RegisterUiState(displayName = "")
        assertTrue(state.nameIsInvalid())
    }

    @Test
    fun nameIsInvalid_singleCharacter_returnsTrue() {
        val state = RegisterUiState(displayName = "R")
        assertTrue(state.nameIsInvalid())
    }

    @Test
    fun nameIsInvalid_twoCharacters_returnsFalse() {
        val state = RegisterUiState(displayName = "Ra")
        assertFalse(state.nameIsInvalid())
    }

    @Test
    fun nameIsInvalid_longName_returnsFalse() {
        val state = RegisterUiState(displayName = "Raf Ahmed")
        assertFalse(state.nameIsInvalid())
    }

    @Test
    fun nameIsInvalid_containsNumbers_returnsTrue() {
        val state = RegisterUiState(displayName = "Raf123")
        assertTrue(state.nameIsInvalid())
    }

    @Test
    fun nameIsInvalid_containsSingleDigit_returnsTrue() {
        val state = RegisterUiState(displayName = "John2")
        assertTrue(state.nameIsInvalid())
    }

    @Test
    fun nameContainsNumbers_withDigits_returnsTrue() {
        val state = RegisterUiState(displayName = "Test99")
        assertTrue(state.nameContainsNumbers())
    }

    @Test
    fun nameContainsNumbers_noDigits_returnsFalse() {
        val state = RegisterUiState(displayName = "Raf Ahmed")
        assertFalse(state.nameContainsNumbers())
    }

    @Test
    fun nameContainsNumbers_onlyDigits_returnsTrue() {
        val state = RegisterUiState(displayName = "12345")
        assertTrue(state.nameContainsNumbers())
    }

    @Test
    fun nameIsInvalid_hyphenatedName_returnsFalse() {
        val state = RegisterUiState(displayName = "Mary-Jane")
        assertFalse(state.nameIsInvalid())
    }

    @Test
    fun nameIsInvalid_apostropheName_returnsFalse() {
        val state = RegisterUiState(displayName = "O'Brien")
        assertFalse(state.nameIsInvalid())
    }

    @Test
    fun passwordIsInvalid_emptyString_returnsTrue() {
        val state = RegisterUiState(password = "")
        assertTrue(state.passwordIsInvalid())
    }

    @Test
    fun passwordIsInvalid_fiveChars_returnsTrue() {
        val state = RegisterUiState(password = "12345")
        assertTrue(state.passwordIsInvalid())
    }

    @Test
    fun passwordIsInvalid_sixChars_returnsFalse() {
        val state = RegisterUiState(password = "123456")
        assertFalse(state.passwordIsInvalid())
    }

    @Test
    fun passwordsDoNotMatch_differentPasswords_returnsTrue() {
        val state = RegisterUiState(password = "Passw0rd", confirmPassword = "Different")
        assertTrue(state.passwordsDoNotMatch())
    }

    @Test
    fun passwordsDoNotMatch_matchingPasswords_returnsFalse() {
        val state = RegisterUiState(password = "Passw0rd", confirmPassword = "Passw0rd")
        assertFalse(state.passwordsDoNotMatch())
    }

    @Test
    fun passwordsDoNotMatch_bothEmpty_returnsFalse() {
        val state = RegisterUiState(password = "", confirmPassword = "")
        assertFalse(state.passwordsDoNotMatch())
    }

    @Test
    fun isValid_invalidName_returnsFalse() {
        val state = RegisterUiState(
            displayName = "R",
            email = "raf@bt.com",
            password = "Passw0rd",
            confirmPassword = "Passw0rd"
        )

        assertFalse(state.nameIsInvalid().not() && state.passwordIsInvalid().not())

        assertTrue(state.nameIsInvalid())
    }

    @Test
    fun isValid_passwordTooShort_returnsFalse() {
        val state = RegisterUiState(
            displayName = "Raf",
            email = "raf@bt.com",
            password = "12345",
            confirmPassword = "12345"
        )
        assertTrue(state.passwordIsInvalid())
    }

    @Test
    fun isValid_passwordsMismatch_returnsFalse() {
        val state = RegisterUiState(
            displayName = "Raf",
            email = "raf@bt.com",
            password = "Passw0rd",
            confirmPassword = "Different"
        )
        assertTrue(state.passwordsDoNotMatch())
    }

    @Test
    fun calculatePasswordStrength_emptyString_returnsWeak() {
        assertEquals(PasswordStrength.WEAK, calculatePasswordStrength(""))
    }

    @Test
    fun calculatePasswordStrength_threeChars_returnsWeak() {
        assertEquals(PasswordStrength.WEAK, calculatePasswordStrength("abc"))
    }

    @Test
    fun calculatePasswordStrength_fiveChars_returnsWeak() {
        assertEquals(PasswordStrength.WEAK, calculatePasswordStrength("abcde"))
    }

    @Test
    fun calculatePasswordStrength_sixLowercaseChars_returnsMedium() {

        assertEquals(PasswordStrength.MEDIUM, calculatePasswordStrength("abcdef"))
    }

    @Test
    fun calculatePasswordStrength_sixCharsWithUppercase_returnsMedium() {

        assertEquals(PasswordStrength.MEDIUM, calculatePasswordStrength("Abcdef"))
    }

    @Test
    fun calculatePasswordStrength_sixCharsWithDigit_returnsMedium() {

        assertEquals(PasswordStrength.MEDIUM, calculatePasswordStrength("abcde1"))
    }

    @Test
    fun calculatePasswordStrength_sixCharsWithUppercaseAndDigit_returnsStrong() {

        assertEquals(PasswordStrength.STRONG, calculatePasswordStrength("Abcde1"))
    }

    @Test
    fun calculatePasswordStrength_longStrongPassword_returnsStrong() {
        assertEquals(PasswordStrength.STRONG, calculatePasswordStrength("MyP4ssw0rd!"))
    }
}
