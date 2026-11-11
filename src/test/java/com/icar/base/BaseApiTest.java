package com.icar.base;

import com.icar.api.services.OrdersService;
import com.icar.config.AllureEnvironmentWriter;
import com.icar.config.EnvConfig;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Sets up Playwright APIRequestContext once per class and cleans created data after each test. */
public abstract class BaseApiTest {

    private static Playwright playwright;
    protected static APIRequestContext requestContext;

    protected OrdersService ordersService;

    @BeforeAll
    static void initContext() {
        AllureEnvironmentWriter.write();
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



}
