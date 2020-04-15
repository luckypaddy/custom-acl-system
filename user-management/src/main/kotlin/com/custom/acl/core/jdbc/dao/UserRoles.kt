package com.custom.acl.core.jdbc.dao

import org.jetbrains.exposed.sql.ReferenceOption.CASCADE
import org.jetbrains.exposed.sql.Table

/**
 * Intermediate table to store the references between [HierarchicalRoles] and [PersistedUsers]
 */
internal object UserRoles : Table(name = "user_role") {
    val user = reference("user", PersistedUsers, CASCADE, CASCADE)
    val role = reference("role", HierarchicalRoles)
}