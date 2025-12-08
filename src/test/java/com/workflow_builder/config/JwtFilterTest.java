package com.workflow_builder.config;

import com.workflow_builder.auth.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class JwtFilterTest {

    @Test
    void testDoFilterInternal_callsValidate() throws Exception {

        // ✅ Mock JwtService
        JwtService jwtService = mock(JwtService.class);

        // ✅ Mock Claims
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user123");
        when(claims.get("role", String.class)).thenReturn("USER");

        // ✅ Mock Jws<Claims>
        @SuppressWarnings("unchecked")
        Jws<Claims> jws = mock(Jws.class);
        when(jws.getBody()).thenReturn(claims);

        // ✅ Stub validate() to return mocked JWS
        when(jwtService.validate("XYZ")).thenReturn(jws);

        JwtFilter filter = new JwtFilter(jwtService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer XYZ");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, (req, res) -> {});

        // ✅ Verification
        verify(jwtService, times(1)).validate("XYZ");
    }
}
