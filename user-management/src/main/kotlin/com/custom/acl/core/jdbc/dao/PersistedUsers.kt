package com.custom.acl.core.jdbc.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

internal object PersistedUsers : IntIdTable(name = "users") {
    val username = varchar("user_name", 32).uniqueIndex()
    val passwordHash = varchar("password_hash", 256)
}

internal class PersistedUser(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<PersistedUser>(PersistedUsers)
    val username by PersistedUsers.username
    val password by PersistedUsers.passwordHash
    val roles by HierarchicalRole via UserRoles
}