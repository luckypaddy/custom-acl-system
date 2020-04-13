package com.custom.acl.core.jdbc.dao

import com.custom.acl.core.role.GrantedRole
import com.custom.acl.core.role.Role
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseHierarchicalRoleDAO(private val db: Database = Database.connect(HikariDataSource())) :
    HierarchicalRoleDAO {
    override fun create(role: GrantedRole, parentRole: GrantedRole) = transaction(db) {
        val parent = HierarchicalRoles
            .slice(HierarchicalRoles.id, HierarchicalRoles.right)
            .select { HierarchicalRoles.identity eq parentRole.getRoleIdentity() }
            .single()
        val newLeft = parent[HierarchicalRoles.right]
        val pId = parent[HierarchicalRoles.id].value

        HierarchicalRoles.update({ HierarchicalRoles.right greaterEq newLeft }) {
            with(SqlExpressionBuilder) {
                it[HierarchicalRoles.right] = HierarchicalRoles.right + 2
            }
        }

        HierarchicalRoles.update({ HierarchicalRoles.left greater newLeft }) {
            with(SqlExpressionBuilder) {
                it[HierarchicalRoles.left] = HierarchicalRoles.left + 2
            }
        }
        HierarchicalRoles.insertAndGetId {
            it[parentId] = pId
            it[identity] = role.getRoleIdentity()
            it[left] = newLeft
            it[right] = newLeft + 1
        }

        return@transaction
    }

    override fun deleteByIdentity(identity: String) {
        TODO("Not yet implemented")
    }

    override fun findBasicRoles(): Collection<GrantedRole> = transaction(db) {
        HierarchicalRole
            .find { HierarchicalRoles.parentId.isNull() }
            .map { Role(it.identity) }
    }


    override fun getEffectiveRoles(roles: Collection<GrantedRole>): Collection<GrantedRole> = transaction {
        val identities = roles.map(GrantedRole::getRoleIdentity)
        val node = HierarchicalRoles.alias("node")


        Join(node).join(HierarchicalRoles, JoinType.LEFT) { node[HierarchicalRoles.identity] inList identities }
            .slice(HierarchicalRoles.identity, HierarchicalRoles.left)
            .select {
                node[HierarchicalRoles.left] greaterEqFixed HierarchicalRoles.left and
                        (node[HierarchicalRoles.left] lessEqFixed HierarchicalRoles.right)
            }
            .withDistinct()
            .orderBy(HierarchicalRoles.left)
            .map { Role(it[HierarchicalRoles.identity]) }
    }

    override fun close() {
    }
}

/** Checks if this expression is greater than or equal to some [t] value */
infix fun <T : Comparable<T>, S : T?> Expression<in S>.greaterEqFixed(other: Expression<in S>): GreaterEqOp =
    GreaterEqOp(this, other)

/** Checks if this expression is less than or equal to some [other] expression */
infix fun <T : Comparable<T>, S : T?> Expression<in S>.lessEqFixed(other: Expression<in S>): LessEqOp =
    LessEqOp(this, other)
