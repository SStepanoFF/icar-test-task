package com.icar.api.data;

import com.icar.api.dto.OrderRequest;

import java.time.LocalDate;
import java.util.UUID;

/** Builds valid payloads with sensible defaults; tests override only what matters. */
public final class OrderDataFactory {

    private OrderDataFactory() {
    }

    public static OrderRequest.OrderRequestBuilder validOrder() {
        return OrderRequest.builder()
                .vehicleId("VEH-" + UUID.randomUUID())
                .dealerId("DLR-001")
                .supplierId("SUP-001")
                .quantity(1)
                .requestedDeliveryDate(LocalDate.now().plusDays(30).toString());
    }
}
