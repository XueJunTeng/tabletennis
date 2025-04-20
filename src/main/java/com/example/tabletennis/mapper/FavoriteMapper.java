package com.example.tabletennis.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteMapper {
    void insertFavorite(Long userId, Integer contentId);
    void deleteFavorite(Long userId, Integer contentId);
    boolean existsFavorite(Long userId, Integer contentId);
}
