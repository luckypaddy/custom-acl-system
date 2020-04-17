package com.custom.acl.core.jdbc.dao

import com.custom.acl.core.role.GrantedRole
import com.custom.acl.core.role.Role
import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

private val logger = KotlinLogging.logger {}

/**
 * Implementation of role hierarchy persisted in database
 *
 * @property db
 */
class RoleHierarchyDatabase(private val db: Database = Database.connect(HikariDataSource())) :
    RoleHierarchyDAO {
    override fun create(role: GrantedRole, parentRole: GrantedRole?) =
        transaction(Connection.TRANSACTION_SERIALIZABLE, 3, db) {
            val (newLeft, pId) = when {
                parentRole != null -> {
                    val identity = parentRole.getRoleIdentity()
                    logger.info { "Searching for parent role:$identity" }
                    val parent = HierarchicalRoles
                        .slice(HierarchicalRoles.id, HierarchicalRoles.right)
                        .select { HierarchicalRoles.identity eq identity }
                        .singleOrNull()
                    if (parent == null) {
                        logger.error { "Identity $identity is not found" }
                        throw IllegalArgumentException("Identity $identity is not found")
                    }
                    parent[HierarchicalRoles.right] to parent[HierarchicalRoles.id].value
                }
                else -> {
                    logger.info {"Searching for max right border of nested sets"}
                    val max = HierarchicalRoles.right.max()
                    val maxValue = HierarchicalRoles.slice(max).selectAll().map { it[max] }.single() ?: 0
                    (maxValue + 1) to null
                }
            }

            logger.info {"Update right borders of nested sets"}
            HierarchicalRoles.update({ HierarchicalRoles.right greaterEq newLeft }) {
                with(SqlExpressionBuilder) {
                    it[right] = right + 2
                }
            }

            logger.info {"Update left borders of nested sets"}
            HierarchicalRoles.update({ HierarchicalRoles.left greater newLeft }) {
                with(SqlExpressionBuilder) {
                    it[left] = left + 2
                }
            }

            logger.info {"Creating new hierarchical role with identity: ${role.getRoleIdentity()}"}
            val entityID = HierarchicalRoles.insertAndGetId {
                it[parentId] = pId
                it[identity] = role.getRoleIdentity()
                it[left] = newLeft
                it[right] = newLeft + 1
            }

            return@transaction Role(HierarchicalRole[entityID].identity)
        }

    override fun delete(role: GrantedRole) = transaction(Connection.TRANSACTION_SERIALIZABLE, 3, db) {
        val identity = role.getRoleIdentity()
        logger.info {"Searching for role with identity: $identity"}
        val node = HierarchicalRoles
            .slice(HierarchicalRoles.left, HierarchicalRoles.right, HierarchicalRoles.parentId)
            .select { HierarchicalRoles.identity eq identity }
            .singleOrNull()

        if (node == null) {
            logger.warn { "There is no role with identity: $identity" }
            return@transaction
        }

        val newLeft = node[HierarchicalRoles.left]
        val newRight = node[HierarchicalRoles.right]
        val ancestor = node[HierarchicalRoles.parentId]
        val noLeafs = (newRight - newLeft) == 1
        val width = newRight - newLeft + 1

        when {
            noLeafs -> {
                logger.info { "Deleting hierarchy node without descendants" }
                HierarchicalRoles.deleteWhere { HierarchicalRoles.identity eq identity }

                HierarchicalRoles.update({ HierarchicalRoles.right greater newRight }) {
                    with(SqlExpressionBuilder) {
                        it[right] = right - width
                    }
                }

                HierarchicalRoles.update({ HierarchicalRoles.left greater newRight }) {
                    with(SqlExpressionBuilder) {
                        it[left] = left - width
                    }
                }
            }
            else -> {
                logger.info { "Deleting hierarchy node without descendants" }
                HierarchicalRoles.deleteWhere { HierarchicalRoles.identity eq identity }

                HierarchicalRoles.update({ HierarchicalRoles.left.between(newLeft, newRight) }) {
                    with(SqlExpressionBuilder) {
                        it[left] = left - 1
                        it[right] = right - 1
                        it[parentId] = ancestor
                    }
                }

                HierarchicalRoles.update({ HierarchicalRoles.right greater newRight }) {
                    with(SqlExpressionBuilder) {
                        it[right] = right - 2
                    }
                }

                HierarchicalRoles.update({ HierarchicalRoles.left greater newRight }) { it ->
                    with(SqlExpressionBuilder) {
                        it[left] = left - 2
                    }
                }

            }
        }
        return@transaction
    }

    override fun findByIdentity(identity: String): GrantedRole? = transaction(db) {
        logger.info {"Searching for role with identity: $identity"}
        HierarchicalRole
            .find { HierarchicalRoles.identity eq identity }
            .map { role -> Role(role.identity) }
            .singleOrNull()
    }

    override fun findAll(): Collection<GrantedRole> = transaction(db) {
        logger.info {"Searching for all roles"}
        HierarchicalRole.all()
            .sortedBy { HierarchicalRoles.left }
            .map { role -> Role(role.identity) }
    }

    override fun effectiveRoles(roles: Collection<GrantedRole>): Collection<GrantedRole> = transaction {
        val identities = roles.map(GrantedRole::getRoleIdentity)
        val node = HierarchicalRoles.alias("node")

        logger.info {"Searching for effective roles for: $identities"}
        Join(node).join(HierarchicalRoles, JoinType.INNER) { node[HierarchicalRoles.identity] inList identities }
            .slice(HierarchicalRoles.identity, HierarchicalRoles.left)
            .select {
                node[HierarchicalRoles.left] greaterEq HierarchicalRoles.left and
                        (node[HierarchicalRoles.left] lessEq HierarchicalRoles.right)
            }
            .withDistinct()
            .orderBy(HierarchicalRoles.left)
            .map { row ->
                Role(row[HierarchicalRoles.identity])
            }
    }

    @Suppress("SENSELESS_NULL_IN_WHEN")
    override fun hierarchy(): Map<GrantedRole, GrantedRole?> = transaction(db) {
        logger.info {"Searching for role hierarchy"}
        val parent = HierarchicalRoles.alias("parent")
        return@transaction Join(HierarchicalRoles)
            .join(parent, JoinType.LEFT) {
                HierarchicalRoles.parentId eq parent[HierarchicalRoles.id]
            }
            .slice(HierarchicalRoles.identity, HierarchicalRoles.left, parent[HierarchicalRoles.identity])
            .selectAll()
            .groupBy(
                HierarchicalRoles.identity,
                HierarchicalRoles.left,
                parent[HierarchicalRoles.identity]
            )
            .orderBy(HierarchicalRoles.left)
            .associate {
                Role(it[HierarchicalRoles.identity]) to
                        when (val parentIdentity = it[parent[HierarchicalRoles.identity]]) {
                            null -> null
                            else -> Role(parentIdentity)
                        }
            }
    }

    override fun close() {
    }
}
