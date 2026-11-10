package com.icar.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import lombok.extern.slf4j.Slf4j;

/**
 * Universal HTTP layer. Knows nothing about business entities,
 * so any endpoint can be covered by a thin subclass (OrdersApi, InvoicesApi, ...).
 */
@Slf4j
public abstract class BaseApi {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    protected final APIRequestContext request;

    protected BaseApi(APIRequestContext request) {
        this.request = request;
    }

    protected ApiResponse get(String path) {
        log.info("GET {}", path);
        return wrap(request.get(path));
    }

    protected ApiResponse post(String path, Object body) {
        log.info("POST {}", path);
        return wrap(request.post(path, jsonBody(body)));
    }

    protected ApiResponse patch(String path, Object body) {
        log.info("PATCH {}", path);
        return wrap(request.patch(path, jsonBody(body)));
    }

    protected ApiResponse delete(String path) {
        log.info("DELETE {}", path);
        return wrap(request.delete(path));
    }

    private RequestOptions jsonBody(Object body) {
        return RequestOptions.create().setData(toJson(body));
    }

    private ApiResponse wrap(APIResponse response) {
        ApiResponse result = new ApiResponse(response.status(), response.text());
        log.info("<- {} {}", result.getStatus(), result.getRawBody());
        return result;
    }

    private String toJson(Object body) {
        try {
            return MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot serialize request body", e);
        }
    }
}
