
package io.bank.mortgage.repo.Impl;

import io.bank.mortgage.domain.model.RefreshToken;
import io.bank.mortgage.repo.RefreshTokenRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepositoryCustom {

    private final R2dbcEntityTemplate template;

    @Override
    public Mono<RefreshToken> insert(RefreshToken token) {
        return template.insert(RefreshToken.class).using(token);
    }


    @Override
    public Mono<Boolean> revokeToken(UUID token) {
        String sql = "UPDATE refresh_tokens SET revoked = TRUE WHERE token = $1";
        return template.getDatabaseClient().sql(sql)
                .bind(0, token)  // Note: in R2DBC, parameter indexes are 0-based
                .fetch().rowsUpdated()
                .map(rows -> rows == 1);
    }

    @Override
    public Mono<Integer> purgeExpired() {
        String sql = "DELETE FROM refresh_tokens WHERE expires_at < NOW() OR revoked = TRUE";
        return template.getDatabaseClient().sql(sql)
                .fetch().rowsUpdated()
                .map(Long::intValue);
    }
    // Add this method to your RefreshTokenRepositoryImpl class
    @Override
    public Mono<RefreshToken> findById(UUID token) {
        String sql = "SELECT * FROM refresh_tokens WHERE token = $1";
        return template.getDatabaseClient().sql(sql)
                .bind(0, token)
                .map((row, metadata) -> {
                    RefreshToken refreshToken = new RefreshToken();
                    refreshToken.setToken(row.get("token", UUID.class));
                    refreshToken.setUserId(row.get("user_id", Long.class));
                    refreshToken.setExpiresAt(row.get("expires_at", OffsetDateTime.class));
                    refreshToken.setRevoked(row.get("revoked", Boolean.class));
                    refreshToken.setCreatedAt(row.get("created_at", OffsetDateTime.class));
                    return refreshToken;
                })
                .one();
    }
}