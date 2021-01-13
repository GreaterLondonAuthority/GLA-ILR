/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ilr.permission

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import uk.gov.london.ilr.security.UserService

@Aspect
@Component
@Order(value = 1)
class PermissionRequiredAspect(val userService: UserService,
                               val permissionService: PermissionService) {

    @Before("@annotation(uk.gov.london.ilr.permission.PermissionRequired)")
    @Throws(Throwable::class)
    fun check(joinPoint: JoinPoint) {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val annotation = method.getAnnotation(PermissionRequired::class.java)
        val permission: PermissionType = annotation.value[0]
        if (userService.currentUser == null || !permissionService.hasCurrentUserPermission(permission)) {
            throw RuntimeException("You don't have permission to perform this action.")
        }
    }
}