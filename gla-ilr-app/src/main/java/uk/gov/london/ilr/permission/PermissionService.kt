/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.permission

import org.springframework.stereotype.Service
import uk.gov.london.common.user.BaseRole
import uk.gov.london.ilr.security.Role
import uk.gov.london.ilr.security.UserService
import java.util.*

@Service
class PermissionService(val userService: UserService) {

    private val default_permissions = setOf(PermissionType.DEFAULT_PERMISSION.permissionKey)
    private val ops_admin_permissions = setOf(PermissionType.UPLOAD_SUPPLEMENTAL_FILE.permissionKey)
    private val gla_org_admin_permissions = setOf(PermissionType.UPLOAD_SUPPLEMENTAL_FILE.permissionKey)
    private val gla_spm_permissions = setOf(PermissionType.UPLOAD_SUPPLEMENTAL_FILE.permissionKey)
    private val gla_pm_permissions = setOf(PermissionType.UPLOAD_SUPPLEMENTAL_FILE.permissionKey)
    private val gla_finance_permissions = setOf(PermissionType.UPLOAD_SUPPLEMENTAL_FILE.permissionKey)
    private val gla_read_only_permissions = setOf(PermissionType.UPLOAD_SUPPLEMENTAL_FILE.permissionKey)
    private val org_admin_permissions = setOf(PermissionType.UPLOAD_SUPPLEMENTAL_FILE.permissionKey)
    private val project_editor_permissions = setOf(PermissionType.UPLOAD_SUPPLEMENTAL_FILE.permissionKey)
    private val project_reader_permissions = setOf(PermissionType.UPLOAD_SUPPLEMENTAL_FILE.permissionKey)

    private val permissions_map = mapOf(
            // GLA_ROLES
            BaseRole.OPS_ADMIN to ops_admin_permissions,
            BaseRole.GLA_ORG_ADMIN to gla_org_admin_permissions,
            BaseRole.GLA_SPM to gla_spm_permissions,
            BaseRole.GLA_PM to gla_pm_permissions,
            BaseRole.GLA_FINANCE to gla_finance_permissions,
            BaseRole.GLA_READ_ONLY to gla_read_only_permissions,

            // EXTERNAL_ROLES
            BaseRole.ORG_ADMIN to org_admin_permissions,
            BaseRole.PROJECT_EDITOR to project_editor_permissions,
            BaseRole.PROJECT_READER to project_reader_permissions
    )

    private fun getRolePermissions(role: Role): Set<String> {
        return permissions_map.getOrDefault(role.name, default_permissions)
    }

    private val userPermissions: Set<String>
        get() {
            val currentUserRoles = userService.currentUser.roles
            val userPermissions: MutableSet<String> = HashSet()
            for (role in currentUserRoles) {
                userPermissions.addAll(getRolePermissions(role))
            }
            return userPermissions
        }

    fun hasCurrentUserPermission(permissionType: PermissionType): Boolean {
        return userPermissions.contains(permissionType.permissionKey)
    }

}