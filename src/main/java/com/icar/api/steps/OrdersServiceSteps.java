package com.icar.api.steps;

import com.icar.api.dto.ApiResponse;
import com.icar.api.data.OrderDataFactory;
import com.icar.api.dto.OrderResponse;
import com.icar.api.services.OrdersService;
import io.qameta.allure.Step;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class OrdersServiceSteps {

    private final OrdersService ordersService;

    public OrdersServiceSteps(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @Step("Given an order exists")
    public OrderResponse createDefaultOrder() {
        ApiResponse response = ordersService.createOrder(OrderDataFactory.validOrder().build());
        return response.as(OrderResponse.class);
    }
}
