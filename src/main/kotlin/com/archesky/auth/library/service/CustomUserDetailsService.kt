package com.archesky.auth.library.service

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component

@Component
class CustomUserDetailsService(private val tokenMappingService: TokenMappingService) : UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val token = tokenMappingService.userTokenMap[username]
        return object : UserDetails {
            override fun getAuthorities(): Collection<GrantedAuthority?> {
                val authoritiesList = ArrayList<SimpleGrantedAuthority>()
                for (role in token!!.roles) {
                    for (permission in role.roles) {
                        authoritiesList.add(SimpleGrantedAuthority("${role.roleName}.$permission"))
                    }
                }
                return authoritiesList
            }

            override fun getPassword(): String {
                return ""
            }

            override fun getUsername(): String {
                return username
            }

            override fun isAccountNonExpired(): Boolean {
                return true
            }

            override fun isAccountNonLocked(): Boolean {
                return true
            }

            override fun isCredentialsNonExpired(): Boolean {
                return true
            }

            override fun isEnabled(): Boolean {
                return true
            }
        }
    }
}
