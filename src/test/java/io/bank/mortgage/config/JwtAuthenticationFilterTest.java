//package io.bank.mortgage.config;
//
//import io.bank.mortgage.service.impl.CustomUserDetails;
//import io.bank.mortgage.service.impl.CustomUserDetailsService;
//import io.jsonwebtoken.Claims;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpHeaders;
//import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
//import org.springframework.mock.web.server.MockServerWebExchange;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.ReactiveSecurityContextHolder;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.server.WebFilterChain;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import java.util.Collections;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class JwtAuthenticationFilterTest {
//
//    @Mock
//    private JwtTokenProvider tokenProvider;
//
//    @Mock
//    private CustomUserDetailsService userDetailsService;
//
//    @Mock
//    private WebFilterChain filterChain;
//
//    @Mock
//    private Claims claims;
//
//    @InjectMocks
//    private JwtAuthenticationFilter jwtAuthenticationFilter;
//
//    private ServerWebExchange exchange;
//    private CustomUserDetails userDetails;
//    private final String validToken = "valid.jwt.token";
//    private final String username = "123456789";
//
//    @BeforeEach
//    void setUp() {
//        // Setup user details
//        userDetails = new CustomUserDetails(
//                1L,
//                username,
//                "hashedPassword",
//                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
//        );
//
//        // Setup claims
//        when(claims.getSubject()).thenReturn(username);
//
//        // Setup filter chain to capture the authentication
//        when(filterChain.filter(any(ServerWebExchange.class)))
//                .thenReturn(Mono.empty());
//    }
//
//    @Test
//    void filter_WithValidToken_SetsAuthentication() {
//        // Arrange
//        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
//                .build();
//        exchange = MockServerWebExchange.from(request);
//
//        when(tokenProvider.validateAndParseClaims(validToken)).thenReturn(Mono.just(claims));
//        when(userDetailsService.findByUsername(username)).thenReturn(Mono.just(userDetails));
//
//        // Act & Assert
//        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
//                .verifyComplete();
//
//        verify(tokenProvider).validateAndParseClaims(validToken);
//        verify(userDetailsService).findByUsername(username);
//        verify(filterChain).filter(any(ServerWebExchange.class));
//    }
//
//    @Test
//    void filter_WithNoAuthHeader_ContinuesChain() {
//        // Arrange
//        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
//                .build();
//        exchange = MockServerWebExchange.from(request);
//
//        // Act & Assert
//        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
//                .verifyComplete();
//
//        verify(tokenProvider, never()).validateAndParseClaims(anyString());
//        verify(userDetailsService, never()).findByUsername(anyString());
//        verify(filterChain).filter(exchange);
//    }
//
//    @Test
//    void filter_WithInvalidAuthHeaderFormat_ContinuesChain() {
//        // Arrange
//        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
//                .header(HttpHeaders.AUTHORIZATION, "InvalidFormat " + validToken)
//                .build();
//        exchange = MockServerWebExchange.from(request);
//
//        // Act & Assert
//        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
//                .verifyComplete();
//
//        verify(tokenProvider, never()).validateAndParseClaims(anyString());
//        verify(userDetailsService, never()).findByUsername(anyString());
//        verify(filterChain).filter(exchange);
//    }
//
//    @Test
//    void filter_WithInvalidToken_ContinuesChain() {
//        // Arrange
//        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
//                .build();
//        exchange = MockServerWebExchange.from(request);
//
//        when(tokenProvider.validateAndParseClaims(validToken)).thenReturn(Mono.empty());
//
//        // Act & Assert
//        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
//                .verifyComplete();
//
//        verify(tokenProvider).validateAndParseClaims(validToken);
//        verify(userDetailsService, never()).findByUsername(anyString());
//        verify(filterChain).filter(exchange);
//    }
//
//    @Test
//    void filter_WithValidTokenButUserNotFound_ContinuesChain() {
//        // Arrange
//        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
//                .build();
//        exchange = MockServerWebExchange.from(request);
//
//        when(tokenProvider.validateAndParseClaims(validToken)).thenReturn(Mono.just(claims));
//        when(userDetailsService.findByUsername(username)).thenReturn(Mono.empty());
//
//        // Act & Assert
//        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, filterChain))
//                .verifyComplete();
//
//        verify(tokenProvider).validateAndParseClaims(validToken);
//        verify(userDetailsService).findByUsername(username);
//        verify(filterChain).filter(exchange);
//    }
//}