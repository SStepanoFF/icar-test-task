package com.icar.api;

import com.icar.api.data.OrderDataFactory;
import com.icar.api.dto.*;
import com.icar.api.services.OrdersService;
import com.icar.api.steps.OrdersServiceSteps;
import com.icar.base.BaseApiTest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("iCar")
@Feature("Vehicle Ordering API")
@Tag("orders")
class OrdersServiceTest extends BaseApiTest {

    private OrdersServiceSteps ordersServiceSteps;
    private final List<String> createdOrderIds = new ArrayList<>();


    @BeforeEach
    void initApis() {
        ordersService = new OrdersService(requestContext);
        ordersServiceSteps = new OrdersServiceSteps(ordersService);
    }

    @AfterEach
    void cleanUp() {
        createdOrderIds.forEach(ordersService::deleteOrder);
        createdOrderIds.clear();
    }
    protected void registerForCleanup(String orderId) {
        createdOrderIds.add(orderId);
    }

    // ------------------------------------------------------------------
    // Positive scenarios
    // ------------------------------------------------------------------

    @Test
    @Tag("smoke")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("POST /orders: valid payload creates an order in initial status")
    void createOrder_validPayload_returns201() {
        OrderRequest request = OrderDataFactory.validOrder().quantity(2).build();

        ApiResponse response = ordersService.createOrder(request);
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
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("GET /orders/{id}: returns the order that was created")
    void getOrder_existing_returnsSameData() {
        OrderResponse created = ordersServiceSteps.createDefaultOrder();
        registerForCleanup(created.getId());

        ApiResponse response = ordersService.getOrder(created.getId());

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.as(OrderResponse.class)).isEqualTo(created);
    }

    @Test
    @DisplayName("PATCH /orders/{id}: updates only the sent fields")
    void updateOrder_quantity_changesOnlyQuantity() {
        OrderResponse created = ordersServiceSteps.createDefaultOrder();
        registerForCleanup(created.getId());

        ApiResponse response = ordersService.updateOrder(created.getId(),
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
        OrderResponse created = ordersServiceSteps.createDefaultOrder();
        registerForCleanup(created.getId());

        ApiResponse deleteResponse = ordersService.deleteOrder(created.getId());
        assertThat(deleteResponse.getStatus()).isIn(200, 204);

        assertThat(ordersService.getOrder(created.getId()).getStatus()).isEqualTo(404);
    }

    // ------------------------------------------------------------------
    // Negative: POST /orders validation
    // ------------------------------------------------------------------

    @Test
    @DisplayName("POST /orders: missing vehicleId is rejected with a validation error")
    void createOrder_missingVehicleId_returns400() {
        OrderRequest invalid = OrderDataFactory.validOrder().vehicleId(null).build();

        ApiResponse response = ordersService.createOrder(invalid);

        assertThat(response.getStatus()).isEqualTo(400);
        ErrorResponse error = response.as(ErrorResponse.class);
        assertThat(error.getMessage()).isNotBlank();
    }

    @ParameterizedTest(name = "POST /orders: {0} -> 400")
    @MethodSource("invalidOrderPayloads")
    @DisplayName("POST /orders: invalid payloads are rejected")
    void createOrder_invalidPayload_returns400(String caseName, OrderRequest invalid) {
        ApiResponse response = ordersService.createOrder(invalid);

        // if the API wrongly accepts the payload, still clean the created order up
        if (response.getStatus() == 201) {
            registerForCleanup(response.as(OrderResponse.class).getId());
        }

        assertThat(response.getStatus()).as(caseName).isEqualTo(400);
        assertThat(response.as(ErrorResponse.class).getMessage()).isNotBlank();
    }

    static Stream<Arguments> invalidOrderPayloads() {
        return Stream.of(
                Arguments.of("dealerId is missing",
                        OrderDataFactory.validOrder().dealerId(null).build()),
                Arguments.of("supplierId is missing",
                        OrderDataFactory.validOrder().supplierId(null).build()),
                Arguments.of("quantity is missing",
                        OrderDataFactory.validOrder().quantity(null).build()),
                Arguments.of("quantity is zero",
                        OrderDataFactory.validOrder().quantity(0).build()),
                Arguments.of("quantity is negative",
                        OrderDataFactory.validOrder().quantity(-1).build()),
                Arguments.of("delivery date has wrong format",
                        OrderDataFactory.validOrder().requestedDeliveryDate("31-12-2026").build()),
                Arguments.of("delivery date is in the past",
                        OrderDataFactory.validOrder()
                                .requestedDeliveryDate(LocalDate.now().minusDays(1).toString()).build())
        );
    }

    // ------------------------------------------------------------------
    // Negative: GET /orders/{id}
    // ------------------------------------------------------------------

    @Test
    @DisplayName("GET /orders/{id}: well-formed but non-existent id returns 404")
    void getOrder_nonExistentId_returns404() {
        ApiResponse response = ordersService.getOrder(UUID.randomUUID().toString());

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.as(ErrorResponse.class).getMessage()).isNotBlank();
    }

