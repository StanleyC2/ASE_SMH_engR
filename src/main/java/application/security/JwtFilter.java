package application.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        logger.info("Logged New API Request: " + request.getMethod() + " " + request.getRequestURL());

        final String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            final String token = header.substring(7);

            if (jwtService.validateToken(token)) {
                final String email = jwtService.extractUsername(token);
                final String userId = jwtService.extractUserId(token);

                // Store email as principal (username) for authentication
                // The userId is embedded in the token and can be extracted when needed
                final UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(email, null, List.of());

                SecurityContextHolder.getContext().setAuthentication(auth);
                
                // Store userId in request attribute for easy access by controllers
                request.setAttribute("userId", userId);
            }
        }

        filterChain.doFilter(request, response);
    }
}
