package io.bank.mortgage.repo.Impl;

import io.bank.mortgage.datatype.DecisionType;
import io.bank.mortgage.datatype.DocumentStatus;
import io.bank.mortgage.datatype.Status;
import io.bank.mortgage.domain.model.Application;
import io.bank.mortgage.domain.model.Decision;
import io.bank.mortgage.domain.model.Document;
import io.bank.mortgage.domain.model.User;
import io.bank.mortgage.dto.NewApplicationCreateRequest;
import io.bank.mortgage.repo.UserRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final R2dbcEntityTemplate template;
    private final PasswordEncoder passwordEncoder;
    private final DocumentRepositoryImpl documentRepository;
    private final DecisionRepositoryImpl decisionRepository;
    private final ApplicationRepositoryImpl applicationRepository;


    @Override
    public Mono<User> insertUser(NewApplicationCreateRequest newApplicationCreateRequest) {
        User userDto = new User();
        userDto.setNationalId(newApplicationCreateRequest.getNationalId());
        userDto.setPasswordHash("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"); // bcrypt hashed password
        userDto.setRoles(Set.of("USER", "ADMIN"));

        return template.insert(userDto)
                .flatMap(savedUser -> {

                    Mono<Void> addRoleMono = addRole(savedUser.getId(), "APPLICANT");

                    Document document = Document.builder()
                            .userId(savedUser.getId().toString()) // Converting Long to String as per Document model
                            .documentStatus(DocumentStatus.PENDING_UPLOAD) // Using enum value instead of String
                            .checksum("APPLICATION")
                            .uploadUrl(newApplicationCreateRequest.getExternalRef())
                            .type("APPLICATION")
                            .contentType("application/pdf")
                            .sizeBytes(0L) // Default value
                            .build();
                    document.onCreate();

                    Mono<Document> documentMono = documentRepository.insert(document);

                    Decision decision = Decision.builder()
                            .userId(savedUser.getId().  toString())  // Converting to String as per Decision model
                            .decisionType(DecisionType.PENDING)    // Assuming PENDING is a valid enum value
                            .comments("Initial application submission")
                            .build();

                    decision.onCreate();
                    Mono<Decision> decisionMono = decisionRepository.insert(decision);


                    Application application = Application.builder()
                            .userId(savedUser.getId().toString())  // Converting to String as per Application model
                            .externalRef(newApplicationCreateRequest.getExternalRef())
                            .nationalIdHash(newApplicationCreateRequest.getNationalId()) // Assuming this is a hash
                            // .nationalIdEnc() - Would need encryption logic to set this
                            .loanAmount(newApplicationCreateRequest.getLoanAmount())
                            .currency(newApplicationCreateRequest.getCurrency())
                            .income(newApplicationCreateRequest.getIncome())
                            .liabilities(newApplicationCreateRequest.getLiabilities())
                            .propertyAddress(newApplicationCreateRequest.getPropertyAddress())
                            .propertyValue(newApplicationCreateRequest.getPropertyValue())
                            .propertyType(newApplicationCreateRequest.getPropertyType())
                            .status(Status.UNDER_REVIEW)  // Assuming NEW is a valid enum value in Status
                            .softDeleted(false)
                            .version(0)  // Initial version
                            .build();

                    application.onCreate();

                    Mono<Application> applicationMono = applicationRepository.insert(application);


                    return Mono.when(addRoleMono, documentMono, decisionMono, applicationMono)
                            .thenReturn(savedUser);
                });
    }

    @Override
    public Mono<User> findByNationalIdWithRoles(String nationalId) {
        String sql = """
                    SELECT u.id, u.national_id, u.password_hash, u.created_at,
                           r.name AS role_name
                      FROM users u
                 LEFT JOIN user_roles ur ON ur.user_id = u.id
                 LEFT JOIN roles       r ON r.id       = ur.role_id
                     WHERE u.national_id = $1
                """;
        return template.getDatabaseClient().sql(sql)
                .bind(0, nationalId)
                .map((row, meta) -> new RoleTuple(
                        row.get("id", Long.class),
                        row.get("national_id", String.class),
                        row.get("password_hash", String.class),
                        row.get("created_at", java.time.OffsetDateTime.class),
                        row.get("role_name", String.class)))
                .all()
                .collect(UserRolesAggregator::new, UserRolesAggregator::accumulate)
                .map(UserRolesAggregator::toUser);
    }

    @Override
    public Mono<Void> addRole(Long userId, String roleName) {
        String sql = """
                    INSERT INTO user_roles (user_id, role_id)
                    SELECT $1, r.id FROM roles r WHERE r.name = $2
                    ON CONFLICT DO NOTHING
                """;
        return template.getDatabaseClient().sql(sql)
                .bind(0, userId)
                .bind(1, roleName)
                .fetch().rowsUpdated().then();
    }

    @Override
    public Mono<Void> removeRole(Long userId, String roleName) {
        String sql = """
                    DELETE FROM user_roles ur USING roles r
                     WHERE ur.user_id = $1 AND ur.role_id = r.id AND r.name = $2
                """;
        return template.getDatabaseClient().sql(sql)
                .bind(0, userId)
                .bind(1, roleName)
                .fetch().rowsUpdated().then();
    }

    @Override
    public Flux<String> rolesOf(Long userId) {
        String sql = """
                    SELECT r.name FROM roles r
                    JOIN user_roles ur ON ur.role_id = r.id
                    WHERE ur.user_id = $1
                """;
        return template.getDatabaseClient().sql(sql)
                .bind(0, userId)
                .map((row, meta) -> row.get("name", String.class))
                .all();
    }

    @Override
    public Mono<User> findById(Long userId) {
        String sql = """
                    SELECT u.id, u.national_id, u.password_hash, u.created_at
                      FROM users u
                     WHERE u.id = $1
                """;
        return template.getDatabaseClient().sql(sql)
                .bind(0, userId)
                .map((row, meta) -> User.builder()
                        .id(row.get("id", Long.class))
                        .nationalId(row.get("national_id", String.class))
                        .passwordHash(row.get("password_hash", String.class))
                        .createdAt(String.valueOf(row.get("created_at", java.time.OffsetDateTime.class)))
                        .build())
                .one();
    }

    /* helpers */
    private record RoleTuple(Long id, String nid, String pwd, java.time.OffsetDateTime created, String role) {
    }

    private static class UserRolesAggregator {
        Long id;
        String nid;
        String pwd;
        java.time.OffsetDateTime created;
        Set<String> roles = new HashSet<>();

        void accumulate(RoleTuple t) {
            if (id == null) {
                id = t.id();
                nid = t.nid();
                pwd = t.pwd();
                created = t.created();
            }
            if (t.role() != null) roles.add(t.role()); // store raw role (APPLICANT/OFFICER), no ROLE_ prefix here
        }

        User toUser() {
            return User.builder()
                    .id(id)
                    .nationalId(nid)
                    .passwordHash(pwd)
                    .roles(roles)
                    .build();
        }
    }
}