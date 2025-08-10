//package io.bank.mortgage.service.impl;
//
//import io.bank.mortgage.domain.model.User;
//import io.bank.mortgage.repo.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import java.util.Collection;
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class CustomUserDetailsServiceTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @InjectMocks
//    private CustomUserDetailsService userDetailsService;
//
//    private User user;
//    private String nationalId;
//
//    @BeforeEach
//    void setUp() {
//        nationalId = "123456789";
//        user = User.builder()
//                .id(1L)
//                .nationalId(nationalId)
//                .passwordHash("hashedPassword")
//                .roles(Set.of("USER", "ADMIN"))
//                .build();
//    }
//
//    @Test
//    void findByUsername_UserExists_ReturnsUserDetails() {
//        // Arrange
//        when(userRepository.findByNationalIdWithRoles(nationalId)).thenReturn(Mono.just(user));
//
//        // Act & Assert
//        StepVerifier.create(userDetailsService.findByUsername(nationalId))
//                .assertNext(userDetails -> {
//                    assertNotNull(userDetails);
//                    assertEquals(nationalId, userDetails.getUsername());
//                    assertEquals("hashedPassword", userDetails.getPassword());
//
//                    // Verify roles are correctly mapped with ROLE_ prefix
//                    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
//                    assertEquals(2, authorities.size());
//                    assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
//                    assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
//
//                    // Verify CustomUserDetails specific properties
//                    assertTrue(userDetails instanceof CustomUserDetails);
//                    assertEquals(1L, ((CustomUserDetails) userDetails).getUserId());
//                })
//                .verifyComplete();
//
//        verify(userRepository).findByNationalIdWithRoles(nationalId);
//    }
//
//    @Test
//    void findByUsername_UserDoesNotExist_ReturnsEmptyMono() {
//        // Arrange
//        String nonExistentNationalId = "nonexistent";
//        when(userRepository.findByNationalIdWithRoles(nonExistentNationalId)).thenReturn(Mono.empty());
//
//        // Act & Assert
//        StepVerifier.create(userDetailsService.findByUsername(nonExistentNationalId))
//                .expectNextCount(0)
//                .verifyComplete();
//
//        verify(userRepository).findByNationalIdWithRoles(nonExistentNationalId);
//    }
//
//    @Test
//    void findByUsername_WithRoleAlreadyPrefixed_DoesNotDuplicatePrefix() {
//        // Arrange
//        User userWithPrefixedRole = User.builder()
//                .id(1L)
//                .nationalId(nationalId)
//                .passwordHash("hashedPassword")
//                .roles(Set.of("ROLE_MANAGER", "ADMIN"))
//                .build();
//
//        when(userRepository.findByNationalIdWithRoles(nationalId)).thenReturn(Mono.just(userWithPrefixedRole));
//
//        // Act & Assert
//        StepVerifier.create(userDetailsService.findByUsername(nationalId))
//                .assertNext(userDetails -> {
//                    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
//                    assertEquals(2, authorities.size());
//                    assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_MANAGER")));
//                    assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
//
//                    // Verify no duplicate ROLE_ prefix
//                    assertFalse(authorities.contains(new SimpleGrantedAuthority("ROLE_ROLE_MANAGER")));
//                })
//                .verifyComplete();
//
//        verify(userRepository).findByNationalIdWithRoles(nationalId);
//    }
//}