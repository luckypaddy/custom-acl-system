package com.custom.acl.core.jdbc.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

internal object HierarchicalRoles : IntIdTable(name = "roles") {
    val identity = varchar("identity", 32)
    val parentId = integer("parent_id").nullable()
    val left = integer("lft")
    val right = integer("rgt")
}

internal class HierarchicalRole(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<HierarchicalRole>(HierarchicalRoles)
    val identity by HierarchicalRoles.identity

}