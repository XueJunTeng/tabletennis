package com.example.tabletennis.entity;

import lombok.Data;

@Data
public class ContentTag {
    private Integer contentId;  // 对应数据库 content_id
    private Integer tagId;      // 对应数据库 tag_id
}