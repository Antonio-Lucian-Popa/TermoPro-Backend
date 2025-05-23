package com.asusoftware.TermoPro.task.model.dto;

import com.asusoftware.TermoPro.user_time_off.model.dto.TimeOffDto;
import lombok.Data;

import java.util.List;

@Data
public class DashboardStatsDto {
    private int totalTasks;
    private int pendingTasks;
    private int completedTasks;
    private int totalOrders;
    private int pendingOrders;
    private int completedOrders;
    private List<TaskDto> recentTasks;
    private List<TaskDto> todayTasks;
    private List<TimeOffDto> upcomingTimeOff;
}
