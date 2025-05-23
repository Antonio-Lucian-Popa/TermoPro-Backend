package com.asusoftware.TermoPro.notification.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationMessage {
    private String title;
    private String message;
    private String type; // "info", "warning" etc.
    private LocalDateTime timestamp;
}