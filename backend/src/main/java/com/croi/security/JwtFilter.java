package com.croi.security;
import com.croi.common.constants.ApiConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);
        if (token == null) {
            if (request.getHeader(ApiConstants.AUTH_HEADER) != null) {
                // A header was sent but didn't parse as "Bearer <token>" — worth knowing,
                // since this looks identical to "no token" further down the chain.
                log.warn("{} {}: Authorization header present but not in 'Bearer <token>' form", request.getMethod(), request.getRequestURI());
            }
        } else if (!jwtProvider.validateToken(token)) {
            // The specific reason (expired / bad signature / malformed) is logged inside
            // JwtProvider.validateToken — this just ties it to the request that hit it.
            log.warn("{} {}: rejected (see JWT validation warning above)", request.getMethod(), request.getRequestURI());
        } else if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String email = jwtProvider.getEmailFromToken(token);
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
                // A validly-signed token for a user that no longer exists (e.g. DB was reset
                // while an old token was still in localStorage). Without this catch, this
                // throws out of the filter uncaught — a confusing 500, not the 403 you'd expect.
                log.warn("{} {}: token is valid but no user exists for email {} (stale token from a reset/deleted account?)",
                        request.getMethod(), request.getRequestURI(), email);
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(ApiConstants.AUTH_HEADER);
        if (header != null && header.startsWith(ApiConstants.BEARER_PREFIX)) {
            return header.substring(ApiConstants.BEARER_PREFIX.length());
        }
        return null;
    }
}
