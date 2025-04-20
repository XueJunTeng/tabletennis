package com.example.tabletennis.dto;

import lombok.Data;

import java.util.List;
@Data
// BatchReadRequest.java
public class BatchReadRequest {
    private List<Long> ids;

}