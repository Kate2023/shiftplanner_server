package com.example.shiftplanner_server;

import com.example.shiftplanner_server.entities.Token;
import com.example.shiftplanner_server.services.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationFilterTest {

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        request.setRequestURI(path);
        return request;
    }

    @Test
    void optionsRequestIsSkippedByFilter() throws Exception {
        MockHttpServletRequest request = request("OPTIONS", "/api/staff");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        tokenAuthenticationFilter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest());
        verifyNoInteractions(tokenService);
    }

    @Test
    void nonApiRequestIsSkippedByFilter() throws Exception {
        MockHttpServletRequest request = request("GET", "/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        tokenAuthenticationFilter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest());
        verifyNoInteractions(tokenService);
    }

    @Test
    void loginRequestIsSkippedByFilter() throws Exception {
        MockHttpServletRequest request = request("POST", "/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        tokenAuthenticationFilter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest());
        verifyNoInteractions(tokenService);
    }

    @Test
    void missingAuthorizationHeaderReturnsUnauthorized() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/staff");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        tokenAuthenticationFilter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
        assertEquals("{\"reason\":\"Missing or invalid Authorization header\"}", response.getContentAsString());
        assertNull(chain.getRequest());
        verifyNoInteractions(tokenService);
    }

    @Test
    void unknownTokenReturnsUnauthorized() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/staff");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(tokenService.getByToken("invalid-token")).thenReturn(Optional.empty());

        tokenAuthenticationFilter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertEquals("{\"reason\":\"Invalid token\"}", response.getContentAsString());
        assertNull(chain.getRequest());
        verify(tokenService).getByToken("invalid-token");
    }

    @Test
    void validTokenInBearerHeaderContinuesFilterChain() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/staff");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(tokenService.getByToken("valid-token")).thenReturn(Optional.of(new Token()));

        tokenAuthenticationFilter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest());
        verify(tokenService).getByToken("valid-token");
    }

    @Test
    void quotedBearerTokenIsNormalizedAndValidated() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/staff");
        request.addHeader(HttpHeaders.AUTHORIZATION, "\"Bearer valid-token\"");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(tokenService.getByToken("valid-token")).thenReturn(Optional.of(new Token()));

        tokenAuthenticationFilter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest());
        verify(tokenService).getByToken("valid-token");
    }

    @Test
    void rawTokenWithoutBearerPrefixIsAccepted() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/staff");
        request.addHeader(HttpHeaders.AUTHORIZATION, "raw-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(tokenService.getByToken("raw-token")).thenReturn(Optional.of(new Token()));

        tokenAuthenticationFilter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest());
        verify(tokenService).getByToken("raw-token");
    }
}

