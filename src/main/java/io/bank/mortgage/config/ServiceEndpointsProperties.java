package io.bank.mortgage.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "services.endpoints")
public class ServiceEndpointsProperties {

    /**
     * Map key = logical service name, value = base URL.
     * Example: "product" -> "http://product-service:8080".
     */
    private Map<String, String> urls;
}