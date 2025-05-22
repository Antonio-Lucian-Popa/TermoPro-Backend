package com.asusoftware.TermoPro.customer_order.model.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCustomerOrderDto {
    private String clientName;
    private String clientPhone;
    private String clientAddress;
    private Double latitude;
    private Double longitude;
    private LocalDate scheduledDate;
    private UUID companyId;
    private UUID teamId; // poate fi null
}
