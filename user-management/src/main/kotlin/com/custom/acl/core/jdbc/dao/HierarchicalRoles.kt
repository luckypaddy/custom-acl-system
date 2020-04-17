package com.custom.acl.core.jdbc.dao

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.upperCase
import java.util.*

/**
 * Roles hierarchy table
 */
object HierarchicalRoles : UUIDTable(name = "roles") {
    val identity = varchar("identity", 32).uniqueIndex()
    val parentId = uuid("parent_id").nullable()
    val left = integer("lft")
    val right = integer("rgt")
}

/**
 * Entity mapped on [HierarchicalRoles] table
 *
 */
internal class HierarchicalRole(id: EntityID<UUID>): UUIDEntity(id) {
    companion object : UUIDEntityClass<HierarchicalRole>(HierarchicalRoles)
    val parentId by HierarchicalRoles.parentId
    val identity by HierarchicalRoles.identity

}