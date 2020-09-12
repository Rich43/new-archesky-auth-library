package com.archesky.auth.library.security

import com.archesky.auth.library.service.TokenMappingService
import com.archesky.auth.library.service.TokenService
import com.archesky.common.library.HeaderUtil.getHost
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.env.Environment
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtTokenFilter(
        @Qualifier("customUserDetailsService") private val userDetailsService: UserDetailsService,
        private val tokenMappingService: TokenMappingService,
        private val env: Environment,
        private val tokenService: TokenService
) : OncePerRequestFilter() {
    private val log = getLogger(this.javaClass)

    private fun resolveToken(request: HttpServletRequest): String? {
        val optReq = Optional.of(request)

        return optReq.map {
            req: HttpServletRequest -> req.getHeader("Authorization")
        }.filter {
            token: String ->
            token.isNotEmpty()
        }.map {
            token: String -> token.replace("Bearer ", "")
        }.orElse(null)
    }

    private fun getAuthentication(token: String?, hostName: String): Authentication? {
        return try {
            val validateToken = tokenService.validateToken(
                    token!!,
                    env.getProperty("archesky.auth.library.server.url", "https://localhost:9443/graphql"),
                    hostName
            )
            tokenMappingService.userTokenMap[validateToken.username] = validateToken
            val userDetails = this.userDetailsService.loadUserByUsername(validateToken.username)
            UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
        } catch (e: Exception) {
            null
        }
    }

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val token = resolveToken(request)
        if (token != null) {
            val auth = getAuthentication(token, getHost(request))
            if (auth == null) {
                log.debug("Invalid token")
            } else {
                log.debug("Token accepted")
            }
            SecurityContextHolder.getContext().authentication = auth
        } else {
            log.debug("No auth token")
        }
        filterChain.doFilter(request, response)
    }
}
