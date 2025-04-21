package com.example.tabletennis.service;

import com.example.tabletennis.dto.ContentTypeDTO;
import com.example.tabletennis.dto.DashboardStatsDTO;
import com.example.tabletennis.dto.UserGrowthDTO;
import com.example.tabletennis.mapper.DashboardMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

// service/DashboardService.java
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardMapper dashboardMapper;

    @Cacheable(value = "dashboardStats", key = "'overview'")
    public DashboardStatsDTO getDashboardStats() {
        return dashboardMapper.selectDashboardStats();
    }

    public List<UserGrowthDTO> getUserGrowthTrend(String timeRange) {
        TimeRange range = parseTimeRange(timeRange);
        return dashboardMapper.selectUserGrowthTrend(range.getStart(), LocalDateTime.now());
    }

    private TimeRange parseTimeRange(String timeRange) {
        // 解析7d/30d等格式
        int days = Integer.parseInt(timeRange.replaceAll("\\D", ""));
        return new TimeRange(LocalDateTime.now().minusDays(days));
    }
    @Cacheable("contentTypes")
    public List<ContentTypeDTO> getContentTypeDistribution() {
        return dashboardMapper.selectContentTypeDistribution();
    }

    @Getter
    @AllArgsConstructor
    private static class TimeRange {
        private LocalDateTime start;
    }
}
