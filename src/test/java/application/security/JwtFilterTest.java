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