// RecommendationMapper.java
package com.example.tabletennis.mapper;

import com.example.tabletennis.entity.Recommendation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface RecommendationMapper {
    void insert(Recommendation recommendation);
    void deleteByUser(Long userId);
    List<Recommendation> selectByUser(Long userId);
    void bulkInsert(@Param("list") List<Recommendation> recommendations);

    void deleteExpiredRecommendations(
            @Param("userId") Long userId,
            @Param("type") String type,
            @Param("now") LocalDateTime now
    );
}