package com.example.tabletennis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data // Lombok注解，自动生成Getter/Setter
@AllArgsConstructor // 生成全参构造函数
public class LikeResponse {
    private boolean isLiked;   // 用户当前是否已点赞
    private int likeCount;     // 内容的最新总点赞数
}