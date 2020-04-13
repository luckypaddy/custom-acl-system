package com.custom.acl.core.jdbc.dao

import org.jetbrains.exposed.sql.Table

internal object UserRoles: Table(name = "user_role") {
    val user = reference("user", PersistedUsers)
    val role = reference("role", HierarchicalRoles)
}