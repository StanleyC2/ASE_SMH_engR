package application.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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
   * @param request HTTP request
   * @param response HTTP response
   * @param filterChain filter chain
   * @throws ServletException servlet exception
   * @throws IOException IO exception
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);

      if (jwtService.validateToken(token)) {
        // You can implement authentication here using SecurityContext
      }
    }

    filterChain.doFilter(request, response);
  }
}
