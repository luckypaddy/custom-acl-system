package com.custom.acl.web.demo.util

/**
 * Pattern to validate an `username`
 */
private val userNamePattern = "[a-zA-Z0-9_.]+".toRegex()

/**
 * Validates that an [userId] (that is also the user name) is a valid identifier.
 * Here we could add additional checks like the length of the user.
 * Or other things like a bad word filter.
 */
fun userNameValid(userId: String) = userId.matches(userNamePattern)