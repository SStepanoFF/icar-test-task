package com.icar.api.services;

import com.icar.api.dto.OrderRequest;
import com.icar.api.dto.OrderUpdateRequest;
import com.icar.core.api.ApiResponse;
import com.icar.core.api.BaseApi;
import com.microsoft.playwright.APIRequestContext;

/** Vehicle Ordering API: one method per endpoint, no assertions here. */
public class OrdersApi extends BaseApi {

    private static final String ORDERS = "/orders";

    public OrdersApi(APIRequestContext request) {
        super(request);
    }

    public ApiResponse createOrder(OrderRequest body) {
        return post(ORDERS, body);
    }

    public ApiResponse getOrder(String id) {
        return get(ORDERS + "/" + id);
    }

    public ApiResponse updateOrder(String id, OrderUpdateRequest body) {
        return patch(ORDERS + "/" + id, body);
    }

    public ApiResponse deleteOrder(String id) {
        return delete(ORDERS + "/" + id);
    }
}
