package com.asusoftware.TermoPro.user_time_off.repository;

import com.asusoftware.TermoPro.user_time_off.model.UserTimeOff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserTimeOffRepository extends JpaRepository<UserTimeOff, UUID> {
    List<UserTimeOff> findAllByUserId(UUID userId);

    List<UserTimeOff> findAllByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate end, LocalDate start);

    // Adică: unde intervalul cererii se suprapune cu data `date`
    default List<UserTimeOff> findAllByDate(LocalDate date) {
        return findAllByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date);
    }

    @Query("""
    SELECT t FROM UserTimeOff t
    JOIN User u ON t.userId = u.id
    WHERE u.companyId = :companyId AND t.approved = false
""")
    List<UserTimeOff> findAllPendingByCompany(@Param("companyId") UUID companyId);

}