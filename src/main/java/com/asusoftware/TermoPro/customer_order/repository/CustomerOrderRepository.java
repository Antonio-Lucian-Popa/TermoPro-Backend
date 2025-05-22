package com.asusoftware.TermoPro.customer_order.repository;

import com.asusoftware.TermoPro.customer_order.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, UUID> {
    List<CustomerOrder> findAllByCompanyId(UUID companyId);
    List<CustomerOrder> findAllByCompanyIdAndScheduledDate(UUID companyId, LocalDate date);

    List<CustomerOrder> findAllByCompanyIdAndStatus(UUID companyId, String status);

    List<CustomerOrder> findAllByCompanyIdAndScheduledDateAndStatus(UUID companyId, LocalDate date, String status);

}