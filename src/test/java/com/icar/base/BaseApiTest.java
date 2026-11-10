package com.icar.base;

import com.icar.api.dto.OrderRequest;
import com.icar.api.dto.OrderResponse;
import com.icar.api.services.OrdersApi;
import com.icar.api.ApiResponse;
import com.icar.config.EnvConfig;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Sets up Playwright APIRequestContext once per class and cleans created data after each test. */
public abstract class BaseApiTest {

    private static Playwright playwright;
    private static APIRequestContext requestContext;

    protected OrdersApi ordersApi;
    private final List<String> createdOrderIds = new ArrayList<>();

    @BeforeAll
    static void initContext() {
        playwright = Playwright.create();

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        if (EnvConfig.apiToken() != null) {
            headers.put("Authorization", "Bearer " + EnvConfig.apiToken());
        }

        requestContext = playwright.request().newContext(new APIRequest.NewContextOptions()
                .setBaseURL(EnvConfig.baseUrl())
                .setTimeout(EnvConfig.timeoutMs())
                .setExtraHTTPHeaders(headers));
    }

    @AfterAll
    static void closeContext() {
        if (requestContext != null) {
            requestContext.dispose();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void initApis() {
        ordersApi = new OrdersApi(requestContext);
    }

    @AfterEach
    void cleanUp() {
        createdOrderIds.forEach(ordersApi::deleteOrder);
        createdOrderIds.clear();
    }

    protected void registerForCleanup(String orderId) {
        createdOrderIds.add(orderId);
    }

    /** Precondition helper: creates an order via API, registers it for cleanup, returns it. */
    protected OrderResponse givenOrderExists(OrderRequest request) {
        ApiResponse response = ordersApi.createOrder(request);
        assertThat(response.getStatus()).as("precondition: order created").isEqualTo(201);
        OrderResponse order = response.as(OrderResponse.class);
        registerForCleanup(order.getId());
        return order;
    }
}
