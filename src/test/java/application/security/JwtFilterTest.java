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
import static org.mockito.Mockito.when;

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