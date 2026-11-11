package com.icar.api.services;

import com.icar.api.dto.OrderRequest;
import com.icar.api.dto.OrderUpdateRequest;
import com.icar.api.dto.ApiResponse;
import com.icar.api.BaseApi;
import com.microsoft.playwright.APIRequestContext;
import io.qameta.allure.Step;

/** Vehicle Ordering API: one method per endpoint, no assertions here. */
public class OrdersService extends BaseApi {

    private static final String ORDERS = "/orders";

    public OrdersService(APIRequestContext request) {
        super(request);
    }

    @Step("Create order")
    public ApiResponse createOrder(OrderRequest body) {
        return post(ORDERS, body);
    }

    @Step("Get order {id}")
    public ApiResponse getOrder(String id) {
        return get(ORDERS + "/" + id);
    }

    @Step("Update order {id}")
    public ApiResponse updateOrder(String id, OrderUpdateRequest body) {
        return patch(ORDERS + "/" + id, body);
    }

    @Step("Delete order {id}")
    public ApiResponse deleteOrder(String id) {
        return delete(ORDERS + "/" + id);
    }
}
