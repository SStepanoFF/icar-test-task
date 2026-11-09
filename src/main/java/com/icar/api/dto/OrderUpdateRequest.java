package com.icar.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** PATCH /orders/{id} payload: only fields to change (nulls are not sent). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateRequest {
    private Integer quantity;
    private String requestedDeliveryDate;
    private OrderStatus status;
}
