package com.custom.acl.core.jdbc.dao

import com.custom.acl.core.role.Role
import com.custom.acl.core.user.User
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

/**
 * Table for storing user related information
 *
 */
object PersistedUsers : UUIDTable(name = "users") {
    val username = varchar("user_name", 32).uniqueIndex()
    val passwordHash = varchar("password_hash", 256)
}

/**
 * Entity mapped on [PersistedUsers] table
 *
 */
internal class PersistedUser(id: EntityID<UUID>): UUIDEntity(id) {
    companion object : UUIDEntityClass<PersistedUser>(PersistedUsers)
    var username by PersistedUsers.username
    var passwordHash by PersistedUsers.passwordHash
    var roles by HierarchicalRole via UserRoles
}

/**
 * Function to convert [PersistedUser] to [User]
 *
 */
internal fun PersistedUser.toUser() = User(
    username = username,
    passwordHash = passwordHash,
    roles = roles.map { hierarchicalRole -> Role(hierarchicalRole.identity) }
)