// controller/DashboardController.java
package com.example.tabletennis.controller;

import com.example.tabletennis.dto.ContentTypeDTO;
import com.example.tabletennis.dto.DashboardStatsDTO;
import com.example.tabletennis.dto.UserGrowthDTO;
import com.example.tabletennis.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    @GetMapping("/growth")
    public ResponseEntity<List<UserGrowthDTO>> getGrowthTrend(
            @RequestParam(defaultValue = "7d") String range) {
        return ResponseEntity.ok(dashboardService.getUserGrowthTrend(range));
    }

    @GetMapping("/content-types")
    public ResponseEntity<List<ContentTypeDTO>> getContentTypes() {
        return ResponseEntity.ok(dashboardService.getContentTypeDistribution());
    }
}