    @ParameterizedTest(name = "GET /orders/{0}: malformed id is rejected")
    @ValueSource(strings = {"abc", "-1", "0", "12345678901234567890"})
    @DisplayName("GET /orders/{id}: malformed id is rejected")
    void getOrder_malformedId_returns4xx(String malformedId) {
        ApiResponse response = ordersService.getOrder(malformedId);

        // ASSUMPTION: contract does not say 400 vs 404 for a malformed id, to confirm with REST docs
        assertThat(response.getStatus()).isIn(400, 404);
        assertThat(response.as(ErrorResponse.class).getMessage()).isNotBlank();
    }

    // ------------------------------------------------------------------
    // Negative: PATCH /orders/{id}
    // ------------------------------------------------------------------

    @Test
    @DisplayName("PATCH /orders/{id}: non-existent id returns 404")
    void updateOrder_nonExistentId_returns404() {
        ApiResponse response = ordersService.updateOrder(UUID.randomUUID().toString(),
                OrderUpdateRequest.builder().quantity(3).build());

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.as(ErrorResponse.class).getMessage()).isNotBlank();
    }

    @ParameterizedTest(name = "PATCH /orders/{0}: malformed id is rejected")
    @ValueSource(strings = {"abc", "-1", "0"})
    @DisplayName("PATCH /orders/{id}: malformed id is rejected")
    void updateOrder_malformedId_returns4xx(String malformedId) {
        ApiResponse response = ordersService.updateOrder(malformedId,
                OrderUpdateRequest.builder().quantity(3).build());

        // ASSUMPTION: 400 vs 404 for a malformed id, to confirm with REST docs
        assertThat(response.getStatus()).isIn(400, 404);
    }

    @Test
    @DisplayName("PATCH /orders/{id}: invalid quantity is rejected and the order stays unchanged")
    void updateOrder_invalidQuantity_returns400AndOrderUnchanged() {
        OrderResponse created = ordersServiceSteps.createDefaultOrder();
        registerForCleanup(created.getId());

        ApiResponse response = ordersService.updateOrder(created.getId(),
                OrderUpdateRequest.builder().quantity(0).build());

        assertThat(response.getStatus()).isEqualTo(400);
        OrderResponse afterAttempt = ordersService.getOrder(created.getId()).as(OrderResponse.class);
        assertThat(afterAttempt).isEqualTo(created);
    }

    @Test
    @DisplayName("PATCH /orders/{id}: deleted order cannot be updated")
    void updateOrder_deletedOrder_returns404() {
        OrderResponse created = ordersServiceSteps.createDefaultOrder();
        registerForCleanup(created.getId());
        ordersService.deleteOrder(created.getId());

        ApiResponse response = ordersService.updateOrder(created.getId(),
                OrderUpdateRequest.builder().quantity(3).build());

        assertThat(response.getStatus()).isEqualTo(404);
    }

    // ------------------------------------------------------------------
    // Negative: DELETE /orders/{id}
    // ------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /orders/{id}: non-existent id returns 404")
    void deleteOrder_nonExistentId_returns404() {
        ApiResponse response = ordersService.deleteOrder(UUID.randomUUID().toString());

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("DELETE /orders/{id}: deleting the same order twice returns 404 the second time")
    void deleteOrder_twice_secondReturns404() {
        OrderResponse created = ordersServiceSteps.createDefaultOrder();
        registerForCleanup(created.getId());

        assertThat(ordersService.deleteOrder(created.getId()).getStatus()).isIn(200, 204);

        // ASSUMPTION: delete is not idempotent in this API, to confirm with REST docs
        assertThat(ordersService.deleteOrder(created.getId()).getStatus()).isEqualTo(404);
    }

}