package com.custom.acl.core.jdbc.dao

import com.custom.acl.core.role.GrantedRole
import com.custom.acl.core.role.hierarchy.RoleHierarchy
import java.io.Closeable

/**
 * DAO Interface for roles with hierarchy
 *
 */
interface HierarchicalRoleDAO: RoleHierarchy, Closeable {
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
     * Find basic roles in hierarchy (with no parent)
     *
     * @return
     */
    fun findBasicRoles(): Collection<GrantedRole>

    /**
     * Find all roles
     *
     * @return
     */
    fun findAll(): Collection<GrantedRole>

}