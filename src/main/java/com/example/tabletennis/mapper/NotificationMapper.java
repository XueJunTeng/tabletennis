package com.example.tabletennis.mapper;

import com.example.tabletennis.entity.Notification;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {
    int insertNotification(Notification notification);
    Page<Notification> selectNotifications(
            @Param("receiverId") Long receiverId,
            @Param("types") List<String> types // 添加类型过滤参数
    );
    void batchUpdateReadStatus(@Param("ids") List<Long> ids,
                               @Param("isRead") boolean isRead);

    int countUnreadByUserId(@Param("userId") Long userId);

    int countUnreadByTypes(@Param("userId") Long userId,
                           @Param("types") List<String> types);
}