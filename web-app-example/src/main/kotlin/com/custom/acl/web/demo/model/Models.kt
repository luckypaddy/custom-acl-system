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
 * @property password password
 */
@Serializable
data class RegistrationRequest(
    val userName: String,
    val password: String
)

/**
 * Request for changing password
 *
 * @property oldPassword old password
 * @property newPassword new password
 */
@Serializable
data class PasswordChangeRequest(
    val oldPassword: String,
    val newPassword: String
)

/**
 * Request for role assignment
 *
 * @property userName user name
 * @property roleIdentities identities of roles to be assigned
 */
@Serializable
data class RolesAssignRequest(
    val userName: String,
    val roleIdentities: List<String>
)

/**
 * Request to process news feed (create or update)
 *
 * @property title title of news
 * @property content content of news
 * @property source link to source
 */
@Serializable
data class ProcessNewsFeedRequest(
    val title: String,
    val content: String,
    @Serializable(with = URLSerializer::class) val source: URL
)

/**
 * Representation of News Feed
 *
 * @property id identifier of news
 * @property userId identifier of user who posted news
 * @property title title of news
 * @property content content of news
 * @property updatedAt datetime of last update
 * @property source link to source
 */
@Serializable
data class NewsFeed(
    val id: Int,
    val userId: String,
    val title: String,
    val content: String,
    @Serializable(with = LocalDateTimeSerializer::class) val updatedAt: LocalDateTime = LocalDateTime.now(),
    @Serializable(with = URLSerializer::class) val source: URL
)