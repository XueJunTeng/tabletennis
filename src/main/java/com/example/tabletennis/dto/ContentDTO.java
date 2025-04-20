package com.example.tabletennis.dto;

import lombok.Data;
import java.util.Date;

@Data
public class ContentDTO {
    private Integer contentId;
    private String title;
    private String description;
    private String type;
    private String status;
    private Date createdTime;
    private String reviewNotes;
    // 其他需要展示的字段
}