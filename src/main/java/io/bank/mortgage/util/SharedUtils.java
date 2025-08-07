package io.bank.mortgage.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for shared functionality including JSON conversions and type handling.
 */
@Slf4j
@Component
public class SharedUtils {
    private final ObjectMapper objectMapper;
    private final Jsonb jsonb;

    public SharedUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        // Initialize Jsonb with appropriate configuration
        JsonbConfig config = new JsonbConfig()
                .withFormatting(true);
        this.jsonb = JsonbBuilder.create(config);
    }

    @SneakyThrows
    public   BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
    // Existing methods
    @SneakyThrows
    public <T> T fromJsonToObject(String json, Class<T> c) {
        return this.objectMapper.readValue(json, c);
    }

    @SneakyThrows
    public <T> T fromJsonToObject(String json, TypeReference<T> c) {
        return this.objectMapper.readValue(json, c);
    }

    public String toJson(Object o, boolean prettify) {
        if (o != null) {
            try {
                return prettify ? this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o) : this.objectMapper.writeValueAsString(o);
            } catch (Exception e) {
                log.error(String.format("Object to json error [ %s ]", o), e);
            }
        }
        return null;
    }

    // New JSONB-specific methods

    /**
     * Converts any object to JSONB string format
     *
     * @param object   The object to convert
     * @param prettify Whether to format the JSON output
     * @return JSONB string representation
     */
    @SneakyThrows
    public <T> String toJsonb(T object, boolean prettify) {
        if (object != null) {
            try {
                if (prettify) {
                    return jsonb.toJson(object);
                } else {
                    // Create a non-formatted instance for this conversion
                    JsonbConfig config = new JsonbConfig()
                            .withFormatting(false);
                    Jsonb compactJsonb = JsonbBuilder.create(config);
                    return compactJsonb.toJson(object);
                }
            } catch (Exception e) {
                log.error(String.format("Object to jsonb error [ %s ]", object), e);
            }
        }
        return null;
    }

    /**
     * Shorthand method for compact JSONB conversion
     *
     * @param object The object to convert
     * @return JSONB string representation without formatting
     */
    public <T> String toJsonb(T object) {
        return toJsonb(object, true);
    }

    /**
     * Converts a JSONB string to a Java object
     *
     * @param json  The JSONB string to convert
     * @param clazz The class of the target object
     * @return Java object of the specified type
     */
    @SneakyThrows
    public <T> T fromJsonb(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            return jsonb.fromJson(json, clazz);
        } catch (Exception e) {
            log.error(String.format("Jsonb to object error [ json=%s, class=%s ]", json, clazz.getName()), e);
            return null;
        }
    }

    /**
     * Converts a JSONB string to a Java object with generic type information
     *
     * @param json          The JSONB string to convert
     * @param typeReference Type reference for complex/generic types
     * @return Java object of the specified type
     */
    @SneakyThrows
    public <T> T fromJsonb(String json, TypeReference<T> typeReference) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            // Using Jackson for complex types since Jsonb doesn't have a direct equivalent
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            log.error(String.format("Jsonb to complex object error [ json=%s, type=%s ]",
                    json, typeReference.getType().getTypeName()), e);
            return null;
        }
    }

    /**
     * Generic method to convert list of any type to JSONB
     *
     * @param list List of objects
     * @return JSONB string representation
     */
    public <T> String listToJsonb(List<T> list) {
        return toJsonb(list);
    }

    /**
     * Generic method to convert JSONB to list of specified type
     *
     * @param json         The JSONB string
     * @param elementClass Class of the list elements
     * @return List of objects of the specified type
     */
    @SneakyThrows
    public <T> List<T> jsonbToList(String json, Class<T> elementClass) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // Using Jackson's type factory for more reliable generic collection handling
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, elementClass));
        } catch (Exception e) {
            log.error(String.format("Jsonb to list error [ json=%s, elementClass=%s ]",
                    json, elementClass.getName()), e);
            return new ArrayList<>();
        }
    }

    /**
     * Generic method to convert map to JSONB
     *
     * @param map Map to convert
     * @return JSONB string representation
     */
    public <K, V> String mapToJsonb(Map<K, V> map) {
        // Fixed: Direct conversion to JSONB without intermediate JSON conversion
        return toJsonb(map);
    }

    /**
     * Generic method to convert JSONB to map with specified key and value types
     *
     * @param json       The JSONB string
     * @param keyClass   Class of the map keys
     * @param valueClass Class of the map values
     * @return Map with keys and values of the specified types
     */
    @SneakyThrows
    public <K, V> Map<K, V> jsonbToMap(String json, Class<K> keyClass, Class<V> valueClass) {
        if (json == null || json.isEmpty()) {
            return Map.of();
        }

        try {
            // Using Jackson's type factory for more reliable generic map handling
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructMapType(Map.class, keyClass, valueClass));
        } catch (Exception e) {
            log.error(String.format("Jsonb to map error [ json=%s, keyClass=%s, valueClass=%s ]",
                    json, keyClass.getName(), valueClass.getName()), e);
            return Map.of();
        }
    }
}