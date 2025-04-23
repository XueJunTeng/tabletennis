package com.example.tabletennis.service;

import com.example.tabletennis.dto.NotificationDTO;
import com.example.tabletennis.entity.Comment;
import com.example.tabletennis.entity.Content;
import com.example.tabletennis.entity.Notification;
import com.example.tabletennis.enums.NotificationType;
import com.example.tabletennis.mapper.CommentMapper;
import com.example.tabletennis.mapper.ContentMapper;
import com.example.tabletennis.mapper.NotificationMapper;
import com.example.tabletennis.mapper.UserMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationMapper notificationMapper;
    private final ContentMapper contentMapper;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;
    private final AuthService userService;

    public PageInfo<Notification> getNotifications(Long receiverId, Integer page, Integer size, List<String> types) {
        PageHelper.startPage(page, size)
                .setCountColumn("n.notification_id"); // 指定分页统计列

        List<Notification> list = notificationMapper.selectNotifications(
                receiverId,
                parseNotificationTypes(types)
        );

        return new PageInfo<>(list);
    }

    private List<String> parseNotificationTypes(List<String> types) {
        // 处理类型转换逻辑
        if (types.contains("REPLY_AND_COMMENT")) {
            return Arrays.asList("COMMENT", "REPLY");
        }
        return types;
    }


    public void createLikeNotification(Long senderId, Integer contentId) {
        // 获取内容作者作为接收者
        Long receiverId = contentMapper.selectAuthorIdByContentId(contentId);

        // 获取发送者用户名
        String senderName = userMapper.selectUsernameById(senderId);

        // 过滤自点赞
        if (Objects.equals(senderId, receiverId)) {
            return;
        }
        Notification notification = new Notification();
        notification.setSenderId(senderId);
        notification.setReceiverId(receiverId);
        notification.setContentId(contentId);
        notification.setType(NotificationType.LIKE);
        notification.setMessage(senderName + " 点赞了你的内容");
        notification.setIsRead(false);

        notificationMapper.insertNotification(notification);
    }

    public void createCommentNotification(Comment comment) {
        // 确定通知接收者和类型
        NotificationTarget target = determineNotificationTarget(comment);
        // 新增空值检查
        if (target == null) {
            return; // 自评论/自回复场景直接返回
        }

        // 构建通知消息
        String message = buildNotificationMessage(comment, target.type());

        // 创建通知实体
        Notification notification = new Notification();
        notification.setSenderId(comment.getUserId());
        notification.setReceiverId(target.receiverId());
        notification.setContentId(Math.toIntExact(comment.getContentId()));
        notification.setType(target.type());
        notification.setCommentId(Math.toIntExact(comment.getCommentId()));
        notification.setMessage(message);
        notification.setCreatedTime(LocalDateTime.now());
        notification.setIsRead(false);

        // 持久化通知
        notificationMapper.insertNotification(notification);
    }

    private NotificationTarget determineNotificationTarget(Comment comment) {
        final Long currentUserId = comment.getUserId();

        try {
            if (comment.getParentId() == null) {
                // 视频评论场景
                Long authorId = contentMapper.selectAuthorIdByContentId(Math.toIntExact(comment.getContentId()));
                return shouldNotify(authorId, currentUserId) ?
                        new NotificationTarget(authorId, NotificationType.COMMENT) : null;
            } else {
                // 评论回复场景
                Long originalCommenterId = commentMapper.selectUserIdByCommentId(comment.getParentId());
                return shouldNotify(originalCommenterId, currentUserId) ?
                        new NotificationTarget(originalCommenterId, NotificationType.REPLY) : null;
            }
        } catch (EmptyResultDataAccessException e) {
            // 处理数据库查询为空的情况
            return null;
        }
    }

    // 统一判断是否需要发送通知
    private boolean shouldNotify(Long targetUserId, Long currentUserId) {
        return targetUserId != null &&
                !targetUserId.equals(currentUserId) &&
                userMapper.existsById(targetUserId);
    }

    private String buildNotificationMessage(Comment comment, NotificationType type) {
        // 增加参数校验
        if (type == null) {
            throw new IllegalArgumentException("通知类型不能为空");
        }

        String username = userService.getUsernameById(comment.getUserId());
        return switch (type) {
            case COMMENT -> String.format("%s 评论了你的内容", username);
            case REPLY -> String.format("%s 回复了你的评论", username);
            default -> throw new IllegalStateException("未处理的通知类型: " + type);
        };
    }

    public void batchMarkAsRead(List<Long> notificationIds) {
        if (!notificationIds.isEmpty()) {
            notificationMapper.batchUpdateReadStatus(notificationIds, true);
        }
    }


    public int countUnreadNotifications(Long userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    // 使用显式构造方法加强空安全
    public record NotificationTarget(Long receiverId, NotificationType type) {
        public NotificationTarget {
            Objects.requireNonNull(receiverId, "接收者ID不能为空");
            Objects.requireNonNull(type, "通知类型不能为空");
        }
    }

    public int countUnreadReplyComment(Long userId) {
        return notificationMapper.countUnreadByTypes(userId, Arrays.asList("COMMENT", "REPLY"));
    }

    public int countUnreadLike(Long userId) {
        return notificationMapper.countUnreadByTypes(userId, Collections.singletonList("LIKE"));
    }
}


