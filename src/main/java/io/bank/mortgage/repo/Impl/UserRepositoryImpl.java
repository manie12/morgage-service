package io.bank.mortgage.repo.Impl;

import io.bank.mortgage.domain.model.User;
import io.bank.mortgage.repo.UserRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final R2dbcEntityTemplate template;

    @Override
    public Mono<User> findByNationalIdWithRoles(String nationalId) {
        String sql = """
                SELECT u.id, u.national_id, u.password_hash, u.created_at,
                       r.name AS role_name
                FROM users u
                LEFT JOIN user_roles ur  ON ur.user_id = u.id
                LEFT JOIN roles       r  ON r.id = ur.role_id
                WHERE u.national_id = :nid
                """;
        return template.getDatabaseClient().sql(sql)
                .bind("nid", nationalId)
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
                SELECT :uid, r.id FROM roles r WHERE r.name = :rname
                ON CONFLICT DO NOTHING
                """;
        return template.getDatabaseClient().sql(sql)
                .bind("uid", userId)
                .bind("rname", roleName)
                .fetch().rowsUpdated().then();
    }

    @Override
    public Mono<Void> removeRole(Long userId, String roleName) {
        String sql = """
                DELETE FROM user_roles ul USING roles r
                WHERE ul.user_id = :uid AND ul.role_id = r.id AND r.name = :rname
                """;
        return template.getDatabaseClient().sql(sql)
                .bind("uid", userId)
                .bind("rname", roleName)
                .fetch().rowsUpdated().then();
    }

    @Override
    public Flux<String> rolesOf(Long userId) {
        String sql = """
                SELECT r.name FROM roles r
                JOIN user_roles ur ON ur.role_id = r.id
                WHERE ur.user_id = :uid
                """;
        return template.getDatabaseClient().sql(sql)
                .bind("uid", userId)
                .map((row, meta) -> row.get("name", String.class))
                .all();
    }

    /* ------------------------------------------------------------------ */
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
            if (t.role() != null) roles.add("ROLE_" + t.role());
        }

        User toUser() {
            return User.builder()
                    .id(id)
                    .nationalId(nid)
                    .passwordHash(pwd)
                    .roles(roles)
                    .createdAt(created)
                    .build();
        }
    }
    @Override
    public Mono<User> findById(Long userId) {
        String sql = """
            SELECT u.id, u.national_id, u.password_hash, u.created_at
            FROM users u
            WHERE u.id = :uid
            """;
        return template.getDatabaseClient().sql(sql)
                .bind("uid", userId)
                .map((row, meta) -> User.builder()
                        .id(row.get("id", Long.class))
                        .nationalId(row.get("national_id", String.class))
                        .passwordHash(row.get("password_hash", String.class))
                        .createdAt(row.get("created_at", java.time.OffsetDateTime.class))
                        .build())
                .one();
    }

}
