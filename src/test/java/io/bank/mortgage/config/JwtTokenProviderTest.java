//package io.bank.mortgage.config;
//
//import io.bank.mortgage.service.impl.CustomUserDetails;
//import io.jsonwebtoken.Claims;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.test.util.ReflectionTestUtils;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@ExtendWith(MockitoExtension.class)
//class JwtTokenProviderTest {
//
//    @InjectMocks
//    private JwtTokenProvider tokenProvider;
//
//    private CustomUserDetails userDetails;
//    private final String jwtSecret = "thisIsATestSecretKeyThatIsLongEnoughForHS256Algorithm";
//    private final long accessExpiration = 3600; // 1 hour
//    private final long refreshExpiration = 86400; // 24 hours
//
//    @BeforeEach
//    void setUp() {
//        // Set up the token provider with test values
//        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", jwtSecret);
//        ReflectionTestUtils.setField(tokenProvider, "accessExpiration", accessExpiration);
//        ReflectionTestUtils.setField(tokenProvider, "refreshExpiration", refreshExpiration);
//
//        // Set up user details
//        userDetails = new CustomUserDetails(
//                1L,
//                "123456789",
//                "hashedPassword",
//                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
//        );
//    }
//
//    @Test
//    void generateAccessToken_ValidUser_ReturnsToken() {
//        // Act
//        String token = tokenProvider.generateAccessToken(userDetails);
//
//        // Assert
//        assertNotNull(token);
//        assertTrue(token.length() > 0);
//    }
//
//    @Test
//    void generateRefreshToken_ReturnsUUID() {
//        // Act
//        String refreshToken = tokenProvider.generateRefreshToken();
//
//        // Assert
//        assertNotNull(refreshToken);
//        // Verify it's a valid UUID
//        assertDoesNotThrow(() -> UUID.fromString(refreshToken));
//    }
//
//    @Test
//    void validateAndParseClaims_ValidToken_ReturnsClaims() {
//        // Arrange
//        String token = tokenProvider.generateAccessToken(userDetails);
//
//        // Act & Assert
//        StepVerifier.create(tokenProvider.validateAndParseClaims(token))
//                .assertNext(claims -> {
//                    assertEquals("123456789", claims.getSubject());
//                    assertEquals(1L, ((Number) claims.get("userId")).longValue());
//
//                    @SuppressWarnings("unchecked")
//                    List<String> roles = (List<String>) claims.get("roles");
//                    assertEquals(1, roles.size());
//                    assertEquals("ROLE_USER", roles.get(0));
//                })
//                .verifyComplete();
//    }
//
//    @Test
//    void validateAndParseClaims_InvalidToken_ReturnsEmptyMono() {
//        // Arrange
//        String invalidToken = "invalid.token.string";
//
//        // Act & Assert
//        StepVerifier.create(tokenProvider.validateAndParseClaims(invalidToken))
//                .verifyComplete(); // Should complete without emitting any value
//    }
//
//    @Test
//    void validateAndParseClaims_ExpiredToken_ReturnsEmptyMono() {
//        // Arrange
//        // Set a very short expiration for this test
//        ReflectionTestUtils.setField(tokenProvider, "accessExpiration", 0L); // Expire immediately
//        String expiredToken = tokenProvider.generateAccessToken(userDetails);
//
//        // Reset the expiration for other tests
//        ReflectionTestUtils.setField(tokenProvider, "accessExpiration", accessExpiration);
//
//        // Act & Assert
//        StepVerifier.create(tokenProvider.validateAndParseClaims(expiredToken))
//                .verifyComplete(); // Should complete without emitting any value
//    }
//
//    @Test
//    void getRefreshExpiration_ReturnsConfiguredValue() {
//        // Act
//        long expiration = tokenProvider.getRefreshExpiration();
//
//        // Assert
//        assertEquals(refreshExpiration, expiration);
//    }
//}