package io.bank.mortgage.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebClientFactory {

    private final ServiceEndpointsProperties props;
    private final WebClient.Builder builder;   // autowired default builder

    /**
     * Local cache so we don't rebuild identical clients.
     */
    private final ConcurrentHashMap<String, WebClient> cache = new ConcurrentHashMap<>();

    public WebClient clientFor(String serviceName) {
        return cache.computeIfAbsent(serviceName, this::buildClient);
    }

    /* ---------- internal ---------- */

    private WebClient buildClient(String serviceName) {
        String baseUrl = props.getUrls().get(serviceName);
        if (baseUrl == null) {
            throw new IllegalArgumentException("Unknown service '" + serviceName +
                    "'. Add it to services.endpoints.* in application.yml");
        }

        return builder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .filter(logRequest())
                .filter(logResponse())
                .filter(errorMappingFilter())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            log.debug("[{}] {} {}", request.logPrefix(),
                    request.method(), request.url());
            return Mono.just(request);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            log.debug("[{}] ← {}", response.logPrefix(), response.statusCode());
            return Mono.just(response);
        });
    }

    private ExchangeFilterFunction errorMappingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(resp -> {
            if (resp.statusCode().isError()) {
                return resp.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .flatMap(body -> {
                            int code = resp.statusCode().value();
                            String reason = resp.statusCode().toString(); // generic
                            return Mono.error(
                                    WebClientResponseException.create(
                                            code, reason,
                                            resp.headers().asHttpHeaders(),
                                            body.getBytes(StandardCharsets.UTF_8),
                                            StandardCharsets.UTF_8
                                    ));
                        });
            }
            return Mono.just(resp);
        });
    }
}