package com.icar.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String id;
    private String vehicleId;
    private String dealerId;
    private String supplierId;
    private Integer quantity;
    private OrderStatus status;
    private String requestedDeliveryDate;
    private String createdAt;
}
