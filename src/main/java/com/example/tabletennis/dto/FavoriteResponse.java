package com.example.tabletennis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FavoriteResponse {
    private boolean isFavorited;
    private int favoriteCount;
}