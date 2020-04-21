package com.custom.acl.web.demo.model

import kotlinx.serialization.Serializable

@Serializable
data class UserCredentials(val name: String, val password: String)