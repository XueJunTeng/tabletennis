package com.example.tabletennis.mapper;

import com.example.tabletennis.dto.ContentTypeDTO;
import com.example.tabletennis.dto.DashboardStatsDTO;
import com.example.tabletennis.dto.UserGrowthDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

// mapper/DashboardMapper.java
@Mapper
public interface DashboardMapper {
    DashboardStatsDTO selectDashboardStats();
    List<UserGrowthDTO> selectUserGrowthTrend(@Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);
    List<ContentTypeDTO> selectContentTypeDistribution();
}