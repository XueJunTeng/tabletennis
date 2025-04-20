// 新建 ReviewRequest.java
package com.example.tabletennis.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotNull
    private String status;
    private String reviewNotes;
}