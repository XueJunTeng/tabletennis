package com.example.tabletennis.entity;

import lombok.Data;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 标签实体类
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    private Integer tagId;      // 标签ID，对应数据库 tag_id
    private String tagName;     // 标签名称（如"发球"），对应数据库 tag_name
    private Integer usageCount; // 标签使用次数，对应数据库 usage_count
    private Float weight;       // 新增字段：标签权重，对应数据库 weight
}