package com.example.tabletennis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// CommentDTO.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private Long commentId;
    private String content;
    private String username;
    private LocalDateTime createTime;
    private List<CommentDTO> replies = new ArrayList<>();
}