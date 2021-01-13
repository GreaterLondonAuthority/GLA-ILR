package uk.gov.london.ilr.security

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component
import uk.gov.london.ilr.ops.OpsService
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class SGWAuthenticationException(var httpStatusCode: Int?) : AuthenticationException("")

@Component
class OPSAuthProvider @Autowired constructor (val opsService: OpsService) : AuthenticationProvider {

    internal var log = LoggerFactory.getLogger(javaClass)

    @Throws(AuthenticationException::class)
    override fun authenticate(authentication: Authentication): Authentication {
        val user = opsService.authenticate(authentication.principal as String, authentication.credentials as String)
        return UsernamePasswordAuthenticationToken(user, null, user.roles)
    }

    override fun supports(authentication: Class<*>): Boolean {
        return UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
    }

}

@Component
class SGWAuthenticationFailureHandler : AuthenticationFailureHandler {

    override fun onAuthenticationFailure(request: HttpServletRequest, response: HttpServletResponse, e: AuthenticationException) {
        if (e is SGWAuthenticationException) {
            response.sendRedirect("/login?error=${e.httpStatusCode}")
        }
        else {
            response.sendRedirect("/login?error")
        }
    }

}
