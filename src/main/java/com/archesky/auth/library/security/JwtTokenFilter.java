package com.archesky.auth.library.security;

import com.archesky.auth.library.model.Token;
import com.archesky.auth.library.service.CustomUserDetailsService;
import com.archesky.auth.library.service.TokenMappingService;
import com.archesky.auth.library.service.TokenService;
import org.slf4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.archesky.auth.library.util.GetHost.getHost;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Component
class JwtTokenFilter extends OncePerRequestFilter {
    private final Logger log = getLogger(this.getClass());
    private final CustomUserDetailsService userDetailsService;
    private final TokenMappingService tokenMappingService;
    private final Environment env;
    private final TokenService tokenService;

    JwtTokenFilter(final CustomUserDetailsService userDetailsService, final TokenMappingService tokenMappingService,
                   final Environment env, final TokenService tokenService) {
        this.userDetailsService = userDetailsService;
        this.tokenMappingService = tokenMappingService;
        this.env = env;
        this.tokenService = tokenService;
    }

    private String resolveToken(final HttpServletRequest request) {
        final String bearerToken = request.getHeader("Authorization");
        log.info("Authorization header for request: {}", bearerToken);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Authentication getAuthentication(final String token, final String hostName){
        log.info("Getting authentication for token {} and hostname {}", token, hostName);
        try {
            final String authURL = env.getProperty(
                    "archesky.auth.library.server.url",
                    "https://localhost:9090/auth-server/auth"
            );
            log.info("Auth server url: {}", authURL);
            final Token validateToken = tokenService.validateToken(authURL, token, hostName);
            tokenMappingService.getUserTokenMap().put(validateToken.getUsername(), validateToken);
            final UserDetails userDetails = this.userDetailsService.loadUserByUsername(validateToken.getUsername());
            return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        } catch (final Exception e) {
           return null;
        }
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        final String token = resolveToken(request);
        if (token != null) {
            final Authentication auth = getAuthentication(token, getHost(request));
            if (auth == null) {
                log.warn("Invalid token");
            } else {
                log.info("Token accepted");
            }
            getContext().setAuthentication(auth);
        } else {
            log.warn("No auth token");
        }
        filterChain.doFilter(request, response);
    }
}