package com.asusoftware.TermoPro.customer_order.model.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrderDto {
    private UUID id;
    private String clientName;
    private String clientPhone;
    private String clientAddress;
    private Double latitude;
    private Double longitude;
    private LocalDate scheduledDate;
    private String status;
    private UUID companyId;
    private UUID teamId;
    private LocalDateTime createdAt;
}