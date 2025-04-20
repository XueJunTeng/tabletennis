package com.example.tabletennis.mapper;

import com.example.tabletennis.entity.UserBehavior;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserBehaviorMapper {
    // 插入行为记录（默认 isActive=1）
    void insertBehavior(UserBehavior userBehavior);

    // 更新行为状态（如取消点赞时设置 isActive=0）
    void updateBehaviorStatus(
            Long userId,
            Integer contentId,
            String behaviorType,
            Boolean isActive
    );
    List<UserBehavior> selectByUserAndType(
            @Param("userId") Long userId,
            @Param("type") String type
    );
    List<UserBehavior> selectByType(@Param("type") String type);
    List<UserBehavior> selectByUser(Long userId);
    List<UserBehavior> selectAll();
}