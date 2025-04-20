package com.example.tabletennis.mapper;

import com.example.tabletennis.entity.Notification;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {
    int insertNotification(Notification notification);
    Page<Notification> selectNotifications(@Param("receiverId") Long receiverId);
}