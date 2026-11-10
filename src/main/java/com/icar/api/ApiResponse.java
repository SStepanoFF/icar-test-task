package com.icar.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

/** Framework-agnostic wrapper around an HTTP response. */
@Getter
public class ApiResponse {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final int status;
    private final String rawBody;

    public ApiResponse(int status, String rawBody) {
        this.status = status;
        this.rawBody = rawBody;
    }

    /** Deserialize body into DTO (use ErrorResponse.class for negative cases). */
    public <T> T as(Class<T> type) {
        try {
            return MAPPER.readValue(rawBody, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Cannot parse response into " + type.getSimpleName() + ". Body: " + rawBody, e);
        }
    }
}
