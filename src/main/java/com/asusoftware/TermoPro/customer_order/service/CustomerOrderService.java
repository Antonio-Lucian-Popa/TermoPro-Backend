package com.asusoftware.TermoPro.customer_order.service;

import com.asusoftware.TermoPro.customer_order.model.CustomerOrder;
import com.asusoftware.TermoPro.customer_order.model.OrderStatus;
import com.asusoftware.TermoPro.customer_order.model.dto.CreateCustomerOrderDto;
import com.asusoftware.TermoPro.customer_order.model.dto.CustomerOrderDto;
import com.asusoftware.TermoPro.customer_order.repository.CustomerOrderRepository;
import com.asusoftware.TermoPro.exception.ResourceNotFoundException;
import com.asusoftware.TermoPro.user.model.UserRole;
import com.asusoftware.TermoPro.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerOrderService {

    private final CustomerOrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ModelMapper mapper;

    @Transactional
    public CustomerOrderDto createOrder(CreateCustomerOrderDto dto) {
        CustomerOrder order = CustomerOrder.builder()
                .clientName(dto.getClientName())
                .clientPhone(dto.getClientPhone())
                .clientAddress(dto.getClientAddress())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .scheduledDate(dto.getScheduledDate())
                .status(OrderStatus.PENDING)
                .companyId(dto.getCompanyId())
                .teamId(dto.getTeamId())
                .createdAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);
        return mapper.map(order, CustomerOrderDto.class);
    }

    public List<CustomerOrderDto> getOrdersByCompany(UUID companyId) {
        return orderRepository.findAllByCompanyId(companyId).stream()
                .map(order -> mapper.map(order, CustomerOrderDto.class))
                .collect(Collectors.toList());
    }

    public CustomerOrderDto getOrderById(UUID orderId) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Comanda nu a fost găsită."));
        return mapper.map(order, CustomerOrderDto.class);
    }

    public List<CustomerOrderDto> filterOrders(UUID companyId, LocalDate date, String status) {
        List<CustomerOrder> orders;

        if (date != null && status != null) {
            orders = orderRepository.findAllByCompanyIdAndScheduledDateAndStatus(companyId, date, status);
        } else if (date != null) {
            orders = orderRepository.findAllByCompanyIdAndScheduledDate(companyId, date);
        } else if (status != null) {
            orders = orderRepository.findAllByCompanyIdAndStatus(companyId, status);
        } else {
            orders = orderRepository.findAllByCompanyId(companyId);
        }

        return orders.stream()
                .map(order -> mapper.map(order, CustomerOrderDto.class))
                .collect(Collectors.toList());
    }


    private void validateUserInCompany(UUID userId, UUID companyId) {
        boolean inCompany = userRepository.existsByIdAndCompanyId(userId, companyId);
        if (!inCompany) {
            throw new SecurityException("Userul nu aparține companiei.");
        }
    }

    private void validateUserCanManageOrders(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Userul nu există."));

        if (user.getRole() != UserRole.OWNER && user.getRole() != UserRole.MANAGER) {
            throw new SecurityException("Doar OWNER sau MANAGER pot șterge comenzi.");
        }
    }


    @Transactional
    public void updateStatus(UUID orderId, OrderStatus newStatus) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Comanda nu a fost găsită."));
        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(UUID orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Comanda nu există.");
        }
        orderRepository.deleteById(orderId);
    }

    public byte[] exportOrdersToPdf(UUID companyId) {
        List<CustomerOrder> orders = orderRepository.findAllByCompanyId(companyId);

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream content = new PDPageContentStream(doc, page);
            content.setFont(PDType1Font.HELVETICA_BOLD, 14);
            content.beginText();
            content.newLineAtOffset(50, 780);
            content.showText("Raport comenzi pentru companie: " + companyId);
            content.endText();

            content.setFont(PDType1Font.HELVETICA, 11);
            float margin = 50;
            float yStart = 750;
            float yPosition = yStart;
            float leading = 18f;

            // Headere tabel
            String[] headers = { "Client", "Telefon", "Adresă", "Dată programare", "Status" };
            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 11);
            content.newLineAtOffset(margin, yPosition);
            for (String h : headers) {
                content.showText(padRight(h, 25));
            }
            content.endText();
            yPosition -= leading;

            // Linie separator
            content.moveTo(margin, yPosition);
            content.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
            content.stroke();
            yPosition -= 10;

            // Conținut tabel
            content.setFont(PDType1Font.HELVETICA, 10);
            for (CustomerOrder o : orders) {
                if (yPosition < 70) {
                    content.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    content = new PDPageContentStream(doc, page);
                    yPosition = yStart;
                }

                content.beginText();
                content.newLineAtOffset(margin, yPosition);
                content.showText(padRight(o.getClientName(), 25));
                content.showText(padRight(o.getClientPhone() != null ? o.getClientPhone() : "-", 25));
                content.showText(padRight(o.getClientAddress() != null ? o.getClientAddress() : "-", 25));
                content.showText(padRight(o.getScheduledDate() != null ? o.getScheduledDate().toString() : "-", 20));
                content.showText(padRight(o.getStatus().toString(), 15));
                content.endText();
                yPosition -= leading;
            }

            content.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Eroare la generarea PDF-ului", e);
        }
    }

    private String padRight(String text, int length) {
        if (text == null) text = "-";
        return String.format("%-" + length + "." + length + "s", text);
    }

}
