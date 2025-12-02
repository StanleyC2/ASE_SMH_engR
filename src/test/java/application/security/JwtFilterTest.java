package application.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.Handler;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;


    @Mock
    private JwtService jwtService;

    @InjectMocks
    private JwtFilter jwtFilter;

    private TestLogHandler logHandler;

    @BeforeEach
    void setUp() {
        // Attach our custom handler to capture logs
        Logger logger = Logger.getLogger(JwtFilter.class.getName());
        logHandler = new TestLogHandler();
        logger.addHandler(logHandler);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogging_GetRequests() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/test"));

        // Execute
        jwtFilter.doFilterInternal(request, response, chain);

        // Verify the log contains the expected text
        boolean containsLog = logHandler.records.stream()
                .anyMatch(r -> r.getMessage().contains("Logged New API Request: GET http://localhost/test"));

        assertTrue(containsLog);
    }

    @Test
    void testLogging_PostRequests() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/test"));

        // Execute
        jwtFilter.doFilterInternal(request, response, chain);

        // Verify the log contains the expected text
        boolean containsLog = logHandler.records.stream()
                .anyMatch(r -> r.getMessage().contains("Logged New API Request: POST " +
                        "http://localhost/test"));

        assertTrue(containsLog);
    }

    @Test
    void testFilterWithValidToken() throws ServletException, IOException {
        String validToken = "valid.jwt.token";
        String authHeader = "Bearer " + validToken;
        
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/protected"));
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.validateToken(validToken)).thenReturn(true);
        when(jwtService.extractUsername(validToken)).thenReturn("user@example.com");
        when(jwtService.extractUserId(validToken)).thenReturn("user1234");

        // Execute
        jwtFilter.doFilterInternal(request, response, chain);

        // Verify token was validated
        verify(jwtService, times(1)).validateToken(validToken);
        verify(jwtService, times(1)).extractUsername(validToken);
        verify(jwtService, times(1)).extractUserId(validToken);
        verify(request, times(1)).setAttribute("userId", "user1234");
    }

    @Test
    void testFilterWithInvalidToken() throws ServletException, IOException {
        String invalidToken = "invalid.jwt.token";
        String authHeader = "Bearer " + invalidToken;
        
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/protected"));
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.validateToken(invalidToken)).thenReturn(false);

        // Execute
        jwtFilter.doFilterInternal(request, response, chain);

        // Verify token was validated but user info was not extracted
        verify(jwtService, times(1)).validateToken(invalidToken);
        verify(jwtService, never()).extractUsername(anyString());
        verify(jwtService, never()).extractUserId(anyString());
        verify(request, never()).setAttribute(eq("userId"), anyString());
    }

    @Test
    void testFilterWithNoAuthorizationHeader() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/public"));
        when(request.getHeader("Authorization")).thenReturn(null);

        // Execute
        jwtFilter.doFilterInternal(request, response, chain);

        // Verify no JWT processing occurred
        verify(jwtService, never()).validateToken(anyString());
        verify(jwtService, never()).extractUsername(anyString());
        verify(jwtService, never()).extractUserId(anyString());
    }

    @Test
    void testFilterWithMalformedAuthorizationHeader() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/protected"));
        when(request.getHeader("Authorization")).thenReturn("Basic someOtherAuth");

        // Execute
        jwtFilter.doFilterInternal(request, response, chain);

        // Verify no JWT processing occurred (header doesn't start with "Bearer ")
        verify(jwtService, never()).validateToken(anyString());
        verify(jwtService, never()).extractUsername(anyString());
        verify(jwtService, never()).extractUserId(anyString());
    }

    @Test
    void testFilterWithEmptyBearerToken() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/protected"));
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        // Execute
        jwtFilter.doFilterInternal(request, response, chain);

        // Verify validation attempted with empty string
        verify(jwtService, times(1)).validateToken("");
    }

    @Test
    void testFilterChainAlwaysContinues() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/test"));
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void testFilterChainContinuesAfterValidToken() throws ServletException, IOException {
        String validToken = "valid.token";
        String authHeader = "Bearer " + validToken;
        
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/resource"));
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.validateToken(validToken)).thenReturn(true);
        when(jwtService.extractUsername(validToken)).thenReturn("test@example.com");
        when(jwtService.extractUserId(validToken)).thenReturn("testuser123");

        jwtFilter.doFilterInternal(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void testFilterChainContinuesAfterInvalidToken() throws ServletException, IOException {
        String invalidToken = "bad.token";
        String authHeader = "Bearer " + invalidToken;
        
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/resource"));
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.validateToken(invalidToken)).thenReturn(false);

        jwtFilter.doFilterInternal(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    void testFilterWithBearerPrefixCaseSensitive() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/test"));
        when(request.getHeader("Authorization")).thenReturn("bearer token"); // lowercase

        jwtFilter.doFilterInternal(request, response, chain);

        verify(jwtService, never()).validateToken(anyString());
    }

    @Test
    void testFilterWithBearerTokenContainingSpaces() throws ServletException, IOException {
        String token = "token.with.dots";
        String authHeader = "Bearer " + token;
        
        when(request.getMethod()).thenReturn("PATCH");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/update"));
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn("user@test.com");
        when(jwtService.extractUserId(token)).thenReturn("user999");

        jwtFilter.doFilterInternal(request, response, chain);

        verify(jwtService, times(1)).validateToken(token);
        verify(request, times(1)).setAttribute("userId", "user999");
    }

    @Test
    void testFilterExtractsAndStoresUserIdCorrectly() throws ServletException, IOException {
        String token = "my.jwt.token";
        String authHeader = "Bearer " + token;
        String expectedEmail = "john.doe@example.com";
        String expectedUserId = "johndoe456";
        
        when(request.getMethod()).thenReturn("PUT");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/profile"));
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn(expectedEmail);
        when(jwtService.extractUserId(token)).thenReturn(expectedUserId);

        jwtFilter.doFilterInternal(request, response, chain);

        verify(jwtService, times(1)).extractUsername(token);
        verify(jwtService, times(1)).extractUserId(token);
        verify(request, times(1)).setAttribute("userId", expectedUserId);
        verify(chain, times(1)).doFilter(request, response);
    }

    // --- Custom log handler to capture logs ---
    static class TestLogHandler extends Handler {
        List<LogRecord> records = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {}

        @Override
        public void close() throws SecurityException {}
    }

}