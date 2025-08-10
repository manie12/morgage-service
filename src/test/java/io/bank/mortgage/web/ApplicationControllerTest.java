//package io.bank.mortgage.web;
//
//import io.bank.mortgage.datatype.DecisionType;
//import io.bank.mortgage.datatype.Status;
//import io.bank.mortgage.domain.model.Application;
//import io.bank.mortgage.dto.DecisionRequest;
//import io.bank.mortgage.dto.NewApplicationCreateRequest;
//import io.bank.mortgage.service.ApplicationService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.Collections;
//import java.util.List;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ApplicationControllerTest {
//
//    @Mock
//    private ApplicationService applicationService;
//
//    @Mock
//    private Authentication authentication;
//
//    @InjectMocks
//    private ApplicationController applicationController;
//
//    private UUID applicationId;
//    private Application application;
//    private NewApplicationCreateRequest createRequest;
//    private DecisionRequest decisionRequest;
//    private String userId;
//
//    @BeforeEach
//    void setUp() {
//        applicationId = UUID.randomUUID();
//        userId = "user123";
//
//        // Setup authentication mock
//        when(authentication.getName()).thenReturn(userId);
//
//        // Setup application
//        application = Application.builder()
//                .id(applicationId)
//                .userId(userId)
//                .externalRef("ext123")
//                .loanAmount(BigDecimal.valueOf(100000))
//                .currency("USD")
//                .income(BigDecimal.valueOf(50000))
//                .liabilities(BigDecimal.valueOf(10000))
//                .propertyAddress("123 Main St")
//                .propertyValue(BigDecimal.valueOf(200000))
//                .propertyType("HOUSE")
//                .status(Status.SUBMITTED)
//                .version(1)
//                .build();
//
//        // Setup create request
//        createRequest = new NewApplicationCreateRequest();
//        createRequest.setExternalRef("ext123");
//        createRequest.setLoanAmount(BigDecimal.valueOf(100000));
//        createRequest.setCurrency("USD");
//        createRequest.setIncome(BigDecimal.valueOf(50000));
//        createRequest.setLiabilities(BigDecimal.valueOf(10000));
//        createRequest.setPropertyAddress("123 Main St");
//        createRequest.setPropertyValue(BigDecimal.valueOf(200000));
//        createRequest.setPropertyType("HOUSE");
//        createRequest.setNationalId("123456789");
//
//        // Setup decision request
//        decisionRequest = new DecisionRequest();
//        decisionRequest.setDecision(DecisionType.APPROVED);
//        decisionRequest.setComments("Approved based on good credit history");
//    }
//
//    @Test
//    void create_ValidRequest_ReturnsCreatedApplication() {
//        // Arrange
//        String idempotencyKey = "idem123";
//        when(applicationService.create(any(NewApplicationCreateRequest.class), anyString(), anyString()))
//                .thenReturn(Mono.just(application));
//
//        // Act & Assert
//        StepVerifier.create(applicationController.create(idempotencyKey, createRequest, authentication))
//                .assertNext(responseEntity -> {
//                    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
//                    assertEquals("1", responseEntity.getHeaders().getETag());
//                    assertEquals(application, responseEntity.getBody());
//                })
//                .verifyComplete();
//
//        verify(applicationService).create(createRequest, userId, idempotencyKey);
//    }
//
//    @Test
//    void get_ApplicationExists_ReturnsApplication() {
//        // Arrange
//        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());
//        when(applicationService.getById(eq(applicationId), anyString(), eq(false)))
//                .thenReturn(Mono.just(application));
//
//        // Act & Assert
//        StepVerifier.create(applicationController.get(applicationId, authentication))
//                .assertNext(responseEntity -> {
//                    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//                    assertEquals("1", responseEntity.getHeaders().getETag());
//                    assertEquals(application, responseEntity.getBody());
//                })
//                .verifyComplete();
//
//        verify(applicationService).getById(applicationId, userId, false);
//    }
//
//    @Test
//    void get_AsOfficer_ReturnsApplication() {
//        // Arrange
//        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_OFFICER"));
//        doReturn(authorities).when(authentication).getAuthorities();
//
//        when(applicationService.getById(eq(applicationId), anyString(), eq(true)))
//                .thenReturn(Mono.just(application));
//
//        // Act & Assert
//        StepVerifier.create(applicationController.get(applicationId, authentication))
//                .assertNext(responseEntity -> {
//                    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//                    assertEquals("1", responseEntity.getHeaders().getETag());
//                    assertEquals(application, responseEntity.getBody());
//                })
//                .verifyComplete();
//
//        verify(applicationService).getById(applicationId, userId, true);
//    }
//
//    @Test
//    void list_WithFilters_ReturnsApplications() {
//        // Arrange
//        Status status = Status.SUBMITTED;
//        Instant from = Instant.now().minus(java.time.Duration.ofDays(7));
//        Instant to = Instant.now();
//        String nationalId = "hashedId";
//        int page = 0;
//        int size = 10;
//
//        when(applicationService.list(eq(status), eq(from), eq(to), eq(nationalId), eq(page), eq(size)))
//                .thenReturn(Flux.just(application));
//
//        // Act & Assert
//        StepVerifier.create(applicationController.list(status, from, to, nationalId, page, size))
//                .consumeNextWith(result -> {
//                    assertEquals(application, result);
//                })
//                .verifyComplete();
//
//        verify(applicationService).list(status, from, to, nationalId, page, size);
//    }
//
//    @Test
//    void decide_ValidRequest_ReturnsUpdatedApplication() {
//        // Arrange
//        int version = 1;
//        Application updatedApplication = Application.builder()
//                .id(applicationId)
//                .userId(userId)
//                .externalRef("ext123")
//                .loanAmount(BigDecimal.valueOf(100000))
//                .currency("USD")
//                .income(BigDecimal.valueOf(50000))
//                .liabilities(BigDecimal.valueOf(10000))
//                .propertyAddress("123 Main St")
//                .propertyValue(BigDecimal.valueOf(200000))
//                .propertyType("HOUSE")
//                .status(Status.APPROVED)
//                .version(2)
//                .build();
//
//        when(applicationService.decide(eq(applicationId), any(DecisionRequest.class),
//                anyString(), anyString(), eq(version), isNull()))
//                .thenReturn(Mono.just(updatedApplication));
//
//        // Act & Assert
//        StepVerifier.create(applicationController.decide(applicationId, decisionRequest, version, authentication))
//                .assertNext(responseEntity -> {
//                    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//                    assertEquals("2", responseEntity.getHeaders().getETag());
//                    assertEquals(updatedApplication, responseEntity.getBody());
//                })
//                .verifyComplete();
//
//        verify(applicationService).decide(eq(applicationId), eq(decisionRequest),
//                eq(userId), eq(userId), eq(version), isNull());
//    }
//}
