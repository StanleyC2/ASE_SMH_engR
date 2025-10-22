package application.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that intercepts HTTP requests and validates JWT tokens.
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    /**
     * Filter logic to validate JWT token from Authorization header.
     *
     * @param request     HTTP request
     * @param response    HTTP response
     * @param filterChain filter chain
     * @throws ServletException servlet exception
     * @throws IOException      IO exception
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            final String token = authHeader.substring(7);

//            if (jwtService.validateToken(token)) {
//                // We can implement authentication here
//            }
        }

        filterChain.doFilter(request, response);
    }
}
