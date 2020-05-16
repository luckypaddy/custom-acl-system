package com.custom.acl.core.jdbc.dao

import com.custom.acl.core.role.GrantedRole
import com.custom.acl.core.role.hierarchy.RoleHierarchy
import java.io.Closeable

/**
 * DAO Interface for roles with hierarchy
 *
 */
interface RoleHierarchyDAO: RoleHierarchy {
    /**
     * Create ROLE with parent relation
     *
     * @param role
     * @param parentRole
     * @return
     */
    fun create(role: GrantedRole, parentRole: GrantedRole?): GrantedRole

    /**
     * Delete ROLE from hierarchy
     *
     * @param role role to be deleted
     */
    fun delete(role: GrantedRole)

    /**
     * Check if role with given identity is present in hierarchy and return it
     *
     * @param identity identity of role to be found
     * @return [GrantedRole] or null if not present
     */
    fun findByIdentity(identity: String): GrantedRole?

    /**
     * Full graph of member_of relations in current role hierarchy
     *
     */
    fun hierarchy(): Map<GrantedRole, GrantedRole?>

    /**
     * Full graph of member_of relations in current role hierarchy with depth of each node
     *
     * @return
     */
    fun hierarchyWithDepth(): Map<GrantedRole,Pair<Long,GrantedRole?>>
}