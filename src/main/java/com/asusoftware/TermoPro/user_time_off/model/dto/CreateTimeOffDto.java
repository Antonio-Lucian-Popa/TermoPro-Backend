package com.asusoftware.TermoPro.user_time_off.model.dto;

import com.asusoftware.TermoPro.user_time_off.model.TimeOffType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class CreateTimeOffDto {
    private UUID userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private TimeOffType type;
    private LocalTime startTime;
    private LocalTime endTime;
}
