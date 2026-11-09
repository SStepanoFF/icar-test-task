package com.icar.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** POST /orders payload. ASSUMPTION: fields are illustrative, to be confirmed with REST docs. */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private String vehicleId;
    private String dealerId;
    private String supplierId;
    private Integer quantity;
    private String requestedDeliveryDate; // ISO-8601, e.g. 2026-12-01
}
