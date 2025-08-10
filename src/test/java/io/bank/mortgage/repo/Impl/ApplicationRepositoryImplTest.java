//package io.bank.mortgage.repo.Impl;
//
//import io.bank.mortgage.datatype.Status;
//import io.bank.mortgage.domain.model.Application;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
//import org.springframework.data.relational.core.query.Query;
//import org.springframework.data.relational.core.query.Update;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.UUID;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ApplicationRepositoryImplTest {
//
//    @Mock
//    private R2dbcEntityTemplate template;
//
//    @InjectMocks
//    private ApplicationRepositoryImpl applicationRepository;
//
//    private Application application;
//    private UUID applicationId;
//
//    @BeforeEach
//    void setUp() {
//        applicationId = UUID.randomUUID();
//        application = Application.builder()
//                .id(applicationId)
//                .userId("user123")
//                .externalRef("ext123")
//                .loanAmount(BigDecimal.valueOf(100000))
//                .currency("USD")
//                .income(BigDecimal.valueOf(50000))
//                .liabilities(BigDecimal.valueOf(10000))
//                .propertyAddress("123 Main St")
//                .propertyValue(BigDecimal.valueOf(200000))
//                .propertyType("HOUSE")
//                .status(Status.SUBMITTED)
//                .nationalIdHash("hashedId")
//                .nationalIdEnc("encryptedId".getBytes())
//                .softDeleted(false)
//                .build();
//    }
//
//    @Test
//    void insert_Success() {
//        // Arrange
//        // Mock the insert operation to return the application
//        when(template.insert(any(Application.class))).thenReturn(Mono.just(application));
//
//        // Act & Assert
//        StepVerifier.create(applicationRepository.insert(application))
//                .expectNext(application)
//                .verifyComplete();
//
//        verify(template).insert(application);
//    }
//
//    @Test
//    void updateStatus_Success() {
//        // Arrange
//        int expectedVersion = 1;
//        Status newStatus = Status.APPROVED;
//
//        // Mock the update operation to return 1 (one row updated)
//        when(template.update(any(Query.class), any(Update.class), eq(Application.class)))
//                .thenReturn(Mono.just(1L));
//
//        // Act & Assert
//        StepVerifier.create(applicationRepository.updateStatus(applicationId, newStatus, expectedVersion))
//                .expectNext(true)
//                .verifyComplete();
//
//        // Verify that update was called with the correct parameters
//        verify(template).update(any(Query.class), any(Update.class), eq(Application.class));
//    }
//
//    @Test
//    void updateStatus_VersionConflict_ReturnsFalse() {
//        // Arrange
//        int expectedVersion = 1;
//        Status newStatus = Status.APPROVED;
//
//        // Mock the update operation to return 0 (no rows updated)
//        when(template.update(any(Query.class), any(Update.class), eq(Application.class)))
//                .thenReturn(Mono.just(0L));
//
//        // Act & Assert
//        StepVerifier.create(applicationRepository.updateStatus(applicationId, newStatus, expectedVersion))
//                .expectNext(false)
//                .verifyComplete();
//
//        // Verify that update was called
//        verify(template).update(any(Query.class), any(Update.class), eq(Application.class));
//    }
//
//    @Test
//    void search_WithAllFilters_Success() {
//        // Arrange
//        Status status = Status.SUBMITTED;
//        Instant from = Instant.now().minus(java.time.Duration.ofDays(7));
//        Instant to = Instant.now();
//        String nationalIdHash = "hashedId";
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // Mock the select operation to return the application
//        when(template.select(any(Query.class), eq(Application.class)))
//                .thenReturn(Flux.just(application));
//
//        // Act & Assert
//        StepVerifier.create(applicationRepository.search(status, from, to, nationalIdHash, pageable))
//                .expectNext(application)
//                .verifyComplete();
//
//        // Verify that select was called with a query containing the filters
//        verify(template).select(any(Query.class), eq(Application.class));
//    }
//
//    @Test
//    void search_WithNoFilters_Success() {
//        // Arrange
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // Mock the select operation to return the application
//        when(template.select(any(Query.class), eq(Application.class)))
//                .thenReturn(Flux.just(application));
//
//        // Act & Assert
//        StepVerifier.create(applicationRepository.search(null, null, null, null, pageable))
//                .expectNext(application)
//                .verifyComplete();
//
//        // Verify that select was called
//        verify(template).select(any(Query.class), eq(Application.class));
//    }
//}
