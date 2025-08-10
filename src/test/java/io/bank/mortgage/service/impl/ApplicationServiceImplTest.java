//package io.bank.mortgage.service.impl;
//
//import io.bank.mortgage.datatype.DecisionType;
//import io.bank.mortgage.datatype.DocumentStatus;
//import io.bank.mortgage.datatype.EventType;
//import io.bank.mortgage.datatype.Status;
//import io.bank.mortgage.domain.model.Application;
//import io.bank.mortgage.domain.model.Decision;
//import io.bank.mortgage.domain.model.Document;
//import io.bank.mortgage.dto.DecisionRequest;
//import io.bank.mortgage.dto.DocumentMetadata;
//import io.bank.mortgage.dto.NewApplicationCreateRequest;
//import io.bank.mortgage.exception.VersionConflictException;
//import io.bank.mortgage.messaging.MessagingService;
//import io.bank.mortgage.repo.ApplicationRepository;
//import io.bank.mortgage.repo.DecisionRepository;
//import io.bank.mortgage.repo.DocumentRepository;
//import io.bank.mortgage.repo.OutboxEventRepository;
//import io.bank.mortgage.util.NationalIdService;
//import io.bank.mortgage.util.SharedUtils;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.crossstore.ChangeSetPersister;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.security.access.AccessDeniedException;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import java.math.BigDecimal;
//import java.nio.charset.StandardCharsets;
//import java.time.Duration;
//import java.time.Instant;
//import java.util.Arrays;
//import java.util.List;
//import java.util.UUID;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ApplicationServiceImplTest {
//
//    @Mock
//    private ApplicationRepository appRepo;
//
//    @Mock
//    private DocumentRepository docRepo;
//
//    @Mock
//    private DecisionRepository decisionRepo;
//
//    @Mock
//    private OutboxEventRepository outboxRepo;
//
//    @Mock
//    private SharedUtils sharedUtils;
//
//    @Mock
//    private NationalIdService nidService;
//
//    @Mock
//    private MessagingService messagingService;
//
//    @InjectMocks
//    private ApplicationServiceImpl applicationService;
//
//    private UUID applicationId;
//    private String applicantUserId;
//    private String correlationId;
//    private NewApplicationCreateRequest createRequest;
//    private Application application;
//
//    @BeforeEach
//    void setUp() {
//        applicationId = UUID.randomUUID();
//        applicantUserId = "user123";
//        correlationId = "corr123";
//
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
//        DocumentMetadata doc1 = DocumentMetadata.builder()
//                .type("ID")
//                .fileName("id.pdf")
//                .contentType("application/pdf")
//                .sizeBytes(1000L)
//                .uploadUrl("http://example.com/upload")
//                .checksum("abc123")
//                .build();
//
//        createRequest.setDocuments(Arrays.asList(doc1));
//
//        byte[] encryptedBytes = "encryptedId".getBytes(StandardCharsets.UTF_8);
//
//        application = Application.builder()
//                .id(applicationId)
//                .userId(applicantUserId)
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
//                .nationalIdEnc(Arrays.toString(encryptedBytes))
//                .softDeleted(false)
//                .build();
//
//        // Setup common mocks
//        when(nidService.hash(anyString())).thenReturn("hashedId");
//        when(nidService.encrypt(anyString())).thenReturn(encryptedBytes);
//        when(sharedUtils.nvl(any(BigDecimal.class))).thenAnswer(i -> i.getArgument(0));
//        when(messagingService.publishApplicationEvent(any(Application.class), any(EventType.class), anyString()))
//                .thenReturn(Mono.empty());
//    }
//
//    @Test
//    void create_NewApplication_Success() {
//        // Arrange
//        when(appRepo.findFirstByUserIdAndExternalRef(anyString(), anyString()))
//                .thenReturn(Mono.empty());
//        when(appRepo.insert(any(Application.class))).thenReturn(Mono.just(application));
//
//        Document document = Document.builder()
//                .id(UUID.randomUUID())
//                .userId(applicationId.toString())
//                .type("ID")
//                .fileName("id.pdf")
//                .contentType("application/pdf")
//                .sizeBytes(1000L)
//                .uploadUrl("http://example.com/upload")
//                .checksum("abc123")
//                .documentStatus(DocumentStatus.PENDING_UPLOAD)
//                .build();
//
//        when(docRepo.insert(any(Document.class))).thenReturn(Mono.just(document));
//
//        // Act & Assert
//        StepVerifier.create(applicationService.create(createRequest, applicantUserId, correlationId))
//                .expectNext(application)
//                .verifyComplete();
//
//        verify(appRepo).findFirstByApplicantUserIdAndExternalRef(applicantUserId, "ext123");
//        verify(appRepo).insert(any(Application.class));
//        verify(docRepo).insert(any(Document.class));
//        verify(messagingService).publishApplicationEvent(any(Application.class), eq(EventType.APPLICATION_CREATED), eq(correlationId));
//    }
//
//    @Test
//    void create_ExistingApplication_ReturnsExisting() {
//        // Arrange
//        when(appRepo.findFirstByApplicantUserIdAndExternalRef(anyString(), anyString()))
//                .thenReturn(Mono.just(application));
//
//        // Act & Assert
//        StepVerifier.create(applicationService.create(createRequest, applicantUserId, correlationId))
//                .expectNext(application)
//                .verifyComplete();
//
//        verify(appRepo).findFirstByApplicantUserIdAndExternalRef(applicantUserId, "ext123");
//        verify(appRepo, never()).insert(any(Application.class));
//        verify(docRepo, never()).insert(any(Document.class));
//        verify(messagingService, never()).publishApplicationEvent(any(Application.class), any(EventType.class), anyString());
//    }
//
//    @Test
//    void getById_ApplicationExists_AuthorizedUser_Success() {
//        // Arrange
//        when(appRepo.findById(applicationId)).thenReturn(Mono.just(application));
//
//        // Act & Assert
//        StepVerifier.create(applicationService.getById(applicationId, applicantUserId, false))
//                .expectNext(application)
//                .verifyComplete();
//
//        verify(appRepo).findById(applicationId);
//    }
//
//    @Test
//    void getById_ApplicationExists_Officer_Success() {
//        // Arrange
//        when(appRepo.findById(applicationId)).thenReturn(Mono.just(application));
//
//        // Act & Assert
//        StepVerifier.create(applicationService.getById(applicationId, "officer123", true))
//                .expectNext(application)
//                .verifyComplete();
//
//        verify(appRepo).findById(applicationId);
//    }
//
//    @Test
//    void getById_ApplicationExists_UnauthorizedUser_ThrowsAccessDeniedException() {
//        // Arrange
//        when(appRepo.findById(applicationId)).thenReturn(Mono.just(application));
//
//        // Act & Assert
//        StepVerifier.create(applicationService.getById(applicationId, "unauthorized123", false))
//                .expectError(AccessDeniedException.class)
//                .verify();
//
//        verify(appRepo).findById(applicationId);
//    }
//
//    @Test
//    void getById_ApplicationNotExists_ThrowsNotFoundException() {
//        // Arrange
//        when(appRepo.findById(applicationId)).thenReturn(Mono.empty());
//
//        // Act & Assert
//        StepVerifier.create(applicationService.getById(applicationId, applicantUserId, false))
//                .expectError(ChangeSetPersister.NotFoundException.class)
//                .verify();
//
//        verify(appRepo).findById(applicationId);
//    }
//
//    @Test
//    void list_WithFilters_Success() {
//        // Arrange
//        Status status = Status.SUBMITTED;
//        Instant from = Instant.now().minus(Duration.ofDays(7));
//        Instant to = Instant.now();
//        String nationalIdHash = "hashedId";
//        int page = 0;
//        int size = 10;
//
//        when(appRepo.search(eq(status), eq(from), eq(to), eq(nationalIdHash), any(PageRequest.class)))
//                .thenReturn(Flux.just(application));
//
//        // Act & Assert
//        StepVerifier.create(applicationService.list(status, from, to, nationalIdHash, page, size))
//                .expectNext(application)
//                .verifyComplete();
//
//        verify(appRepo).search(eq(status), eq(from), eq(to), eq(nationalIdHash), any(PageRequest.class));
//    }
//
//    @Test
//    void decide_ValidRequest_Success() {
//        // Arrange
//        DecisionRequest decisionRequest = new DecisionRequest();
//        decisionRequest.setDecision(DecisionType.REJECTED);
//        decisionRequest.setComments("Rejected due to insufficient income");
//
//        String officerId = "officer123";
//        String officerName = "John Officer";
//        int expectedVersion = 1;
//
//        when(appRepo.updateStatus(eq(applicationId), eq(Status.REJECTED), eq(expectedVersion)))
//                .thenReturn(Mono.just(true));
//        when(appRepo.findById(applicationId)).thenReturn(Mono.just(application));
//
//        Decision decision = Decision.builder()
//                .id(UUID.randomUUID())
//                .userId(applicationId.toString())
//                .decisionType(DecisionType.REJECTED)
//                .comments("Rejected due to insufficient income")
//                .officerUserId(officerId)
//                .officerName(officerName)
//                .build();
//
//        when(decisionRepo.insert(any(Decision.class))).thenReturn(Mono.just(decision));
//
//        // Act & Assert
//        StepVerifier.create(applicationService.decide(applicationId, decisionRequest, officerId, officerName, expectedVersion, correlationId))
//                .expectNext(application)
//                .verifyComplete();
//
//        verify(appRepo).updateStatus(applicationId, Status.REJECTED, expectedVersion);
//        verify(appRepo).findById(applicationId);
//        verify(decisionRepo).insert(any(Decision.class));
//        verify(messagingService).publishApplicationEvent(any(Application.class), eq(EventType.APPLICATION_DECISION_MADE), eq(correlationId));
//    }
//
//    @Test
//    void decide_VersionConflict_ThrowsVersionConflictException() {
//        // Arrange
//        DecisionRequest decisionRequest = new DecisionRequest();
//        decisionRequest.setDecision(DecisionType.REJECTED);
//        decisionRequest.setComments("Rejected due to insufficient income");
//
//        String officerId = "officer123";
//        String officerName = "John Officer";
//        int expectedVersion = 1;
//
//        when(appRepo.updateStatus(eq(applicationId), eq(Status.REJECTED), eq(expectedVersion)))
//                .thenReturn(Mono.just(false));
//
//        // Act & Assert
//        StepVerifier.create(applicationService.decide(applicationId, decisionRequest, officerId, officerName, expectedVersion, correlationId))
//                .expectError(VersionConflictException.class)
//                .verify();
//
//        verify(appRepo).updateStatus(applicationId, Status.REJECTED, expectedVersion);
//        verify(appRepo, never()).findById(any(UUID.class));
//        verify(decisionRepo, never()).insert(any(Decision.class));
//        verify(messagingService, never()).publishApplicationEvent(any(Application.class), any(EventType.class), anyString());
//    }
//}
