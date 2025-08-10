package io.bank.mortgage.repo.Impl;

import io.bank.mortgage.datatype.DocumentStatus;
import io.bank.mortgage.domain.model.Document;
import io.bank.mortgage.repo.DocumentRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;


@Repository
@RequiredArgsConstructor
class DocumentRepositoryImpl implements DocumentRepositoryCustom {

    private final R2dbcEntityTemplate template;

    @Override
    public Mono<Document> insert(Document document) {
        return template.insert(Document.class).using(document);
    }


    @Override
    public Mono<Boolean> updateStatus(UUID documentId, DocumentStatus newStatus, String checksum) {
        Update upd = Update.update("status", newStatus.name())
                .set("updated_at", Instant.now());
        if (checksum != null) upd = upd.set("checksum", checksum);

        Query q = Query.query(Criteria.where("id").is(documentId));
        return template.update(q, upd, Document.class)
                .map(rows -> rows == 1);
    }

    @Override
    public Flux<Document> search(UUID applicationId, DocumentStatus status, Pageable pageable) {
        Criteria c = Criteria.empty();
        if (applicationId != null) c = c.and(Criteria.where("application_id").is(applicationId));
        if (status != null) c = c.and(Criteria.where("status").is(status.name()));
        Query q = Query.query(c).with(pageable);
        return template.select(q, Document.class);
    }
}
