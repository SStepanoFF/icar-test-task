package com.icar.api;

import com.icar.api.data.OrderDataFactory;
import com.icar.api.dto.ErrorResponse;
import com.icar.api.dto.OrderRequest;
import com.icar.api.dto.OrderResponse;
import com.icar.api.dto.OrderStatus;
import com.icar.api.dto.OrderUpdateRequest;
import com.icar.base.BaseApiTest;
import com.icar.core.api.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("orders")
class OrdersApiTest extends BaseApiTest {

    @Test
    @Tag("smoke")
    @DisplayName("POST /orders: valid payload creates an order in initial status")
    void createOrder_validPayload_returns201() {
        OrderRequest request = OrderDataFactory.validOrder().quantity(2).build();

        ApiResponse response = ordersApi.createOrder(request);
        assertThat(response.getStatus()).isEqualTo(201);

        OrderResponse order = response.as(OrderResponse.class);
        registerForCleanup(order.getId());
        assertThat(order.getId()).isNotBlank();
        assertThat(order.getVehicleId()).isEqualTo(request.getVehicleId());
        assertThat(order.getQuantity()).isEqualTo(2);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);
    }

    @Test
    @Tag("smoke")
    @DisplayName("GET /orders/{id}: returns the order that was created")
    void getOrder_existing_returnsSameData() {
        OrderResponse created = givenOrderExists(OrderDataFactory.validOrder().build());

        ApiResponse response = ordersApi.getOrder(created.getId());

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.as(OrderResponse.class)).isEqualTo(created);
    }

    @Test
    @DisplayName("PATCH /orders/{id}: updates only the sent fields")
    void updateOrder_quantity_changesOnlyQuantity() {
        OrderResponse created = givenOrderExists(OrderDataFactory.validOrder().quantity(1).build());

        ApiResponse response = ordersApi.updateOrder(created.getId(),
                OrderUpdateRequest.builder().quantity(5).build());

        assertThat(response.getStatus()).isEqualTo(200);
        OrderResponse updated = response.as(OrderResponse.class);
        assertThat(updated.getQuantity()).isEqualTo(5);
        assertThat(updated.getVehicleId()).isEqualTo(created.getVehicleId());
        assertThat(updated.getDealerId()).isEqualTo(created.getDealerId());
    }

    @Test
    @DisplayName("DELETE /orders/{id}: order is removed and no longer retrievable")
    void deleteOrder_existing_thenGetReturns404() {
        OrderResponse created = givenOrderExists(OrderDataFactory.validOrder().build());

        ApiResponse deleteResponse = ordersApi.deleteOrder(created.getId());
        assertThat(deleteResponse.getStatus()).isIn(200, 204);

        assertThat(ordersApi.getOrder(created.getId()).getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("POST /orders: missing vehicleId is rejected with a validation error")
    void createOrder_missingVehicleId_returns400() {
        OrderRequest invalid = OrderDataFactory.validOrder().vehicleId(null).build();

        ApiResponse response = ordersApi.createOrder(invalid);

        assertThat(response.getStatus()).isEqualTo(400);
        ErrorResponse error = response.as(ErrorResponse.class);
        assertThat(error.getMessage()).isNotBlank();
    }
}
