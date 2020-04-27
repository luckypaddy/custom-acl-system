package com.custom.acl.web.demo.model

import kotlinx.serialization.Serializable
import java.net.URL
import java.time.LocalDateTime

/**
 * User credentials for authentication
 *
 *
 * @property name user name
 * @property password user password
 */
@Serializable
data class UserCredentials(
    val name: String,
    val password: String
)

/**
 * Request for registration of user
 *
 * @property userName user name
 * @property password
 */
@Serializable
data class RegistrationRequest(
    val userName: String,
    val password: String
)

@Serializable
data class PasswordChangeRequest(
    val oldPassword: String,
    val newPassword: String
)

@Serializable
data class RolesAssignRequest(
    val userName: String,
    val roleIdentities: List<String>
)

@Serializable
data class ProcessNewsFeedRequest(
    val title: String,
    val content: String,
    @Serializable(with = URLSerializer::class) val source: URL
)

@Serializable
data class NewsFeed(
    val id: Int,
    val userId: String,
    val title: String,
    val content: String,
    @Serializable(with = LocalDateTimeSerializer::class) val updatedAt: LocalDateTime = LocalDateTime.now(),
    @Serializable(with = URLSerializer::class) val source: URL
)