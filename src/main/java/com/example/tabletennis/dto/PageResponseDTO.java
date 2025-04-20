package com.example.tabletennis.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PageResponseDTO<T> {
    private Integer total;        // 总记录数
    private Integer currentPage;  // 当前页码
    private Integer pageSize;     // 每页数量
    private List<T> data;         // 分页数据
}