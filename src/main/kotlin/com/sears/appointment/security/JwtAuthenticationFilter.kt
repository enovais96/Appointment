package com.sears.appointment.security

import com.sears.appointment.repositories.UserRepository
import com.sears.appointment.utils.JwtUtils
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtUtils: JwtUtils,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 1. Look for the Authorization header
        val authHeader = request.getHeader("Authorization")
        
        // If no Authorization header or doesn't start with "Bearer ", continue to the next filter
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }
        
        // 2. Extract the token (remove "Bearer " prefix)
        val token = authHeader.substring(7)
        
        try {
            // 3. Check if the token is valid
            if (jwtUtils.isTokenExpired(token)) {
                // Token has expired
                filterChain.doFilter(request, response)
                return
            }
            
            // 4. Check if the token is of type "access" and not "refresh"
            if (!jwtUtils.isAccessToken(token)) {
                // Token is not an access token
                filterChain.doFilter(request, response)
                return
            }
            
            // 5. Extract the userId from the token
            val userId = jwtUtils.getUserIdFromToken(token)
            
            // 6. Verify if a user with this ID exists
            val user = userRepository.findById(userId).orElse(null)
            if (user == null) {
                // User not found, token is invalid
                filterChain.doFilter(request, response)
                return
            }
            
            // 7. Store the user ID in the SecurityContextHolder
            val authorities = listOf(SimpleGrantedAuthority("USER"))
            val authentication = UsernamePasswordAuthenticationToken(
                userId, null, authorities
            )
            SecurityContextHolder.getContext().authentication = authentication
            
        } catch (e: Exception) {
            // Token is invalid
            logger.error("Error validating JWT token: ${e.message}")
        }
        
        // Continue the filter chain
        filterChain.doFilter(request, response)
    }
} 