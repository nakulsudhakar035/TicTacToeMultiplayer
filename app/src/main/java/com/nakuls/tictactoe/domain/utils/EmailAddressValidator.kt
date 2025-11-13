package com.nakuls.tictactoe.domain.utils

object EmailAddressValidator {

    // A regular expression pattern that covers most standard email formats.
    // It is a practical compromise between the strict RFC 5322 and common usage,
    // ensuring it catches malformed addresses without being overly restrictive.
    private val EMAIL_REGEX = "^[\\w!#\$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#\$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$".toRegex()

    /**
     * Validates whether a given string is a correctly formatted email address.
     *
     * @param email The string to validate.
     * @return true if the string matches the email regex pattern, false otherwise.
     */
    fun isValidEmail(email: String): Boolean {
        // 1. Check for null or blank input early.
        if (email.isBlank()) {
            return false
        }

        // 2. Normalize and check against the pattern.
        return EMAIL_REGEX.matches(email.trim())
    }
}