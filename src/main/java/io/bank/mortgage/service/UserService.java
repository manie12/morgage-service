//package io.bank.mortgage.service;
//
//import io.bank.mortgage.domain.model.User;
//import io.bank.mortgage.dto.NewApplicationCreateRequest;
//import io.bank.mortgage.repo.Impl.UserRepositoryImpl;
//import io.bank.mortgage.repo.UserRepository;
//import io.bank.mortgage.repo.UserRepositoryCustom;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Mono;
//
//import java.math.BigDecimal;
//import java.time.OffsetDateTime;
//import java.util.Set;
//
//@Service
//@RequiredArgsConstructor
//public abstract class UserService implements UserRepositoryCustom {
//
//    private final UserRepositoryImpl userRepo;
//    private final PasswordEncoder passwordEncoder;
//
//    public Mono<User> registerApplicant(NewApplicationCreateRequest newApplicationCreateRequest) {
//
//        NewApplicationCreateRequest request = new NewApplicationCreateRequest();
//        request.setExternalRef("REF-12345");
//        request.setNationalId(newApplicationCreateRequest.getNationalId());
//        request.setFullName("John Doe");
//        request.setEmail("john.doe@example.com");
//        request.setPhone("+1234567890");
//        request.setLoanAmount(new BigDecimal("250000.00"));
//        request.setCurrency("USD");
//        request.setIncome(new BigDecimal("75000.00"));
//        request.setLiabilities(new BigDecimal("15000.00"));
//        request.setPropertyAddress("123 Main Street, Anytown, ST 12345");
//        request.setPropertyValue(new BigDecimal("350000.00"));
//        request.setPropertyType("RESIDENTIAL");
//
//
//        return userRepo.insertUser(request)
//                .flatMap(saved -> userRepo.addRole(saved.getId(), "APPLICANT").thenReturn(saved));
//    }
//}