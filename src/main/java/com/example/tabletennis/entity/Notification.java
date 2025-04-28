package com.example.tabletennis.entity;

import com.example.tabletennis.enums.ContentStatus;
import com.example.tabletennis.enums.NotificationType;
import lombok.Data;
import java.time.LocalDateTime;


@Data
public class Notification {
    private Integer notificationId;
    private Long senderId;
    private Long receiverId;
    private Integer contentId;
    private Integer commentId;
    private NotificationType type;//LIKE,REPLY,COMMENT,SYSTEM
    private String message;
    private Boolean isRead;
    private LocalDateTime createdTime;
    //非数据库字段
    private String senderAvatarUrl;   // 发送者头像
    private String senderUsername;    // 发送者用户名
    private String contentTitle;      // 内容标题（直接通过 content_id 关联）
    private String commentContent;    // 评论内容（通过 comment_id 关联）
    private String contentType;       //内容类型
    private ContentStatus contentStatus;     //内容审核状态
}