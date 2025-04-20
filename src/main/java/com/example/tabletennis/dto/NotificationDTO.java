package com.example.tabletennis.dto;

import com.example.tabletennis.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationDTO {
    private NotificationType type;
    private Long actorUserId;
    private Integer contentId;
    private Integer commentId;
    private String message;
}
