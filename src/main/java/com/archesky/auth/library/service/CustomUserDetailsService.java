package com.archesky.auth.library.service;

import com.archesky.auth.library.model.Role;
import com.archesky.auth.library.model.Token;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Component
public class CustomUserDetailsService implements UserDetailsService {
    private final TokenMappingService tokenMappingService;

    public CustomUserDetailsService(final TokenMappingService tokenMappingService) {
        this.tokenMappingService = tokenMappingService;
    }

    public ArrayList<SimpleGrantedAuthority> getAuthoritiesByUsername(final String username) throws UsernameNotFoundException {
        final Map<String, Token> userTokenMap = tokenMappingService.getUserTokenMap();
        final Token token = userTokenMap.get(username);
        final ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (final Role role : token.getRoles()) {
            for (final String permission : role.getRoles()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName() + "_" + permission));
            }
        }
        return authorities;
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        return new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return getAuthoritiesByUsername(username);
            }

            @Override
            public String getPassword() {
                return "";
            }

            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public boolean isAccountNonExpired() {
                return true;
            }

            @Override
            public boolean isAccountNonLocked() {
                return true;
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }
        };
    }
}
