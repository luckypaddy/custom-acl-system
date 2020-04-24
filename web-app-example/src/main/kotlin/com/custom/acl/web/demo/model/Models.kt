package com.custom.acl.web.demo.model

import kotlinx.serialization.Serializable

@Serializable
data class UserCredentials(val name: String, val password: String)

@Serializable
data class RegistrationRequest(val userName: String, val password: String)

@Serializable
data class PasswordChangeRequest(val userName: String, val oldPassword: String, val newPassword: String)