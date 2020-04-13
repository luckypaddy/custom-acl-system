package com.custom.acl.core.role.hierarchy

import com.custom.acl.core.role.GrantedRole

/**
 * Basis interface of a role hierarchy.
 *
 */
interface RoleHierarchy {
    /**
     * Returns an list of all roles which are transitively reachable for assigned roles
     *
     * @param roles list of assigned roles
     * @return List of all reachable roles for granted roles
     */
    fun getEffectiveRoles(roles: Collection<GrantedRole>): Collection<GrantedRole>
}
