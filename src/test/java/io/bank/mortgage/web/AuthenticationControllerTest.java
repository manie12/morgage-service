//package io.bank.mortgage.web;
//
//import io.bank.mortgage.config.JwtTokenProvider;
//import io.bank.mortgage.domain.model.RefreshToken;
//import io.bank.mortgage.domain.model.User;
//import io.bank.mortgage.dto.NewApplicationCreateRequest;
//import io.bank.mortgage.repo.Impl.RefreshTokenRepositoryImpl;
//import io.bank.mortgage.repo.Impl.UserRepositoryImpl;
//import io.bank.mortgage.service.impl.CustomUserDetails;
//import io.bank.mortgage.web.AuthenticationController.LoginRequest;
//import io.bank.mortgage.web.AuthenticationController.RefreshRequest;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.dao.DuplicateKeyException;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.ReactiveAuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import java.time.OffsetDateTime;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class AuthenticationControllerTest {
//
//    @Mock
//    private ReactiveAuthenticationManager authenticationManager;
//
//    @Mock
//    private JwtTokenProvider tokenProvider;
//
//    @Mock
//    private RefreshTokenRepositoryImpl refreshTokenRepository;
//
//    @Mock
//    private UserRepositoryImpl userRepository;
//
//    @Mock
//    private UserRepositoryImpl userService;
//
//    @InjectMocks
//    private AuthenticationController authenticationController;
//
//    private NewApplicationCreateRequest registrationRequest;
//    private LoginRequest loginRequest;
//    private RefreshRequest refreshRequest;
//    private User user;
//    private CustomUserDetails userDetails;
//    private RefreshToken refreshToken;
//    private String accessTokenValue;
//    private String refreshTokenValue;
//
//    @BeforeEach
//    void setUp() {
//        // Setup registration request
//        registrationRequest = new NewApplicationCreateRequest();
//        registrationRequest.setNationalId("123456789");
//        registrationRequest.setExternalRef("ext123");
//
//        // Setup login request
//        loginRequest = new LoginRequest("123456789", "password");
//
//        // Setup refresh request
//        refreshTokenValue = UUID.randomUUID().toString();
//        refreshRequest = new RefreshRequest(refreshTokenValue);
//
//        // Setup user
//        user = User.builder()
//                .id(1L)
//                .nationalId("123456789")
//                .passwordHash("hashedPassword")
//                .roles(Set.of("ROLE_USER"))
//                .build();
//
//        // Setup user details
//        userDetails = new CustomUserDetails(
//                1L,
//                "123456789",
//                "hashedPassword",
//                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
//        );
//
//        // Setup refresh token
//        refreshToken = RefreshToken.builder()
//                .token(UUID.fromString(refreshTokenValue))
//                .userId(1L)
//                .expiresAt(OffsetDateTime.now().plusDays(7))
//                .revoked(false)
//                .createdAt(OffsetDateTime.now())
//                .build();
//
//        // Setup token values
//        accessTokenValue = "access-token-value";
//    }
//
//    @Test
//    void register_NewUser_Success() {
//        // Arrange
//        when(userRepository.findByNationalIdWithRoles(registrationRequest.getNationalId()))
//                .thenReturn(Mono.empty());
//        when(userService.insertUser(registrationRequest))
//                .thenReturn(Mono.just(user));
//
//        // Act & Assert
//        StepVerifier.create(authenticationController.register(registrationRequest))
//                .assertNext(responseEntity -> {
//                    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//                    assertEquals("User registered successfully", responseEntity.getBody().get("message"));
//                })
//                .verifyComplete();
//
//        verify(userRepository).findByNationalIdWithRoles(registrationRequest.getNationalId());
//        verify(userService).insertUser(registrationRequest);
//    }
//
//    @Test
//    void register_ExistingUser_ReturnsConflict() {
//        // Arrange
//        when(userRepository.findByNationalIdWithRoles(registrationRequest.getNationalId()))
//                .thenReturn(Mono.just(user));
//
//        // Act & Assert
//        StepVerifier.create(authenticationController.register(registrationRequest))
//                .assertNext(responseEntity -> {
//                    assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
//                    assertEquals("National ID already exists", responseEntity.getBody().get("error"));
//                })
//                .verifyComplete();
//
//        verify(userRepository).findByNationalIdWithRoles(registrationRequest.getNationalId());
//        verify(userService, never()).insertUser(any());
//    }
//
//    @Test
//    void login_ValidCredentials_ReturnsTokens() {
//        // Arrange
//        Authentication authentication = new UsernamePasswordAuthenticationToken(
//                userDetails, null, userDetails.getAuthorities());
//
//        when(authenticationManager.authenticate(any(Authentication.class)))
//                .thenReturn(Mono.just(authentication));
//        when(tokenProvider.generateAccessToken(userDetails))
//                .thenReturn(accessTokenValue);
//        when(tokenProvider.generateRefreshToken())
//                .thenReturn(refreshTokenValue);
//        when(tokenProvider.getRefreshExpiration())
//                .thenReturn(604800000L); // 7 days in milliseconds
//        when(refreshTokenRepository.insert(any(RefreshToken.class)))
//                .thenReturn(Mono.just(refreshToken));
//
//        // Act & Assert
//        StepVerifier.create(authenticationController.login(loginRequest))
//                .assertNext(responseEntity -> {
//                    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//                    assertEquals(accessTokenValue, responseEntity.getBody().get("accessToken"));
//                    assertEquals(refreshTokenValue, responseEntity.getBody().get("refreshToken"));
//                })
//                .verifyComplete();
//
//        verify(authenticationManager).authenticate(any(Authentication.class));
//        verify(tokenProvider).generateAccessToken(userDetails);
//        verify(tokenProvider).generateRefreshToken();
//        verify(refreshTokenRepository).insert(any(RefreshToken.class));
//    }
//
//    @Test
//    void refresh_ValidToken_ReturnsNewTokens() {
//        // Arrange
//        String newAccessToken = "new-access-token";
//        String newRefreshToken = UUID.randomUUID().toString();
//
//        when(refreshTokenRepository.findById(UUID.fromString(refreshTokenValue)))
//                .thenReturn(Mono.just(refreshToken));
//        when(refreshTokenRepository.revokeToken(refreshToken.getToken()))
//                .thenReturn(Mono.empty());
//        when(userRepository.findById(refreshToken.getUserId()))
//                .thenReturn(Mono.just(user));
//        when(userRepository.findByNationalIdWithRoles(user.getNationalId()))
//                .thenReturn(Mono.just(user));
//        when(tokenProvider.generateAccessToken(any(CustomUserDetails.class)))
//                .thenReturn(newAccessToken);
//        when(tokenProvider.generateRefreshToken())
//                .thenReturn(newRefreshToken);
//        when(tokenProvider.getRefreshExpiration())
//                .thenReturn(604800000L); // 7 days in milliseconds
//        when(refreshTokenRepository.insert(any(RefreshToken.class)))
//                .thenReturn(Mono.just(RefreshToken.builder().token(UUID.fromString(newRefreshToken)).build()));
//
//        // Act & Assert
//        StepVerifier.create(authenticationController.refresh(refreshRequest))
//                .assertNext(responseEntity -> {
//                    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//                    assertEquals(newAccessToken, responseEntity.getBody().get("accessToken"));
//                    assertEquals(newRefreshToken, responseEntity.getBody().get("refreshToken"));
//                })
//                .verifyComplete();
//
//        verify(refreshTokenRepository).findById(UUID.fromString(refreshTokenValue));
//        verify(refreshTokenRepository).revokeToken(refreshToken.getToken());
//        verify(userRepository).findById(refreshToken.getUserId());
//        verify(userRepository).findByNationalIdWithRoles(user.getNationalId());
//        verify(tokenProvider).generateAccessToken(any(CustomUserDetails.class));
//        verify(tokenProvider).generateRefreshToken();
//        verify(refreshTokenRepository).insert(any(RefreshToken.class));
//    }
//
//    @Test
//    void refresh_InvalidToken_ReturnsUnauthorized() {
//        // Arrange
//        when(refreshTokenRepository.findById(UUID.fromString(refreshTokenValue)))
//                .thenReturn(Mono.empty());
//
//        // Act & Assert
//        StepVerifier.create(authenticationController.refresh(refreshRequest))
//                .assertNext(responseEntity -> {
//                    assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
//                    assertNull(responseEntity.getBody());
//                })
//                .verifyComplete();
//
//        verify(refreshTokenRepository).findById(UUID.fromString(refreshTokenValue));
//        verify(refreshTokenRepository, never()).revokeToken(any());
//        verify(userRepository, never()).findById(anyLong());
//        verify(userRepository, never()).findByNationalIdWithRoles(anyString());
//        verify(tokenProvider, never()).generateAccessToken(any());
//        verify(tokenProvider, never()).generateRefreshToken();
//        verify(refreshTokenRepository, never()).insert(any());
//    }
//
//    @Test
//    void refresh_ExpiredToken_ReturnsUnauthorized() {
//        // Arrange
//        RefreshToken expiredToken = RefreshToken.builder()
//                .token(UUID.fromString(refreshTokenValue))
//                .userId(1L)
//                .expiresAt(OffsetDateTime.now().minusDays(1))
//                .revoked(false)
//                .createdAt(OffsetDateTime.now().minusDays(8))
//                .build();
//
//        when(refreshTokenRepository.findById(UUID.fromString(refreshTokenValue)))
//                .thenReturn(Mono.just(expiredToken));
//
//        // Act & Assert
//        StepVerifier.create(authenticationController.refresh(refreshRequest))
//                .assertNext(responseEntity -> {
//                    assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
//                    assertNull(responseEntity.getBody());
//                })
//                .verifyComplete();
//
//        verify(refreshTokenRepository).findById(UUID.fromString(refreshTokenValue));
//        verify(refreshTokenRepository, never()).revokeToken(any());
//        verify(userRepository, never()).findById(anyLong());
//        verify(userRepository, never()).findByNationalIdWithRoles(anyString());
//        verify(tokenProvider, never()).generateAccessToken(any());
//        verify(tokenProvider, never()).generateRefreshToken();
//        verify(refreshTokenRepository, never()).insert(any());
//    }
//
//    @Test
//    void refresh_RevokedToken_ReturnsUnauthorized() {
//        // Arrange
//        RefreshToken revokedToken = RefreshToken.builder()
//                .token(UUID.fromString(refreshTokenValue))
//                .userId(1L)
//                .expiresAt(OffsetDateTime.now().plusDays(7))
//                .revoked(true)
//                .createdAt(OffsetDateTime.now())
//                .build();
//
//        when(refreshTokenRepository.findById(UUID.fromString(refreshTokenValue)))
//                .thenReturn(Mono.just(revokedToken));
//
//        // Act & Assert
//        StepVerifier.create(authenticationController.refresh(refreshRequest))
//                .assertNext(responseEntity -> {
//                    assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
//                    assertNull(responseEntity.getBody());
//                })
//                .verifyComplete();
//
//        verify(refreshTokenRepository).findById(UUID.fromString(refreshTokenValue));
//        verify(refreshTokenRepository, never()).revokeToken(any());
//        verify(userRepository, never()).findById(anyLong());
//        verify(userRepository, never()).findByNationalIdWithRoles(anyString());
//        verify(tokenProvider, never()).generateAccessToken(any());
//        verify(tokenProvider, never()).generateRefreshToken();
//        verify(refreshTokenRepository, never()).insert(any());
//    }
//}