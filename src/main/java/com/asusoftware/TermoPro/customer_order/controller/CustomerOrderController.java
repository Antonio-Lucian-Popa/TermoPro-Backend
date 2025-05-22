package com.asusoftware.TermoPro.customer_order.controller;

import com.asusoftware.TermoPro.customer_order.model.OrderStatus;
import com.asusoftware.TermoPro.customer_order.model.dto.CreateCustomerOrderDto;
import com.asusoftware.TermoPro.customer_order.model.dto.CustomerOrderDto;
import com.asusoftware.TermoPro.customer_order.service.CustomerOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class CustomerOrderController {

    private final CustomerOrderService orderService;

    /**
     * Creează o nouă comandă pentru o companie.
     */
    @PostMapping
    public ResponseEntity<CustomerOrderDto> createOrder(@RequestBody @Valid CreateCustomerOrderDto dto) {
        return ResponseEntity.ok(orderService.createOrder(dto));
    }

    /**
     * Obține toate comenzile dintr-o companie.
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<CustomerOrderDto>> getOrdersByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(orderService.getOrdersByCompany(companyId));
    }

    /**
     * Obține o comandă după ID.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<CustomerOrderDto> getOrderById(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @GetMapping("/company/{companyId}/filter")
    public ResponseEntity<List<CustomerOrderDto>> filterOrders(
            @PathVariable UUID companyId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(orderService.filterOrders(companyId, date, status));
    }


    /**
     * Actualizează statusul unei comenzi.
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID orderId,
            @RequestParam String status
    ) {
        orderService.updateStatus(orderId, OrderStatus.valueOf(status));
        return ResponseEntity.noContent().build();
    }

    /**
     * Șterge o comandă.
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}